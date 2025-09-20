package com.dew.utils.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.*;

public class BlurUtil {
    private static int blurProgram = -1;
    private static int vertexShader = -1;
    private static int fragmentShader = -1;

    private static Framebuffer inputFramebuffer;
    private static Framebuffer outputFramebuffer;
    private static Framebuffer tempFramebuffer;

    private static int lastWidth = -1;
    private static int lastHeight = -1;

    private static int textureSampler = -1;
    private static int texelSize = -1;
    private static int direction = -1;
    private static int radius = -1;

    private static final String VERTEX_SHADER_SOURCE =
            "#version 120\n" +
                    "void main() {\n" +
                    "    gl_TexCoord[0] = gl_MultiTexCoord0;\n" +
                    "    gl_Position = ftransform();\n" +
                    "}";

    private static final String FRAGMENT_SHADER_SOURCE =
            "#version 120\n" +
                    "uniform sampler2D texture;\n" +
                    "uniform vec2 texelSize;\n" +
                    "uniform vec2 direction;\n" +
                    "uniform float radius;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    vec2 uv = gl_TexCoord[0].xy;\n" +
                    "    vec4 color = vec4(0.0);\n" +
                    "    float total = 0.0;\n" +
                    "    \n" +
                    "    for (float i = -radius; i <= radius; i++) {\n" +
                    "        vec2 offset = direction * texelSize * i;\n" +
                    "        float weight = exp(-0.5 * (i * i) / (radius * radius * 0.2));\n" +
                    "        color += texture2D(texture, uv + offset) * weight;\n" +
                    "        total += weight;\n" +
                    "    }\n" +
                    "    \n" +
                    "    gl_FragColor = color / total;\n" +
                    "}";

    public static void initBlurShader() {
        if (!OpenGlHelper.isFramebufferEnabled()) {
            System.err.println("Framebuffers not supported!");
            return;
        }

        try {
            cleanup();

            vertexShader = createShader(VERTEX_SHADER_SOURCE, ARBVertexShader.GL_VERTEX_SHADER_ARB);
            fragmentShader = createShader(FRAGMENT_SHADER_SOURCE, ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);

            blurProgram = ARBShaderObjects.glCreateProgramObjectARB();

            if (blurProgram == 0) {
                System.err.println("Failed to create shader program!");
                return;
            }

            ARBShaderObjects.glAttachObjectARB(blurProgram, vertexShader);
            ARBShaderObjects.glAttachObjectARB(blurProgram, fragmentShader);
            ARBShaderObjects.glLinkProgramARB(blurProgram);

            if (ARBShaderObjects.glGetObjectParameteriARB(blurProgram, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
                System.err.println("Shader linking failed!");
                System.err.println(getLogInfo(blurProgram));
                return;
            }

            textureSampler = ARBShaderObjects.glGetUniformLocationARB(blurProgram, "texture");
            texelSize = ARBShaderObjects.glGetUniformLocationARB(blurProgram, "texelSize");
            direction = ARBShaderObjects.glGetUniformLocationARB(blurProgram, "direction");
            radius = ARBShaderObjects.glGetUniformLocationARB(blurProgram, "radius");

            createFramebuffers();

            System.out.println("init blurShader");

        } catch (Exception e) {
            System.err.println("Failed to initialize blur shader: " + e.getMessage());
        }
    }

    private static int createShader(String shaderSource, int shaderType) {
        int shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

        if (shader == 0) {
            return 0;
        }

        ARBShaderObjects.glShaderSourceARB(shader, shaderSource);
        ARBShaderObjects.glCompileShaderARB(shader);

        if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
            System.err.println("Shader compilation failed!");
            System.err.println(getLogInfo(shader));
            return 0;
        }

        return shader;
    }

    private static String getLogInfo(int shader) {
        return ARBShaderObjects.glGetInfoLogARB(shader, ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    private static void createFramebuffers() {
        Minecraft mc = Minecraft.getMinecraft();

        if (inputFramebuffer != null) inputFramebuffer.deleteFramebuffer();
        if (outputFramebuffer != null) outputFramebuffer.deleteFramebuffer();
        if (tempFramebuffer != null) tempFramebuffer.deleteFramebuffer();

        inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
        outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
        tempFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);

        lastWidth = mc.displayWidth;
        lastHeight = mc.displayHeight;
    }

    private static void checkResize() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.displayWidth != lastWidth || mc.displayHeight != lastHeight) {
            createFramebuffers();
        }
    }

    public static void drawBlurredRect(float x, float y, float width, float height, float blurRadius) {
        if (blurProgram == -1 || !OpenGlHelper.isFramebufferEnabled()) {
            System.err.println("Blur shader not initialized!");
            return;
        }

        checkResize();

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);

        float scaleX = (float) mc.displayWidth / sr.getScaledWidth();
        float scaleY = (float) mc.displayHeight / sr.getScaledHeight();

        float realX = x * scaleX;
        float realY = y * scaleY;
        float realWidth = width * scaleX;
        float realHeight = height * scaleY;

        inputFramebuffer.bindFramebuffer(false);
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);

        GlStateManager.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        mc.getFramebuffer().bindFramebufferTexture();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, mc.displayWidth, mc.displayHeight, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);

        drawTexturedRect(0, 0, mc.displayWidth, mc.displayHeight);

        tempFramebuffer.bindFramebuffer(false);
        GlStateManager.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        applyBlur(inputFramebuffer, blurRadius, 1.0f, 0.0f);

        outputFramebuffer.bindFramebuffer(false);
        GlStateManager.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        applyBlur(tempFramebuffer, blurRadius, 0.0f, 1.0f);

        mc.getFramebuffer().bindFramebuffer(false);

        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);

        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        outputFramebuffer.bindFramebufferTexture();

        float u1 = realX / mc.displayWidth;
        float v1 = realY / mc.displayHeight;
        float u2 = (realX + realWidth) / mc.displayWidth;
        float v2 = (realY + realHeight) / mc.displayHeight;

        drawTexturedRect(x, y, width, height, u1, v1, u2, v2);

        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
    }

    private static void applyBlur(Framebuffer source, float blurRadius, float dirX, float dirY) {
        Minecraft mc = Minecraft.getMinecraft();

        ARBShaderObjects.glUseProgramObjectARB(blurProgram);

        ARBShaderObjects.glUniform1iARB(textureSampler, 0);
        ARBShaderObjects.glUniform2fARB(texelSize, 1.0F / mc.displayWidth, 1.0F / mc.displayHeight);
        ARBShaderObjects.glUniform2fARB(direction, dirX, dirY);
        ARBShaderObjects.glUniform1fARB(radius, blurRadius);

        source.bindFramebufferTexture();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        drawTexturedRect(0, 0, mc.displayWidth, mc.displayHeight);

        ARBShaderObjects.glUseProgramObjectARB(0);
    }

    private static void drawTexturedRect(float x, float y, float width, float height) {
        drawTexturedRect(x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    private static void drawTexturedRect(float x, float y, float width, float height, float u1, float v1, float u2, float v2) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + height, 0.0D).tex(u1, v2).endVertex();
        worldRenderer.pos(x + width, y + height, 0.0D).tex(u2, v2).endVertex();
        worldRenderer.pos(x + width, y, 0.0D).tex(u2, v1).endVertex();
        worldRenderer.pos(x, y, 0.0D).tex(u1, v1).endVertex();
        tessellator.draw();
    }

    public static void drawBlurredRect(float x, float y, float width, float height, float blurRadius, int backgroundColor) {
        drawBlurredRect(x, y, width, height, blurRadius);

        if (backgroundColor != 0) {
            drawRect(x, y, x + width, y + height, backgroundColor);
        }
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static boolean isInitialized() {
        return blurProgram != -1;
    }

    public static void cleanup() {
        if (blurProgram != -1) {
            ARBShaderObjects.glDeleteObjectARB(blurProgram);
            blurProgram = -1;
        }
        if (vertexShader != -1) {
            ARBShaderObjects.glDeleteObjectARB(vertexShader);
            vertexShader = -1;
        }
        if (fragmentShader != -1) {
            ARBShaderObjects.glDeleteObjectARB(fragmentShader);
            fragmentShader = -1;
        }

        if (inputFramebuffer != null) {
            inputFramebuffer.deleteFramebuffer();
            inputFramebuffer = null;
        }
        if (outputFramebuffer != null) {
            outputFramebuffer.deleteFramebuffer();
            outputFramebuffer = null;
        }
        if (tempFramebuffer != null) {
            tempFramebuffer.deleteFramebuffer();
            tempFramebuffer = null;
        }
    }
}