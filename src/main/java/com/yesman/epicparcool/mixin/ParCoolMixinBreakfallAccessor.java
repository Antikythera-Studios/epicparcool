package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.api.action.SynchronizedProperty;
import com.alrex.parcool.common.action.impl.Breakfall;

@Mixin(value = Breakfall.class)
public interface ParCoolMixinBreakfallAccessor {
	@Accessor(value = "propertyWorkingBreakfallType", remap = false)
	SynchronizedProperty<Breakfall.BreakfallType> getWorkingBreakfallType();
}
