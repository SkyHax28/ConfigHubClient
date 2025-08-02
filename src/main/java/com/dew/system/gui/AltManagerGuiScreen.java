package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.altmanager.Alt;
import com.dew.system.altmanager.login.MicrosoftAuth;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class AltManagerGuiScreen extends GuiScreen {
    private final int entryHeight = 20;
    private final int listTop = 55;
    private final int listBottomPadding = 100;
    private Alt selectedAlt;
    private int selectedAltIndex = -1;
    private int scrollOffset = 0;

    @Override
    public void initGui() {
        DewCommon.altManager.readAlts();
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, width / 2 - 40 + 200, 85, 80, 20, "Cancel"));
        this.buttonList.add(new GuiButton(1, width / 2 - 40 + 200, 60, 80, 20, "Cracked"));
        this.buttonList.add(new GuiButton(2, width / 2 - 40 - 200, 85, 80, 20, "Use Microsoft"));
        this.buttonList.add(new GuiButton(3, width / 2 - 40 - 200, 60, 80, 20, "Use Selected"));
        this.buttonList.add(new GuiButton(4, width / 2 - 40 - 200, 110, 80, 20, "Token Login"));
        super.initGui();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            int altsSize = DewCommon.altManager.getAlts().size();
            int maxOffset = Math.max(0, altsSize * entryHeight - (height - listTop - listBottomPadding));
            scrollOffset -= dWheel / 10;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        List<Alt> alts = DewCommon.altManager.getAlts();

        this.drawCenteredString(IMinecraft.mc.bitFontRendererObj, "Currently Logged Into: " + mc.session.getUsername() + " (" + DewCommon.altManager.getAlts().size() + " Alts)", width / 2, 5, 0xFFFFFF);
        this.drawCenteredString(IMinecraft.mc.bitFontRendererObj, DewCommon.altManager.status, width / 2, 21, 0xFFFFFF);

        int y = listTop - scrollOffset;
        for (int i = 0; i < alts.size(); i++) {
            Alt alt = alts.get(i);
            int entryTop = y + i * entryHeight;

            if (entryTop + entryHeight >= listTop && entryTop <= height - listBottomPadding) {
                if (i == selectedAltIndex) {
                    drawRect(width / 2 - 150, entryTop, width / 2 + 150, entryTop + entryHeight, 0x8855AAFF);
                } else {
                    drawRect(width / 2 - 150, entryTop, width / 2 + 150, entryTop + entryHeight, 0x55222222);
                }

                String label = alt.getEmail() != null ? alt.getUsername() : "[Cracked]";
                this.drawCenteredString(this.fontRendererObj, label, width / 2, entryTop + 6, 0xFFFFFF);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            List<Alt> alts = DewCommon.altManager.getAlts();
            int y = listTop - scrollOffset;

            for (int i = 0; i < alts.size(); i++) {
                int entryTop = y + i * entryHeight;

                if (entryTop + entryHeight < listTop || entryTop > height - listBottomPadding) continue;

                if (mouseX >= width / 2 - 150 && mouseX <= width / 2 + 150 && mouseY >= entryTop && mouseY <= entryTop + entryHeight) {
                    selectedAltIndex = i;
                    selectedAlt = alts.get(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onGuiClosed() {
        DewCommon.altManager.status = "Waiting...";
        DewCommon.altManager.saveAlts();
        MicrosoftAuth.stopServer();
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
        }
    }
}
