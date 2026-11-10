package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.alrex.parcool.common.action.impl.ChargeJump;

import net.minecraft.client.player.LocalPlayer;
import yesman.epicfight.client.events.engine.ControlEngine;

@Mixin(value = ChargeJump.class)
public class ParCoolMixinChargeJump {
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;isShiftKeyDown()Z"
		),
		method = "isInputActive()Z",
		remap = false
	)
	private boolean epicparcool$isShiftKeyDown(LocalPlayer self) {
		if (ControlEngine.getInstance().moverToggling()) {
			return true;
		}
		
		return self.isShiftKeyDown();
	}
}