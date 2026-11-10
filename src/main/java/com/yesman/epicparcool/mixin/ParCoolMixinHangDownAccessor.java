package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.api.action.SynchronizedProperty;
import com.alrex.parcool.common.action.impl.HangDown;

@Mixin(value = HangDown.class)
public interface ParCoolMixinHangDownAccessor {
	@Accessor(value = "propertyHangingBarAxis", remap = false)
	SynchronizedProperty<?> getHangingBarAxis();
}
