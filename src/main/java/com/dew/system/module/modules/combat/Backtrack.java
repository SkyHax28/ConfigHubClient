package com.dew.system.module.modules.combat;

import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.PacketUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class Backtrack extends Module {
    private long maxHistoryTime = 0;
    private double smoothHistoryTime = 0;
    private final Map<UUID, List<PositionRecord>> positionHistory = new HashMap<>();
    public Backtrack() {
        super("Backtrack", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        maxHistoryTime = 0;
        smoothHistoryTime = 0;
        positionHistory.clear();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        maxHistoryTime = 0;
        smoothHistoryTime = 0;
        positionHistory.clear();
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        int ping = PacketUtil.getCurrentPingOrMinusOne();
        if (ping == -1) {
            ping = 0;
        }

        double targetTime = Math.min(ping, 30);
        smoothHistoryTime += (targetTime - smoothHistoryTime) * mc.timer.renderPartialTicks;
        maxHistoryTime = Math.round(smoothHistoryTime);

        long now = System.currentTimeMillis();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer) continue;

            positionHistory.putIfAbsent(player.getUniqueID(), new ArrayList<>());
            List<PositionRecord> history = positionHistory.get(player.getUniqueID());

            history.add(new PositionRecord(now, player.posX, player.posY, player.posZ));
            history.removeIf(record -> now - record.time > maxHistoryTime);
        }
    }

    public Entity getBestBacktrackEntity(Entity target) {
        if (!this.isEnabled()) return target;

        List<PositionRecord> history = positionHistory.get(target.getUniqueID());
        if (history == null || history.isEmpty()) return target;

        EntityPlayerSP self = mc.thePlayer;
        double bestDist = Double.MAX_VALUE;
        PositionRecord bestRecord = null;

        for (PositionRecord record : history) {
            double dist = self.getDistance(record.x, record.y, record.z);
            if (dist < bestDist) {
                bestDist = dist;
                bestRecord = record;
            }
        }

        if (bestRecord == null) return target;

        EntityOtherPlayerMP fake = new EntityOtherPlayerMP(mc.theWorld, ((EntityPlayer) target).getGameProfile());

        fake.copyLocationAndAnglesFrom(target);
        fake.setPosition(bestRecord.x, bestRecord.y, bestRecord.z);
        fake.rotationYaw = target.rotationYaw;
        fake.rotationPitch = target.rotationPitch;
        fake.onGround = target.onGround;

        return fake;
    }

    private static class PositionRecord {
        long time;
        double x, y, z;

        public PositionRecord(long time, double x, double y, double z) {
            this.time = time;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}