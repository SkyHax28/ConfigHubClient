package com.dew.system.module.modules.combat;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.PacketUtil;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

public class FastUse extends Module {

    public FastUse() {
        super("Fast Use", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (this.isConsumingItem() && mc.thePlayer.getItemInUseDuration() > 14) {
            for (int i = 0; i <= 20; ++i) {
                PacketUtil.sendPacket(new C03PacketPlayer(true));
            }

            mc.playerController.onStoppedUsingItem(mc.thePlayer);
        }
    }

    private boolean isConsumingItem() {
        if (!mc.thePlayer.isUsingItem()) return false;

        Item item = mc.thePlayer.getItemInUse().getItem();
        return item instanceof ItemFood || item instanceof ItemBucketMilk || item instanceof ItemPotion;
    }
}
