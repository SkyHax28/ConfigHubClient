package com.dew.system.module.modules.ghost;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.MovementUtil;
import net.minecraft.block.Block;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemBlock;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

public class BridgeAssist extends Module {

    private static final NumberValue maxSneakTime = new NumberValue("Max Sneak Time", 1.0, 1.0, 5.0, 1.0);
    private int resetTick = 0;
    private int bridTick = 0;
    private boolean bridging = false;

    public BridgeAssist() {
        super("Bridge Assist", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    public boolean isBridging() {
        return this.bridging;
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        this.resetState();
    }

    private void resetState() {
        resetTick = 0;
        bridTick = 0;
        bridging = false;
        mc.gameSettings.keyBindSneak.setKeyDown(false);
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        if (bridTick >= 1 && resetTick == 0) {
            bridTick++;
            if (bridTick >= 8) {
                bridging = false;
                bridTick = 0;
            }
        }

        if (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) return;

        if (this.isNearEdge() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) {
            mc.rightClickDelayTimer = 0;
            if (mc.thePlayer.rotationPitch >= 72f && mc.thePlayer.onGround) {
                mc.gameSettings.keyBindSneak.setKeyDown(true);
                bridging = true;
                bridTick = -1;
            } else {
                this.resetFunction();
            }
        } else {
            this.resetFunction();
        }
    }

    private boolean isNearEdge() {
        double px = mc.thePlayer.posX;
        double py = mc.thePlayer.posY;
        double pz = mc.thePlayer.posZ;

        int baseY = (int) Math.floor(py) - 1;

        double radius = 0.35;
        double step = Math.PI / 36;

        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            radius += 0.15;
        }

        for (double angle = 0; angle < Math.PI * 2; angle += step) {
            double checkX = px + Math.cos(angle) * radius;
            double checkZ = pz + Math.sin(angle) * radius;

            BlockPos pos = new BlockPos(checkX, baseY, checkZ);
            Block block = mc.theWorld.getBlockState(pos).getBlock();

            if (block.isReplaceable(mc.theWorld, pos) || !block.getMaterial().isSolid() || !block.isFullCube()) {
                return true;
            }
        }

        return false;
    }

    private void resetFunction() {
        if (resetTick >= maxSneakTime.get().intValue()) {
            mc.gameSettings.keyBindSneak.setKeyDown(false);
            bridTick = 1;
            resetTick = 0;
        } else if (bridTick == -1) {
            resetTick++;
        }
    }
}
