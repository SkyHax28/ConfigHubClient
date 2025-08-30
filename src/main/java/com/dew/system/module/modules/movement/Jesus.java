package com.dew.system.module.modules.movement;

import com.dew.DewCommon;
import com.dew.system.event.events.BlockBBEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.MovementUtil;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

public class Jesus extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Vanilla", "Vanilla", "Verus Speed Only");

    public Jesus() {
        super("Jesus", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.isSneaking() && mc.thePlayer.getDistanceSqToCenter(event.blockPos) < 2) return;

        if (event.blockPos.getY() == mc.thePlayer.getPosition().down().getY() && (event.block == Blocks.water || event.block == Blocks.flowing_water || event.block == Blocks.lava || event.block == Blocks.flowing_lava) && (mode.get().equals("Vanilla") || mode.get().equals("Verus Speed Only") && DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() && DewCommon.moduleManager.getModule(SpeedModule.class).getMode().equals("Verus") && MovementUtil.isMoving())) {
            int x = event.blockPos.getX();
            int y = event.blockPos.getY();
            int z = event.blockPos.getZ();
            event.boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
        }
    }
}
