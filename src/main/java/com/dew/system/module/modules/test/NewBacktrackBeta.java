package com.dew.system.module.modules.test;

import com.dew.DewCommon;
import com.dew.system.event.events.AttackEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.Aura;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NewBacktrackBeta extends Module {

    private final List<PacketEntry> packetQueue = new CopyOnWriteArrayList<>();
    private final List<PosEntry> positions = new CopyOnWriteArrayList<>();
    private PosEntry latestServerPos = null;
    private EntityLivingBase target;
    private final long lastDelay = 80;
    public NewBacktrackBeta() {
        super("NewbacktrackBeta", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onEnable() {
        clear();
    }

    @Override
    public void onDisable() {
        clear();
    }

    private void clear() {
        packetQueue.clear();
        positions.clear();
        latestServerPos = null;
        target = null;
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S14PacketEntity || packet instanceof S18PacketEntityTeleport) {
            int id = (packet instanceof S14PacketEntity) ? ((S14PacketEntity) packet).entityId : ((S18PacketEntityTeleport) packet).getEntityId();
            Entity entity = mc.theWorld.getEntityByID(id);
            if (entity instanceof EntityLivingBase && entity == target) {
                positions.add(new PosEntry(entity.posX, entity.posY, entity.posZ, System.currentTimeMillis()));
                latestServerPos = new PosEntry(entity.posX, entity.posY, entity.posZ, System.currentTimeMillis());
                event.cancel();
                packetQueue.add(new PacketEntry(packet, System.currentTimeMillis()));
            }
        }

        if (packet instanceof S13PacketDestroyEntities && target != null) {
            int[] ids = ((S13PacketDestroyEntities) packet).getEntityIDs();
            for (int id : ids) {
                if (id == target.getEntityId()) {
                    clear();
                    break;
                }
            }
        }

        if (packet instanceof S06PacketUpdateHealth && ((S06PacketUpdateHealth) packet).getHealth() <= 0) {
            clear();
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (target == null || latestServerPos == null) return;

        double x = latestServerPos.x - mc.getRenderManager().viewerPosX;
        double y = latestServerPos.y - mc.getRenderManager().viewerPosY;
        double z = latestServerPos.z - mc.getRenderManager().viewerPosZ;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1f, 0f, 0f, 0.5f);

        AxisAlignedBB bb = target.getEntityBoundingBox().offset(-target.posX + x, -target.posY + y, -target.posZ + z);
        RenderUtil.drawOutlinedBoundingBox(bb, 1f, 0f, 0f, 0.5f);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (target == null || mc.thePlayer == null || mc.theWorld == null) return;

        if (target != DewCommon.moduleManager.getModule(Aura.class).target && DewCommon.moduleManager.getModule(Aura.class).isEnabled() && mc.thePlayer.getDistanceToEntity(target) >= 3f) {
            clear();
            LogUtil.printChat("nerd");
            return;
        }

        long currentTime = System.currentTimeMillis();
        packetQueue.removeIf(entry -> {
            if (currentTime - entry.time >= lastDelay) {
                PacketUtil.processPacketClientSide(entry.packet);
                return true;
            }
            return false;
        });

        positions.removeIf(pos -> currentTime - pos.time >= lastDelay);
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (event.target instanceof EntityLivingBase) {
            if (target != event.target) {
                clear();
            }
            target = (EntityLivingBase) event.target;
        }
    }

    private static class PacketEntry {
        Packet<?> packet;
        long time;

        public PacketEntry(Packet<?> packet, long time) {
            this.packet = packet;
            this.time = time;
        }
    }

    private static class PosEntry {
        double x, y, z;
        long time;

        public PosEntry(double x, double y, double z, long time) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.time = time;
        }
    }
}
