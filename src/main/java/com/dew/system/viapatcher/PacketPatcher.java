package com.dew.system.viapatcher;

import com.dew.IMinecraft;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import com.viaversion.viabackwards.protocol.v1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemSnowball;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

public class PacketPatcher {

    private static final Minecraft mc = IMinecraft.mc;

    public static void handleFixedSendPackets(SendPacketEvent event) {
        Packet<?> packet = event.packet;

        if (event.isCancelled()) return;

        if (!mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8) && packet instanceof C0APacketAnimation) {
            event.cancel();
            PacketWrapper fixedC0A = PacketWrapper.create(ServerboundPackets1_9.SWING, Via.getManager().getConnectionManager().getConnections().iterator().next());
            fixedC0A.write(Types.VAR_INT, 0);
            fixedC0A.sendToServer(Protocol1_9To1_8.class);
        }

        if (!mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_16) && (packet instanceof C08PacketPlayerBlockPlacement && IMinecraft.mc.theWorld.getBlockState(((C08PacketPlayerBlockPlacement) packet).getPosition()).getBlock() instanceof BlockAir && (((C08PacketPlayerBlockPlacement) packet).getStack().getItem() instanceof ItemSnowball || ((C08PacketPlayerBlockPlacement) packet).getStack().getItem() instanceof ItemEnderPearl || ((C08PacketPlayerBlockPlacement) packet).getStack().getItem() instanceof ItemEnderEye || ((C08PacketPlayerBlockPlacement) packet).getStack().getItem() instanceof ItemExpBottle || (((C08PacketPlayerBlockPlacement) packet).getStack().getMetadata() & 0x4000) != 0) || packet instanceof C0EPacketClickWindow && (((C0EPacketClickWindow) packet).getMode() == 4 || ((C0EPacketClickWindow) packet).getSlotId() == -999) || packet instanceof C07PacketPlayerDigging && IMinecraft.mc.thePlayer.getHeldItem() != null && (((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.DROP_ITEM || ((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.DROP_ALL_ITEMS))) {
            PacketWrapper swingPacket = PacketWrapper.create(ServerboundPackets1_9.SWING, Via.getManager().getConnectionManager().getConnections().iterator().next());
            swingPacket.write(Types.VAR_INT, 0);
            swingPacket.sendToServer(Protocol1_9To1_8.class);
        }

        if (!mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_11) && packet instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockDirection() != 255) {
            event.cancel();
            PacketWrapper fixedC08 = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM_ON, Via.getManager().getConnectionManager().getConnections().iterator().next());
            fixedC08.write(Types.BLOCK_POSITION1_8, new BlockPosition(((C08PacketPlayerBlockPlacement) packet).getPosition().getX(), ((C08PacketPlayerBlockPlacement) packet).getPosition().getY(), ((C08PacketPlayerBlockPlacement) packet).getPosition().getZ()));
            fixedC08.write(Types.VAR_INT, ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockDirection());
            fixedC08.write(Types.VAR_INT, 0);
            fixedC08.write(Types.FLOAT, ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockOffsetX());
            fixedC08.write(Types.FLOAT, ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockOffsetY());
            fixedC08.write(Types.FLOAT, ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockOffsetZ());
            fixedC08.sendToServer(Protocol1_11To1_10.class);
        }
    }

    public static void handleFixedReceivePackets(ReceivedPacketEvent event) {
        Packet<?> packet = event.packet;

        if (event.isCancelled()) return;

    }
}
