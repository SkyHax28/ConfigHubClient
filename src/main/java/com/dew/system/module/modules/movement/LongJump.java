package com.dew.system.module.modules.movement;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

public class LongJump extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Verus", "Verus");

    public LongJump() {
        super("Long Jump", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        if (mode.get().equals("Verus")) {
            BlockPos downPos = mc.thePlayer.getPosition().add(0.0, -1.5, 0.0);
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(downPos, 1, new ItemStack(Blocks.stone.getItem(mc.theWorld, downPos)), 0.0F, 0.5F + ((float) Math.random()) * 0.44F, 0.0F));
            mc.thePlayer.motionY = 1;
            MovementUtil.strafe(0.3f);
        }
    }
}
