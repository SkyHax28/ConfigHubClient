package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.BlinkUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

public class FastBow extends Module {

    public FastBow() {
        super("Fast Bow", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !mc.thePlayer.isUsingItem()) return;

        ItemStack currentStack = mc.thePlayer.inventory.getCurrentItem();

        if (currentStack != null && currentStack.getItem() instanceof ItemBow) {
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.getCurrentEquippedItem(), 0f, 0f, 0f));

            for (int i = 0; i <= 20; ++i) {
                PacketUtil.sendPacket(new C03PacketPlayer.C05PacketPlayerLook(event.yaw, event.pitch, true));
            }

            PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            mc.thePlayer.setItemInUseCount(currentStack.getMaxItemUseDuration() - 1);
        }
    }
}
