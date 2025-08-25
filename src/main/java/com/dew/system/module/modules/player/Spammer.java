package com.dew.system.module.modules.player;

import com.dew.system.event.events.TickEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.Clock;
import com.dew.utils.LogUtil;
import com.dew.utils.RandomUtil;
import com.dew.utils.TimerUtil;
import org.lwjgl.input.Keyboard;

public class Spammer extends Module {

    private static final NumberValue repeatAmount = new NumberValue("Repeat Amount", 5.0, 1.0, 10.0, 1.0);
    private static final NumberValue delay = new NumberValue("Delay", 5000.0, 0.0, 10000.0, 1000.0);
    private final Clock clock = new Clock();
    public Spammer() {
        super("Spammer", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onEnable() {
        clock.reset();
    }

    @Override
    public void onDisable() {
        clock.reset();
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (clock.hasTimePassed(delay.get().intValue())) {
            for (int i = 0; i < repeatAmount.get().intValue(); i++) {
                this.sendChat();
            }
            LogUtil.printChat("Sent chat");
            clock.reset();
        }
    }

    private void sendChat() {
        mc.thePlayer.sendChatMessage(RandomUtil.randomString(5) + " - https://discord.gg/hBesbvw5mM - " + RandomUtil.randomString(5));
    }
}
