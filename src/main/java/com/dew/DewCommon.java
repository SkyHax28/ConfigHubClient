package com.dew;

import com.dew.system.altmanager.AltManager;
import com.dew.system.command.CommandManager;
import com.dew.system.config.BindConfigManager;
import com.dew.system.config.ClientConfigManager;
import com.dew.system.config.ModuleConfigManager;
import com.dew.system.event.EventManager;
import com.dew.system.gui.NewClickGuiScreen;
import com.dew.system.module.HandleEvents;
import com.dew.system.module.ModuleManager;
import com.dew.system.mongodb.MongoManager;
import com.dew.system.rotation.RotationManager;
import com.dew.system.special.CapeImageLoader;
import com.dew.utils.LogUtil;
import com.dew.utils.font.CustomFontRenderer;
import com.dew.utils.pathfinder.MainPathFinder;
import com.dew.utils.pathfinder.SmartPlayerNavigator;
import de.florianmichael.viamcp.ViaMCP;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;
import java.io.InputStream;

import static com.dew.IMinecraft.mc;

public class DewCommon {

    public static String versionUpdateDate = "September 7th";

    public static final File BASE_CFG_DIR = new File(mc.mcDataDir, "dew");
    public static String clientName = "Dew";
    public static EventManager eventManager;
    public static ModuleManager moduleManager;
    public static CommandManager commandManager;
    public static HandleEvents handleEvents;
    public static CustomFontRenderer customFontRenderer;
    public static AltManager altManager;
    public static ModuleConfigManager moduleConfigManager;
    public static BindConfigManager bindConfigManager;
    public static ClientConfigManager clientConfigManager;
    public static RotationManager rotationManager;
    public static MainPathFinder pathFinder;
    public static SmartPlayerNavigator smartPlayerNavigator;
    public static NewClickGuiScreen clickGuiScreen;
    public static MongoManager mongoManager;

    public static void preInitClient() {
        LogUtil.infoLog("(PRE) Initializing " + clientName + " Client...");
    }

    public static void postInitClient() {
        LogUtil.infoLog("(POST) Initializing " + clientName + " Client...");

        if (!BASE_CFG_DIR.exists()) {
            BASE_CFG_DIR.mkdirs();
        }

        initViaMCP();
        CapeImageLoader.loadAllCapes();
        eventManager = new EventManager();
        rotationManager = new RotationManager();
        moduleConfigManager = new ModuleConfigManager();
        bindConfigManager = new BindConfigManager();
        clientConfigManager = new ClientConfigManager();
        moduleManager = new ModuleManager();
        commandManager = new CommandManager();
        handleEvents = new HandleEvents();
        pathFinder = new MainPathFinder();
        smartPlayerNavigator = new SmartPlayerNavigator();
        clickGuiScreen = new NewClickGuiScreen();
        mongoManager = new MongoManager();
        Font font;
        try {
            InputStream is = mc.getResourceManager()
                    .getResource(new ResourceLocation("minecraft", "dew/Helvetica.ttf"))
                    .getInputStream();

            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(32f);
            LogUtil.infoLog("Loaded custom fonts");
        } catch (Exception e) {
            e.printStackTrace();
            font = new Font("Arial", Font.PLAIN, 32);
            LogUtil.infoLog("Failed to load custom fonts");
        }
        customFontRenderer = new CustomFontRenderer(font);

        altManager = new AltManager();
    }

    private static void initViaMCP() {
        try {
            ViaMCP.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
