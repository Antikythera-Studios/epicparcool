package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.api.action.SynchronizedProperty;
import com.alrex.parcool.common.action.impl.HorizontalWallRun;

@Mixin(value = HorizontalWallRun.class)
public interface ParCoolMixinHorizontalWallRunAccessor {
	@Accessor(value = "propertyLeftToWall", remap = false)
	SynchronizedProperty<Boolean> getLeftToWall();
}
