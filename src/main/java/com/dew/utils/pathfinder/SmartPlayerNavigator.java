package com.dew.utils.pathfinder;

import com.dew.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SmartPlayerNavigator {

    private final Minecraft mc = IMinecraft.mc;
    private final List<Vec3> pathPoints = new ArrayList<>();
    private int currentIndex = 0;
    private boolean navigating = false;

    public void startPathTo(Entity target) {
        World world = mc.theWorld;
        EntityPlayerSP player = mc.thePlayer;

        SmartWalkNodeProcessor processor = new SmartWalkNodeProcessor();
        processor.initProcessor(world, player);

        PathPoint start = processor.getPathPointTo(player);
        PathPoint end = processor.getPathPointToCoords(
                target, target.posX, target.getEntityBoundingBox().minY, target.posZ
        );

        PathFinder finder = new PathFinder(processor);
        PathEntity path = finder.createEntityPath(start, end);

        if (path != null && path.getCurrentPathLength() > 0) {
            pathPoints.clear();
            for (int i = 0; i < path.getCurrentPathLength(); i++) {
                pathPoints.add(path.getVectorFromIndex(player, i));
            }
            currentIndex = 0;
            navigating = true;
        }
    }

    public void stop() {
        navigating = false;
        setKeyPresses(false, false, false, false);
        mc.gameSettings.keyBindJump.setKeyDown(false);
    }

    public void onUpdate() {
        if (!navigating || pathPoints.isEmpty() || currentIndex >= pathPoints.size()) {
            stop();
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        Vec3 target = pathPoints.get(currentIndex);

        double dx = target.xCoord - player.posX;
        double dz = target.zCoord - player.posZ;
        double dy = target.yCoord - player.posY;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        if (distXZ < 0.5 && Math.abs(dy) < 1.5) {
            currentIndex++;
            if (currentIndex >= pathPoints.size()) {
                stop();
            }
            return;
        }

        if (player.getDistance(target.xCoord, target.yCoord, target.zCoord) >= 1f) {
            mc.gameSettings.keyBindSprint.setKeyDown(true);
        }

        float desiredYaw = (float) (MathHelper.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float currentYaw = player.rotationYaw;
        float yawDiff = MathHelper.wrapAngleTo180_float(desiredYaw - currentYaw);

        float maxTurn = 90f;
        if (Math.abs(yawDiff) > maxTurn) {
            yawDiff = Math.signum(yawDiff) * maxTurn;
        }
        player.rotationYaw += yawDiff;

        boolean forward = Math.abs(yawDiff) < 45.0f;
        boolean jump = player.onGround && dy > 0.5;

        setKeyPresses(forward, false, false, false);
        mc.gameSettings.keyBindJump.setKeyDown(jump);
    }

    private void setKeyPresses(boolean forward, boolean back, boolean left, boolean right) {
        if (!forward) {
            mc.gameSettings.keyBindSprint.setKeyDown(false);
        }
        mc.gameSettings.keyBindForward.setKeyDown(forward);
        mc.gameSettings.keyBindBack.setKeyDown(back);
        mc.gameSettings.keyBindLeft.setKeyDown(left);
        mc.gameSettings.keyBindRight.setKeyDown(right);
    }
}