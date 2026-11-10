package com.yesman.epicparcool;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.alrex.parcool.client.input.ParCoolKeyBinds;
import com.alrex.parcool.common.action.impl.HangDown;
import com.alrex.parcool.util.VectorUtil;
import com.yesman.epicparcool.animations.ParCoolAnimations;
import com.yesman.epicparcool.mixin.ParCoolMixinHangDownAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class ParCoolUtils {
	public static float idealYRotForHanging(HangDown action, Player player) {
		Vec3 bodyVec = VectorUtil.fromYawDegree(player.yBodyRot).normalize();
		Vec3 lookVec = player.getLookAngle();
		Vec3 idealLookVec;
		
		if (Math.abs(lookVec.x) > Math.abs(lookVec.z)) {
			idealLookVec = new Vec3(lookVec.x > 0.0D ? 1 : -1, 0.0D, 0.0D);
		} else {
			idealLookVec = new Vec3(0.0D, 0.0D, lookVec.z > 0.0D ? 1 : -1);
		}
		
		double differenceAngle = Math.acos(bodyVec.dot(idealLookVec));
		differenceAngle /= 4.0D;
		
		return (float)VectorUtil.toYawDegree(idealLookVec.yRot((float) differenceAngle));
	};
	
	public static boolean isOrthogonalToBar(HangDown action) {
		return action.getBlendFactorOrthogonalToBar(1.0F) > 0.5F;
	}

	@Nullable
	public static BarAxis getHangingBarAxis(HangDown action) {
		Object axis = ((ParCoolMixinHangDownAccessor)action).getHangingBarAxis().get();
		return axis == null ? null : BarAxis.valueOf(((Enum<?>)axis).name());
	}

	@SuppressWarnings("incomplete-switch")
	public static Vec3 getHangableBars(LivingEntity entity, Vec3 simulateNextPos) {
		Vec3 entityPos = entity.position();
		boolean posChanged = false;
    	
    	if (!Vec3.ZERO.equals(simulateNextPos)) {
			entity.setPos(entityPos.add(simulateNextPos));
			posChanged = true;
    	}
		
		double bbWidth = entity.getBbWidth() / 4;
		double bbHeight = 0.35;
		AABB bb = new AABB(
			entity.getX() - bbWidth,
			entity.getY() + entity.getBbHeight(),
			entity.getZ() - bbWidth,
			entity.getX() + bbWidth,
			entity.getY() + entity.getBbHeight() + bbHeight,
			entity.getZ() + bbWidth
		);
		
		if (entity.getCommandSenderWorld().noCollision(entity, bb)) {
			if (posChanged) {
	    		entity.setPos(entityPos);
	    	}
			
			return null;
		}
		
		BlockPos pos = new BlockPos(
			(int) Math.floor(entity.getX()),
			(int) Math.floor(entity.getY() + entity.getBbHeight() + 0.4),
			(int) Math.floor(entity.getZ())
		);
		
		if (!entity.getCommandSenderWorld().isLoaded(pos)) {
			if (posChanged) {
	    		entity.setPos(entityPos);
	    	}
			
			return null;
		}
		
		BlockState state = entity.getCommandSenderWorld().getBlockState(pos);
		Block block = state.getBlock();
		BarAxis axis = null;
		
		if (block instanceof RotatedPillarBlock) { // Chain
			if (state.isCollisionShapeFullBlock(entity.getCommandSenderWorld(), pos)) {
				if (posChanged) {
		    		entity.setPos(entityPos);
		    	}
				
				return null;
			}
			
			Direction.Axis pillarAxis = state.getValue(RotatedPillarBlock.AXIS);
			switch (pillarAxis) {
				case X:
					axis = BarAxis.X;
					break;
				case Z:
					axis = BarAxis.Z;
					break;
			}
		} else if (block instanceof DirectionalBlock) { // End rod, lighting rod
			if (state.isCollisionShapeFullBlock(entity.getCommandSenderWorld(), pos)) {
				if (posChanged) {
		    		entity.setPos(entityPos);
		    	}
				
				return null;
			}
			
			Direction direction = state.getValue(DirectionalBlock.FACING);
			switch (direction) {
				case EAST:
				case WEST:
					axis = BarAxis.X;
					break;
				case NORTH:
				case SOUTH:
					axis = BarAxis.Z;
			}
		} else if (block instanceof CrossCollisionBlock) { // Iron Bars, Fence, Stained glass
			int zCount = 0;
			int xCount = 0;
			if (state.getValue(CrossCollisionBlock.NORTH)) zCount++;
			if (state.getValue(CrossCollisionBlock.SOUTH)) zCount++;
			if (state.getValue(CrossCollisionBlock.EAST)) xCount++;
			if (state.getValue(CrossCollisionBlock.WEST)) xCount++;
			if (zCount > 0 && xCount == 0) axis = BarAxis.Z;
			if (xCount > 0 && zCount == 0) axis = BarAxis.X;
		} else if (block instanceof WallBlock) { // All types of wall blocks
			int zCount = 0;
			int xCount = 0;
			if (state.getValue(WallBlock.NORTH_WALL) != WallSide.NONE) zCount++;
			if (state.getValue(WallBlock.SOUTH_WALL) != WallSide.NONE) zCount++;
			if (state.getValue(WallBlock.EAST_WALL) != WallSide.NONE) xCount++;
			if (state.getValue(WallBlock.WEST_WALL) != WallSide.NONE) xCount++;
			if (zCount > 0 && xCount == 0) axis = BarAxis.Z;
			if (xCount > 0 && zCount == 0) axis = BarAxis.X;
		}
		
		Vec3 simulatedDestPosition = entity.position();
		
		if (posChanged) {
    		entity.setPos(entityPos);
    	}
		
		if (axis == null) {
			return null;
		}
		
		VoxelShape shape = state.getCollisionShape(entity.level(), pos);
		double min = pos.getY() - (0.5D - shape.min(Direction.Axis.Y));
		
		switch (axis) {
		case X -> {
			return new Vec3(simulatedDestPosition.x, min, pos.getZ() + 0.5D);
		}
		case Z -> {
			return new Vec3(pos.getX() + 0.5D, min, simulatedDestPosition.z);
		}
		default -> {
			return null;
		}
		}
	}
	
    public static void spawnJumpParticles(LivingEntity entity, Vec3 wallDirection, Vec3 jumpDirection) {
        Level level = entity.level();
        Vec3 pos = entity.position();
        BlockPos leanedBlock = new BlockPos(
            (int) Math.floor(pos.x() + wallDirection.x()),
            (int) Math.floor(pos.y() + entity.getBbHeight() * 0.25),
            (int) Math.floor(pos.z() + wallDirection.z())
        );
        if (!level.isLoaded(leanedBlock)) return;
        float width = entity.getBbWidth();
        BlockState blockstate = level.getBlockState(leanedBlock);
        Vec3 horizontalJumpDirection = jumpDirection.multiply(1, 0, 1).normalize();
        wallDirection = wallDirection.normalize();
        Vec3 orthogonalToWallVec = wallDirection.yRot((float) (Math.PI / 2)).normalize();

        //doing "Conjugate of (horizontalJumpDirection/-wallDirection)" as complex number(x + z i)
        Vec3 differenceVec =
                new Vec3(
                        -wallDirection.x() * horizontalJumpDirection.x() - wallDirection.z() * horizontalJumpDirection.z(), 0,
                        wallDirection.z() * horizontalJumpDirection.x() - wallDirection.x() * horizontalJumpDirection.z()
                ).multiply(1, 0, -1).normalize();
        Vec3 particleBaseDirection =
                new Vec3(
                        -wallDirection.x() * differenceVec.x() + wallDirection.z() * differenceVec.z(), 0,
                        -wallDirection.x() * differenceVec.z() - wallDirection.z() * differenceVec.x()
                );
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            for (int i = 0; i < 10; i++) {
                Vec3 particlePos = new Vec3(
                        pos.x() + (wallDirection.x() * 0.4 + orthogonalToWallVec.x() * (entity.getRandom().nextDouble() - 0.5D)) * width,
                        pos.y() + 0.1D + 0.3 * entity.getRandom().nextDouble(),
                        pos.z() + (wallDirection.z() * 0.4 + orthogonalToWallVec.z() * (entity.getRandom().nextDouble() - 0.5D)) * width
                );
                Vec3 particleSpeed = particleBaseDirection
                        .yRot((float) (Math.PI * 0.2 * (entity.getRandom().nextDouble() - 0.5)))
                        .scale(3 + 9 * entity.getRandom().nextDouble())
                        .add(0, -jumpDirection.y() * 3 * entity.getRandom().nextDouble(), 0);
                level.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, blockstate).setPos(leanedBlock),
                        particlePos.x(),
                        particlePos.y(),
                        particlePos.z(),
                        particleSpeed.x(),
                        particleSpeed.y(),
                        particleSpeed.z()
                );
            }
        }
    }
    
    public static BarAxis getLookBarAxis(float rotation) {
    	rotation = Math.abs(Mth.wrapDegrees(rotation));
    	return Math.abs(rotation - 90.0F) > 45.0F ? BarAxis.Z : BarAxis.X;
    }
    
    public static record ScanResult(Vec3 grabDirection, Vec3 grabPosition, ClingType clingType) {
    }
    
    public static ScanResult getCollidingPos(Entity entity, Level level, AABB above, AABB below, Vec3 directionsuppose, double xExpand, double zExpand) {
    	if (!level.noCollision(entity, above.expandTowards(xExpand, 0.0D, zExpand))) {
    		return null;
    	}
    	
		AABB expandedBelow = below.expandTowards(xExpand, 0.0D, zExpand);
		VoxelShape shapeSum = Shapes.empty();
		double touchingHeight = -100.0D;
		
		for (VoxelShape voxelshape : level.getBlockCollisions(entity, expandedBelow)) {
			if (voxelshape.isEmpty()) {
				continue;
			}
			
			for (AABB aabb : voxelshape.toAabbs()) {
				if (aabb.intersects(expandedBelow)) {
					if (touchingHeight < aabb.maxY) {
						touchingHeight = aabb.maxY;
					}
					
					shapeSum = Shapes.or(shapeSum, Shapes.create(aabb));
				}
			}
		}
		
		if (shapeSum.isEmpty()) {
			return null;
		}
		
		VoxelShape visualShapeSum = Shapes.empty();
		AABB visualBB =  entity.getBoundingBox().inflate(0.2D, 0.0D, 0.2D);
		visualBB.setMaxY(below.maxY);
		
		for (VoxelShape voxelshape : level.getBlockCollisions(entity, visualBB)) {
			if (voxelshape.isEmpty()) {
				continue;
			}
			
			for (AABB aabb : voxelshape.toAabbs()) {
				if (aabb.intersects(visualBB)) {
					visualShapeSum = Shapes.or(visualShapeSum, Shapes.create(aabb));
				}
			}
		}
		
		AABB visualAbove = visualBB.setMaxY(above.maxY).setMinY(above.minY);
		List<AABB> shapeBBs = new ArrayList<>(visualShapeSum.toAabbs());
		List<AABB> ungrabbableBBs = new ArrayList<>();
		shapeBBs.removeIf(aabb -> aabb.intersects(visualAbove));
		
		visualShapeSum = Shapes.empty();
		
		for (AABB aabb : shapeBBs) {
			if (Double.compare(aabb.maxY, touchingHeight) == 0) {
				visualShapeSum = Shapes.or(visualShapeSum, Shapes.create(aabb));
			} else {
				ungrabbableBBs.add(aabb);
			}
		}
		
		AABB entityBB = entity.getBoundingBox().deflate(0.01D, 0.0D, 0.01D).contract(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
		boolean destBlocked = false;
		
		for (AABB aabb : ungrabbableBBs) {
			destBlocked |= aabb.intersects(entityBB);
		}
		
		if (destBlocked) {
			return null;
		}
		
		AABB shapeBound = shapeSum.bounds();
		
		if (xExpand != 0.0D && zExpand == 0.0D) {
			double xCollide = xExpand > 0.0D ? shapeBound.minX : shapeBound.maxX;
			
			if (visualShapeSum.min(Direction.Axis.Z) < visualBB.minZ && visualShapeSum.max(Direction.Axis.Z) > visualBB.maxZ) {
				return new ScanResult(directionsuppose, new Vec3(xCollide, shapeBound.maxY, entity.getZ()), ClingType.STRAIGHT);
			} else {
				if ((entity.getZ() - shapeBound.minZ) < 0.25D) {
					return new ScanResult(directionsuppose.add(0.0D, 0.0D, 1.0D), new Vec3(xCollide, shapeBound.maxY, shapeBound.minZ), ClingType.OUTER_CORNER);
				} else if ((shapeBound.maxZ - entity.getZ()) < 0.25D) {
					return new ScanResult(directionsuppose.add(0.0D, 0.0D, -1.0D), new Vec3(xCollide, shapeBound.maxY, shapeBound.maxZ), ClingType.OUTER_CORNER);
				} else {
					return new ScanResult(directionsuppose, new Vec3(xCollide, shapeBound.maxY, shapeBound.minZ + (shapeBound.maxZ - shapeBound.minZ) * 0.5D), ClingType.STRAIGHT);
				}
			}
		} else if (xExpand == 0.0D && zExpand != 0.0D) {
			double zCollide = zExpand > 0.0D ? shapeBound.minZ : shapeBound.maxZ;
			
			if (visualShapeSum.min(Direction.Axis.X) < visualBB.minX && visualShapeSum.max(Direction.Axis.X) > visualBB.maxX) {
				return new ScanResult(directionsuppose, new Vec3(entity.getX(), shapeBound.maxY, zCollide), ClingType.STRAIGHT);
			} else {
				if ((entity.getX() - shapeBound.minX) < 0.25D) {
					return new ScanResult(directionsuppose.add(1.0D, 0.0D, 0.0D), new Vec3(shapeBound.minX, shapeBound.maxY, zCollide), ClingType.OUTER_CORNER);
				} else if ((shapeBound.maxX - entity.getX()) < 0.25D) {
					return new ScanResult(directionsuppose.add(-1.0D, 0.0D, 0.0D), new Vec3(shapeBound.maxX, shapeBound.maxY, zCollide), ClingType.OUTER_CORNER);
				} else {
					return new ScanResult(directionsuppose, new Vec3(shapeBound.minX + (shapeBound.maxX - shapeBound.minX) * 0.5D, shapeBound.maxY, zCollide), ClingType.STRAIGHT);
				}
			}
		} else {
			double xCollide = xExpand > 0.0D ? shapeBound.minX : shapeBound.maxX;
			double zCollide = zExpand > 0.0D ? shapeBound.minZ : shapeBound.maxZ;
			
			return new ScanResult(directionsuppose, new Vec3(xCollide, shapeBound.maxY, zCollide), ClingType.OUTER_CORNER);
		}
    }
    
    public static ScanResult getGrabbableWall(Entity entity, Vec3 simulateNextPos) {
    	double baseLine1 = entity.getEyeHeight() + (entity.getBbHeight() - entity.getEyeHeight()) / 2;
		double baseLine2 = entity.getBbHeight() + (entity.getBbHeight() - entity.getEyeHeight()) / 2;
		
		ScanResult wall = getGrabbableWall(entity, simulateNextPos, baseLine1);
		if (wall != null) return wall;
		
		return getGrabbableWall(entity, simulateNextPos, baseLine2);
    }
    
    public static ScanResult getGrabbableWall(Entity entity, Vec3 simulateNextPos, double hangHeight) {
    	Vec3 pos = entity.position();
    	Level level = entity.getCommandSenderWorld();
    	boolean posChanged = false;
    	
    	if (!Vec3.ZERO.equals(simulateNextPos)) {
			entity.setPos(pos.add(simulateNextPos));
			posChanged = true;
    	}
    	
    	double expandingSize = entity.getBbWidth() * 0.49D;
    	Vec3 start = entity.position();
    	
		AABB belowHangHeight = new AABB(
			start.x() - expandingSize,
			start.y() + hangHeight - entity.getBbHeight() / 6,
			start.z() - expandingSize,
			start.x() + expandingSize,
			start.y() + hangHeight,
			start.z() + expandingSize
		);
		AABB aboveHangHeight = new AABB(
			start.x() - expandingSize,
			start.y() + hangHeight,
			start.z() - expandingSize,
			start.x() + expandingSize,
			start.y() + entity.getBbHeight(),
			start.z() + expandingSize
		);
		
		double checkingExpandSize = entity.getBbWidth() * 0.5D;
		ScanResult scanResult;
		ScanResult xScanResult = null;
		ScanResult zScanResult = null;
		
		scanResult = getCollidingPos(entity, level, aboveHangHeight, belowHangHeight, new Vec3(1.0D, 0.0D, 0.0D), checkingExpandSize, 0.0D);
		
		if (scanResult != null) {
			xScanResult = scanResult;
		} else {
			scanResult = getCollidingPos(entity, level, aboveHangHeight, belowHangHeight, new Vec3(-1.0D, 0.0D, 0.0D), -checkingExpandSize, 0.0D);
			
			if (scanResult != null) {
				xScanResult = scanResult;
			}
		}
		
		scanResult = getCollidingPos(entity, level, aboveHangHeight, belowHangHeight, new Vec3(0.0D, 0.0D, 1.0D), 0.0D, checkingExpandSize);
		
		if (scanResult != null) {
			zScanResult = scanResult;
		} else {
			scanResult = getCollidingPos(entity, level, aboveHangHeight, belowHangHeight, new Vec3(0.0D, 0.0D, -1.0D), 0.0D, -checkingExpandSize);
			
			if (scanResult != null) {
				zScanResult = scanResult;
			}
		}
		
		if (xScanResult != null || zScanResult != null) {
			if (xScanResult != null && zScanResult != null) {
				Vec3 wall = xScanResult.grabDirection.add(zScanResult.grabDirection);
				
				scanResult = new ScanResult(
					new Vec3(MathUtils.getSign(wall.x), 0.0D, MathUtils.getSign(wall.z)),
					new Vec3(xScanResult.grabPosition().x, xScanResult.grabPosition().y, zScanResult.grabPosition().z),
					ClingType.INNER_CORNER
				);
			} else {
				scanResult = ParseUtil.nvl(xScanResult, zScanResult);
			}
			
			if (posChanged) {
	    		entity.setPos(pos);
	    	}
			
			return scanResult;
		}
		
		// Check diagonal
		scanResult = getCollidingPos(entity, level, aboveHangHeight, belowHangHeight, new Vec3(1.0D, 0.0D, 1.0D), checkingExpandSize, checkingExpandSize);
		
		if (scanResult == null) {
			scanResult = getCollidingPos(entity, level, aboveHangHeight, belowHangHeight, new Vec3(-1.0D, 0.0D, 1.0D), -checkingExpandSize, checkingExpandSize);
			
			if (scanResult == null) {
				scanResult = getCollidingPos(entity, level, aboveHangHeight, belowHangHeight, new Vec3(1.0D, 0.0D, -1.0D), checkingExpandSize, -checkingExpandSize);
				
				if (scanResult == null) {
					scanResult = getCollidingPos(entity, level, aboveHangHeight, belowHangHeight, new Vec3(-1.0D, 0.0D, -1.0D), -checkingExpandSize, -checkingExpandSize);
				}
			}
		}
		
		if (posChanged) {
    		entity.setPos(pos);
    	}
		
		return scanResult;
    }
    
	@Nullable
	public static boolean scanTerrainAndStartClingAction(PlayerPatch<?> playerpatch, WallMoveType moveType) {
		if (!ParCoolKeyBinds.HANG.state().isDown()) {
			return false;
		}
		
		LivingEntity entity = playerpatch.getOriginal();
		double hangHeight = entity.getBbHeight() + (entity.getBbHeight() - entity.getEyeHeight()) * 0.5D;
		Level world = entity.getCommandSenderWorld();
		
		switch (moveType) {
		case CLING_START -> {
			ScanResult scanResult = getGrabbableWall(entity, Vec3.ZERO);
			
			if (scanResult == null) {
				return false;
			}
			
			Vec3 currentAdjacentWall = scanResult.grabDirection();
			ClingType clingType = scanResult.clingType();
			Vec3 collideHandPos = scanResult.grabPosition();
			float slipperiness;
			
			BlockPos blockPos = new BlockPos(
				(int)Math.floor(entity.getX() + currentAdjacentWall.x),
				(int)(entity.getBoundingBox().minY + hangHeight - 0.3D),
				(int)Math.floor(entity.getZ() + currentAdjacentWall.z)
			);
			
			BlockPos grabbingBlockPos = blockPos.below();
			
			if (!world.isLoaded(grabbingBlockPos)) {
				return false;
			}
			
			BlockState grabbingBlockState = world.getBlockState(grabbingBlockPos);
			slipperiness = grabbingBlockState.getFriction(world, grabbingBlockPos, entity);
			
			if (slipperiness > 0.9D) {
				return false;
			}
			
			float destYRot = (float)Mth.wrapDegrees(MathUtils.getYRotOfVector(currentAdjacentWall));
			AssetAccessor<? extends StaticAnimation> startAnimation = null;
			
			if (clingType == ClingType.STRAIGHT) {
				startAnimation = ParCoolAnimations.BIPED_CLING_START;
				playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLING_DESTINATION, startAnimation, collideHandPos);
			} else if (clingType == ClingType.INNER_CORNER) {
				startAnimation = ParCoolAnimations.BIPED_CLING_START_INNER_CORNER;
				playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CORNER_CLING_DESTINATION, collideHandPos);
			} else if (clingType == ClingType.OUTER_CORNER) {
				startAnimation = ParCoolAnimations.BIPED_CLING_START_OUTER_CORNER;
				playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CORNER_CLING_DESTINATION, collideHandPos);
			}
			
			playerpatch.setModelYRot(destYRot, true);
			playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLIFF_Y_ROT, destYRot);
			playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLING_TYPE, clingType);
			playerpatch.playAnimationSynchronized(startAnimation, 0.0F);
		}
		case MOVE_LEFT, MOVE_RIGHT -> {
			ClingType currentDirection = playerpatch.getAnimator().getVariables().getOrDefaultSharedVariable(ParCoolAnimations.CLING_TYPE);
			
			switch (currentDirection) {
			case INNER_CORNER -> {
				float currentYRot = playerpatch.getYRot();
				float moveYRot = Mth.wrapDegrees(currentYRot + (moveType == WallMoveType.MOVE_LEFT ? -45.0F : 45.0F));
				AnimationAccessor<? extends ActionAnimation> cornerMoveAnimation = (moveType == WallMoveType.MOVE_LEFT ? ParCoolAnimations.BIPED_CLING_MOVE_LEFT_INNER_CORNER2 : ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_INNER_CORNER2);
				playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLIFF_START_Y_ROT, cornerMoveAnimation, currentYRot);
				playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLIFF_DEST_Y_ROT, cornerMoveAnimation, moveYRot);
				playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLIFF_Y_ROT, moveYRot);
				
				Vec3 moveVec = cornerMoveAnimation.get().getExpectedMovement(playerpatch, cornerMoveAnimation.get().getTotalTime());
				
				if (world.noCollision(playerpatch.getOriginal().getBoundingBox().contract(0.1D, 0, 0.1D).move(moveVec))) {
					playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLING_TYPE, ClingType.STRAIGHT);
					playerpatch.playAnimationSynchronized(cornerMoveAnimation, 0.0F);
				} else {
					playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLIFF_Y_ROT, currentYRot);
				}
			}
			case OUTER_CORNER -> {
				float currentYRot = playerpatch.getYRot();
				float destYRot = Mth.wrapDegrees(currentYRot + (moveType == WallMoveType.MOVE_LEFT ? -45.0F : 45.0F));
				float moveYRot = Mth.wrapDegrees(currentYRot + (moveType == WallMoveType.MOVE_LEFT ? 45.0F : -45.0F));
				AnimationAccessor<? extends ActionAnimation> cornerMoveAnimation = (moveType == WallMoveType.MOVE_LEFT ? ParCoolAnimations.BIPED_CLING_MOVE_LEFT_OUTER_CORNER2 : ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_OUTER_CORNER2);
				
				playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLIFF_START_Y_ROT, cornerMoveAnimation, currentYRot);
				playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLIFF_DEST_Y_ROT, cornerMoveAnimation, destYRot);
				playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLIFF_Y_ROT, moveYRot);
				
				Vec3 moveVec = cornerMoveAnimation.get().getExpectedMovement(playerpatch, cornerMoveAnimation.get().getTotalTime());
				
				if (world.noCollision(playerpatch.getOriginal().getBoundingBox().contract(0.1D, 0, 0.1D).move(moveVec))) {
					playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLING_TYPE, ClingType.STRAIGHT);
					playerpatch.playAnimationSynchronized(cornerMoveAnimation, 0.0F);
				} else {
					playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLIFF_Y_ROT, currentYRot);
				}
			}
			case STRAIGHT -> {
				float moveYRot = Mth.wrapDegrees(playerpatch.getYRot() + (moveType == WallMoveType.MOVE_LEFT ? -90.0F : 90.0F));
				Vec3 moveVec = VectorUtil.fromYawDegree(moveYRot).scale(0.637184D);
				ScanResult scanResult;
				BarAxis axis = getLookBarAxis(moveYRot);
				boolean destBlocked = !world.noCollision(entity.getBoundingBox().inflate(axis == BarAxis.X ? 0.2D : 0.0D, 0.0D, axis == BarAxis.Z ? 0.2D : 0.0D).move(moveVec));
				
	    		if (destBlocked) {
	    			scanResult = getGrabbableWall(entity, Vec3.ZERO);
	    		} else {
	    			scanResult = getGrabbableWall(entity, moveVec);
	    		}
	    		
				if (scanResult == null) {
					return false;
				}
				
				ClingType clingType = !destBlocked && scanResult.clingType() == ClingType.INNER_CORNER ? ClingType.STRAIGHT : scanResult.clingType();
				playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLING_TYPE, clingType);
				
				switch (clingType) {
				case INNER_CORNER -> {
					AnimationAccessor<? extends ActionAnimation> cornerMoveAnimation = (moveType == WallMoveType.MOVE_LEFT ? ParCoolAnimations.BIPED_CLING_MOVE_LEFT_INNER_CORNER1 : ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_INNER_CORNER1);
					float visualYRot = Mth.wrapDegrees(playerpatch.getYRot() + (moveType == WallMoveType.MOVE_LEFT ? -45.0F : 45.0F));
					
					playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CORNER_CLING_DESTINATION, scanResult.grabPosition);
					playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLIFF_START_Y_ROT, cornerMoveAnimation, playerpatch.getYRot());
					playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLIFF_DEST_Y_ROT, cornerMoveAnimation, moveYRot);
					playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLIFF_Y_ROT, visualYRot);
					playerpatch.playAnimationSynchronized(cornerMoveAnimation, 0.0F);
				}
				case OUTER_CORNER -> {
					AnimationAccessor<? extends ActionAnimation> cornerMoveAnimation = (moveType == WallMoveType.MOVE_LEFT ? ParCoolAnimations.BIPED_CLING_MOVE_LEFT_OUTER_CORNER1 : ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_OUTER_CORNER1);
					float visualYRot = Mth.wrapDegrees(playerpatch.getYRot() + (moveType == WallMoveType.MOVE_LEFT ? 45.0F : -45.0F));
					
					playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CORNER_CLING_DESTINATION, scanResult.grabPosition);
					playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLIFF_START_Y_ROT, cornerMoveAnimation, playerpatch.getYRot());
					playerpatch.getAnimator().getVariables().put(ParCoolAnimations.CLIFF_DEST_Y_ROT, cornerMoveAnimation, playerpatch.getYRot());
					playerpatch.getAnimator().getVariables().putSharedVariable(ParCoolAnimations.CLIFF_Y_ROT, visualYRot);
					playerpatch.playAnimationSynchronized(cornerMoveAnimation, 0.0F);
				}
				case STRAIGHT -> {
					AnimationAccessor<? extends ActionAnimation> moveAnimation = (moveType == WallMoveType.MOVE_LEFT ? ParCoolAnimations.BIPED_CLING_MOVE_LEFT : ParCoolAnimations.BIPED_CLING_MOVE_RIGHT);
					playerpatch.playAnimationSynchronized(moveAnimation, 0.0F);
				}
				}
			}
			}
		}
		default -> {
			throw new UnsupportedOperationException("Invalid Cling Type");
		}
		}
		
		return true;
	}
	
	public enum ClingType {
		STRAIGHT(false), INNER_CORNER(true), OUTER_CORNER(true);
		
		boolean diagonal;
		
		ClingType(boolean diagonal) {
			this.diagonal = diagonal;
		}
		
		public boolean diagonal() {
			return this.diagonal;
		}
	}
	
	public enum WallMoveType {
		CLING_START, MOVE_LEFT, MOVE_RIGHT;
	}
	
	public enum BarAxis {
		X, Z;
	}
}
