package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.api.action.SynchronizedProperty;
import com.alrex.parcool.common.action.impl.Vault;

@Mixin(value = Vault.class)
public interface ParCoolMixinVaultAccessor {
	@Accessor(value = "propertyVaultType", remap = false)
	SynchronizedProperty<?> getVaultType();
}
