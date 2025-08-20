package com.dew.system.gui;

import com.dew.utils.alt.APIUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class TokenLoginScreen extends GuiScreen {
    private GuiTextField tokenField;
    private String status = "Not logged in";

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int centerX = this.width / 2;

        this.buttonList.clear();
        this.tokenField = new GuiTextField(0, this.fontRendererObj, centerX - 100, this.height / 2 - 10, 200, 20);
        this.tokenField.setMaxStringLength(9999);
        this.tokenField.setFocused(true);

        this.buttonList.add(new GuiButton(1, centerX - 50, this.height / 2 + 20, 100, 20, "Login"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            String token = tokenField.getText().trim();
            if (!token.isEmpty()) {
                try {
                    if (token.contains("|")) {
                        token = token.split("\\|")[0];
                    }
                    String[] profile = APIUtils.getProfileInfo(token);
                    String username = profile[0];
                    String uuid = profile[1];
                    mc.session = new Session(username, uuid, token, "legacy");
                    status = "Logged in as: " + username;
                } catch (Exception e) {
                    status = "Invalid token!";
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int centerX = this.width / 2;

        this.drawCenteredString(this.fontRendererObj, "Token Login", centerX, this.height / 2 - 40, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, status, centerX, this.height / 2 - 25, 0x00FF00);

        this.tokenField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(new AltManagerGuiScreen());
            return;
        }
        this.tokenField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.tokenField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
}
