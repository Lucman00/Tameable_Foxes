package com.foxtaming.lc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FoxTaming implements ModInitializer {
	public static final String MOD_ID = "fox-taming";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) ->
				origin.getEntities(
						EntityTypeTest.forClass(Fox.class),
						f -> player.getUUID().equals(((TameableFox) f).fox_taming$getOwnerUUID())
				).forEach(f -> ((TameableFox) f).fox_taming$setSitting(true))
		);

		EntitySleepEvents.START_SLEEPING.register(((entity, sleepingPos) -> {
			if (!(entity instanceof ServerPlayer player)) return;
			if (!(player.level() instanceof ServerLevel level)) return;

			level.getEntitiesOfClass(
					Fox.class,
					new AABB(sleepingPos).inflate(32),
					f -> player.getUUID().equals(((TameableFox) f).fox_taming$getOwnerUUID())
			).forEach(f -> ((TameableFox) f).fox_taming$setHomeBed(sleepingPos));
		}));


		UseEntityCallback.EVENT.register(((player, level, hand, entity, hitResult) -> {
			if (!(entity instanceof Fox fox)) return InteractionResult.PASS;
			if (level.isClientSide()) return InteractionResult.PASS;
			ItemStack stack = player.getItemInHand(hand);
			TameableFox tameable = (TameableFox) fox;

			if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND
					&& player.getUUID().equals(tameable.fox_taming$getOwnerUUID())) {
				boolean sit = !tameable.fox_taming$isSitting();
				tameable.fox_taming$setSitting(sit);
				return InteractionResult.SUCCESS;
			}

			if (!stack.is(Items.SWEET_BERRIES)) return InteractionResult.PASS;

			if (tameable.fox_taming$isTamed()) return InteractionResult.PASS;

			stack.consume(1, player);
			player.swing(hand, true);

			int attempts = Math.min(tameable.fox_taming$getTameAttempts() + 1, 6);
			tameable.fox_taming$setTameAttempts(attempts);

			float chance = attempts * .09f; // 6% per attempt to tame, cap at 54% ( don't 100% it so players can get unlucky )
											// we love RNG baby



			boolean success = level.getRandom().nextFloat() < chance;

			if (success) {
				tameable.fox_taming$setOwnerUUID(player.getUUID());
				tameable.fox_taming$setTameAttempts(0);
			}

			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(
						success ? ParticleTypes.HEART : ParticleTypes.SMOKE,
						fox.getX(), fox.getY() + 0.5, fox.getZ(),
						7,
						fox.getBbWidth() / 2.0,
						0.3,
						fox.getBbWidth() / 2.0,
						0.02
				);
			}



			return InteractionResult.SUCCESS;
		}));
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
