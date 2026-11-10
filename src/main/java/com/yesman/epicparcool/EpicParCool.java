package com.yesman.epicparcool;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.yesman.epicparcool.client.event.ParCoolClientEvents;
import com.yesman.epicparcool.client.screen.EpicParCoolConfigurations;
import com.yesman.epicparcool.event.ParCoolEvents;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.main.EpicFightSharedConstants;

@Mod(EpicParCool.MODID)
public class EpicParCool {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "epicparcool";
	public static final String LEAST_PARCOOL_VERSION = "4.0.2.0";
	
	public EpicParCool(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(ParCoolEvents::onSetup);
		modEventBus.addListener(this::constructMod);
		
		if (EpicFightSharedConstants.isPhysicalClient()) {
			modEventBus.addListener(ParCoolClientEvents::onSetup);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, EpicParCoolConfigurations::new);

            EpicFightEventHooks.Player.CAST_SKILL.registerEvent(ParCoolClientEvents::skillExecute);
            EpicFightClientEventHooks.Entity.MODIFY_PLAYER_LIVING_MOTION_BASE.registerEvent(ParCoolClientEvents::onBaseLayerUpdateEvent);
		}

        EpicFightEventHooks.Animation.INIT_ANIMATOR.registerEvent(ParCoolEvents::onInitAnimatorEvent);
	}
	
	public void constructMod(FMLConstructModEvent event) {
		LivingMotion.ENUM_MANAGER.registerEnumCls(EpicParCool.MODID, ParcoolLivingMotions.class);
	}
}
