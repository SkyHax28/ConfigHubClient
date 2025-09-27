package net.minecraft.client.gui;

import com.dew.DewCommon;
import com.dew.utils.Lerper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiButton extends Gui
{
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    public int xPosition;
    public int yPosition;
    public String displayString;
    public int id;
    public boolean enabled;
    public boolean visible;
    protected boolean hovered;

    private static final Color BACKGROUND_PRIMARY = new Color(25, 25, 35, 240);
    private static final Color BACKGROUND_SECONDARY = new Color(35, 35, 45, 220);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255, 200);
    private static final Color HOVER_COLOR = new Color(120, 170, 255, 150);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255, 255);
    private static final Color TEXT_DISABLED = new Color(160, 160, 160, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 80, 180);

    public GuiButton(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
    }

    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!this.enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        return i;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                    mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            drawGradientRect(this.xPosition, this.yPosition,
                    this.xPosition + this.width, this.yPosition + this.height,
                    BACKGROUND_PRIMARY, BACKGROUND_SECONDARY);

            if (this.hovered && this.enabled) {
                Color hoverOverlay = new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(),
                        HOVER_COLOR.getBlue(), 30);
                drawRect(this.xPosition, this.yPosition,
                        this.xPosition + this.width, this.yPosition + this.height,
                        hoverOverlay);
            }

            drawRect(this.xPosition, this.yPosition,
                    this.xPosition + this.width, this.yPosition + 2,
                    this.enabled ? ACCENT_COLOR : new Color(80, 80, 80, 200));

            drawBorder(this.xPosition, this.yPosition,
                    this.xPosition + this.width, this.yPosition + this.height,
                    BORDER_COLOR);

            Color textColor = this.enabled ? TEXT_PRIMARY : TEXT_DISABLED;

            drawCenteredString(
                    mc.fontRendererObj,
                    this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 6) / 2,
                    textColor.getRGB()
            );

            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    private void drawRect(int left, int top, int right, int bottom, Color color) {
        drawRect(left, top, right, bottom, color.getRGB());
    }

    private void drawGradientRect(int left, int top, int right, int bottom, Color startColor, Color endColor) {
        int steps = bottom - top;
        for (int i = 0; i < steps; i++) {
            float ratio = (float) i / steps;
            int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * ratio);
            int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * ratio);
            int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * ratio);
            int a = (int) (startColor.getAlpha() + (endColor.getAlpha() - startColor.getAlpha()) * ratio);

            drawRect(left, top + i, right, top + i + 1, new Color(r, g, b, a).getRGB());
        }
    }

    private void drawBorder(int left, int top, int right, int bottom, Color color) {
        drawRect(left, top, left + 1, bottom, color.getRGB());
        drawRect(right - 1, top, right, bottom, color.getRGB());
        drawRect(left, top, right, top + 1, color.getRGB());
        drawRect(left, bottom - 1, right, bottom, color.getRGB());
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }

    public void mouseReleased(int mouseX, int mouseY)
    {
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    public boolean isMouseOver()
    {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY)
    {
    }

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return this.width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}