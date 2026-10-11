package com.foxtaming.lc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.EnumSet;
import java.util.UUID;

public class FollowFoxOwnerGoal extends Goal {
    private final Fox fox;
    private final double speed;
    private final float startDist;
    private float stopDist;
    private final float minStop, maxStop;
    private Player owner;
    private int repathTimer;

    public FollowFoxOwnerGoal(Fox fox, double speed, float startDist, float minStop, float maxStop) {
        this.fox = fox;
        this.speed = speed;
        this.startDist = startDist;
        this.minStop = minStop;
        this.maxStop = maxStop;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }



    @Override
    public boolean canUse() {
        UUID id = ((TameableFox) fox).fox_taming$getOwnerUUID();
        if (id == null) return false;
        Player p = fox.level().getPlayerByUUID(id);
        if (p == null || p.isSpectator()) return false;
        if (fox.distanceToSqr(p) < startDist * startDist) return false;
        owner = p;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return owner != null && owner.isAlive()
                && fox.distanceToSqr(owner) > stopDist * stopDist;
    }

    @Override
    public void start() {
        repathTimer = 0;
        stopDist = minStop + fox.getRandom().nextFloat() * (maxStop - minStop);
    }

    private static final double TELEPORT_DIST = 32.0;

    @Override
    public void tick() {
        fox.getLookControl().setLookAt(owner, 10.0f, fox.getMaxHeadXRot());
        if (--repathTimer <= 0) {
            repathTimer = 10;
            if (fox.distanceToSqr(owner) >= TELEPORT_DIST * TELEPORT_DIST) {
                teleportNearOwner();
            } else {
                fox.getNavigation().moveTo(owner, speed);
            }
        }
    }

    private void teleportNearOwner() {
        BlockPos c = owner.blockPosition();
        for (int i = 0; i < 10; i++) {
            int dx = fox.getRandom().nextIntBetweenInclusive(-3, 3);
            int dz = fox.getRandom().nextIntBetweenInclusive(-3, 3);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue; // not right on top of the owner
            if (fox.randomTeleport(c.getX() + dx + 0.5, c.getY(), c.getZ() + dz + 0.5, false)) {
                fox.getNavigation().stop();
                return;
            }
        }
    }
    @Override
    public void stop() {
        owner = null;
        fox.getNavigation().stop();
    }
}