package com.dew.utils.font;

import com.dew.utils.LogUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

public class CustomFontRenderer {
    private static final int TEX_WIDTH = 512;
    private static final int TEX_HEIGHT = 512;
    private static final int PADDING = 2;

    private final Font font;
    private final CharData[] charData = new CharData[256];
    private int texId;

    public CustomFontRenderer(Font font) {
        this.font = font;
        generateFontTexture();
        LogUtil.infoLog("init customFontRenderer");
    }

    private void generateFontTexture() {
        BufferedImage image = new BufferedImage(TEX_WIDTH, TEX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(font);
        g.setColor(Color.WHITE);
        FontMetrics metrics = g.getFontMetrics();

        int x = 0, y = 0, lineHeight = 0;

        for (int i = 0; i < 256; i++) {
            char ch = (char) i;
            int width = metrics.charWidth(ch) + PADDING * 2;
            int height = metrics.getHeight() + PADDING * 2;

            if (x + width >= TEX_WIDTH) {
                x = 0;
                y += lineHeight;
                lineHeight = 0;
            }

            g.drawString(String.valueOf(ch), x + PADDING, y + metrics.getAscent() + PADDING);
            charData[i] = new CharData(x, y, width, height);

            x += width;
            if (height > lineHeight) lineHeight = height;
        }

        g.dispose();
        texId = uploadTexture(image);
    }

    private int uploadTexture(BufferedImage image) {
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        ByteBuffer buffer = BufferUtils.createByteBuffer(TEX_WIDTH * TEX_HEIGHT * 4);

        for (int y = 0; y < TEX_HEIGHT; y++) {
            for (int x = 0; x < TEX_WIDTH; x++) {
                int pixel = pixels[y * TEX_WIDTH + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();

        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, TEX_WIDTH, TEX_HEIGHT, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        return id;
    }

    public void drawString(String text, float x, float y, int color, float size) {
        y += 1.1f;

        float scale = Math.min(size, 1.0f);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float posX = x;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_QUADS);

        float epsX = 0.5f / TEX_WIDTH;
        float epsY = 0.5f / TEX_HEIGHT;

        for (char c : text.toCharArray()) {
            if (c >= 256) continue;
            CharData data = charData[c];
            if (data == null) continue;

            float texX = data.x / (float) TEX_WIDTH + epsX;
            float texY = data.y / (float) TEX_HEIGHT + epsY;
            float texW = (data.width) / (float) TEX_WIDTH - epsX * 2;
            float texH = (data.height) / (float) TEX_HEIGHT - epsY * 2;

            float w = data.width * scale;
            float h = data.height * scale;

            GL11.glTexCoord2f(texX, texY);
            GL11.glVertex2f(posX, y);
            GL11.glTexCoord2f(texX, texY + texH);
            GL11.glVertex2f(posX, y + h);
            GL11.glTexCoord2f(texX + texW, texY + texH);
            GL11.glVertex2f(posX + w, y + h);
            GL11.glTexCoord2f(texX + texW, texY);
            GL11.glVertex2f(posX + w, y);

            posX += w - (PADDING * scale * 2);
        }

        GL11.glEnd();

        GL11.glPopAttrib();
    }

    public float getStringWidth(String text, float size) {
        float width = 0f;
        for (char c : text.toCharArray()) {
            if (c >= 256) continue;
            CharData data = charData[c];
            if (data != null) {
                width += (data.width - PADDING * 2) * size;
            }
        }
        return width;
    }

    public void drawCenteredString(String text, float centerX, float y, int color, float size) {
        float width = getStringWidth(text, size);
        float x = centerX - (width / 2.0f);
        drawString(text, x, y, color, size);
    }

    public void drawStringWithShadow(String text, float x, float y, int color, float size) {
        int shadowColor = (color & 0xFF000000);
        float shadowAlpha = ((color >> 24) & 0xFF) / 255f * 0.5f;

        drawString(text, x + 1, y + 1, (shadowColor & 0x00FFFFFF) | ((int) (shadowAlpha * 255) << 24), size);
        drawString(text, x, y, color, size);
    }

    public void drawCenteredStringWithShadow(String text, float centerX, float y, int color, float size) {
        float width = getStringWidth(text, size);
        float x = centerX - (width / 2.0f);
        drawStringWithShadow(text, x, y, color, size);
    }

    private static class CharData {
        public int x, y, width, height;

        public CharData(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}