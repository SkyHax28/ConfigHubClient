package net.minecraft.client.gui;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import com.dew.IMinecraft;
import com.dew.system.altmanager.alt.SessionChanger;
import com.dew.utils.RandomUtil;
import com.dew.utils.ServerUtil;
import com.dew.utils.VPNUtil;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.gui.GuiProtocolSelector;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IChatComponent;

public class GuiDisconnected extends GuiScreen
{
    private String reason;
    private IChatComponent message;
    private List<String> multilineMessage;
    private final GuiScreen parentScreen;
    private int field_175353_i;

    public GuiDisconnected(GuiScreen screen, String reasonLocalizationKey, IChatComponent chatComp)
    {
        this.parentScreen = screen;
        this.reason = I18n.format(reasonLocalizationKey, new Object[0]);
        this.message = chatComp;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    public void initGui()
    {
        this.buttonList.clear();
        this.multilineMessage = this.fontRendererObj.listFormattedStringToWidth(this.message.getFormattedText(), this.width - 50);
        this.field_175353_i = this.multilineMessage.size() * this.fontRendererObj.FONT_HEIGHT;
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT, I18n.format("gui.toMenu", new Object[0])));

        String lastServerString = "Last Server";
        if (ServerUtil.serverData != null) {
            String fullIp = ServerUtil.serverData.serverIP;
            int maxWidth = 80;

            if (IMinecraft.mc.fontRendererObj.getStringWidth(fullIp) <= maxWidth) {
                lastServerString = fullIp;
            } else {
                String ellipsis = "...";
                int ellipsisWidth = IMinecraft.mc.fontRendererObj.getStringWidth(ellipsis);
                StringBuilder trimmed = new StringBuilder();

                for (int i = 0; i < fullIp.length(); i++) {
                    String sub = fullIp.substring(0, i + 1);
                    int width = IMinecraft.mc.fontRendererObj.getStringWidth(sub);
                    if (width + ellipsisWidth >= maxWidth) break;
                    trimmed.append(fullIp.charAt(i));
                }

                lastServerString = trimmed + ellipsis;
            }
        }

        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 2 + field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 22, 200, 20, "Reconnect to §7" + lastServerString));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 2 + field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 44, 200, 20, "Set Random Cracked"));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 100, this.height / 2 + field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + 66, 200, 20, "Reconnect Mullvad"));
        this.buttonList.add(new GuiButton(69, 5, this.height - 28, 90, 20, "Protocol"));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.mc.displayGuiScreen(this.parentScreen);
        } else if (button.id == 1) {
            ServerUtil.connectToLastServer();
        } else if (button.id == 2) {
            SessionChanger.getInstance().setUserOffline(RandomUtil.randomString(RandomUtil.nextInt(5, 14)));
        } else if (button.id == 3) {
            VPNUtil.reconnectMullvad();
        } else if (button.id == 69) {
            this.mc.displayGuiScreen(new GuiProtocolSelector(this));
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(IMinecraft.mc.bitFontRendererObj, "Currently Logged Into: " + mc.session.getUsername(), width / 2, 5, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj, this.reason, this.width / 2, this.height / 2 - this.field_175353_i / 2 - this.fontRendererObj.FONT_HEIGHT * 2, 11184810);
        int i = this.height / 2 - this.field_175353_i / 2;

        if (this.multilineMessage != null)
        {
            for (String s : this.multilineMessage)
            {
                this.drawCenteredString(this.fontRendererObj, s, this.width / 2, i, 16777215);
                i += this.fontRendererObj.FONT_HEIGHT;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
