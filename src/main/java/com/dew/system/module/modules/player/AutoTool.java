package com.dew.system.module.modules.player;

import com.dew.system.event.events.AttackEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.utils.AutoToolManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

public class AutoTool extends Module {

    private static final BooleanValue autoSword = new BooleanValue("Auto Sword", true);
    public final AutoToolManager autoToolManager = new AutoToolManager();
    private BlockPos currentBlock = null;
    private boolean noSwordUpdates = true;

    public AutoTool() {
        super("Auto Tool", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    public int getOriginalSlot() {
        return autoToolManager.originalSlot;
    }

    public void doNotUpdateSwordNow(boolean state) {
        this.noSwordUpdates = state;
    }

    @Override
    public void onDisable() {
        currentBlock = null;
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        currentBlock = null;
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (autoSword.get() && !event.target.isDead && !noSwordUpdates)
            autoToolManager.switchToBestSword();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (currentBlock != null && !mc.playerController.getIsHittingBlock()) {
            autoToolManager.stop();
            currentBlock = null;
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (mc.thePlayer == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof C07PacketPlayerDigging) {
            if (((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                currentBlock = ((C07PacketPlayerDigging) packet).getPosition();
                autoToolManager.start(currentBlock);
            } else if (((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK || ((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                autoToolManager.stop();
                currentBlock = null;
            }
        }
    }
}
