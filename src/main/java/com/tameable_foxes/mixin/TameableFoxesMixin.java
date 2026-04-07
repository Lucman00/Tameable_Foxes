package com.tameable_foxes.mixin;

import com.tameable_foxes.entity.Accessor.EntityAccessor;
import com.tameable_foxes.entity.TameableFox;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
//defineSynchedData

@Mixin(Fox.class)
public abstract class TameableFoxesMixin implements TameableFox {
	private static final EntityDataAccessor<Byte> FOX_TAME_FLAGS =
			SynchedEntityData.defineId(Fox.class, EntityDataSerializers.BYTE);

	private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> FOX_OWNER =
			SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void injectTameData(SynchedEntityData.Builder builder, CallbackInfo ci){
		builder.define(FOX_TAME_FLAGS, (byte) 0);
		builder.define(FOX_OWNER, Optional.empty());
	}

	@Override
	public boolean isTame(){
		return (((EntityAccessor)(Object)this).getEntityDataAccessor().get(FOX_TAME_FLAGS) & 4) != 0;
	}
	public void setTame(boolean tamed){
		SynchedEntityData data = ((EntityAccessor)(Object)this).getEntityDataAccessor();
		byte current =  data.get(FOX_TAME_FLAGS);
		if (tamed) {
			data.set(FOX_TAME_FLAGS, (byte) (current | 4) );
		} else {
			data.set(FOX_TAME_FLAGS, (byte) (current & -5) );
		}
	}

	@Override
	public @Nullable EntityReference<LivingEntity> getOwnerReference(){
		return ((EntityAccessor)(Object)this).getEntityDataAccessor().get(FOX_OWNER).orElse(null);
	}
	public void setOwner(@Nullable LivingEntity owner){
		((EntityAccessor)(Object)this).getEntityDataAccessor()
				.set(FOX_OWNER, Optional.ofNullable(owner).map(EntityReference::of));}


	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void saveTameData(ValueOutput output, CallbackInfo ci){
		EntityReference.store(this.getOwnerReference(), output, "FoxOwner");
		output.putBoolean("FoxIsTamed", this.isTame());
	}


	@Inject(method="readAdditionalSaveData", at = @At("TAIL"))

	private void loadTameData(ValueInput input, CallbackInfo ci){
		SynchedEntityData data = ((EntityAccessor)(Object)this).getEntityDataAccessor();
		EntityReference<LivingEntity> owner = EntityReference.readWithOldOwnerConversion(
				input, "FoxOwner", ((Fox) (Object)this).level()
		);
		if (owner != null){
			data.set(FOX_OWNER, Optional.of(owner));
			this.setTame(true);
		}else {
			data.set(FOX_OWNER, Optional.empty());
			this.setTame(false);
		}
	}

}