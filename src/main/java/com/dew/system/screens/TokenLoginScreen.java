package com.dew.system.screens;

import com.dew.system.screens.utils.APIUtils;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import static com.dew.system.screens.utils.SessionChanger.setSession;
public class TokenLoginScreen extends GuiScreen {
    private GuiTextField tokenField;
    private String status = "Not logged in";
    public GuiScreen previousScreen;
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
                    String[] profile = APIUtils.getProfileInfo(token);
                    String username = profile[0];
                    String uuid = profile[1];
                    setSession(username, uuid, token, "mojang");
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
