package com.yesman.epicparcool.animations;

import java.util.Optional;

import net.minecraft.world.entity.EntityDimensions;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.alrex.parcool.ParCool;
import com.alrex.parcool.api.ParCoolSoundEvents;
import com.alrex.parcool.client.input.ParCoolKeyBinds;
import com.alrex.parcool.common.Parkourability;
import com.alrex.parcool.common.action.ParCoolActions;
import com.alrex.parcool.util.VectorUtil;
import com.mojang.datafixers.util.Pair;
import com.yesman.epicparcool.EpicParCool;
import com.yesman.epicparcool.ParCoolUtils;
import com.yesman.epicparcool.ParCoolUtils.ClingType;
import com.yesman.epicparcool.mixin.EpicFightMixinLayer;
import com.yesman.epicparcool.mixin.ParCoolMixinRideZiplineAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.AnimationVariables.IndependentVariableKey;
import yesman.epicfight.api.animation.AnimationVariables.SharedVariableKey;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.Side;
import yesman.epicfight.api.animation.property.AnimationEvent.SimpleEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@EventBusSubscriber(modid = EpicParCool.MODID)
public class ParCoolAnimations {
    public static AnimationAccessor<StaticAnimation> BIPED_CLING_TO_CLIFF;
    public static AnimationAccessor<StaticAnimation> BIPED_CLING_TO_CLIFF_INNER_CORNER;
    public static AnimationAccessor<StaticAnimation> BIPED_CLING_TO_CLIFF_OUTER_CORNER;
    public static AnimationAccessor<StaticAnimation> BIPED_CLING_TO_CLIFF_LOOK_LEFT;
    public static AnimationAccessor<StaticAnimation> BIPED_CLING_TO_CLIFF_LOOK_RIGHT;
    public static AnimationAccessor<ActionAnimation> BIPED_WALL_JUMP_LEFT_START;
    public static AnimationAccessor<StaticAnimation> BIPED_WALL_JUMP_LEFT;
    public static AnimationAccessor<ActionAnimation> BIPED_WALL_JUMP_RIGHT_START;
    public static AnimationAccessor<StaticAnimation> BIPED_WALL_JUMP_RIGHT;
    public static AnimationAccessor<StaticAnimation> BIPED_DIVE;
    public static AnimationAccessor<StaticAnimation> BIPED_SKY_DIVE;
    public static AnimationAccessor<StaticAnimation> BIPED_WALL_SLIDE_LEFT;
    public static AnimationAccessor<StaticAnimation> BIPED_WALL_SLIDE_RIGHT;
    public static AnimationAccessor<StaticAnimation> BIPED_WALL_RUN_LEFT;
    public static AnimationAccessor<StaticAnimation> BIPED_WALL_RUN_RIGHT;
    public static AnimationAccessor<StaticAnimation> BIPED_WALL_RUN_VERTICAL;
    public static AnimationAccessor<StaticAnimation> BIPED_FAST_RUN;
    public static AnimationAccessor<StaticAnimation> BIPED_CAT_LEAP;
    public static AnimationAccessor<StaticAnimation> BIPED_CAT_LEAP_PREPARATION;
    public static AnimationAccessor<StaticAnimation> BIPED_HANG_DOWN;
    public static AnimationAccessor<StaticAnimation> BIPED_HANG_DOWN_ORTHOGONAL;
    public static AnimationAccessor<StaticAnimation> BIPED_JUMP_FROM_BAR;
    public static AnimationAccessor<StaticAnimation> BIPED_SLIDE;
    public static AnimationAccessor<StaticAnimation> BIPED_CLIMB_UP_NO_ACTION;
    public static AnimationAccessor<StaticAnimation> BIPED_HIDE_IN_BLOCK_HORIZONTAL;
    public static AnimationAccessor<StaticAnimation> BIPED_RIDE_ZIPLINE_FORWARD;
    public static AnimationAccessor<StaticAnimation> BIPED_RIDE_ZIPLINE_SIDE;
    public static AnimationAccessor<StaticAnimation> BIPED_ROLL_FORWARD;
    public static AnimationAccessor<StaticAnimation> BIPED_ROLL_BACKWARD;
    public static AnimationAccessor<StaticAnimation> BIPED_ROLL_LEFT;
    public static AnimationAccessor<StaticAnimation> BIPED_ROLL_RIGHT;

    public static AnimationAccessor<ActionAnimation> BIPED_FLIP_FOWARD;
    public static AnimationAccessor<ActionAnimation> BIPED_FLIP_BACKWARD;
    public static AnimationAccessor<ActionAnimation> BIPED_CLIMB_UP;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_START;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_START_INNER_CORNER;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_START_OUTER_CORNER;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_LEFT;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_RIGHT;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_LEFT_INNER_CORNER1;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_LEFT_INNER_CORNER2;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_RIGHT_INNER_CORNER1;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_RIGHT_INNER_CORNER2;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_LEFT_OUTER_CORNER1;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_LEFT_OUTER_CORNER2;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_RIGHT_OUTER_CORNER1;
    public static AnimationAccessor<ActionAnimation> BIPED_CLING_MOVE_RIGHT_OUTER_CORNER2;
    public static AnimationAccessor<ActionAnimation> BIPED_VAULT_FORWARD;
    public static AnimationAccessor<ActionAnimation> BIPED_VAULT_LEFT;
    public static AnimationAccessor<ActionAnimation> BIPED_VAULT_RIGHT;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_INERTIA_ORTHOGONAL;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_INERTIA;
    public static AnimationAccessor<ActionAnimation> BIPED_JUMP_FROM_BAR_START_ORTHOGONAL;
    public static AnimationAccessor<ActionAnimation> BIPED_JUMP_FROM_BAR_START;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_MOVE_FORWARD_START;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_MOVE_FORWARD_CROSS1;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_MOVE_FORWARD_CROSS2;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_MOVE_FORWARD_END1;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_MOVE_FORWARD_END2;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_MOVE_BACKWARD;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_MOVE_LEFT;
    public static AnimationAccessor<ActionAnimation> BIPED_HANG_DOWN_MOVE_RIGHT;

    public static AnimationAccessor<MovementAnimation> BIPED_CRAWL;

    public static final SharedVariableKey<ClingType> CLING_TYPE = AnimationVariables.unsynchShared((animator) -> ClingType.STRAIGHT, true);
    public static final IndependentVariableKey<Vec3> JUMP_DIRECTION = AnimationVariables.unsyncIndependent((animator) -> new Vec3(0.0D, 0.0D, 0.0D), true);
    public static final IndependentVariableKey<Vec3> WALL_DIRECTION = AnimationVariables.unsyncIndependent((animator) -> new Vec3(0.0D, 0.0D, 0.0D), true);

    public static final SharedVariableKey<Boolean> ON_EDGE = AnimationVariables.unsynchShared((animator) -> false, true);
    public static final SharedVariableKey<Float> CLIFF_Y_ROT = AnimationVariables.unsynchShared((animator) -> 0.0F, true);
    public static final SharedVariableKey<Vec3> CORNER_CLING_DESTINATION = AnimationVariables.unsynchShared((animator) -> animator.getEntityPatch().getOriginal().position(), true);
    public static final IndependentVariableKey<Float> CLIFF_START_Y_ROT = AnimationVariables.unsyncIndependent((animator) -> 0.0F, true);
    public static final IndependentVariableKey<Float> CLIFF_DEST_Y_ROT = AnimationVariables.unsyncIndependent((animator) -> 0.0F, true);
    public static final IndependentVariableKey<Vec3> CLING_DESTINATION = AnimationVariables.unsyncIndependent((animator) -> animator.getEntityPatch().getOriginal().position(), true);

    @SubscribeEvent
    public static void registerAnimations(AnimationRegistryEvent event) {
        event.newBuilder(EpicParCool.MODID, ParCoolAnimations::build);
    }

    public static void build(AnimationBuilder builder) {
        BIPED_CLING_TO_CLIFF = builder.nextAccessor("biped/cling_to_cliff", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .addEvents(SimpleEvent.create((entitypatch, animation, params) -> {
                    if (entitypatch instanceof PlayerPatch<?> playerpatch && playerpatch.isEpicFightMode()) {
                        entitypatch.setYRot(entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT));
                    } else {
                        entitypatch.setYRot(entitypatch.getOriginal().yBodyRot);
                    }
                }, Side.LOCAL_CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_CLING_TO_CLIFF_INNER_CORNER = builder.nextAccessor("biped/cling_to_cliff_inner_corner", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .addEvents(SimpleEvent.create((entitypatch, animation, params) -> {
                    entitypatch.setYRot(entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT));
                }, Side.LOCAL_CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_CLING_TO_CLIFF_OUTER_CORNER = builder.nextAccessor("biped/cling_to_cliff_outer_corner", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .addEvents(SimpleEvent.create((entitypatch, animation, params) -> {
                    entitypatch.setYRot(entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT));
                }, Side.LOCAL_CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_CLING_TO_CLIFF_LOOK_LEFT = builder.nextAccessor("biped/cling_to_cliff_left", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addProperty(StaticAnimationProperty.POSE_MODIFIER, (DynamicAnimation self, Pose pose, LivingEntityPatch<?> entitypatch, float elapsedTime, float partialTicks) -> {
                    if (pose.hasTransform("Head") && entitypatch.getArmature().hasJoint("Head") && entitypatch instanceof AbstractClientPlayerPatch playerpatch) {
                        float headRotO = playerpatch.getYRotO() - 90.0F - entitypatch.getOriginal().yHeadRotO;
                        float headRot = playerpatch.getYRot() - 90.0F - entitypatch.getOriginal().yHeadRot;

                        float partialHeadRot = Mth.wrapDegrees(MathUtils.lerpBetween(headRotO, headRot, partialTicks));
                        float zRot = entitypatch.getOriginal().getXRot();
                        partialHeadRot = Mth.clamp(partialHeadRot, -90.0F, 90.0F);

                        OpenMatrix4f toOriginalRotation = entitypatch.getArmature().getBoundTransformFor(pose, entitypatch.getArmature().searchJointByName("Head")).removeScale().removeTranslation().invert();
                        Vec3f zAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Z_AXIS, null);
                        Vec3f yAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Y_AXIS, null);

                        OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(partialHeadRot, yAxis).rotateDeg(zRot, zAxis);
                        pose.orElseEmpty("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
                    }
                })
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .addEvents(SimpleEvent.create((entitypatch, animation, params) -> {
                    entitypatch.setYRot(entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT));
                }, Side.LOCAL_CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_CLING_TO_CLIFF_LOOK_RIGHT = builder.nextAccessor("biped/cling_to_cliff_right", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
                .addProperty(StaticAnimationProperty.POSE_MODIFIER, (DynamicAnimation self, Pose pose, LivingEntityPatch<?> entitypatch, float elapsedTime, float partialTicks) -> {
                    if (pose.hasTransform("Head") && entitypatch.getArmature().hasJoint("Head") && entitypatch instanceof AbstractClientPlayerPatch playerpatch) {
                        float headRotO = playerpatch.getYRotO() + 90.0F - entitypatch.getOriginal().yHeadRotO;
                        float headRot = playerpatch.getYRot() + 90.0F - entitypatch.getOriginal().yHeadRot;
                        float partialHeadRot = Mth.wrapDegrees(MathUtils.lerpBetween(headRotO, headRot, partialTicks));
                        float zRot = -entitypatch.getOriginal().getXRot();
                        partialHeadRot = Mth.clamp(partialHeadRot, -90.0F, 90.0F);

                        OpenMatrix4f toOriginalRotation = entitypatch.getArmature().getBoundTransformFor(pose, entitypatch.getArmature().searchJointByName("Head")).removeScale().removeTranslation().invert();
                        Vec3f zAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Z_AXIS, null);
                        Vec3f yAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Y_AXIS, null);

                        OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(partialHeadRot, yAxis).rotateDeg(zRot, zAxis);
                        pose.orElseEmpty("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
                    }
                })
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .addEvents(SimpleEvent.create((entitypatch, animation, params) -> {
                    entitypatch.setYRot(entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT));
                }, Side.LOCAL_CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_WALL_JUMP_LEFT_START = builder.nextAccessor("biped/wall_jump_left_start", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.WALL_JUMP, Side.CLIENT)
                )
        );

        BIPED_WALL_JUMP_LEFT = builder.nextAccessor("biped/wall_jump_left", (accessor) ->
            new StaticAnimation(0.15F, false, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
                    .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
        );

        BIPED_WALL_JUMP_RIGHT_START = builder.nextAccessor("biped/wall_jump_right_start", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.WALL_JUMP, Side.CLIENT)
                )
        );

        BIPED_WALL_JUMP_RIGHT = builder.nextAccessor("biped/wall_jump_right", (accessor) ->
            new StaticAnimation(0.15F, false, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
                    .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
        );

        BIPED_DIVE = builder.nextAccessor("biped/dive", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED).addProperty(StaticAnimationProperty.POSE_MODIFIER,
                new AnimationProperty.PoseModifier() {
                    public static final Vector3f RANDOM_AXIS = new Vector3f();

                    @Override
                    public void modify(DynamicAnimation self, Pose pose, LivingEntityPatch<?> entitypatch, float elapsedTime, float partialTicks) {
                        float modifier = Math.min(elapsedTime, 1.0F) * 0.01F;
                        RandomSource random = entitypatch.getOriginal().getRandom();

                        JointTransform chestJt = pose.getJointTransformData().get("Root");
                        RANDOM_AXIS.set(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
                        chestJt.rotation().rotateAxis((float)random.nextGaussian() * modifier, RANDOM_AXIS);

                        JointTransform thighL = pose.getJointTransformData().get("Thigh_L");
                        RANDOM_AXIS.set(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
                        thighL.rotation().rotateAxis((float)random.nextGaussian() * modifier, RANDOM_AXIS);

                        JointTransform thighR = pose.getJointTransformData().get("Thigh_R");
                        RANDOM_AXIS.set(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
                        thighR.rotation().rotateAxis((float)random.nextGaussian() * modifier, RANDOM_AXIS);
                    }
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_SKY_DIVE = builder.nextAccessor("biped/sky_dive", (accessor) ->
            new StaticAnimation(0.35F, true, accessor, Armatures.BIPED).addProperty(StaticAnimationProperty.POSE_MODIFIER,
                new AnimationProperty.PoseModifier() {
                    public static final Vector3f RANDOM_AXIS = new Vector3f();

                    @Override
                    public void modify(DynamicAnimation self, Pose pose, LivingEntityPatch<?> entitypatch, float elapsedTime, float partialTicks) {
                        float modifier = Math.min(elapsedTime, 1.0F) * 0.03F;
                        RandomSource random = entitypatch.getOriginal().getRandom();

                        JointTransform chestJt = pose.getJointTransformData().get("Root");
                        RANDOM_AXIS.set(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
                        chestJt.rotation().rotateAxis((float)random.nextGaussian() * modifier, RANDOM_AXIS);

                        JointTransform thighL = pose.getJointTransformData().get("Thigh_L");
                        RANDOM_AXIS.set(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
                        thighL.rotation().rotateAxis((float)random.nextGaussian() * modifier, RANDOM_AXIS);

                        JointTransform thighR = pose.getJointTransformData().get("Thigh_R");
                        RANDOM_AXIS.set(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
                        thighR.rotation().rotateAxis((float)random.nextGaussian() * modifier, RANDOM_AXIS);

                        JointTransform armL = pose.getJointTransformData().get("Arm_L");
                        RANDOM_AXIS.set(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
                        armL.rotation().rotateAxis((float)random.nextGaussian() * modifier, RANDOM_AXIS);

                        JointTransform armR = pose.getJointTransformData().get("Arm_R");
                        RANDOM_AXIS.set(random.nextFloat(), random.nextFloat(), random.nextFloat()).normalize();
                        armR.rotation().rotateAxis((float)random.nextGaussian() * modifier, RANDOM_AXIS);
                    }
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_WALL_SLIDE_LEFT = builder.nextAccessor("biped/wall_slide_left", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_WALL_SLIDE_RIGHT = builder.nextAccessor("biped/wall_slide_right", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_WALL_RUN_LEFT = builder.nextAccessor("biped/wall_run_left", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 0.5F)
                    .addState(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_WALL_RUN_RIGHT = builder.nextAccessor("biped/wall_run_right", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 0.5F)
                    .addState(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_WALL_RUN_VERTICAL = builder.nextAccessor("biped/wall_run_vertical", (accessor) ->
            new StaticAnimation(false, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addState(EntityState.UPDATE_LIVING_MOTION, false)
                    .addState(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_FAST_RUN = builder.nextAccessor("biped/fast_run", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 100.0F)
                    .addStateRemoveOld(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_CAT_LEAP = builder.nextAccessor("biped/cat_leap", (accessor) ->
            new StaticAnimation(0.05F, false, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_CAT_LEAP_PREPARATION = builder.nextAccessor("biped/cat_leap_preparation", (accessor) ->
            new StaticAnimation(0.15F, true, accessor, Armatures.BIPED)
                .addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, (DynamicAnimation self, LivingEntityPatch<?> entitypatch, float speed, float prevElapsedTime, float elapsedTime) -> {
                    if (self.isLinkAnimation()) {
                        return 1.0F;
                    }

                    return MathUtils.bezierCurve(1.0F - elapsedTime / self.getTotalTime());
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10.0F)
                    .addStateRemoveOld(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_HANG_DOWN = builder.nextAccessor("biped/hang_down", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
        );

        BIPED_HANG_DOWN_ORTHOGONAL = builder.nextAccessor("biped/hang_down_orthogonal", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
        );

        BIPED_JUMP_FROM_BAR = builder.nextAccessor("biped/jump_from_bar", (accessor) ->
            new StaticAnimation(false, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .addEvents(SimpleEvent.create((entitypatch, animation, param) -> {
                    ParCoolKeyBinds.HANG.key().setDown(false);
                }, Side.LOCAL_CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_SLIDE = builder.nextAccessor("biped/slide", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true)
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.CAN_USE_ITEM, false)
        );

        BIPED_CLIMB_UP_NO_ACTION = builder.nextAccessor("biped/climb_up_no_action", (accessor) ->
            new StaticAnimation(false, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_HIDE_IN_BLOCK_HORIZONTAL = builder.nextAccessor("biped/hide_horizontal", (accessor) ->
            new StaticAnimation(true, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create((entitypatch, animation, params) -> {
                        if (entitypatch instanceof PlayerPatch<?> playerpatch) {
                            Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
                            Vec3 lookVec = parkourability.get(ParCoolActions.HIDE_IN_BLOCK).getFacingVec();

                            if (lookVec != null) {
                                float yRot = (float) VectorUtil.toYawDegree(lookVec);
                                entitypatch.setYRot(yRot);
                            }
                        }
                    }, Side.CLIENT)
                )
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_RIDE_ZIPLINE_FORWARD = builder.nextAccessor("biped/ride_zipline_forward", (accessor) ->
            new StaticAnimation(false, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create((entitypatch, animation, params) -> {
                        ((EpicFightMixinLayer)entitypatch.getClientAnimator().baseLayer).getLinkAnimation().setNextStartTime(0.5F);
                    }, Side.CLIENT)
                )
                .addProperty(StaticAnimationProperty.ELAPSED_TIME_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
                    float modular = 0.5F;

                    if (entitypatch instanceof PlayerPatch<?> playerpatch) {
                        Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
                        ParCoolMixinRideZiplineAccessor action = (ParCoolMixinRideZiplineAccessor)parkourability.get(ParCoolActions.RIDE_ZIPLINE);
                        double d1 = Math.abs(action.getSpeed());
                        double d2 = -1.0D / (d1 + 1.0D) + 1.0D;
                        Vec3 offset = parkourability.get(ParCoolActions.RIDE_ZIPLINE).getZiplineOffset().normalize();
                        Vec3 lookVec = VectorUtil.fromYawDegree(playerpatch.getYRot());
                        double dot = offset.dot(lookVec);

                        modular += MathUtils.getSign(dot) * d2 * 0.5D;
                    }

                    return Pair.of(prevElapsedTime, modular);
                })
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_RIDE_ZIPLINE_SIDE = builder.nextAccessor("biped/ride_zipline_side", (accessor) ->
            new StaticAnimation(false, accessor, Armatures.BIPED)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create((entitypatch, animation, params) -> {
                        ((EpicFightMixinLayer)entitypatch.getClientAnimator().baseLayer).getLinkAnimation().setNextStartTime(0.5F);
                    }, Side.CLIENT)
                )
                .addProperty(StaticAnimationProperty.ELAPSED_TIME_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
                    float modular = 0.5F;

                    if (entitypatch instanceof PlayerPatch<?> playerpatch) {
                        Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
                        ParCoolMixinRideZiplineAccessor action = (ParCoolMixinRideZiplineAccessor)parkourability.get(ParCoolActions.RIDE_ZIPLINE);
                        double d1 = Math.abs(action.getSpeed());
                        double d2 = -1.0D / (d1 + 1.0D) + 1.0D;
                        double yawToEndPoint = VectorUtil.toYawDegree(parkourability.get(ParCoolActions.RIDE_ZIPLINE).getZiplineOffset());
                        double playerYaw = Mth.wrapDegrees(playerpatch.getYRot() - yawToEndPoint);

                        modular += ((playerYaw > 0.0D) ? -1.0D : 1.0D) * d2 * 0.5D;
                    }

                    return Pair.of(prevElapsedTime, modular);
                })
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.TURNING_LOCKED, true)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_CLIMB_UP = builder.nextAccessor("biped/climb_up", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_ROLL_FORWARD = builder.nextAccessor("biped/roll_forward", (accessor) ->
            new StaticAnimation(0.05F, false, accessor, Armatures.BIPED)
                .addProperty(StaticAnimationProperty.POSE_MODIFIER, (DynamicAnimation self, Pose pose, LivingEntityPatch<?> entitypatch, float elapsedTime, float partialTicks) -> {
                    Vec3f translation = pose.orElseEmpty("Root").translation();
                    translation.x = 0.0F;
                    translation.z = 0.0F;
                })
                .addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, (DynamicAnimation self, LivingEntityPatch<?> entitypatch, float speed, float prevElapsedTime, float elapsedTime) -> {
                    return 1.5F;
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
                .setResourceLocation(EpicFight.MODID, "biped/skill/roll_forward")
        );

        BIPED_ROLL_BACKWARD = builder.nextAccessor("biped/roll_backward", (accessor) ->
            new StaticAnimation(0.05F, false, accessor, Armatures.BIPED)
                .addProperty(StaticAnimationProperty.POSE_MODIFIER, (DynamicAnimation self, Pose pose, LivingEntityPatch<?> entitypatch, float elapsedTime, float partialTicks) -> {
                    Vec3f translation = pose.orElseEmpty("Root").translation();
                    translation.x = 0.0F;
                    translation.z = 0.0F;
                })
                .addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, (DynamicAnimation self, LivingEntityPatch<?> entitypatch, float speed, float prevElapsedTime, float elapsedTime) -> {
                    return 1.5F;
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
                .setResourceLocation(EpicFight.MODID, "biped/skill/roll_backward")
        );

        BIPED_ROLL_LEFT = builder.nextAccessor("biped/roll_left", (accessor) ->
            new StaticAnimation(0.05F, false, accessor, Armatures.BIPED)
                .addProperty(StaticAnimationProperty.POSE_MODIFIER, (DynamicAnimation self, Pose pose, LivingEntityPatch<?> entitypatch, float elapsedTime, float partialTicks) -> {
                    Vec3f translation = pose.orElseEmpty("Root").translation();
                    translation.x = 0.0F;
                    translation.z = 0.0F;
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_ROLL_RIGHT = builder.nextAccessor("biped/roll_right", (accessor) ->
            new StaticAnimation(0.05F, false, accessor, Armatures.BIPED)
                .addProperty(StaticAnimationProperty.POSE_MODIFIER, (DynamicAnimation self, Pose pose, LivingEntityPatch<?> entitypatch, float elapsedTime, float partialTicks) -> {
                    Vec3f translation = pose.orElseEmpty("Root").translation();
                    translation.x = 0.0F;
                    translation.z = 0.0F;
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addProperty(StaticAnimationProperty.ON_ITEM_CHANGE_EVENT, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK_WHEN_ITEM_CHANGED, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
                .newTimePair(0.0F, 10000.0F)
                    .addStateRemoveOld(EntityState.COMBO_ATTACKS_DOABLE, false)
                    .addStateRemoveOld(EntityState.SKILL_EXECUTABLE, false)
                    .addStateRemoveOld(EntityState.UPDATE_LIVING_MOTION, false)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_FLIP_FOWARD = builder.nextAccessor("biped/flip_forward", (accessor) ->
            new ActionAnimation(0.05F, 0.7F, accessor, Armatures.BIPED)
                .setResourceLocation(EpicFight.MODID, "biped/skill/phantom_ascent_forward")
                .addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false)
                .newTimePair(0.0F, 0.5F)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_FLIP_BACKWARD = builder.nextAccessor("biped/flip_backward", (accessor) ->
            new ActionAnimation(0.05F, 0.7F, accessor, Armatures.BIPED)
                .setResourceLocation(EpicFight.MODID, "biped/skill/phantom_ascent_backward")
                .addStateRemoveOld(EntityState.MOVEMENT_LOCKED, false)
                .newTimePair(0.0F, 0.5F)
                    .addStateRemoveOld(EntityState.INACTION, true)
        );

        BIPED_CRAWL = builder.nextAccessor("biped/crawl", (accessor) ->
            new MovementAnimation(true, accessor, Armatures.BIPED)
                .addProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER, (self, entitypatch, speed, prevElapsedTime, elapsedTime) -> {
                    return speed;
                })
        );

        BIPED_CLING_START = builder.nextAccessor("biped/cling_start", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLING_DESTINATION, BIPED_CLING_START);
                })
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_CLING_START_INNER_CORNER = builder.nextAccessor("biped/cling_start_inner_corner", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT)
                )
        );

        BIPED_CLING_START_OUTER_CORNER = builder.nextAccessor("biped/cling_start_outer_corner", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT)
                )
        );

        BIPED_CLING_MOVE_LEFT = builder.nextAccessor("biped/cling_move_left", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_TARGET_DISTANCE)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(ReusableSources.PLAY_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT),
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT)
                )
        );

        BIPED_CLING_MOVE_RIGHT = builder.nextAccessor("biped/cling_move_right", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_TARGET_DISTANCE)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(ReusableSources.PLAY_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT),
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT)
                )
        );

        BIPED_CLING_MOVE_LEFT_INNER_CORNER1 = builder.nextAccessor("biped/cling_move_left_inner_corner1", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER, ReusableSources.ANIMATION_YROT)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_DEST_Y_ROT, BIPED_CLING_MOVE_LEFT_INNER_CORNER1);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.PLAY_CONER_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT)
                )
        );

        BIPED_CLING_MOVE_LEFT_INNER_CORNER2 = builder.nextAccessor("biped/cling_move_left_inner_corner2", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER, ReusableSources.ANIMATION_YROT)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_DEST_Y_ROT, BIPED_CLING_MOVE_LEFT_INNER_CORNER2);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.PLAY_CONER_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT)
                )
        );

        BIPED_CLING_MOVE_RIGHT_INNER_CORNER1 = builder.nextAccessor("biped/cling_move_right_inner_corner1", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER, ReusableSources.ANIMATION_YROT)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_DEST_Y_ROT, BIPED_CLING_MOVE_RIGHT_INNER_CORNER1);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.PLAY_CONER_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT)
                )
        );

        BIPED_CLING_MOVE_RIGHT_INNER_CORNER2 = builder.nextAccessor("biped/cling_move_right_inner_corner2", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER, ReusableSources.ANIMATION_YROT)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_DEST_Y_ROT, BIPED_CLING_MOVE_RIGHT_INNER_CORNER2);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.PLAY_CONER_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT)
                )
        );

        BIPED_CLING_MOVE_LEFT_OUTER_CORNER1 = builder.nextAccessor("biped/cling_move_left_outer_corner1", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER, ReusableSources.ANIMATION_YROT)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_DEST_Y_ROT, BIPED_CLING_MOVE_LEFT_OUTER_CORNER1);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addProperty(StaticAnimationProperty.NO_PHYSICS, true)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, AnimationEvent.Side.CLIENT),
                    SimpleEvent.create(ReusableSources.PLAY_CONER_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.TICK_EVENTS, SimpleEvent.create(ReusableSources.SCALE_DOWN_ON_CORNER, AnimationEvent.Side.BOTH))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, AnimationEvent.Side.CLIENT),
                    SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
                )
        );

        BIPED_CLING_MOVE_LEFT_OUTER_CORNER2 = builder.nextAccessor("biped/cling_move_left_outer_corner2", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER, ReusableSources.ANIMATION_YROT)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_DEST_Y_ROT, BIPED_CLING_MOVE_LEFT_OUTER_CORNER2);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addProperty(StaticAnimationProperty.NO_PHYSICS, true)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.PLAY_CONER_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.TICK_EVENTS, SimpleEvent.create(ReusableSources.SCALE_DOWN_ON_CORNER, AnimationEvent.Side.BOTH))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT),
                    SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
                )
        );

        BIPED_CLING_MOVE_RIGHT_OUTER_CORNER1 = builder.nextAccessor("biped/cling_move_right_outer_corner1", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER, ReusableSources.ANIMATION_YROT)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_DEST_Y_ROT, BIPED_CLING_MOVE_RIGHT_OUTER_CORNER1);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addProperty(StaticAnimationProperty.NO_PHYSICS, true)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.PLAY_CONER_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.TICK_EVENTS, SimpleEvent.create(ReusableSources.SCALE_DOWN_ON_CORNER, AnimationEvent.Side.BOTH))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT),
                    SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
                )
        );

        BIPED_CLING_MOVE_RIGHT_OUTER_CORNER2 = builder.nextAccessor("biped/cling_move_right_outer_corner2", (accessor) ->
            new ActionAnimation(0.1F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 1)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addProperty(ActionAnimationProperty.FIXED_HEAD_ROTATION, false)
                .addProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER, ReusableSources.ANIMATION_YROT)
                .addProperty(ActionAnimationProperty.DEST_COORD_YROT_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_DEST_Y_ROT, BIPED_CLING_MOVE_RIGHT_OUTER_CORNER2);
                })
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CORNER_CLING_DESTINATION);
                })
                .addProperty(StaticAnimationProperty.NO_PHYSICS, true)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT),
                    SimpleEvent.create(ReusableSources.PLAY_CONER_CLING_MOVE_SOUND, AnimationEvent.Side.CLIENT)
                )
                .addEvents(StaticAnimationProperty.TICK_EVENTS, SimpleEvent.create(ReusableSources.SCALE_DOWN_ON_CORNER, AnimationEvent.Side.BOTH))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT),
                    SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
                )
        );

        BIPED_VAULT_FORWARD = builder.nextAccessor("biped/vault_forward", (accessor) ->
            new ActionAnimation(0.1F, 0.3F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_VAULT_LEFT = builder.nextAccessor("biped/vault_left", (accessor) ->
            new ActionAnimation(0.1F, 0.3F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_VAULT_RIGHT = builder.nextAccessor("biped/vault_right", (accessor) ->
            new ActionAnimation(0.1F, 0.3F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.MOVE_VERTICAL, true)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_INERTIA = builder.nextAccessor("biped/hang_down_inertia", (accessor) ->
            new ActionAnimation(0.05F, 1.35F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 2)
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return ParCoolUtils.getHangableBars(entitypatch.getOriginal(), Vec3.ZERO);
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_INERTIA_ORTHOGONAL = builder.nextAccessor("biped/hang_down_inertia_orthogonal", (accessor) ->
            new ActionAnimation(0.05F, 1.35F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.TRACE_ORIGIN_AS_DESTINATION)
                .addProperty(ActionAnimationProperty.COORD_SET_TICK, null)
                .addProperty(ActionAnimationProperty.COORD_GET, MoveCoordFunctions.WORLD_COORD)
                .addProperty(ActionAnimationProperty.COORD_DEST_KEYFRAME_INDEX, 2)
                .addProperty(ActionAnimationProperty.DEST_LOCATION_PROVIDER, (self, entitypatch) -> {
                    return ParCoolUtils.getHangableBars(entitypatch.getOriginal(), Vec3.ZERO);
                })
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_JUMP_FROM_BAR_START_ORTHOGONAL = builder.nextAccessor("biped/jump_from_bar_start_orthogonal", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(SimpleEvent.create((entitypatch, animation, param) -> {
                    ParCoolKeyBinds.HANG.key().setDown(false);
                }, Side.LOCAL_CLIENT))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT),
                    SimpleEvent.create((entitypatch, animation, param) -> {
                        if (entitypatch instanceof AbstractClientPlayerPatch<?> playerpatch && playerpatch.getOriginal().isLocalPlayer()) {
                            entitypatch.getOriginal().setDeltaMovement(entitypatch.getOriginal().getDeltaMovement().add(entitypatch.getOriginal().getLookAngle().multiply(1, 0, 1).normalize().scale(entitypatch.getOriginal().getBbWidth() * 0.75)));
                        }

                        if (ParCool.getConfig().client().enableActionSounds.get()) {
                            entitypatch.getOriginal().playSound(ParCoolSoundEvents.LONG_JUMP.get(), 1f, 1f);
                        }

                        entitypatch.getAnimator().reserveAnimation(BIPED_JUMP_FROM_BAR);
                    }
                , Side.CLIENT))
        );

        BIPED_JUMP_FROM_BAR_START = builder.nextAccessor("biped/jump_from_bar_start", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(SimpleEvent.create((entitypatch, animation, param) -> {
                    ParCoolKeyBinds.HANG.key().setDown(false);
                }, Side.LOCAL_CLIENT))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS,
                    SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT),
                    SimpleEvent.create((entitypatch, animation, param) -> {
                        if (entitypatch.isLogicalClient()) {
                            if (entitypatch instanceof AbstractClientPlayerPatch<?> playerpatch && playerpatch.getOriginal().isLocalPlayer()) {
                                entitypatch.getOriginal().setDeltaMovement(entitypatch.getOriginal().getDeltaMovement().add(entitypatch.getOriginal().getLookAngle().multiply(1, 0, 1).normalize().scale(entitypatch.getOriginal().getBbWidth() * 0.75)));
                            }

                            if (ParCool.getConfig().client().enableActionSounds.get()) {
                                entitypatch.getOriginal().playSound(ParCoolSoundEvents.LONG_JUMP.get(), 1f, 1f);
                            }

                            entitypatch.getAnimator().reserveAnimation(BIPED_JUMP_FROM_BAR);
                        }
                    }
                , Side.CLIENT))
        );

        BIPED_HANG_DOWN_MOVE_FORWARD_START = builder.nextAccessor("biped/hang_down_move_start", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(ReusableSources.PLAY_HANG_MOVE_SOUND, AnimationEvent.Side.CLIENT),SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.<AnimationEvent.E1<Boolean>>create((entitypatch, animation, params) -> {
                    if (params.first() && entitypatch instanceof PlayerPatch<?> playerpatch && playerpatch.isEpicFightMode()) {
                        if (Minecraft.getInstance().options.keyUp.isDown() && ParCoolKeyBinds.HANG.key().isDown()) {
                            playerpatch.reserveAnimation(BIPED_HANG_DOWN_MOVE_FORWARD_CROSS1);
                        } else {
                            playerpatch.reserveAnimation(BIPED_HANG_DOWN_MOVE_FORWARD_END1);
                        }
                    }
                }, Side.LOCAL_CLIENT), SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_MOVE_FORWARD_CROSS1 = builder.nextAccessor("biped/hang_down_move_cross1", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(ReusableSources.PLAY_HANG_MOVE_SOUND, AnimationEvent.Side.CLIENT), SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.<AnimationEvent.E1<Boolean>>create((entitypatch, animation, params) -> {
                    if (params.first() && entitypatch instanceof PlayerPatch<?> playerpatch && playerpatch.isEpicFightMode()) {
                        Vec3 movement = BIPED_HANG_DOWN_MOVE_FORWARD_CROSS1.get().getExpectedMovement(entitypatch, BIPED_HANG_DOWN_MOVE_FORWARD_CROSS1.get().getTotalTime());
                        movement = movement.add(BIPED_HANG_DOWN_MOVE_FORWARD_END2.get().getExpectedMovement(entitypatch, BIPED_HANG_DOWN_MOVE_FORWARD_END2.get().getTotalTime()));
                        Vec3 hangDownDest = ParCoolUtils.getHangableBars(entitypatch.getOriginal(), movement);

                        if (Minecraft.getInstance().options.keyUp.isDown() && ParCoolKeyBinds.HANG.key().isDown() && hangDownDest != null) {
                            playerpatch.reserveAnimation(BIPED_HANG_DOWN_MOVE_FORWARD_CROSS2);
                        } else {
                            playerpatch.reserveAnimation(BIPED_HANG_DOWN_MOVE_FORWARD_END2);
                        }
                    }
                }, Side.LOCAL_CLIENT), SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_MOVE_FORWARD_CROSS2 = builder.nextAccessor("biped/hang_down_move_cross2", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(ReusableSources.PLAY_HANG_MOVE_SOUND, AnimationEvent.Side.CLIENT), SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.<AnimationEvent.E1<Boolean>>create((entitypatch, animation, params) -> {
                    if (params.first() && entitypatch instanceof PlayerPatch<?> playerpatch && playerpatch.isEpicFightMode()) {
                        Vec3 movement = BIPED_HANG_DOWN_MOVE_FORWARD_CROSS2.get().getExpectedMovement(entitypatch, BIPED_HANG_DOWN_MOVE_FORWARD_CROSS2.get().getTotalTime());
                        movement = movement.add(BIPED_HANG_DOWN_MOVE_FORWARD_END1.get().getExpectedMovement(entitypatch, BIPED_HANG_DOWN_MOVE_FORWARD_END1.get().getTotalTime()));
                        Vec3 hangDownDest = ParCoolUtils.getHangableBars(entitypatch.getOriginal(), movement);

                        if (Minecraft.getInstance().options.keyUp.isDown() && ParCoolKeyBinds.HANG.key().isDown() && hangDownDest != null) {
                            playerpatch.reserveAnimation(BIPED_HANG_DOWN_MOVE_FORWARD_CROSS1);
                        } else {
                            playerpatch.reserveAnimation(BIPED_HANG_DOWN_MOVE_FORWARD_END1);
                        }
                    }
                }, Side.LOCAL_CLIENT), SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_MOVE_FORWARD_END1 = builder.nextAccessor("biped/hang_down_move_end1", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(ReusableSources.PLAY_HANG_MOVE_SOUND, AnimationEvent.Side.CLIENT), SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_MOVE_FORWARD_END2 = builder.nextAccessor("biped/hang_down_move_end2", (accessor) ->
            new ActionAnimation(0.05F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(ReusableSources.PLAY_HANG_MOVE_SOUND, AnimationEvent.Side.CLIENT), SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_MOVE_BACKWARD = builder.nextAccessor("biped/hang_down_move_backward", (accessor) ->
            new ActionAnimation(0.15F, 0.6F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(ReusableSources.PLAY_HANG_MOVE_SOUND, AnimationEvent.Side.CLIENT), SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_MOVE_LEFT = builder.nextAccessor("biped/hang_down_move_left", (accessor) ->
            new ActionAnimation(0.15F, 0.45F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(ReusableSources.PLAY_HANG_MOVE_SOUND, AnimationEvent.Side.CLIENT), SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );

        BIPED_HANG_DOWN_MOVE_RIGHT = builder.nextAccessor("biped/hang_down_move_right", (accessor) ->
            new ActionAnimation(0.15F, 0.45F, accessor, Armatures.BIPED)
                .addProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT, true)
                .addProperty(ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(-1.0F, 10.0F))
                .addEvents(StaticAnimationProperty.ON_BEGIN_EVENTS, SimpleEvent.create(ReusableSources.PLAY_HANG_MOVE_SOUND, AnimationEvent.Side.CLIENT), SimpleEvent.create(Animations.ReusableSources.SET_TOOLS_BACK, Side.CLIENT))
                .addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.REVERT_TO_HANDS, Side.CLIENT))
        );
    }

    public static class ReusableSources {
        public static final AnimationEvent.E0 WALL_JUMP = (entitypatch, animation, param) -> {
            Optional<Vec3> jumpDirectionOpt = entitypatch.getAnimator().getVariables().get(JUMP_DIRECTION, animation);
            Optional<Vec3> wallDirectionOpt = entitypatch.getAnimator().getVariables().get(WALL_DIRECTION, animation);

            if (jumpDirectionOpt.isEmpty() || wallDirectionOpt.isEmpty()) {
                return;
            }

            Vec3 jumpDirection = jumpDirectionOpt.get();
            Vec3 wallDirection = wallDirectionOpt.get();
            Vec3 jumpMotion = jumpDirection.scale(0.59);
            Vec3 motion = entitypatch.getOriginal().getDeltaMovement();
            BlockPos leanedBlock = new BlockPos(
                (int) (entitypatch.getOriginal().getX() + wallDirection.x()),
                (int) (entitypatch.getOriginal().getBoundingBox().minY + entitypatch.getOriginal().getBbHeight() * 0.25),
                (int) (entitypatch.getOriginal().getZ() + wallDirection.z())
            );

            float slipperiness = entitypatch.getOriginal().getCommandSenderWorld().isLoaded(leanedBlock) ?
                    entitypatch.getOriginal().getCommandSenderWorld().getBlockState(leanedBlock).getFriction(entitypatch.getOriginal().getCommandSenderWorld(), leanedBlock, entitypatch.getOriginal())
                    : 0.6f;

            double ySpeed;

            if (slipperiness > 0.9) {// icy blocks
                ySpeed = motion.y();
            } else {
                ySpeed = motion.y() > jumpMotion.y() ? motion.y + jumpMotion.y() : jumpMotion.y();
                ParCoolUtils.spawnJumpParticles(entitypatch.getOriginal(), wallDirection, jumpDirection);
            }

            if (entitypatch instanceof LocalPlayerPatch localPlayerPatch) {
                Parkourability parkourability = Parkourability.get(localPlayerPatch.getOriginal());
                parkourability.get(ParCoolActions.HANG_ON).finish();
                entitypatch.getOriginal().setDeltaMovement(motion.x() + jumpMotion.x(), ySpeed, motion.z() + jumpMotion.z());
            }

            if (ParCool.getConfig().client().enableActionSounds.get()) {
                entitypatch.getOriginal().playSound(ParCoolSoundEvents.WALL_JUMP.get(), 1f, 1f);
            }

            if (animation == BIPED_WALL_JUMP_LEFT_START) {
                entitypatch.getAnimator().reserveAnimation(BIPED_WALL_JUMP_LEFT);
            } else if (animation == BIPED_WALL_JUMP_RIGHT_START) {
                entitypatch.getAnimator().reserveAnimation(BIPED_WALL_JUMP_RIGHT);
            }
        };

        public static final AnimationEvent.E0 PLAY_HANG_MOVE_SOUND = (entitypatch, animation, params) -> {
            BlockPos bp = entitypatch.getOriginal().blockPosition().above().above();
            BlockState state = entitypatch.getLevel().getBlockState(bp);
            SoundType soundType = state.getSoundType(entitypatch.getLevel(), bp, entitypatch.getOriginal());
            entitypatch.playSound(soundType.getPlaceSound(), 0, 0);
        };

        public static final AnimationEvent.E0 PLAY_CLING_MOVE_SOUND = (entitypatch, animation, params) -> {
            Vec3 destination = entitypatch.getOriginal().getEyePosition().add(MathUtils.getVectorForRotation(0.0F, entitypatch.getYRot()));
            BlockPos bp = new BlockPos((int)Math.floor(destination.x), (int)Math.floor(destination.y), (int)Math.floor(destination.z));
            BlockState state = entitypatch.getLevel().getBlockState(bp);
            SoundType soundType = state.getSoundType(entitypatch.getLevel(), bp, entitypatch.getOriginal());
            entitypatch.playSound(soundType.getHitSound(), 0, 0);
        };

        public static final AnimationEvent.E0 PLAY_CONER_CLING_MOVE_SOUND = (entitypatch, animation, params) -> {
            Vec3 destination = entitypatch.getOriginal().getEyePosition().add(MathUtils.getVectorForRotation(0.0F, entitypatch.getYRot()));
            BlockPos bp = new BlockPos((int)Math.floor(destination.x), (int)Math.floor(destination.y), (int)Math.floor(destination.z));
            BlockState state = entitypatch.getLevel().getBlockState(bp);
            SoundType soundType = state.getSoundType(entitypatch.getLevel(), bp, entitypatch.getOriginal());
            entitypatch.playSound(soundType.getHitSound(), 0, 0);
        };

        /// Scale down the player in corner wall movement
        public static final AnimationEvent.E0 SCALE_DOWN_ON_CORNER = (entitypatch, animation, params) -> {
            entitypatch.resetSize(EntityDimensions.scalable(0.5F, 1.8F));
        };

        public static final AnimationProperty.YRotProvider ANIMATION_YROT = (self, entitypatch) -> {
            if (self.isLinkAnimation()) {
                return entitypatch.getYRot();
            }

            Quaternionf qInitRot = self.getCoord().getInterpolatedRotation(0.0F);
            Quaternionf qDestRot = self.getCoord().getInterpolatedRotation(self.getTotalTime());
            Quaternionf qRot = self.getCoord().getInterpolatedRotation(entitypatch.getAnimator().getPlayerFor(self.getAccessor()).getElapsedTime());
            Vector3f initAngles = qInitRot.getEulerAnglesXYZ(new Vector3f());
            Vector3f destAngles = qDestRot.getEulerAnglesXYZ(new Vector3f());
            Vector3f angles = qRot.getEulerAnglesXYZ(new Vector3f());
            float initRot = entitypatch.getAnimator().getVariables().getOrDefault(CLIFF_START_Y_ROT, self.getRealAnimation());
            float destRot = entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(CLIFF_Y_ROT);
            double yRot = Math.toDegrees(angles.y);
            double initYRot = Math.toDegrees(initAngles.y);
            double destYRot = Math.toDegrees(destAngles.y);
            double progression = (yRot - initYRot) / (destYRot - initYRot);

            return MathUtils.lerpDegree(initRot, destRot, (float)progression);
        };
    }
}
