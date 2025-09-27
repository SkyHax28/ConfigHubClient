package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.altmanager.Alt;
import com.dew.system.altmanager.alt.SessionChanger;
import com.dew.system.altmanager.login.MicrosoftAuth;
import com.dew.utils.RandomUtil;
import com.dew.utils.alt.APIUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Session;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AltManagerGuiScreen extends GuiScreen {
    private static final Color BACKGROUND_OVERLAY = new Color(0, 0, 0, 180);
    private static final Color PANEL_BACKGROUND = new Color(25, 25, 35, 240);
    private static final Color PANEL_SECONDARY = new Color(35, 35, 45, 220);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255, 200);
    private static final Color HOVER_COLOR = new Color(120, 170, 255, 150);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(200, 200, 200, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 80, 180);
    private static final Color SELECTED_COLOR = new Color(100, 150, 255, 100);
    private static final Color ALT_ENTRY_COLOR = new Color(45, 45, 55, 200);

    private final int entryHeight = 25;
    private final int listTop = 100;
    private final int listBottomPadding = 60;
    private final int listWidth = 400;
    private Alt selectedAlt;
    private int selectedAltIndex = -1;
    private int scrollOffset = 0;

    private final int scrollBarWidth = 8;
    private boolean scrollBarDragging = false;
    private int dragStartY = 0;
    private int dragStartOffset = 0;

    private long lastClickTime = 0;
    private int lastClickedIndex = -1;
    private static final long DOUBLE_CLICK_TIME = 500;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void initGui() {
        DewCommon.altManager.readAlts();
        this.buttonList.clear();

        int buttonWidth = 120;
        int buttonHeight = 25;
        int rightX = width / 2 + listWidth / 2 + 20;
        int leftX = width / 2 - listWidth / 2 - buttonWidth - 20;

        this.buttonList.add(new GuiButton(1, rightX, 80, buttonWidth, buttonHeight, "Login Cracked"));
        this.buttonList.add(new GuiButton(5, rightX, 110, buttonWidth, buttonHeight, "Free Random MS"));
        this.buttonList.add(new GuiButton(6, rightX, 140, buttonWidth, buttonHeight, "Random Cracked"));
        this.buttonList.add(new GuiButton(0, rightX, 200, buttonWidth, buttonHeight, "Back to Menu"));

        this.buttonList.add(new GuiButton(3, leftX, 80, buttonWidth, buttonHeight, "Use Selected"));
        this.buttonList.add(new GuiButton(2, leftX, 110, buttonWidth, buttonHeight, "Use Microsoft"));
        this.buttonList.add(new GuiButton(4, leftX, 140, buttonWidth, buttonHeight, "Token Login"));

        super.initGui();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            int altsSize = DewCommon.altManager.getAlts().size();
            int listHeight = height - listTop - listBottomPadding;
            int maxOffset = Math.max(0, altsSize * entryHeight - listHeight);
            scrollOffset -= dWheel / 5;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, this.width, this.height, BACKGROUND_OVERLAY.getRGB());

        List<Alt> alts = DewCommon.altManager.getAlts();

        int panelX = width / 2 - listWidth / 2;
        int panelY = 35;
        int panelHeight = height - 70;

        drawGradientRect(panelX, panelY, panelX + listWidth, panelY + panelHeight, PANEL_BACKGROUND, PANEL_SECONDARY);
        drawRect(panelX, panelY, panelX + listWidth, panelY + 3, ACCENT_COLOR.getRGB());
        drawBorder(panelX, panelY, panelX + listWidth, panelY + panelHeight, BORDER_COLOR);

        DewCommon.customFontRenderer.drawCenteredStringWithShadow("Alt Manager", width / 2f, panelY + 10, TEXT_PRIMARY.getRGB(), 0.6f);
        DewCommon.customFontRenderer.drawCenteredStringWithShadow("Current: " + mc.session.getUsername() + " (" + alts.size() + " Alts)", width / 2f, panelY + 34, TEXT_SECONDARY.getRGB(), 0.35f);
        DewCommon.customFontRenderer.drawCenteredStringWithShadow(DewCommon.altManager.status, width / 2f, panelY + 48, TEXT_SECONDARY.getRGB(), 0.3f);

        drawAltList(alts, mouseX, mouseY);
        drawScrollBar(alts.size());

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawAltList(List<Alt> alts, int mouseX, int mouseY) {
        int listX = width / 2 - listWidth / 2 + 5;
        int listRight = width / 2 + listWidth / 2 - 15;
        int listHeight = height - listTop - listBottomPadding;

        int clipTop = listTop;
        int clipBottom = listTop + listHeight;

        int y = listTop - scrollOffset;

        for (int i = 0; i < alts.size(); i++) {
            Alt alt = alts.get(i);
            int entryTop = y + i * entryHeight;
            int entryBottom = entryTop + entryHeight;

            if (entryBottom < clipTop || entryTop > clipBottom) {
                continue;
            }

            int drawTop = Math.max(entryTop, clipTop);
            int drawBottom = Math.min(entryBottom, clipBottom);

            boolean isHovered = mouseX >= listX && mouseX <= listRight &&
                    mouseY >= drawTop && mouseY <= drawBottom;

            Color bgColor;
            if (i == selectedAltIndex) {
                bgColor = SELECTED_COLOR;
            } else if (isHovered) {
                bgColor = new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(), HOVER_COLOR.getBlue(), 50);
            } else {
                bgColor = ALT_ENTRY_COLOR;
            }

            drawRect(listX, drawTop, listRight, drawBottom, bgColor.getRGB());
            if (drawTop == entryTop && drawBottom == entryBottom) {
                drawBorder(listX, drawTop, listRight, drawBottom, new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(), 100));
            }

            if (entryTop >= clipTop - 5 && entryTop <= clipBottom - 10) {
                String displayName = alt.getUsername();
                String accountType = alt.getEmail() != null ? "[Premium]" : "[Cracked]";

                int textY1 = Math.max(entryTop + 2, clipTop);
                int textY2 = Math.max(entryTop + 14, clipTop);

                if (textY1 <= clipBottom - 10) {
                    DewCommon.customFontRenderer.drawStringWithShadow(displayName, listX + 8, textY1, TEXT_PRIMARY.getRGB(), 0.35f);
                }

                if (textY2 <= clipBottom - 10) {
                    DewCommon.customFontRenderer.drawStringWithShadow(accountType, listX + 8, textY2, TEXT_SECONDARY.getRGB(), 0.25f);
                }
            }
        }
    }

    private void drawScrollBar(int altsCount) {
        int listHeight = height - listTop - listBottomPadding;
        int maxOffset = Math.max(0, altsCount * entryHeight - listHeight);

        if (maxOffset <= 0) return;

        int scrollBarX = width / 2 + listWidth / 2 - scrollBarWidth - 2;
        int scrollBarY = listTop;
        int scrollBarHeight = listHeight;

        drawRect(scrollBarX, scrollBarY, scrollBarX + scrollBarWidth, scrollBarY + scrollBarHeight,
                new Color(60, 60, 80, 150).getRGB());

        float scrollRatio = (float) scrollOffset / maxOffset;
        float handleHeight = Math.max(20, (float) listHeight * listHeight / (altsCount * entryHeight));
        float handleY = scrollBarY + (scrollBarHeight - handleHeight) * scrollRatio;
        Color handleColor = scrollBarDragging ? ACCENT_COLOR : new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 180);

        drawRect(scrollBarX + 1, (int)handleY, scrollBarX + scrollBarWidth - 1, (int)(handleY + handleHeight), handleColor.getRGB());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            int scrollBarX = width / 2 + listWidth / 2 - scrollBarWidth - 2;
            int scrollBarY = listTop;
            int scrollBarHeight = height - listTop - listBottomPadding;

            if (mouseX >= scrollBarX && mouseX <= scrollBarX + scrollBarWidth && mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight) {
                List<Alt> alts = DewCommon.altManager.getAlts();
                int maxOffset = Math.max(0, alts.size() * entryHeight - scrollBarHeight);

                if (maxOffset > 0) {
                    scrollBarDragging = true;
                    dragStartY = mouseY;
                    dragStartOffset = scrollOffset;
                }
                return;
            }

            List<Alt> alts = DewCommon.altManager.getAlts();
            int listX = width / 2 - listWidth / 2 + 5;
            int listRight = width / 2 + listWidth / 2 - 15;
            int listHeight = height - listTop - listBottomPadding;
            int clipTop = listTop;
            int clipBottom = listTop + listHeight;
            int y = listTop - scrollOffset;

            for (int i = 0; i < alts.size(); i++) {
                int entryTop = y + i * entryHeight;
                int entryBottom = entryTop + entryHeight;

                if (entryBottom < clipTop || entryTop > clipBottom) continue;

                int drawTop = Math.max(entryTop, clipTop);
                int drawBottom = Math.min(entryBottom, clipBottom);

                if (mouseX >= listX && mouseX <= listRight && mouseY >= drawTop && mouseY <= drawBottom) {
                    long currentTime = System.currentTimeMillis();

                    if (i == lastClickedIndex && currentTime - lastClickTime < DOUBLE_CLICK_TIME && selectedAlt != null && MicrosoftAuth.replyServer == null) {
                        new Thread(() -> DewCommon.altManager.login(selectedAlt, false)).start();
                        return;
                    }

                    selectedAltIndex = i;
                    selectedAlt = alts.get(i);
                    lastClickedIndex = i;
                    lastClickTime = currentTime;
                    break;
                }
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (scrollBarDragging && clickedMouseButton == 0) {
            List<Alt> alts = DewCommon.altManager.getAlts();
            int listHeight = height - listTop - listBottomPadding;
            int maxOffset = Math.max(0, alts.size() * entryHeight - listHeight);

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
    public void onGuiClosed() {
        DewCommon.altManager.status = "Waiting...";
        DewCommon.altManager.saveAlts();
        MicrosoftAuth.stopServer();
    }

    private String decodeToString(String string) {
        byte[] decodedBytes = Base64.getDecoder().decode(string);
        return new String(decodedBytes);
    }

    private List<String> getAltTokens() {
        Request request = new Request.Builder()
                .url(decodeToString("aHR0cHM6Ly9uYXR0b2dyZWF0YXBpLnBhZ2VzLmRldi9kZXcvZnJlZWFsdF90b2tlbnMudHh0"))
                .get()
                .header("User-Agent", DewCommon.clientName)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string().trim();
                return Arrays.asList(body.split("\\r?\\n\\r?\\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Collections.emptyList();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(new GuiMainMenu());
                break;
            case 1:
                mc.displayGuiScreen(new CrackedLoginGuiScreen());
                break;
            case 2:
                if (MicrosoftAuth.replyServer == null) {
                    new Thread(() -> {
                        Alt alt = new Alt("Pending", false);
                        MicrosoftAuth.requestAuthToken((authToken) -> {
                            alt.setAuthToken(authToken);
                            if (authToken != null) {
                                new Thread(() -> DewCommon.altManager.login(alt, true)).start();
                            }
                        });
                    }).start();
                }
                break;
            case 3:
                if (selectedAlt != null && MicrosoftAuth.replyServer == null) {
                    new Thread(() -> DewCommon.altManager.login(selectedAlt, false)).start();
                }
                break;
            case 4:
                mc.displayGuiScreen(new TokenLoginScreen());
                break;
            case 5:
                try {
                    List<String> tokens = this.getAltTokens();
                    if (tokens.isEmpty()) return;
                    String token = tokens.get(ThreadLocalRandom.current().nextInt(tokens.size()));
                    String[] profile = APIUtils.getProfileInfo(token);
                    String username = profile[0];
                    String uuid = profile[1];
                    mc.session = new Session(username, uuid, token, "legacy");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 6:
                SessionChanger.getInstance().setUserOffline(RandomUtil.randomString(RandomUtil.nextInt(5, 14)));
                break;
        }
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