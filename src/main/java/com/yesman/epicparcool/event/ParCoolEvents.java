package com.yesman.epicparcool.event;

import java.util.Map;
import java.util.function.BiFunction;

import com.alrex.parcool.api.action.Action;
import com.alrex.parcool.api.action.ParCoolActionEvent;
import com.alrex.parcool.client.input.ParCoolKeyBinds;
import com.alrex.parcool.common.Parkourability;
import com.alrex.parcool.common.action.ParCoolActions;
import com.alrex.parcool.common.action.impl.Breakfall;
import com.alrex.parcool.common.action.impl.ChargeJump;
import com.alrex.parcool.common.action.impl.ClimbUp;
import com.alrex.parcool.common.action.impl.Dodge;
import com.alrex.parcool.common.action.impl.HangDown;
import com.alrex.parcool.common.action.impl.HangOn;
import com.alrex.parcool.common.action.impl.LongJump;
import com.alrex.parcool.common.action.impl.TrickJump;
import com.alrex.parcool.common.action.impl.Vault;
import com.alrex.parcool.common.action.impl.WallJump;
import com.alrex.parcool.common.action.impl.WallRun;
import com.alrex.parcool.util.EntityUtil;
import com.google.common.collect.Maps;
import com.yesman.epicparcool.EpicParCool;
import com.yesman.epicparcool.ParCoolUtils;
import com.yesman.epicparcool.ParcoolLivingMotions;
import com.yesman.epicparcool.animations.ParCoolAnimations;
import com.yesman.epicparcool.mixin.ParCoolMixinBreakfallAccessor;
import com.yesman.epicparcool.mixin.ParCoolMixinVaultAccessor;
import com.yesman.epicparcool.mixin.ParCoolMixinWallJumpAccessor;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.event.types.animation.InitAnimatorEvent;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@EventBusSubscriber(modid = EpicParCool.MODID)
public class ParCoolEvents {
    private static final Map<Class<? extends com.alrex.parcool.api.action.Action>, BiFunction<PlayerPatch<?>, ParCoolActionEvent.Start.Post, AssetAccessor<? extends StaticAnimation>>> PARCOOL_ACTION_START_MAPPING = Maps.newHashMap();
    private static final Map<Class<? extends com.alrex.parcool.api.action.Action>, BiFunction<PlayerPatch<?>, Action, Boolean>> PARCOOL_ACTION_CANCEL_EVENTS = Maps.newHashMap();

    //Mod bus event
    public static void onSetup(FMLCommonSetupEvent event) {
        PARCOOL_ACTION_START_MAPPING.clear();
        PARCOOL_ACTION_CANCEL_EVENTS.clear();

        PARCOOL_ACTION_START_MAPPING.put(LongJump.class, (playerpatch, startEvent) -> {
            Parkourability parkourability = Parkourability.get(startEvent.getPlayer());

            if (parkourability.get(ParCoolActions.SLIDE).isDoing() || parkourability.get(ParCoolActions.CRAWL).isDoing()) {
                return null;
            }

            return ParCoolAnimations.BIPED_CAT_LEAP;
        });

        PARCOOL_ACTION_START_MAPPING.put(Breakfall.class, (playerpatch, startEvent) -> {
            if (((ParCoolMixinBreakfallAccessor)startEvent.getAction()).getWorkingBreakfallType().get() != Breakfall.BreakfallType.ROLL) {
                return null;
            }

            return ParCoolAnimations.BIPED_ROLL_FORWARD;
        });

        PARCOOL_ACTION_START_MAPPING.put(TrickJump.class, (playerpatch, startEvent) -> {
            LocalPlayer clientPlayer = (LocalPlayer)startEvent.getPlayer();

            if (clientPlayer.input.forwardImpulse < 0.0F) {
                return ParCoolAnimations.BIPED_FLIP_BACKWARD;
            } else {
                return ParCoolAnimations.BIPED_FLIP_FOWARD;
            }
        });

        PARCOOL_ACTION_START_MAPPING.put(HangDown.class, (playerpatch, startEvent) -> {
            HangDown action = ((HangDown)startEvent.getAction());
            float yRot = ParCoolUtils.idealYRotForHanging(action, startEvent.getPlayer());

            if (ParCoolUtils.isOrthogonalToBar(action)) {
                playerpatch.setModelYRot(yRot, true);
                return ParCoolAnimations.BIPED_HANG_DOWN_INERTIA_ORTHOGONAL;
            } else {
                playerpatch.setModelYRot(yRot, true);
                return ParCoolAnimations.BIPED_HANG_DOWN_INERTIA;
            }
        });

        PARCOOL_ACTION_START_MAPPING.put(Vault.class, (playerpatch, startEvent) -> {
            Object vaultType = ((ParCoolMixinVaultAccessor)startEvent.getAction()).getVaultType().get();

            if (vaultType == null) {
                return ParCoolAnimations.BIPED_VAULT_FORWARD;
            }

            String type = ((Enum<?>)vaultType).name();

            switch (type) {
            case "FORWARD" -> {
                return ParCoolAnimations.BIPED_VAULT_FORWARD;
            }
            case "LEFT" -> {
                return ParCoolAnimations.BIPED_VAULT_LEFT;
            }
            case "RIGHT" -> {
                return ParCoolAnimations.BIPED_VAULT_RIGHT;
            }
            default -> {
                throw new UnsupportedOperationException("Invalid animation type");
            }
            }
        });

        PARCOOL_ACTION_START_MAPPING.put(ClimbUp.class, (playerpatch, startEvent) -> {
            playerpatch.getAnimator().getVariables().getSharedVariable(ParCoolAnimations.CLIFF_Y_ROT).ifPresent((yRot) -> {
                playerpatch.setModelYRot(yRot, true);
            });

            return ParCoolAnimations.BIPED_CLIMB_UP_NO_ACTION;
        });

        PARCOOL_ACTION_START_MAPPING.put(ChargeJump.class, (playerpatch, startEvent) -> {
            return ParCoolAnimations.BIPED_CAT_LEAP;
        });

        PARCOOL_ACTION_START_MAPPING.put(Dodge.class, (playerpatch, startEvent) -> {
            LocalPlayer clientPlayer = (LocalPlayer)startEvent.getPlayer();
            AnimationAccessor<? extends StaticAnimation> rollAnimation = ParCoolAnimations.BIPED_ROLL_FORWARD;

            if (clientPlayer.input.leftImpulse < -0.5F) {
                rollAnimation = ParCoolAnimations.BIPED_ROLL_RIGHT;
            } else if (clientPlayer.input.leftImpulse > 0.5F) {
                rollAnimation = ParCoolAnimations.BIPED_ROLL_LEFT;
            } else if (clientPlayer.input.forwardImpulse < -0.5F) {
                rollAnimation = ParCoolAnimations.BIPED_ROLL_BACKWARD;
            }

            playerpatch.setStamina(playerpatch.getStamina() - EpicFightSkills.ROLL.get().getConsumption());

            return rollAnimation;
        });

        PARCOOL_ACTION_START_MAPPING.put(WallRun.class, (playerpatch, startEvent) -> {
            return ParCoolAnimations.BIPED_WALL_RUN_VERTICAL;
        });

        PARCOOL_ACTION_CANCEL_EVENTS.put(ClimbUp.class, (playerpatch, action) -> {
            if (playerpatch.getEntityState().inaction()) {
                return true;
            }

            return false;
        });

        PARCOOL_ACTION_CANCEL_EVENTS.put(Vault.class, (playerpatch, action) -> {
            if (playerpatch.getAnimator().getPlayerFor(null).getAnimation().get().getRealAnimation() == ParCoolAnimations.BIPED_CLIMB_UP_NO_ACTION) {
                return true;
            }

            return false;
        });

        PARCOOL_ACTION_CANCEL_EVENTS.put(WallJump.class, (playerpatch, action) -> {
            AssetAccessor<? extends StaticAnimation> currentPlay = playerpatch.getAnimator().getPlayerFor(null).getAnimation().get().getRealAnimation();

            if (
                currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF ||
                currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF_INNER_CORNER ||
                currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF_OUTER_CORNER ||
                currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF_LOOK_LEFT ||
                currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF_LOOK_RIGHT
            ) {
                Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
                WallJump wallJump = (WallJump)action;

                if (parkourability.get(ParCoolActions.HANG_ON).isDoing() && wallJump.canStart()) {
                    Vec3 jumpDirection = ((ParCoolMixinWallJumpAccessor)wallJump).getJumpVec().get();
                    Vec3 wallDirection = parkourability.getAdditionalProperties().getDefaultWallInteraction().asVec();
                    int animType = getWallJumpAnimType(playerpatch.getOriginal(), wallDirection);

                    switch (animType) {
                    case 0 -> {
                        //entitypatch.playAnimationSynchronized(ParCoolAnimations.BIPED_JUMP_FROM_BAR_START, 0.0f);
                    }
                    case 1 -> {
                        playerpatch.getAnimator().getVariables().getSharedVariable(ParCoolAnimations.CLIFF_Y_ROT).ifPresent((yRot) -> {
                            playerpatch.setModelYRot(yRot - 90.0F, true);
                        });

                        playerpatch.getAnimator().getVariables().put(ParCoolAnimations.JUMP_DIRECTION, ParCoolAnimations.BIPED_WALL_JUMP_LEFT_START, jumpDirection);
                        playerpatch.getAnimator().getVariables().put(ParCoolAnimations.WALL_DIRECTION, ParCoolAnimations.BIPED_WALL_JUMP_LEFT_START, wallDirection);
                        playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_WALL_JUMP_LEFT_START, 0.0F);
                    }
                    case 2 -> {
                        playerpatch.getAnimator().getVariables().getSharedVariable(ParCoolAnimations.CLIFF_Y_ROT).ifPresent((yRot) -> {
                            playerpatch.setModelYRot(yRot + 90.0F, true);
                        });

                        playerpatch.getAnimator().getVariables().put(ParCoolAnimations.JUMP_DIRECTION, ParCoolAnimations.BIPED_WALL_JUMP_RIGHT_START, jumpDirection);
                        playerpatch.getAnimator().getVariables().put(ParCoolAnimations.WALL_DIRECTION, ParCoolAnimations.BIPED_WALL_JUMP_RIGHT_START, wallDirection);
                        playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_WALL_JUMP_RIGHT_START, 0.0F);
                    }
                    default -> {
                        throw new UnsupportedOperationException("No matching wall jump animation type " + animType);
                    }
                    }
                }

                return true;
            } else {
                Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());

                if (((WallJump)action).canStart()) {
                    int animType = getWallJumpAnimType(playerpatch.getOriginal(), parkourability.getAdditionalProperties().getDefaultWallInteraction().asVec());

                    switch (animType) {
                    case 0 -> {
                    }
                    case 1 -> {
                        playerpatch.playAnimationInClientSide(ParCoolAnimations.BIPED_WALL_JUMP_LEFT, 0.0F);
                    }
                    case 2 -> {
                        playerpatch.playAnimationInClientSide(ParCoolAnimations.BIPED_WALL_JUMP_RIGHT, 0.0F);
                    }
                    }
                }

                return false;
            }
        });

        PARCOOL_ACTION_CANCEL_EVENTS.put(HangOn.class, (playerpatch, action) -> {
            if (playerpatch.getAnimator().getPlayerFor(null).getAnimation().get().getRealAnimation() == ParCoolAnimations.BIPED_CLIMB_UP_NO_ACTION) {
                return true;
            }

            if (((HangOn)action).canStart()) {
                if (!ParCoolUtils.scanTerrainAndStartClingAction(playerpatch, ParCoolUtils.WallMoveType.CLING_START)) {
                    return true;
                }
            }

            return false;
        });

        PARCOOL_ACTION_CANCEL_EVENTS.put(Dodge.class, (playerpatch, action) -> {
            if (EpicFightKeyMappings.DODGE.getKey() == ParCoolKeyBinds.DODGE.key().getKey()) {
                if (playerpatch.getSkill(SkillSlots.DODGE).getSkill() != null) {
                    return true;
                }
            }

            if (!playerpatch.getOriginal().isCreative() && !playerpatch.hasStamina(EpicFightSkills.ROLL.get().getConsumption())) {
                return true;
            }

            return false;
        });

        PARCOOL_ACTION_CANCEL_EVENTS.put(TrickJump.class, (playerpatch, action) -> {
            if (playerpatch.getEntityState().movementLocked()) {
                return true;
            }

            return false;
        });
    }

    public static void onInitAnimatorEvent(InitAnimatorEvent event) {
        if (event.getEntityPatch() instanceof PlayerPatch<?>) {
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CAT_LEAP_PREPARATION, ParCoolAnimations.BIPED_CAT_LEAP_PREPARATION);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF, ParCoolAnimations.BIPED_CLING_TO_CLIFF);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF_INNER_CORNER, ParCoolAnimations.BIPED_CLING_TO_CLIFF_INNER_CORNER);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF_OUTER_CORNER, ParCoolAnimations.BIPED_CLING_TO_CLIFF_OUTER_CORNER);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF_LEFT, ParCoolAnimations.BIPED_CLING_TO_CLIFF_LOOK_LEFT);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF_RIGHT, ParCoolAnimations.BIPED_CLING_TO_CLIFF_LOOK_RIGHT);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.DIVE, ParCoolAnimations.BIPED_DIVE);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.SKY_DIVE, ParCoolAnimations.BIPED_SKY_DIVE);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.WALL_SLIDING_LEFT, ParCoolAnimations.BIPED_WALL_SLIDE_LEFT);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.WALL_SLIDING_RIGHT, ParCoolAnimations.BIPED_WALL_SLIDE_RIGHT);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.WALL_RUNNING_LEFT, ParCoolAnimations.BIPED_WALL_RUN_LEFT);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.WALL_RUNNING_RIGHT, ParCoolAnimations.BIPED_WALL_RUN_RIGHT);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.FAST_RUN, ParCoolAnimations.BIPED_FAST_RUN);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.HANG_DOWN_ORTHOGONAL, ParCoolAnimations.BIPED_HANG_DOWN_ORTHOGONAL);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.HANG_DOWN, ParCoolAnimations.BIPED_HANG_DOWN);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.SLIDING, ParCoolAnimations.BIPED_SLIDE);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.HIDE_IN_BLOCK_HORIZONTAL, ParCoolAnimations.BIPED_HIDE_IN_BLOCK_HORIZONTAL);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.RIDE_ZIPLINE_FORWARD, ParCoolAnimations.BIPED_RIDE_ZIPLINE_FORWARD);
            event.getAnimator().addLivingAnimation(ParcoolLivingMotions.RIDE_ZIPLINE_SIDE, ParCoolAnimations.BIPED_RIDE_ZIPLINE_SIDE);

            //event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CRAWL, ParCoolAnimations.BIPED_CRAWL);
        }
    }

    @SubscribeEvent
    public static void onParCoolActionEvent$TryToStartEvent(ParCoolActionEvent.TryToStart event) {
        if (PARCOOL_ACTION_CANCEL_EVENTS.containsKey(event.getAction().getClass())) {
            PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(event.getPlayer(), PlayerPatch.class);

            if (playerpatch != null && playerpatch.isEpicFightMode() && PARCOOL_ACTION_CANCEL_EVENTS.get(event.getAction().getClass()).apply(playerpatch, event.getAction())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onParCoolActionEvent$StartEvent(ParCoolActionEvent.Start.Post event) {
        PlayerPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getPlayer(), PlayerPatch.class);

        if (entitypatch != null && entitypatch.isLogicalClient() && entitypatch.getOriginal().isLocalPlayer() && entitypatch.isEpicFightMode() && PARCOOL_ACTION_START_MAPPING.containsKey(event.getAction().getClass())) {
            AssetAccessor<? extends StaticAnimation> animation = PARCOOL_ACTION_START_MAPPING.get(event.getAction().getClass()).apply(entitypatch, event);

            if (animation != null) {
                entitypatch.playAnimationSynchronized(animation, 0.0F);
            }
        }

        if (event.getAction().getClass() == HangOn.class) {
            event.getPlayer().setForcedPose(net.minecraft.world.entity.Pose.STANDING);
        }
    }

    @SubscribeEvent
    public static void onParCoolActionEvent$TryToContinueEvent(ParCoolActionEvent.TryToContinue event) {
        if (event.getAction() instanceof HangDown hangDown && event.getPlayer().isLocalPlayer() && ParCoolKeyBinds.JUMP.state().isJustPressed()) {
            PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(event.getPlayer(), PlayerPatch.class);

            if (playerpatch != null && playerpatch.isEpicFightMode()) {
                if (ParCoolUtils.isOrthogonalToBar(hangDown)) {
                    playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_JUMP_FROM_BAR_START_ORTHOGONAL, 0.0F);
                } else {
                    playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_JUMP_FROM_BAR_START, 0.0F);
                }
            }
        }
    }

    private static int getWallJumpAnimType(Player player, Vec3 wallDirection) {
        return EntityUtil.getHorizontalLookAngle(player).cross(wallDirection).y < 0.0D ? 1 : 2;
    }

    @SubscribeEvent
    public static void onParCoolActionEvent$FinishEvent(ParCoolActionEvent.Finish.Post event) {
        if (event.getAction().getClass() == HangOn.class) {
            event.getPlayer().setForcedPose(null);
        }
    }
}
