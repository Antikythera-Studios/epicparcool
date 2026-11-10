package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.api.action.SynchronizedProperty;
import com.alrex.parcool.common.action.impl.WallJump;

@Mixin(value = WallJump.class)
public interface ParCoolMixinWallJumpAccessor {
	@Accessor(value = "propertyJumpVec", remap = false)
	SynchronizedProperty<net.minecraft.world.phys.Vec3> getJumpVec();
}
