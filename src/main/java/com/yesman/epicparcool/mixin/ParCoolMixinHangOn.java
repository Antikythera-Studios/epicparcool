package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.alrex.parcool.api.action.ContinuableAction;
import com.alrex.parcool.common.action.impl.HangOn;
import com.yesman.epicparcool.animations.ParCoolAnimations;

import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = HangOn.class)
public abstract class ParCoolMixinHangOn extends ContinuableAction {
	private ParCoolMixinHangOn() {
		super(null, null);
	}
	
	@Inject(at = @At(value = "HEAD"), method = "canContinue()Z", cancellable = true, remap = false)
	public void epicparcool$canContinue(CallbackInfoReturnable<Boolean> callback) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(this.parkourability.player(), PlayerPatch.class);
		
		if (playerpatch != null && playerpatch.isEpicFightMode()) {
			AssetAccessor<? extends StaticAnimation> nowPlaying = playerpatch.getAnimator().getPlayerFor(null).getRealAnimation();
			
			if (nowPlaying == ParCoolAnimations.BIPED_CLING_START ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_OUTER_CORNER2 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT_INNER_CORNER1 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT_INNER_CORNER2 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_INNER_CORNER1 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_INNER_CORNER2 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT_OUTER_CORNER1 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT_OUTER_CORNER2 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_OUTER_CORNER1 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_OUTER_CORNER2
			) {
				callback.setReturnValue(true);
			}
		}
	}
}
