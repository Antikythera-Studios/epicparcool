package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.alrex.parcool.api.action.Action;
import com.alrex.parcool.api.action.StaminaConsumption;
import com.alrex.parcool.common.Parkourability;
import com.alrex.parcool.common.action.impl.Dodge;

import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = Action.class)
public class ParCoolMixinAction {
	@Shadow(remap = false)
	@Final
	protected Parkourability parkourability;

	@Inject(at = @At(value = "HEAD"), method = "takeCost(Lcom/alrex/parcool/api/action/StaminaConsumption$Type;)V", cancellable = true, remap = false)
	public void epicparcool$takeCost(StaminaConsumption.Type type, CallbackInfo callback) {
		if ((Object)this instanceof Dodge) {
			PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(this.parkourability.player(), PlayerPatch.class);

			if (playerpatch != null && playerpatch.isEpicFightMode()) {
				callback.cancel();
			}
		}
	}
}
