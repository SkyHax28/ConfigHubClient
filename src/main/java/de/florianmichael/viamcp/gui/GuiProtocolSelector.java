package de.florianmichael.viamcp.gui;

import com.dew.DewCommon;
import com.dew.system.gui.AltManagerGuiScreen;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.protocolinfo.ProtocolInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;

public class GuiProtocolSelector extends GuiScreen {
    private final GuiScreen parent;
    public SlotList list;

    private static final Color BACKGROUND_OVERLAY = new Color(0, 0, 0, 180);
    private static final Color PANEL_BACKGROUND = new Color(25, 25, 35, 240);
    private static final Color PANEL_SECONDARY = new Color(35, 35, 45, 220);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255, 200);
    private static final Color HOVER_COLOR = new Color(120, 170, 255, 150);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(200, 200, 200, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 80, 180);
    private static final Color SELECTED_COLOR = new Color(100, 150, 255, 100);
    private static final Color PROTOCOL_ENTRY_COLOR = new Color(45, 45, 55, 200);

    private final int listWidth = 400;
    private final int entryHeight = 29;
    private final int listTop = 120;
    private final int listBottomPadding = 60;
    private int scrollOffset = 0;
    private final int scrollBarWidth = 8;
    private boolean scrollBarDragging = false;
    private int dragStartY = 0;
    private int dragStartOffset = 0;

    public GuiProtocolSelector(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();

        int buttonWidth = 120;
        int buttonHeight = 25;
        int rightX = width / 2 + listWidth / 2 + 20;

        buttonList.add(new GuiButton(0, rightX, 80, buttonWidth, buttonHeight, "Back to Menu"));

        list = new SlotList(mc, width, height, listTop, height - listBottomPadding);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) throws IOException {
        list.actionPerformed(guiButton);

        if (guiButton.id == 0) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            int protocolsSize = ViaLoadingBase.getProtocols().size();
            int listHeight = height - listTop - listBottomPadding;
            int maxOffset = Math.max(0, protocolsSize * entryHeight - listHeight);
            scrollOffset -= dWheel / 5;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
        }

        list.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            int scrollBarX = width / 2 + listWidth / 2 - scrollBarWidth - 2;
            int scrollBarY = listTop;
            int scrollBarHeight = height - listTop - listBottomPadding;

            if (mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth &&
                    mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight) {
                int protocolsSize = ViaLoadingBase.getProtocols().size();
                int maxOffset = Math.max(0, protocolsSize * entryHeight - scrollBarHeight);

                if (maxOffset > 0) {
                    scrollBarDragging = true;
                    dragStartY = mouseY;
                    dragStartOffset = scrollOffset;
                }
                return;
            }

            int listX = width / 2 - listWidth / 2 + 5;
            int listRight = width / 2 + listWidth / 2 - 15;
            int listHeight = height - listTop - listBottomPadding;
            int clipTop = listTop;
            int clipBottom = listTop + listHeight;
            int y = listTop - scrollOffset;

            for (int i = 0; i < ViaLoadingBase.getProtocols().size(); i++) {
                int entryTop = y + i * entryHeight;
                int entryBottom = entryTop + entryHeight;

                if (entryBottom < clipTop || entryTop > clipBottom) continue;

                int drawTop = Math.max(entryTop, clipTop);
                int drawBottom = Math.min(entryBottom, clipBottom);

                if (mouseX >= listX && mouseX <= listRight && mouseY >= drawTop && mouseY <= drawBottom) {
                    final ProtocolVersion protocolVersion = ViaLoadingBase.getProtocols().get(i);
                    ViaLoadingBase.getInstance().reload(protocolVersion);
                    break;
                }
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (scrollBarDragging && clickedMouseButton == 0) {
            int protocolsSize = ViaLoadingBase.getProtocols().size();
            int listHeight = height - listTop - listBottomPadding;
            int maxOffset = Math.max(0, protocolsSize * entryHeight - listHeight);

            if (maxOffset > 0) {
                int deltaY = mouseY - dragStartY;
                float scrollSensitivity = 2f;
                float scrollRatio = (float) deltaY * scrollSensitivity / listHeight;
                scrollOffset = (int) MathHelper.clamp_float(dragStartOffset + scrollRatio * maxOffset, 0, maxOffset);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        scrollBarDragging = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, BACKGROUND_OVERLAY.getRGB());

        int panelX = width / 2 - listWidth / 2;
        int panelY = 35;
        int panelHeight = height - 70;

        drawGradientRect(panelX, panelY, panelX + listWidth, panelY + panelHeight, PANEL_BACKGROUND, PANEL_SECONDARY);
        drawRect(panelX, panelY, panelX + listWidth, panelY + 3, ACCENT_COLOR.getRGB());
        drawBorder(panelX, panelY, panelX + listWidth, panelY + panelHeight, BORDER_COLOR);

        String title = "Protocol Selector";
        DewCommon.customFontRenderer.drawCenteredString(title, this.width / 2f, panelY + 15, TEXT_PRIMARY.getRGB(), 0.6f);

        final ProtocolInfo protocolInfo = ProtocolInfo.fromProtocolVersion(ViaLoadingBase.getInstance().getTargetVersion());
        final String versionTitle = "Current: " + ViaLoadingBase.getInstance().getTargetVersion().getName() + " - " + protocolInfo.getName();
        final String versionReleased = "Released: " + protocolInfo.getReleaseDate();

        DewCommon.customFontRenderer.drawCenteredString("Version Information", width / 2f, panelY + 40, TEXT_SECONDARY.getRGB(), 0.35f);
        DewCommon.customFontRenderer.drawCenteredString(versionTitle, width / 2f, panelY + 53, TEXT_PRIMARY.getRGB(), 0.35f);
        DewCommon.customFontRenderer.drawCenteredString(versionReleased, width / 2f, panelY + 66, TEXT_SECONDARY.getRGB(), 0.35f);

        drawProtocolList(mouseX, mouseY);
        drawScrollBar();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawProtocolList(int mouseX, int mouseY) {
        int listX = width / 2 - listWidth / 2 + 5;
        int listRight = width / 2 + listWidth / 2 - 15;
        int listHeight = height - listTop - listBottomPadding;

        int clipTop = listTop;
        int clipBottom = listTop + listHeight;

        int y = listTop - scrollOffset;
        int currentProtocolIndex = ViaLoadingBase.PROTOCOLS.indexOf(ViaLoadingBase.getInstance().getTargetVersion());

        for (int i = 0; i < ViaLoadingBase.getProtocols().size(); i++) {
            ProtocolVersion protocol = ViaLoadingBase.getProtocols().get(i);
            int entryTop = y + i * entryHeight;
            int entryBottom = entryTop + entryHeight;

            if (entryBottom < clipTop || entryTop > clipBottom) {
                continue;
            }

            int drawTop = Math.max(entryTop, clipTop);
            int drawBottom = Math.min(entryBottom, clipBottom);

            boolean isHovered = mouseX >= listX && mouseX <= listRight &&
                    mouseY >= drawTop && mouseY <= drawBottom;
            boolean isSelected = i == currentProtocolIndex;

            Color bgColor;
            if (isSelected) {
                bgColor = SELECTED_COLOR;
            } else if (isHovered) {
                bgColor = new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(), HOVER_COLOR.getBlue(), 50);
            } else {
                bgColor = PROTOCOL_ENTRY_COLOR;
            }

            drawRect(listX, drawTop, listRight, drawBottom, bgColor.getRGB());
            if (drawTop == entryTop && drawBottom == entryBottom) {
                drawBorder(listX, drawTop, listRight, drawBottom, new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(), 100));
            }

            if (entryTop >= clipTop - 5 && entryTop <= clipBottom - 15) {
                String protocolName = protocol.getName();
                if (protocol == ProtocolVersion.v1_8) {
                    protocolName += " (Native)";
                }

                String pvnInfo = "PVN: " + protocol.getVersion();

                Color textColor = isSelected ? new Color(100, 255, 100, 255) : TEXT_PRIMARY;
                Color secondaryTextColor = isSelected ? new Color(150, 255, 150, 200) : TEXT_SECONDARY;

                int textY1 = Math.max(entryTop + 4, clipTop);
                int textY2 = Math.max(entryTop + 16, clipTop);

                if (textY1 <= clipBottom - 15) {
                    DewCommon.customFontRenderer.drawCenteredString(protocolName, width / 2f, textY1, textColor.getRGB(), 0.35f);
                }

                if (textY2 <= clipBottom - 10) {
                    DewCommon.customFontRenderer.drawCenteredString(pvnInfo, width / 2f, textY2, secondaryTextColor.getRGB(), 0.25f);
                }
            }
        }
    }

    private void drawScrollBar() {
        int protocolsCount = ViaLoadingBase.getProtocols().size();
        int listHeight = height - listTop - listBottomPadding;
        int maxOffset = Math.max(0, protocolsCount * entryHeight - listHeight);

        if (maxOffset <= 0) return;

        int scrollBarX = width / 2 + listWidth / 2 - scrollBarWidth - 2;
        int scrollBarY = listTop;
        int scrollBarHeight = listHeight;

        drawRect(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight,
                new Color(60, 60, 80, 150).getRGB());

        float scrollRatio = (float) scrollOffset / maxOffset;
        float handleHeight = Math.max(20, (float) listHeight * listHeight / (protocolsCount * entryHeight));
        float handleY = scrollBarY + (scrollBarHeight - handleHeight) * scrollRatio;
        Color handleColor = scrollBarDragging ? ACCENT_COLOR : new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 180);

        drawRect(scrollBarX + 1, (int)handleY, scrollBarX + scrollBarWidth - 1, (int)(handleY + handleHeight), handleColor.getRGB());
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }
        super.keyTyped(character, key);
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

    class SlotList extends GuiSlot {

        public SlotList(Minecraft mc, int width, int height, int top, int bottom) {
            super(mc, width, height, top + 30, bottom, entryHeight);
        }

        @Override
        protected int getSize() {
            return ViaLoadingBase.getProtocols().size();
        }

        @Override
        protected void elementClicked(int i, boolean b, int i1, int i2) {
            final ProtocolVersion protocolVersion = ViaLoadingBase.getProtocols().get(i);
            ViaLoadingBase.getInstance().reload(protocolVersion);
        }

        @Override
        protected boolean isSelected(int i) {
            return ViaLoadingBase.PROTOCOLS.indexOf(ViaLoadingBase.getInstance().getTargetVersion()) == i;
        }

        @Override
        protected void drawBackground() {
        }

        @Override
        protected void drawSlot(int i, int i1, int i2, int i3, int i4, int i5) {
        }

        @Override
        public void handleMouseInput() {
        }
    }
}