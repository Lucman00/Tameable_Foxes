package com.tameable_foxes.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import org.jetbrains.annotations.Nullable;



public interface TameableFox extends OwnableEntity {
    boolean isTame();
    void setTame(boolean tame);
    void setOwner(@Nullable LivingEntity owner );
}