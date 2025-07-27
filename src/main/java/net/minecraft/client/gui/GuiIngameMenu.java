package net.minecraft.client.gui;

import java.io.IOException;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.altmanager.alt.SessionChanger;
import com.dew.system.module.modules.combat.Aura;
import com.dew.utils.RandomUtil;
import com.dew.utils.ServerUtil;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;

public class GuiIngameMenu extends GuiScreen
{
    private int field_146445_a;
    private int field_146444_f;

    private GuiButton disableAuraButton;

    public void initGui()
    {
        this.field_146445_a = 0;
        this.buttonList.clear();
        int i = -16;
        int j = 98;
        GuiButton reconnect1Button;
        GuiButton reconnect2Button;
        GuiButton connectLastButton;
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 120 + i, 98, 20, "Disconnect"));
        this.buttonList.add(reconnect1Button = new GuiButton(8, this.width / 2 + 2, this.height / 4 + 120 + i, 98, 20, "Reconnect"));
        this.buttonList.add(new GuiButton(10, this.width / 2 - 100, this.height / 4 + 168 + i, 98, 20, "Clear Memory"));
        this.buttonList.add(reconnect2Button = new GuiButton(9, this.width / 2 + 2, this.height / 4 + 168 + i, 98, 20, "Reconnect Crack"));
        this.buttonList.add(connectLastButton = new GuiButton(11, this.width / 2 + 2, this.height / 4 + 192 + i, 98, 20, "Connect Last"));
        this.buttonList.add(disableAuraButton = new GuiButton(12, this.width / 2 - 100, this.height / 4 + 192 + i, 98, 20, "Disable Aura"));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 4 + 24 + i, I18n.format("menu.returnToGame", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 96 + i, 98, 20, I18n.format("menu.options", new Object[0])));
        GuiButton guibutton;
        this.buttonList.add(guibutton = new GuiButton(7, this.width / 2 + 2, this.height / 4 + 96 + i, 98, 20, I18n.format("menu.shareToLan", new Object[0])));
        this.buttonList.add(new GuiButton(5, this.width / 2 - 100, this.height / 4 + 48 + i, 98, 20, I18n.format("gui.achievements", new Object[0])));
        this.buttonList.add(new GuiButton(6, this.width / 2 + 2, this.height / 4 + 48 + i, 98, 20, I18n.format("gui.stats", new Object[0])));

        guibutton.enabled = this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic();
        reconnect1Button.enabled = !this.mc.isIntegratedServerRunning() && !this.mc.isSingleplayer();
        reconnect2Button.enabled = !this.mc.isIntegratedServerRunning() && !this.mc.isSingleplayer();
        connectLastButton.enabled = ServerUtil.serverData != null;
        disableAuraButton.enabled = DewCommon.moduleManager.getModule(Aura.class).isEnabled();
    }

    private void quit(GuiButton button) {
        boolean flag = this.mc.isIntegratedServerRunning();
        boolean flag1 = this.mc.isConnectedToRealms();
        button.enabled = false;
        this.mc.theWorld.sendQuittingDisconnectingPacket();
        this.mc.loadWorld((WorldClient)null);

        if (flag)
        {
            this.mc.displayGuiScreen(new GuiMainMenu());
        }
        else if (flag1)
        {
            RealmsBridge realmsbridge = new RealmsBridge();
            realmsbridge.switchToRealms(new GuiMainMenu());
        }
        else
        {
            this.mc.displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;

            case 1:
                this.quit(button);

            case 2:
            case 3:
            default:
                break;

            case 4:
                this.mc.displayGuiScreen((GuiScreen)null);
                this.mc.setIngameFocus();
                break;

            case 5:
                this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
                break;

            case 6:
                this.mc.displayGuiScreen(new GuiStats(this, this.mc.thePlayer.getStatFileWriter()));
                break;

            case 7:
                this.mc.displayGuiScreen(new GuiShareToLan(this));
                break;

            case 8:
            case 11:
                this.quit(button);
                ServerUtil.connectToLastServer();
                break;

            case 9:
                this.quit(button);
                SessionChanger.getInstance().setUserOffline(RandomUtil.randomString(RandomUtil.nextInt(5, 14)));
                ServerUtil.connectToLastServer();
                break;

            case 10:
                System.gc();
                break;

            case 12:
                Aura auraModule = DewCommon.moduleManager.getModule(Aura.class);
                auraModule.setState(false);
                disableAuraButton.enabled = auraModule.isEnabled();
                break;
        }
    }

    public void updateScreen()
    {
        super.updateScreen();
        ++this.field_146444_f;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, I18n.format("menu.game", new Object[0]), this.width / 2, 40, 16777215);
        this.drawCenteredString(IMinecraft.mc.bitFontRendererObj, "Currently Logged Into: " + mc.session.getUsername(), width / 2, 5, 0xFFFFFF);
        String renderVersion = IMinecraft.mc.isSingleplayer() || ViaLoadingBase.getInstance().getTargetVersion().getVersion() == ViaLoadingBase.getInstance().getNativeVersion() ? "Native" : ViaLoadingBase.getInstance().getTargetVersion().getName();
        this.drawCenteredString(IMinecraft.mc.bitFontRendererObj, "Protocol: " + renderVersion, width / 2, 16, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
