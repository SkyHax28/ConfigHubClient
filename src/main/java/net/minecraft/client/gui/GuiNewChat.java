package net.minecraft.client.gui;

import com.dew.DewCommon;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiNewChat extends Gui
{
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.<String>newArrayList();
    private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    private final List<ChatLine> drawnChatLines = Lists.<ChatLine>newArrayList();
    private int scrollPos;
    private boolean isScrolled;

    private static final Color BACKGROUND_PRIMARY = new Color(25, 25, 35, 220);
    private static final Color BACKGROUND_SECONDARY = new Color(35, 35, 45, 200);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255, 160);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 80, 140);
    private static final Color SCROLLBAR_COLOR = new Color(100, 150, 255, 200);
    private static final Color SCROLLBAR_BACKGROUND = new Color(45, 45, 55, 180);

    public GuiNewChat(Minecraft mcIn)
    {
        this.mc = mcIn;
    }

    public void drawChat(int updateCounter)
    {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN)
        {
            int i = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int k = this.drawnChatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0)
            {
                if (this.getChatOpen())
                {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int l = MathHelper.ceiling_float_int((float)this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                int visibleMessages = 0;
                for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1)
                {
                    ChatLine chatline = (ChatLine)this.drawnChatLines.get(i1 + this.scrollPos);
                    if (chatline != null)
                    {
                        int j1 = updateCounter - chatline.getUpdatedCounter();
                        if (j1 < 200 || flag)
                        {
                            double d0 = (double)j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int)(255.0D * d0);

                            if (flag)
                            {
                                l1 = 255;
                            }

                            l1 = (int)((float)l1 * f);

                            if (l1 > 3)
                            {
                                visibleMessages++;
                            }
                        }
                    }
                }

                if (visibleMessages > 0)
                {
                    int containerHeight = visibleMessages * 9 + 6;
                    int backgroundAlpha = flag ? 255 : (int)(f * 180);
                    drawModernBackground(-2, -containerHeight + 4, l + 8, 4, backgroundAlpha);
                }

                for (int i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1)
                {
                    ChatLine chatline = (ChatLine)this.drawnChatLines.get(i1 + this.scrollPos);

                    if (chatline != null)
                    {
                        int j1 = updateCounter - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag)
                        {
                            double d0 = (double)j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int)(255.0D * d0);

                            if (flag)
                            {
                                l1 = 255;
                            }

                            l1 = (int)((float)l1 * f);
                            ++j;

                            if (l1 > 3)
                            {
                                int i2 = 0;
                                int j2 = -i1 * 9;

                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();

                                mc.fontRendererObj.drawStringWithShadow(s, (float)i2 + 4, (float)(j2 - 7), new Color(255, 255, 255, l1).getRGB());

                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (flag)
                {
                    int k2 = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = k * k2 + k;
                    int i3 = j * k2 + j;
                    int j3 = this.scrollPos * i3 / k;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3)
                    {
                        drawModernScrollbar(0, -j3 + 2, 3, -j3 - k1 + 20, l2, i3);
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    private void drawModernBackground(int left, int top, int right, int bottom, int alpha)
    {
        Color bgPrimary = new Color(BACKGROUND_PRIMARY.getRed(), BACKGROUND_PRIMARY.getGreen(),
                BACKGROUND_PRIMARY.getBlue(), (BACKGROUND_PRIMARY.getAlpha() * alpha) / 255);
        Color bgSecondary = new Color(BACKGROUND_SECONDARY.getRed(), BACKGROUND_SECONDARY.getGreen(),
                BACKGROUND_SECONDARY.getBlue(), (BACKGROUND_SECONDARY.getAlpha() * alpha) / 255);
        Color accent = new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(),
                ACCENT_COLOR.getBlue(), (ACCENT_COLOR.getAlpha() * alpha) / 255);
        Color border = new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(),
                BORDER_COLOR.getBlue(), (BORDER_COLOR.getAlpha() * alpha) / 255);

        drawGradientRect(left, top, right, bottom, bgPrimary, bgSecondary);
        drawRect(left, top, right, top + 1, accent.getRGB());
        drawBorder(left, top, right, bottom, border);
    }

    private void drawModernScrollbar(int left, int top, int right, int bottom, int totalHeight, int visibleHeight)
    {
        Color scrollThumb = new Color(SCROLLBAR_COLOR.getRed(), SCROLLBAR_COLOR.getGreen(), SCROLLBAR_COLOR.getBlue(), this.isScrolled ? 255 : 180);
        drawRect(left + 1, top, right - 1, bottom, scrollThumb.getRGB());

        if (this.isScrolled) {
            Color highlight = new Color(255, 255, 255, 100);
            drawRect(left + 1, top, left + 2, bottom, highlight.getRGB());
        }
    }

    private void drawGradientRect(int left, int top, int right, int bottom, Color startColor, Color endColor)
    {
        int steps = Math.max(1, bottom - top);
        for (int i = 0; i < steps; i++)
        {
            float ratio = (float) i / steps;
            int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * ratio);
            int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * ratio);
            int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * ratio);
            int a = (int) (startColor.getAlpha() + (endColor.getAlpha() - startColor.getAlpha()) * ratio);

            drawRect(left, top + i, right, top + i + 1, new Color(r, g, b, a).getRGB());
        }
    }

    private void drawBorder(int left, int top, int right, int bottom, Color color)
    {
        int borderColor = color.getRGB();
        drawRect(left, top, left + 1, bottom, borderColor);
        drawRect(right - 1, top, right, bottom, borderColor);
        drawRect(left, top, right, top + 1, borderColor);
        drawRect(left, bottom - 1, right, bottom, borderColor);
    }

    public void clearChatMessages()
    {
        this.drawnChatLines.clear();
        this.chatLines.clear();
        this.sentMessages.clear();
    }

    public void printChatMessage(IChatComponent chatComponent)
    {
        this.printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId)
    {
        this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
        logger.info("[CHAT] " + chatComponent.getUnformattedText());
    }

    private void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly)
    {
        if (chatLineId != 0)
        {
            this.deleteChatLine(chatLineId);
        }

        int i = MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();

        for (IChatComponent ichatcomponent : list)
        {
            if (flag && this.scrollPos > 0)
            {
                this.isScrolled = true;
                this.scroll(1);
            }

            this.drawnChatLines.add(0, new ChatLine(updateCounter, ichatcomponent, chatLineId));
        }

        while (this.drawnChatLines.size() > 100)
        {
            this.drawnChatLines.remove(this.drawnChatLines.size() - 1);
        }

        if (!displayOnly)
        {
            this.chatLines.add(0, new ChatLine(updateCounter, chatComponent, chatLineId));

            while (this.chatLines.size() > 100)
            {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }

    public void refreshChat()
    {
        this.drawnChatLines.clear();
        this.resetScroll();

        for (int i = this.chatLines.size() - 1; i >= 0; --i)
        {
            ChatLine chatline = (ChatLine)this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    public List<String> getSentMessages()
    {
        return this.sentMessages;
    }

    public void addToSentMessages(String message)
    {
        if (this.sentMessages.isEmpty() || !((String)this.sentMessages.get(this.sentMessages.size() - 1)).equals(message))
        {
            this.sentMessages.add(message);
        }
    }

    public void resetScroll()
    {
        this.scrollPos = 0;
        this.isScrolled = false;
    }

    public void scroll(int amount)
    {
        this.scrollPos += amount;
        int i = this.drawnChatLines.size();

        if (this.scrollPos > i - this.getLineCount())
        {
            this.scrollPos = i - this.getLineCount();
        }

        if (this.scrollPos <= 0)
        {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

    public IChatComponent getChatComponent(int mouseX, int mouseY)
    {
        if (!this.getChatOpen())
        {
            return null;
        }
        else
        {
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int i = scaledresolution.getScaleFactor();
            float f = this.getChatScale();
            int j = mouseX / i - 3;
            int k = mouseY / i - 27;
            j = MathHelper.floor_float((float)j / f);
            k = MathHelper.floor_float((float)k / f);

            if (j >= 0 && k >= 0)
            {
                int l = Math.min(this.getLineCount(), this.drawnChatLines.size());

                if (j <= MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l)
                {
                    int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;

                    if (i1 >= 0 && i1 < this.drawnChatLines.size())
                    {
                        ChatLine chatline = (ChatLine)this.drawnChatLines.get(i1);
                        int j1 = 0;

                        for (IChatComponent ichatcomponent : chatline.getChatComponent())
                        {
                            if (ichatcomponent instanceof ChatComponentText)
                            {
                                j1 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue(), false));

                                if (j1 > j)
                                {
                                    return ichatcomponent;
                                }
                            }
                        }
                    }

                    return null;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    public boolean getChatOpen()
    {
        return this.mc.currentScreen instanceof GuiChat;
    }

    public void deleteChatLine(int id)
    {
        Iterator<ChatLine> iterator = this.drawnChatLines.iterator();

        while (iterator.hasNext())
        {
            ChatLine chatline = (ChatLine)iterator.next();

            if (chatline.getChatLineID() == id)
            {
                iterator.remove();
            }
        }

        iterator = this.chatLines.iterator();

        while (iterator.hasNext())
        {
            ChatLine chatline1 = (ChatLine)iterator.next();

            if (chatline1.getChatLineID() == id)
            {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth()
    {
        return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
    }

    public int getChatHeight()
    {
        return calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
    }

    public float getChatScale()
    {
        return this.mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float scale)
    {
        int i = 320;
        int j = 40;
        return MathHelper.floor_float(scale * (float)(i - j) + (float)j);
    }

    public static int calculateChatboxHeight(float scale)
    {
        int i = 180;
        int j = 20;
        return MathHelper.floor_float(scale * (float)(i - j) + (float)j);
    }

    public int getLineCount()
    {
        return this.getChatHeight() / 9;
    }
}