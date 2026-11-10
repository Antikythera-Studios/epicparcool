package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.common.action.impl.RideZipline;

@Mixin(value = RideZipline.class)
public interface ParCoolMixinRideZiplineAccessor {
	@Accessor(remap = false)
	double getSpeed();
}
