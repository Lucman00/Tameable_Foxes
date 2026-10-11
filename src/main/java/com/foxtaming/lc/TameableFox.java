package com.foxtaming.lc;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface TameableFox {
    @Nullable UUID fox_taming$getOwnerUUID();
    @Nullable BlockPos fox_taming$getHomeBed();
    int fox_taming$getTameAttempts();
    boolean fox_taming$isSitting();

    void fox_taming$setSitting(boolean sitting);
    void fox_taming$setSitPose(boolean sitting);
    void fox_taming$setOwnerUUID(@Nullable UUID uuid);
    void fox_taming$setTameAttempts(int attempts);
    void fox_taming$setHomeBed(@Nullable BlockPos pos);
    void fox_taming$setSleeping(boolean sleeping);


    default boolean fox_taming$isTamed() {
        return fox_taming$getOwnerUUID() != null;
    }
}
