package com.foxtaming.lc;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SleepOnOwnerBedGoal extends Goal {
    private final Fox fox;
    private final TameableFox tameable;
    private BlockPos bed;
    private int ticksTrying;
    private int nextTryTick;

    public SleepOnOwnerBedGoal(Fox fox) {
        this.fox = fox;
        this.tameable = (TameableFox) fox;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean isOnBed() {
        if (!fox.onGround()) return false;
        BlockPos pos = fox.blockPosition();
        if (pos.equals(bed)) return true;

        BlockState state = fox.level().getBlockState(bed);
        return state.getBlock() instanceof BedBlock
                && pos.equals(bed.relative(BedBlock.getConnectedDirection(state))); // the other half
    }

    private boolean wantsToSleep() {
        return fox.level().isBrightOutside();
    }

    @Override
    public boolean canUse() {
        if (fox.tickCount < nextTryTick) return false;
        BlockPos home = tameable.fox_taming$getHomeBed();
        if (home == null || fox.isSleeping() || !wantsToSleep()) return false;

        if (!fox.level().getBlockState(home).is(BlockTags.BEDS)) {
            tameable.fox_taming$setHomeBed(null);
            return false;
        }
        if (fox.distanceToSqr(Vec3.atCenterOf(home)) > 24.0 * 24.0) return false;

        bed = home;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return bed != null && !fox.isSleeping() && wantsToSleep() && ticksTrying < 200;
    }

    @Override
    public void start() {
        ticksTrying = 0;
        fox.getNavigation().moveTo(bed.getX() + 1, bed.getY(), bed.getZ() + 1, 1.0);
    }


    @Override
    public void tick() {
        ticksTrying++;

        if (isOnBed()) {
            fox.getNavigation().stop();
            tameable.fox_taming$setSleeping(true);
            return;
        }

        if (fox.distanceToSqr(Vec3.atCenterOf(bed)) < 9.0) {
            fox.getNavigation().stop();
            fox.getMoveControl().setWantedPosition(bed.getX() + 0.5, bed.getY(), bed.getZ() + 0.5, 1.0);
        } else if (fox.getNavigation().isDone()) {
            fox.getNavigation().moveTo(bed.getX() + 0.5, bed.getY(), bed.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public void stop() {
        if (!fox.isSleeping()) nextTryTick = fox.tickCount + 600;
        bed = null;
    }
}