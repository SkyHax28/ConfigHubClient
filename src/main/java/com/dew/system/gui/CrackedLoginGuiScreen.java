package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.system.altmanager.alt.SessionChanger;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class CrackedLoginGuiScreen extends GuiScreen {
    private GuiTextField username;
    private boolean loggedIn = false;

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button.id == 0) {
            new Thread(() -> SessionChanger.getInstance().setUserOffline(this.username.getText())).start();
            loggedIn = true;
            mc.displayGuiScreen(new AltManagerGuiScreen());
        }
    }

    @Override
    public void drawScreen(final int x2, final int y2, final float z2) {
        this.drawDefaultBackground();
        final ScaledResolution sr = new ScaledResolution(this.mc);
        this.username.drawTextBox();
        drawCenteredString(this.mc.fontRendererObj, "Username", this.width / 2, sr.getScaledHeight() / 2 - 65, -1);
        super.drawScreen(x2, y2, z2);
    }

    @Override
    public void initGui() {
        final ScaledResolution sr = new ScaledResolution(this.mc);
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50 - 10, this.height / 2 - 20, 120, 20, I18n.format("Login (Cracked)")));
        (this.username = new GuiTextField(100, this.fontRendererObj, this.width / 2 - 50 - 10, sr.getScaledHeight() / 2 - 50, 120, 20)).setFocused(true);
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(new AltManagerGuiScreen());
            return;
        } else if (character == '\t' && !this.username.isFocused()) {
            this.username.setFocused(true);
        } else if (character == '\r') {
            this.actionPerformed(this.buttonList.get(0));
        }
        super.keyTyped(character, key);
        this.username.textboxKeyTyped(character, key);
    }

    @Override
    protected void mouseClicked(final int x2, final int y2, final int button) {
        try {
            super.mouseClicked(x2, y2, button);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.username.mouseClicked(x2, y2, button);
    }

    @Override
    public void onGuiClosed() {
        mc.entityRenderer.loadEntityShader(null);
        Keyboard.enableRepeatEvents(false);

        if (loggedIn)
            DewCommon.altManager.status = "Logged in as " + this.username.getText() + " (Cracked)";
    }

    @Override
    public void updateScreen() {
        this.username.updateCursorCounter();
    }
}