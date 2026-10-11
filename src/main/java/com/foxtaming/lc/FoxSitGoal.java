package com.foxtaming.lc;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.fox.Fox;

import java.util.EnumSet;

public class FoxSitGoal extends Goal {
    private final Fox fox;
    private final TameableFox tameable;

    public FoxSitGoal(Fox fox) {
        this.fox = fox;
        this.tameable = (TameableFox) fox;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override public boolean canUse() { return tameable.fox_taming$isSitting(); }
    @Override public boolean canContinueToUse() { return tameable.fox_taming$isSitting(); }

    @Override
    public void start() {
        fox.getNavigation().stop();
        tameable.fox_taming$setSitPose(true);
    }

    @Override
    public void tick() {
        tameable.fox_taming$setSitPose(true);
    }

    @Override
    public void stop() {
        tameable.fox_taming$setSitPose(false);
    }
}