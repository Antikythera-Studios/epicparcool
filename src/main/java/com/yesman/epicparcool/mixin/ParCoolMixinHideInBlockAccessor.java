package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.api.action.SynchronizedProperty;
import com.alrex.parcool.common.action.impl.HideInBlock;

@Mixin(value = HideInBlock.class)
public interface ParCoolMixinHideInBlockAccessor {
	@Accessor(value = "propertyStanding", remap = false)
	SynchronizedProperty<Boolean> getStanding();
}
