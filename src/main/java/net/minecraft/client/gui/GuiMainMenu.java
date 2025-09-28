package net.minecraft.client.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.gui.AltManagerGuiScreen;
import com.dew.system.userdata.DataSaver;
import com.dew.utils.Lerper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.florianmichael.viamcp.gui.GuiProtocolSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.optifine.reflect.Reflector;
import org.apache.commons.io.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback
{
    private static final Logger logger = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    private String splashText;
    private GuiButton buttonResetDemo;
    private final Object threadLock = new Object();
    private String openGLWarning1;
    private String openGLWarning2;
    private String openGLWarningLink;
    private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
    public static final String field_96138_a = "Please click " + EnumChatFormatting.UNDERLINE + "here" + EnumChatFormatting.RESET + " for more information.";
    private int field_92024_r;
    private int field_92023_s;
    private int field_92022_t;
    private int field_92021_u;
    private int field_92020_v;
    private int field_92019_w;
    private GuiButton accountManagerButton;
    private GuiScreen field_183503_M;
    private GuiButton modButton;
    private GuiScreen modUpdateNotification;

    private static final Color BACKGROUND_PRIMARY = new Color(25, 25, 35, 240);
    private static final Color BACKGROUND_SECONDARY = new Color(35, 35, 45, 220);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255, 200);
    private static final Color HOVER_COLOR = new Color(120, 170, 255, 150);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 80, 180);

    private float buttonAnimation = 0f;
    private long lastTime = System.currentTimeMillis();
    private boolean animationsInitialized = false;

    public GuiMainMenu()
    {
        this.openGLWarning2 = field_96138_a;
        this.splashText = "missingno";
        BufferedReader bufferedreader = null;

        try
        {
            List<String> list = Lists.<String>newArrayList();
            bufferedreader = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(), Charsets.UTF_8));
            String s;

            while ((s = bufferedreader.readLine()) != null)
            {
                s = s.trim();

                if (!s.isEmpty())
                {
                    list.add(s);
                }
            }

            if (!list.isEmpty())
            {
                while (true)
                {
                    this.splashText = (String)list.get(RANDOM.nextInt(list.size()));

                    if (this.splashText.hashCode() != 125780783)
                    {
                        break;
                    }
                }
            }
        }
        catch (IOException var12)
        {
            ;
        }
        finally
        {
            if (bufferedreader != null)
            {
                try
                {
                    bufferedreader.close();
                }
                catch (IOException var11)
                {
                    ;
                }
            }
        }

        this.openGLWarning1 = "";
    }

    public void updateScreen()
    {
        updateAnimations();
    }

    private void updateAnimations()
    {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;

        if (!animationsInitialized) {
            buttonAnimation = 0f;
            animationsInitialized = true;
        }

        float targetAnimation = 1f;
        float speed = 3f;

        if (Math.abs(buttonAnimation - targetAnimation) > 0.01f) {
            buttonAnimation = Lerper.lerp(buttonAnimation, targetAnimation, speed * deltaTime);
            buttonAnimation = Math.max(0f, Math.min(1f, buttonAnimation));
        }
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    public void initGui()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24)
        {
            this.splashText = "Merry X-mas!";
        }
        else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1)
        {
            this.splashText = "Happy new year!";
        }
        else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31)
        {
            this.splashText = "OOoooOOOoooo! Spooky!";
        }

        animationsInitialized = false;

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 200;
        int buttonHeight = 25;
        int buttonSpacing = 30;

        this.addModernButton(1, centerX - buttonWidth / 2, centerY - 60, buttonWidth, buttonHeight, "Singleplayer");
        this.addModernButton(2, centerX - buttonWidth / 2, centerY - 25, buttonWidth, buttonHeight, "Multiplayer");
        this.addModernButton(14, centerX - buttonWidth / 2, centerY + 10, buttonWidth, buttonHeight, "Account Manager");
        this.addModernButton(0, centerX - buttonWidth / 2, centerY + 45, buttonWidth, buttonHeight, "Options");
        this.addModernButton(4, centerX - buttonWidth / 2, centerY + 80, buttonWidth, buttonHeight, "Quit Game");

        if (Reflector.GuiModList_Constructor.exists())
        {
            this.addModernButton(6, centerX - buttonWidth / 2, centerY + 115, buttonWidth, buttonHeight, I18n.format("fml.menu.mods", new Object[0]));
        }

        synchronized (this.threadLock)
        {
            this.field_92023_s = this.fontRendererObj.getStringWidth(this.openGLWarning1);
            this.field_92024_r = this.fontRendererObj.getStringWidth(this.openGLWarning2);
            int k = Math.max(this.field_92023_s, this.field_92024_r);
            this.field_92022_t = (this.width - k) / 2;
            this.field_92021_u = ((GuiButton)this.buttonList.get(0)).yPosition - 24;
            this.field_92020_v = this.field_92022_t + k;
            this.field_92019_w = this.field_92021_u + 24;
        }

        this.mc.setConnectedToRealms(false);
    }

    private void addModernButton(int id, int x, int y, int width, int height, String text)
    {
        this.buttonList.add(new ModernGuiButton(id, x, y, width, height, text));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (button.id == 1)
        {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2)
        {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 14)
        {
            this.mc.displayGuiScreen(new AltManagerGuiScreen());
        }

        if (button.id == 4)
        {
            this.mc.shutdown();
        }

        if (button.id == 6 && Reflector.GuiModList_Constructor.exists())
        {
            this.mc.displayGuiScreen((GuiScreen)Reflector.newInstance(Reflector.GuiModList_Constructor, new Object[] {this}));
        }

        if (button.id == 11)
        {
            this.mc.launchIntegratedServer("Demo_World", "Demo_World", DemoWorldServer.demoWorldSettings);
        }

        if (button.id == 12)
        {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            WorldInfo worldinfo = isaveformat.getWorldInfo("Demo_World");

            if (worldinfo != null)
            {
                GuiYesNo guiyesno = GuiSelectWorld.makeDeleteWorldYesNo(this, worldinfo.getWorldName(), 12);
                this.mc.displayGuiScreen(guiyesno);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawGradientRect(0, 0, this.width, this.height, BACKGROUND_PRIMARY.getRGB(), BACKGROUND_SECONDARY.getRGB());

        int titleY = 40;
        int titleHeight = 70;

        drawModernRect(this.width / 2 - 150, titleY, this.width / 2 + 150, titleY + titleHeight, BACKGROUND_SECONDARY, ACCENT_COLOR);

        DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                DewCommon.clientName + " Client",
                this.width / 2f,
                titleY + 14,
                TEXT_PRIMARY.getRGB(),
                0.7f
        );

        DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                "Minecraft 1.8.x to 1.21.8 - " + DataSaver.userName,
                this.width / 2f,
                titleY + 40,
                new Color(180, 180, 180).getRGB(),
                0.4f
        );

        for (GuiButton button : this.buttonList) {
            if (button instanceof ModernGuiButton) {
                ((ModernGuiButton) button).drawButton(this.mc, mouseX, mouseY, buttonAnimation);
            }
        }

        String versionInfo = "Version from " + DewCommon.versionUpdateDate;
        DewCommon.customFontRenderer.drawStringWithShadow(versionInfo, 5, this.height - 15, new Color(120, 120, 120).getRGB(), 0.3f);

        String userInfo = "Currently Logged In: " + mc.session.getUsername();
        DewCommon.customFontRenderer.drawCenteredStringWithShadow(userInfo, this.width / 2f, this.height - 30, TEXT_PRIMARY.getRGB(), 0.35f);

        if (this.openGLWarning1 != null && this.openGLWarning1.length() > 0)
        {
            drawRect(this.field_92022_t - 2, this.field_92021_u - 2, this.field_92020_v + 2, this.field_92019_w - 1, 1428160512);
            this.drawString(this.fontRendererObj, this.openGLWarning1, this.field_92022_t, this.field_92021_u, -1);
            this.drawString(this.fontRendererObj, this.openGLWarning2, (this.width - this.field_92024_r) / 2, ((GuiButton)this.buttonList.get(0)).yPosition - 12, -1);
        }
    }

    private void drawModernRect(int left, int top, int right, int bottom, Color bgColor, Color accentColor)
    {
        drawGradientRect(left, top, right, bottom,
                new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), (int)(bgColor.getAlpha() * buttonAnimation)).getRGB(),
                new Color(bgColor.getRed() + 10, bgColor.getGreen() + 10, bgColor.getBlue() + 10, (int)(bgColor.getAlpha() * buttonAnimation)).getRGB());

        drawRect(left, top, right, top + 2,
                new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), (int)(accentColor.getAlpha() * buttonAnimation)).getRGB());

        drawBorder(left, top, right, bottom,
                new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(), (int)(BORDER_COLOR.getAlpha() * buttonAnimation)));
    }

    private void drawBorder(int left, int top, int right, int bottom, Color color)
    {
        drawRect(left, top, left + 1, bottom, color.getRGB());
        drawRect(right - 1, top, right, bottom, color.getRGB());
        drawRect(left, top, right, top + 1, color.getRGB());
        drawRect(left, bottom - 1, right, bottom, color.getRGB());
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        synchronized (this.threadLock)
        {
            if (this.openGLWarning1.length() > 0 && mouseX >= this.field_92022_t && mouseX <= this.field_92020_v && mouseY >= this.field_92021_u && mouseY <= this.field_92019_w)
            {
                GuiConfirmOpenLink guiconfirmopenlink = new GuiConfirmOpenLink(this, this.openGLWarningLink, 13, true);
                guiconfirmopenlink.disableSecurityWarning();
                this.mc.displayGuiScreen(guiconfirmopenlink);
            }
        }
    }

    public class ModernGuiButton extends GuiButton
    {
        private float hoverAnimation = 0f;
        private long lastHoverTime = System.currentTimeMillis();

        public ModernGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
        {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        public void drawButton(Minecraft mc, int mouseX, int mouseY, float globalAnimation)
        {
            if (this.visible)
            {
                long currentTime = System.currentTimeMillis();
                float deltaTime = (currentTime - lastHoverTime) / 1000f;
                lastHoverTime = currentTime;

                boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                        mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

                float targetHover = hovered ? 1f : 0f;
                float hoverSpeed = 8f;

                if (Math.abs(hoverAnimation - targetHover) > 0.01f) {
                    hoverAnimation = Lerper.lerp(hoverAnimation, targetHover, hoverSpeed * deltaTime);
                    hoverAnimation = Math.max(0f, Math.min(1f, hoverAnimation));
                }

                int animatedAlpha = (int)(255 * globalAnimation);

                Color bgColor = new Color(BACKGROUND_SECONDARY.getRed(), BACKGROUND_SECONDARY.getGreen(),
                        BACKGROUND_SECONDARY.getBlue(), Math.min(animatedAlpha, BACKGROUND_SECONDARY.getAlpha()));

                Color hoverColor = new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(),
                        HOVER_COLOR.getBlue(), (int)(HOVER_COLOR.getAlpha() * hoverAnimation));

                drawGradientRect(this.xPosition, this.yPosition, this.xPosition + this.width,
                        this.yPosition + this.height, bgColor.getRGB(),
                        new Color(bgColor.getRed() + 15, bgColor.getGreen() + 15, bgColor.getBlue() + 15, bgColor.getAlpha()).getRGB());

                if (hoverAnimation > 0.01f) {
                    drawRect(this.xPosition, this.yPosition, this.xPosition + this.width,
                            this.yPosition + this.height, hoverColor.getRGB());
                }

                Color accentColor = new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(),
                        ACCENT_COLOR.getBlue(), Math.min(animatedAlpha, ACCENT_COLOR.getAlpha()));
                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width,
                        this.yPosition + 2, accentColor.getRGB());

                Color borderColor = new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(),
                        BORDER_COLOR.getBlue(), Math.min(animatedAlpha, BORDER_COLOR.getAlpha()));
                drawModernBorder(this.xPosition, this.yPosition, this.xPosition + this.width,
                        this.yPosition + this.height, borderColor);

                Color textColor = new Color(TEXT_PRIMARY.getRed(), TEXT_PRIMARY.getGreen(),
                        TEXT_PRIMARY.getBlue(), Math.min(animatedAlpha, TEXT_PRIMARY.getAlpha()));
                DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                        this.displayString,
                        this.xPosition + this.width / 2f,
                        this.yPosition + (this.height - 8) / 2f - 3f,
                        textColor.getRGB(),
                        0.4f
                );
            }
        }

        private void drawModernBorder(int left, int top, int right, int bottom, Color color)
        {
            drawRect(left, top, left + 1, bottom, color.getRGB());
            drawRect(right - 1, top, right, bottom, color.getRGB());
            drawRect(left, top, right, top + 1, color.getRGB());
            drawRect(left, bottom - 1, right, bottom, color.getRGB());
        }
    }

    public void confirmClicked(boolean result, int id)
    {
        if (result && id == 12)
        {
            ISaveFormat isaveformat = this.mc.getSaveLoader();
            isaveformat.flushCache();
            isaveformat.deleteWorldDirectory("Demo_World");
            this.mc.displayGuiScreen(this);
        }
        else if (id == 13)
        {
            if (result)
            {
                try
                {
                    Class<?> oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
                    oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {new URI(this.openGLWarningLink)});
                }
                catch (Throwable throwable)
                {
                    logger.error("Couldn\'t open link", throwable);
                }
            }

            this.mc.displayGuiScreen(this);
        }
    }
}