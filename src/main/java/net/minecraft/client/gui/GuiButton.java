package net.minecraft.client.gui;

import com.dew.DewCommon;
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
            FontRenderer font = mc.fontRendererObj;
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                    mouseX < this.xPosition + this.width &&
                    mouseY < this.yPosition + this.height;

            int x = this.xPosition;
            int y = this.yPosition;
            int w = this.width;
            int h = this.height;

            drawRect(x, y, x + w, y + h, new Color(20, 20, 20, 120).getRGB());

            drawRect(x - 1, y, x, y + h, new Color(0, 0, 0, 60).getRGB());
            drawRect(x + w, y, x + w + 1, y + h, new Color(0, 0, 0, 60).getRGB());
            drawRect(x, y - 1, x + w, y, new Color(0, 0, 0, 60).getRGB());
            drawRect(x, y + h, x + w, y + h + 1, new Color(0, 0, 0, 60).getRGB());

            if (hovered)
                drawRect(x, y, x + w, y + h, new Color(255, 255, 255, 30).getRGB());

            int textColor = !enabled ? 0xFFAAAAAA : (hovered ? 0xFFFFD966 : 0xFFFFFFFF);

            if (mc.currentScreen instanceof GuiMainMenu) {
                this.displayString = this.displayString.replaceAll("§.", "");
                DewCommon.customFontRenderer.drawCenteredString(this.displayString, x + w / 2f, y + (h - 13) / 2f, textColor, 0.34f);
            } else {
                drawCenteredString(font, this.displayString, x + w / 2, y + (h - 8) / 2, textColor);
            }

            this.mouseDragged(mc, mouseX, mouseY);
        }
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
