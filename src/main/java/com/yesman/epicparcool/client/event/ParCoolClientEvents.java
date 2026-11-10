package com.yesman.epicparcool.client.event;

import java.util.Map;

import com.alrex.parcool.api.action.Action;
import com.alrex.parcool.api.action.ActionEntry;
import com.alrex.parcool.api.action.ContinuableAction;
import com.alrex.parcool.common.Parkourability;
import com.alrex.parcool.common.action.ParCoolActions;
import com.alrex.parcool.common.action.impl.HangDown;
import com.alrex.parcool.common.action.impl.HangOn;
import com.alrex.parcool.common.action.impl.RideZipline;
import com.alrex.parcool.common.action.impl.SlideDown;
import com.alrex.parcool.util.VectorUtil;
import com.google.common.collect.Maps;
import com.yesman.epicparcool.EpicParCool;
import com.yesman.epicparcool.ParCoolUtils;
import com.yesman.epicparcool.ParCoolUtils.BarAxis;
import com.yesman.epicparcool.ParCoolUtils.ClingType;
import com.yesman.epicparcool.ParcoolLivingMotions;
import com.yesman.epicparcool.animations.ParCoolAnimations;
import com.yesman.epicparcool.mixin.ParCoolMixinHideInBlockAccessor;
import com.yesman.epicparcool.mixin.ParCoolMixinHorizontalWallRunAccessor;

import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.entity.ModifyPlayerLivingMotionEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@EventBusSubscriber(modid = EpicParCool.MODID, value = Dist.CLIENT)
public class ParCoolClientEvents {
	@FunctionalInterface
	public interface LifecycleAnimationLinker {
		void accept(Action action, Parkourability parkourability, ModifyPlayerLivingMotionEvent.BaseLayer animationUpdateEvent);
	}
	
	private static final Map<ActionEntry<? extends ContinuableAction>, LifecycleAnimationLinker> PARCOOL_ANIMATOR_MAPPING = Maps.newLinkedHashMap();
	
	//Mod bus event
	public static void onSetup(FMLClientSetupEvent event) {
		PARCOOL_ANIMATOR_MAPPING.clear();
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.CHARGE_JUMP, (action, parkourability, livingMotionUpdateEvent) -> {
			livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CAT_LEAP_PREPARATION);
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.HANG_ON, (action, parkourability, livingMotionUpdateEvent) -> {
			livingMotionUpdateEvent.getPlayerPatch().getAnimator().getVariables().getSharedVariable(ParCoolAnimations.CLING_TYPE).ifPresentOrElse((clingDirection) -> {
				if (clingDirection == ClingType.OUTER_CORNER) {
					livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF_OUTER_CORNER);
				} else if (clingDirection == ClingType.INNER_CORNER) {
					livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF_INNER_CORNER);
				} else {
					HangOn hangOn = (HangOn)action;
					
					if (hangOn.getBlendFactorLeftToWall() > 0.5F) {
						livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF_LEFT);
					} else if (hangOn.getBlendFactorRightToWall() > 0.5F) {
						livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF_RIGHT);
					} else {
						livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF);
					}
				}
			}, () -> {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF);
			});
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.SKYDIVE, (action, parkourability, livingMotionUpdateEvent) -> {
			if (!livingMotionUpdateEvent.getPlayerPatch().getOriginal().isFallFlying()) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.SKY_DIVE);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.DIVE, (action, parkourability, livingMotionUpdateEvent) -> {
			if (!livingMotionUpdateEvent.getPlayerPatch().getOriginal().isFallFlying()) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.DIVE);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.SLIDE_DOWN, (action, parkourability, livingMotionUpdateEvent) -> {
			Vec3 wall = ((SlideDown)action).getWallVec(1.0F);
			
			if (wall == null) {
				return;
			}
			
			Vec3 bodyVec = VectorUtil.fromYawDegree(livingMotionUpdateEvent.getPlayerPatch().getOriginal().yBodyRot);
			Vec3 vec = new Vec3(bodyVec.x, 0, bodyVec.z).normalize();
			Vec3 dividedVec = new Vec3(vec.x * wall.x + vec.z * wall.z, 0, -vec.x * wall.z + vec.z * wall.x).normalize();
			
			if (dividedVec.z < 0) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.WALL_SLIDING_RIGHT);
			} else {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.WALL_SLIDING_LEFT);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.HORIZONTAL_WALL_RUN, (action, parkourability, livingMotionUpdateEvent) -> {
			if (!Boolean.TRUE.equals(((ParCoolMixinHorizontalWallRunAccessor)action).getLeftToWall().get())) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.WALL_RUNNING_RIGHT);
			} else {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.WALL_RUNNING_LEFT);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.FAST_RUN, (action, parkourability, livingMotionUpdateEvent) -> {
			if (livingMotionUpdateEvent.getMotion() == LivingMotions.RUN) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.FAST_RUN);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.HANG_DOWN, (action, parkourability, livingMotionUpdateEvent) -> {
			if (ParCoolUtils.isOrthogonalToBar((HangDown)action)) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.HANG_DOWN_ORTHOGONAL);
			} else {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.HANG_DOWN);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.SLIDE, (action, parkourability, livingMotionUpdateEvent) -> {
			livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.SLIDING);
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.HIDE_IN_BLOCK, (action, parkourability, livingMotionUpdateEvent) -> {
			if (Boolean.FALSE.equals(((ParCoolMixinHideInBlockAccessor)action).getStanding().get())) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.HIDE_IN_BLOCK_HORIZONTAL);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.RIDE_ZIPLINE, (action, parkourability, livingMotionUpdateEvent) -> {
			LivingMotion oldMotion = livingMotionUpdateEvent.getPlayerPatch().getClientAnimator().currentMotion();
			
			if (oldMotion == ParcoolLivingMotions.RIDE_ZIPLINE_FORWARD || oldMotion == ParcoolLivingMotions.RIDE_ZIPLINE_SIDE) {
				livingMotionUpdateEvent.setMotion(oldMotion);
				return;
			}
			
			Vec3 offset = ((RideZipline)action).getZiplineOffset();
			
			if (offset == null) {
				return;
			}
			
			offset = offset.normalize();
			Vec3 lookVec = VectorUtil.fromYawDegree(livingMotionUpdateEvent.getPlayerPatch().getOriginal().getYRot());
			double dot = offset.dot(lookVec);
			double yRot = VectorUtil.toYawDegree(offset);
			
			if (Math.abs(dot) > 0.5D) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.RIDE_ZIPLINE_FORWARD);
				
				if (dot < 0.0D) {
					yRot += 180.0D;
				}
				
				livingMotionUpdateEvent.getPlayerPatch().setYRot((float)yRot);
			} else {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.RIDE_ZIPLINE_SIDE);
			}
		});
		
		/**
		PARCOOL_ANIMATOR_MAPPING.put(ParCoolActions.CRAWL, (action, parkourability, livingMotionUpdateEvent) -> {
			livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CRAWL);
		});
		**/
	}

	public static void skillExecute(SkillCastEvent skillexecuteevent) {
		PlayerPatch<?> playerpatch = skillexecuteevent.getPlayerPatch();
		
		// Extend phantom ascent
		if (skillexecuteevent.getSkillContainer().getSkill() == EpicFightSkills.PHANTOM_ASCENT.get()) {
			Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
			
			if (parkourability.get(ParCoolActions.HANG_ON).isDoing() || parkourability.get(ParCoolActions.CLIMB_UP).isDoing()) {
                skillexecuteevent.cancel();
				return;
			}
			
			if (parkourability.get(ParCoolActions.WALL_JUMP).canStart()) {
				skillexecuteevent.cancel();
				skillexecuteevent.getSkillContainer().getDataManager().setData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, true);
				return;
			}
			
			if (parkourability.get(ParCoolActions.WALL_RUN).canStart()) {
				skillexecuteevent.cancel();
				skillexecuteevent.getSkillContainer().getDataManager().setData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, true);
				return;
			}
			
			if (parkourability.get(ParCoolActions.HANG_DOWN).isDoing()) {
				skillexecuteevent.cancel();
				skillexecuteevent.getSkillContainer().getDataManager().setData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, true);
			}
		}
	}
	
	public static void onBaseLayerUpdateEvent(ModifyPlayerLivingMotionEvent.BaseLayer event) {
		if (event.inaction()) {
			return;
		}
		
		Parkourability parkourability = Parkourability.get(event.getPlayerPatch().getOriginal());
		
		if (parkourability == null) {
			return;
		}
		
		for (Map.Entry<ActionEntry<? extends ContinuableAction>, LifecycleAnimationLinker> entry : PARCOOL_ANIMATOR_MAPPING.entrySet()) {
			ContinuableAction action = parkourability.get(entry.getKey());
			
			if (action.isDoing()) {
				entry.getValue().accept(action, parkourability, event);
				return;
			}
		}
	}
	
	@SubscribeEvent
	public static void onMovementInputUpdateEvent(MovementInputUpdateEvent event) {
		LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LocalPlayerPatch.class);
		
		if (playerpatch == null || !playerpatch.isEpicFightMode()) {
			return;
		}
		
		Parkourability parkourability = Parkourability.get(event.getEntity());
		HangDown hangDown;
		
		if ((hangDown = parkourability.get(ParCoolActions.HANG_DOWN)) != null && hangDown.isDoing()) {
			if (!playerpatch.getEntityState().inaction()) {
				float yRot = ParCoolUtils.idealYRotForHanging(hangDown, event.getEntity());
				BarAxis barAxis = ParCoolUtils.getHangingBarAxis(hangDown);
				boolean axisMismatches = false;
				
				if (ParCoolUtils.isOrthogonalToBar(hangDown)) {
					if (barAxis == ParCoolUtils.getLookBarAxis(yRot)) {
						axisMismatches = true;
					}
				} else {
					if (barAxis != ParCoolUtils.getLookBarAxis(yRot)) {
						axisMismatches = true;
					}
				}
				
				if (!axisMismatches) {
					playerpatch.setModelYRot(yRot, true);
					AssetAccessor<? extends ActionAnimation> animationAccessor = null;
					AssetAccessor<? extends ActionAnimation> endAnimationAccessor = null;
					
					if (ParCoolUtils.isOrthogonalToBar(hangDown)) {
						if (event.getInput().left) {
							animationAccessor = ParCoolAnimations.BIPED_HANG_DOWN_MOVE_LEFT;
						} else if (event.getInput().right) {
							animationAccessor = ParCoolAnimations.BIPED_HANG_DOWN_MOVE_RIGHT;
						}
					} else {
						if (event.getInput().up) {
							animationAccessor = ParCoolAnimations.BIPED_HANG_DOWN_MOVE_FORWARD_START;
							endAnimationAccessor = ParCoolAnimations.BIPED_HANG_DOWN_MOVE_FORWARD_END1;
						} else if (event.getInput().down) {
							animationAccessor = ParCoolAnimations.BIPED_HANG_DOWN_MOVE_BACKWARD;
						}
					}
					
					if (animationAccessor != null) {
						Vec3 simulatedMove = animationAccessor.get().getExpectedMovement(playerpatch, animationAccessor.get().getTotalTime());
						
						if (endAnimationAccessor != null) {
							simulatedMove = simulatedMove.add(endAnimationAccessor.get().getExpectedMovement(playerpatch, endAnimationAccessor.get().getTotalTime()));
						}
						
						Vec3 hangDownDest = ParCoolUtils.getHangableBars(playerpatch.getOriginal(), simulatedMove);
						
						if (hangDownDest != null) {
							playerpatch.playAnimationSynchronized(animationAccessor, 0.0F);
						}
					}
				}
			}
			
			event.getInput().left = false;
			event.getInput().right = false;
			event.getInput().up = false;
			event.getInput().down = false;
			event.getInput().forwardImpulse = 0.0F;
			event.getInput().leftImpulse = 0.0F;
			event.getEntity().setDeltaMovement(0, 0, 0);
		} else if (parkourability.get(ParCoolActions.HANG_ON).isDoing()) {
			if (!playerpatch.getEntityState().inaction()) {
				if (event.getInput().left) {
					ParCoolUtils.scanTerrainAndStartClingAction(playerpatch, ParCoolUtils.WallMoveType.MOVE_LEFT);
				} else if (event.getInput().right) {
					ParCoolUtils.scanTerrainAndStartClingAction(playerpatch, ParCoolUtils.WallMoveType.MOVE_RIGHT);
				}
			}
			
			event.getInput().left = false;
			event.getInput().right = false;
			event.getInput().up = false;
			event.getInput().down = false;
			event.getInput().forwardImpulse = 0.0F;
			event.getInput().leftImpulse = 0.0F;
		}
	}

    public static void addEventHooks() {
        EpicFightEventHooks.Player.CAST_SKILL.registerEvent(ParCoolClientEvents::skillExecute);
        EpicFightClientEventHooks.Entity.MODIFY_PLAYER_LIVING_MOTION_BASE.registerEvent(ParCoolClientEvents::onBaseLayerUpdateEvent);
    }
}
