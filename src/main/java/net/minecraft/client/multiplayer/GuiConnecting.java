package net.minecraft.client.multiplayer;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import com.dew.DewCommon;
import com.dew.system.event.events.GuiConnectingEventActionPerformed;
import com.dew.system.event.events.WorldEvent;
import com.dew.utils.Lerper;
import com.dew.utils.ServerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GuiConnecting extends GuiScreen
{
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private NetworkManager networkManager;
    private boolean cancel;
    private final GuiScreen previousGuiScreen;

    private float fadeAnimation = 0f;
    private float pulseAnimation = 0f;
    private long lastTime = System.currentTimeMillis();
    private int dotsCount = 0;
    private long lastDotTime = 0;

    private static final Color BACKGROUND_PRIMARY = new Color(25, 25, 35, 240);
    private static final Color BACKGROUND_SECONDARY = new Color(35, 35, 45, 220);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255, 200);
    private static final Color HOVER_COLOR = new Color(120, 170, 255, 150);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(200, 200, 200, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 80, 180);

    public GuiConnecting(GuiScreen p_i1181_1_, Minecraft mcIn, ServerData p_i1181_3_)
    {
        this.mc = mcIn;
        this.previousGuiScreen = p_i1181_1_;
        ServerAddress serveraddress = ServerAddress.fromString(p_i1181_3_.serverIP);
        mcIn.loadWorld(null);
        mcIn.setServerData(p_i1181_3_);
        this.connect(serveraddress.getIP(), serveraddress.getPort());
    }

    public GuiConnecting(GuiScreen p_i1182_1_, Minecraft mcIn, String hostName, int port)
    {
        this.mc = mcIn;
        this.previousGuiScreen = p_i1182_1_;
        mcIn.loadWorld(null);
        this.connect(hostName, port);
    }

    private void connect(final String ip, final int port)
    {
        ServerUtil.serverData = new ServerData("", ip + ":" + port, false);
        logger.info("Connecting to " + ip + ", " + port);
        WorldEvent worldEvent = new WorldEvent(ip, port);
        DewCommon.eventManager.call(worldEvent);
        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet())
        {
            public void run()
            {
                InetAddress inetaddress = null;

                try
                {
                    if (GuiConnecting.this.cancel)
                    {
                        return;
                    }

                    inetaddress = InetAddress.getByName(ip);
                    GuiConnecting.this.networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, GuiConnecting.this.mc.gameSettings.isUsingNativeTransport());
                    GuiConnecting.this.networkManager.setNetHandler(new NetHandlerLoginClient(GuiConnecting.this.networkManager, GuiConnecting.this.mc, GuiConnecting.this.previousGuiScreen));
                    GuiConnecting.this.networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN));
                    GuiConnecting.this.networkManager.sendPacket(new C00PacketLoginStart(GuiConnecting.this.mc.getSession().getProfile()));
                }
                catch (UnknownHostException unknownhostexception)
                {
                    if (GuiConnecting.this.cancel)
                    {
                        return;
                    }

                    GuiConnecting.logger.error("Couldn't connect to server", unknownhostexception);
                    GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", "Unknown host")));
                }
                catch (Exception exception)
                {
                    if (GuiConnecting.this.cancel)
                    {
                        return;
                    }

                    GuiConnecting.logger.error("Couldn't connect to server", exception);
                    String s = exception.toString();

                    if (inetaddress != null)
                    {
                        String s1 = inetaddress + ":" + port;
                        s = s.replaceAll(s1, "");
                    }

                    GuiConnecting.this.mc.displayGuiScreen(new GuiDisconnected(GuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", s)));
                }
            }
        }).start();
    }

    public void updateScreen()
    {
        updateAnimations();

        if (this.networkManager != null)
        {
            if (this.networkManager.isChannelOpen())
            {
                this.networkManager.processReceivedPackets();
            }
            else
            {
                this.networkManager.checkDisconnected();
            }
        }
    }

    private void updateAnimations()
    {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;

        if (fadeAnimation < 1f) {
            fadeAnimation = Lerper.lerp(fadeAnimation, 1f, 3f * deltaTime);
            fadeAnimation = Math.max(0f, Math.min(1f, fadeAnimation));
        }

        pulseAnimation += deltaTime * 3f;

        if (currentTime - lastDotTime > 500) {
            dotsCount = (dotsCount + 1) % 4;
            lastDotTime = currentTime;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == 1) {
            try {
                this.actionPerformed(this.buttonList.get(0));
            } catch (Exception e) {
                this.cancel = true;
                if (this.networkManager != null) {
                    this.networkManager.closeChannel(new ChatComponentText("Aborted"));
                }
                this.mc.displayGuiScreen(this.previousGuiScreen);
            }
        }
    }

    public void initGui()
    {
        this.buttonList.clear();
        this.buttonList.add(new ModernGuiButton(0, this.width / 2 - 75, this.height / 2 + 40, 150, 25, "Cancel"));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            GuiConnectingEventActionPerformed event = new GuiConnectingEventActionPerformed();
            DewCommon.eventManager.call(event);
            this.cancel = true;

            if (this.networkManager != null)
            {
                this.networkManager.closeChannel(new ChatComponentText("Aborted"));
            }

            this.mc.displayGuiScreen(this.previousGuiScreen);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        drawConnectionPanel(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawConnectionPanel(int mouseX, int mouseY)
    {
        int panelWidth = 350;
        int panelHeight = 150;
        int panelX = this.width / 2 - panelWidth / 2;
        int panelY = this.height / 2 - panelHeight / 2;

        Color bgPrimary = new Color(BACKGROUND_PRIMARY.getRed(), BACKGROUND_PRIMARY.getGreen(),
                BACKGROUND_PRIMARY.getBlue(), clampAlpha((int)(BACKGROUND_PRIMARY.getAlpha() * fadeAnimation)));
        Color bgSecondary = new Color(BACKGROUND_SECONDARY.getRed(), BACKGROUND_SECONDARY.getGreen(),
                BACKGROUND_SECONDARY.getBlue(), clampAlpha((int)(BACKGROUND_SECONDARY.getAlpha() * fadeAnimation)));

        drawGradientRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight, bgPrimary, bgSecondary);

        Color accentColor = new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(),
                ACCENT_COLOR.getBlue(), clampAlpha((int)(ACCENT_COLOR.getAlpha() * fadeAnimation)));
        drawRect(panelX, panelY, panelX + panelWidth, panelY + 3, accentColor);

        Color borderColor = new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(),
                BORDER_COLOR.getBlue(), clampAlpha((int)(BORDER_COLOR.getAlpha() * fadeAnimation)));
        drawBorder(panelX, panelY, panelX + panelWidth, panelY + panelHeight, borderColor);

        String statusText;
        if (this.networkManager == null) {
            statusText = "Connecting to the server";
        } else {
            statusText = "Logging in";
        }

        String dots = "";
        for (int i = 0; i < dotsCount; i++) {
            dots += ".";
        }
        statusText += dots;

        DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                statusText,
                this.width / 2f,
                panelY + 25,
                Color.WHITE.getRGB(),
                0.4f
        );

        String serverIPRaw = ServerUtil.serverData.serverIP;
        String displayIP;
        String displayPort = "";

        if (serverIPRaw.contains(":")) {
            String[] parts = serverIPRaw.split(":");
            displayIP = parts[0];
            if (parts.length > 1) {
                displayPort = parts[1];
            }
        } else {
            displayIP = serverIPRaw;
        }

        String serverInfo;
        if (!displayPort.isEmpty()) {
            serverInfo = "IP: " + displayIP + ", Port: " + displayPort;
        } else {
            serverInfo = "IP: " + displayIP;
        }

        DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                serverInfo,
                this.width / 2f,
                panelY + 45,
                Color.WHITE.getRGB(),
                0.35f
        );

        drawLoadingBar(panelX + 40, panelY + 80, panelWidth - 80, 4);
    }

    private int clampAlpha(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    private void drawLoadingBar(int x, int y, int width, int height)
    {
        Color bgColor = new Color(40, 40, 50, clampAlpha((int)(180 * fadeAnimation)));
        drawRect(x, y, x + width, y + height, bgColor);

        float progress = (float)((Math.sin(pulseAnimation) + 1) / 2);
        int progressWidth = (int)(width * 0.3f);
        int progressX = x + (int)((width - progressWidth) * progress);

        Color progressColor = new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), clampAlpha((int)(200 * fadeAnimation)));
        drawGradientRect(progressX, y, progressX + progressWidth, y + height, progressColor, new Color(progressColor.getRed(), progressColor.getGreen(), progressColor.getBlue(), clampAlpha((int)(100 * fadeAnimation))));
    }

    private void drawRect(int left, int top, int right, int bottom, Color color) {
        Gui.drawRect(left, top, right, bottom, color.getRGB());
    }

    private void drawGradientRect(int left, int top, int right, int bottom, Color startColor, Color endColor) {
        int steps = bottom - top;
        if (steps <= 0) return;

        for (int i = 0; i < steps; i++) {
            float ratio = (float) i / steps;
            int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * ratio);
            int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * ratio);
            int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * ratio);
            int a = (int) (startColor.getAlpha() + (endColor.getAlpha() - startColor.getAlpha()) * ratio);

            Gui.drawRect(left, top + i, right, top + i + 1, new Color(r, g, b, a).getRGB());
        }
    }

    private void drawBorder(int left, int top, int right, int bottom, Color color) {
        Gui.drawRect(left, top, left + 1, bottom, color.getRGB());
        Gui.drawRect(right - 1, top, right, bottom, color.getRGB());
        Gui.drawRect(left, top, right, top + 1, color.getRGB());
        Gui.drawRect(left, bottom - 1, right, bottom, color.getRGB());
    }

    public class ModernGuiButton extends GuiButton {
        private float hoverAnimation = 0f;
        private long lastButtonTime = System.currentTimeMillis();

        public ModernGuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                long currentTime = System.currentTimeMillis();
                float deltaTime = (currentTime - lastButtonTime) / 1000f;
                lastButtonTime = currentTime;

                boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

                float targetHover = hovered ? 1f : 0f;
                hoverAnimation = Lerper.lerp(hoverAnimation, targetHover, 8f * deltaTime);

                Color bgColor = new Color(BACKGROUND_SECONDARY.getRed(), BACKGROUND_SECONDARY.getGreen(), BACKGROUND_SECONDARY.getBlue(), clampAlpha((int)(BACKGROUND_SECONDARY.getAlpha() * fadeAnimation)));
                Color hoverColor = new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(), HOVER_COLOR.getBlue(), clampAlpha((int)(HOVER_COLOR.getAlpha() * hoverAnimation * fadeAnimation)));

                drawGradientRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, bgColor.getRGB(), bgColor.getRGB());

                if (hoverAnimation > 0.01f) {
                    drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, hoverColor.getRGB());
                }

                Color borderColor = new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(), BORDER_COLOR.getBlue(), clampAlpha((int)(BORDER_COLOR.getAlpha() * fadeAnimation)));
                drawBorder(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, borderColor);

                DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                        this.displayString,
                        this.xPosition + this.width / 2f,
                        this.yPosition + (this.height - 17) / 2f,
                        Color.WHITE.getRGB(),
                        0.4f
                );
            }
        }
    }
}