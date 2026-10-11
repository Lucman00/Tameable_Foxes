package com.foxtaming.lc.mixin;

import com.foxtaming.lc.FollowFoxOwnerGoal;
import com.foxtaming.lc.FoxSitGoal;
import com.foxtaming.lc.SleepOnOwnerBedGoal;
import com.foxtaming.lc.TameableFox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.attribute.modifier.BooleanModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Fox.class)
public abstract class FoxMixin extends Animal implements TameableFox {

    protected FoxMixin(EntityType<? extends Animal> type, Level level){
        super(type,level);
    }


    @Unique private UUID        fox_taming$ownerUUID;
    @Unique private int         fox_taming$tameAttempts;
    @Unique private BlockPos    fox_taming$homeBed;
    @Unique private boolean     fox_taming$sitting;

    @Shadow protected abstract void setSleeping(boolean sleeping);
    @Shadow abstract void setSitting(boolean sitting);



    //------------- getters -------------
    @Override
    public UUID fox_taming$getOwnerUUID()   { return fox_taming$ownerUUID; }

    @Override
    public int fox_taming$getTameAttempts() { return fox_taming$tameAttempts;}

    @Override
    public BlockPos fox_taming$getHomeBed() { return fox_taming$homeBed; }

    @Override
    public boolean fox_taming$isSitting()   { return fox_taming$sitting; }

    //------------------------------------


    //------------- setters --------------
    @Override
    public void fox_taming$setOwnerUUID(UUID uuid)          { this.fox_taming$ownerUUID = uuid; }

    @Override
    public void fox_taming$setTameAttempts(int attempts)    { this.fox_taming$tameAttempts = attempts;   }

    @Override
    public void fox_taming$setHomeBed(BlockPos pos)         { this.fox_taming$homeBed = pos;}

    @Override
    public void fox_taming$setSleeping(boolean s)           { setSleeping(s);}
    @Override
    public void fox_taming$setSitting(boolean s)            { this.fox_taming$sitting = s; }
    @Override
    public void fox_taming$setSitPose(boolean s)            { setSitting(s); }

    //------------------------------------


    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void fox_taming$save(ValueOutput output, CallbackInfo ci) {
        if (fox_taming$ownerUUID != null) {
            output.store("Owner", UUIDUtil.CODEC, fox_taming$ownerUUID);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void fox_taming$load(ValueInput input, CallbackInfo ci) {
        fox_taming$ownerUUID = input.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void fox_taming$addGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(5, new FollowFoxOwnerGoal((Fox) (Object) this, 0.9, 16.0f, 5.0f, 10.0f));
        this.goalSelector.addGoal(4, new SleepOnOwnerBedGoal((Fox) (Object) this));
        this.goalSelector.addGoal(2, new FoxSitGoal((Fox) (Object) this));
    }
    @Inject(method = "trusts", at = @At("HEAD"), cancellable = true)
    private void fox_taming$trustOwner(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (fox_taming$ownerUUID == null) return;
        if (entity instanceof Player) cir.setReturnValue(true);

    }
}

