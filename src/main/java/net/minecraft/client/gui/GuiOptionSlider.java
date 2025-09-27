package net.minecraft.client.gui;

import com.dew.DewCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class GuiOptionSlider extends GuiButton
{
    private static final Color BACKGROUND_PRIMARY = new Color(25, 25, 35, 240);
    private static final Color BACKGROUND_SECONDARY = new Color(35, 35, 45, 220);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255, 200);
    private static final Color HOVER_COLOR = new Color(120, 170, 255, 150);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255, 255);
    private static final Color TEXT_DISABLED = new Color(160, 160, 160, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 80, 180);
    private static final Color SLIDER_HANDLE = new Color(255, 255, 255, 255);

    private float sliderValue;
    public boolean dragging;
    private GameSettings.Options options;
    private final float field_146132_r;
    private final float field_146131_s;

    public GuiOptionSlider(int p_i45016_1_, int p_i45016_2_, int p_i45016_3_, GameSettings.Options p_i45016_4_)
    {
        this(p_i45016_1_, p_i45016_2_, p_i45016_3_, p_i45016_4_, 0.0F, 1.0F);
    }

    public GuiOptionSlider(int p_i45017_1_, int p_i45017_2_, int p_i45017_3_, GameSettings.Options p_i45017_4_, float p_i45017_5_, float p_i45017_6_)
    {
        super(p_i45017_1_, p_i45017_2_, p_i45017_3_, 150, 20, "");
        this.sliderValue = 1.0F;
        this.options = p_i45017_4_;
        this.field_146132_r = p_i45017_5_;
        this.field_146131_s = p_i45017_6_;
        Minecraft minecraft = Minecraft.getMinecraft();
        this.sliderValue = p_i45017_4_.normalizeValue(minecraft.gameSettings.getOptionFloatValue(p_i45017_4_));
        this.displayString = minecraft.gameSettings.getKeyBinding(p_i45017_4_);
    }

    protected int getHoverState(boolean mouseOver)
    {
        return 0;
    }

    @Override
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

            drawSliderTrack();

            Color textColor = this.enabled ? TEXT_PRIMARY : TEXT_DISABLED;

            drawCenteredString(
                    mc.fontRendererObj,
                    this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + 5,
                    textColor.getRGB()
            );

            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    private void drawSliderTrack() {
        int trackHeight = 4;
        int trackY = this.yPosition + this.height - 6;

        drawRect(this.xPosition + 4, trackY,
                this.xPosition + this.width - 4, trackY + trackHeight,
                new Color(60, 60, 80, 200).getRGB());

        int progressWidth = (int)((this.width - 8) * this.sliderValue);
        if (progressWidth > 0) {
            Color progressColor = this.enabled ? ACCENT_COLOR : new Color(80, 80, 80, 200);
            drawRect(this.xPosition + 4, trackY,
                    this.xPosition + 4 + progressWidth, trackY + trackHeight,
                    progressColor.getRGB());
        }

        int handleX = this.xPosition + 4 + (int)((this.width - 8) * this.sliderValue) - 3;
        int handleY = trackY - 2;
        int handleWidth = 6;
        int handleHeight = trackHeight + 4;

        Color handleColor = this.enabled ? SLIDER_HANDLE : TEXT_DISABLED;
        drawRect(handleX, handleY, handleX + handleWidth, handleY + handleHeight,
                handleColor.getRGB());

        drawBorder(handleX, handleY, handleX + handleWidth, handleY + handleHeight, BORDER_COLOR);
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible && this.dragging)
        {
            this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            float f = this.options.denormalizeValue(this.sliderValue);
            mc.gameSettings.setOptionFloatValue(this.options, f);
            this.sliderValue = this.options.normalizeValue(f);
            this.displayString = mc.gameSettings.getKeyBinding(this.options);
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        if (super.mousePressed(mc, mouseX, mouseY))
        {
            this.sliderValue = (float)(mouseX - (this.xPosition + 4)) / (float)(this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            mc.gameSettings.setOptionFloatValue(this.options, this.options.denormalizeValue(this.sliderValue));
            this.displayString = mc.gameSettings.getKeyBinding(this.options);
            this.dragging = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY)
    {
        this.dragging = false;
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
}