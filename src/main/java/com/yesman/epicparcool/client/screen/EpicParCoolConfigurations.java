package com.yesman.epicparcool.client.screen;

import com.alrex.parcool.ParCool;
import com.alrex.parcool.common.stamina.StaminaTypes;
import com.yesman.epicparcool.EpicParCool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent.HorizontalSizing;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.client.gui.screen.config.EpicFightSettingScreen;
import yesman.epicfight.client.gui.widgets.*;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.config.ClientConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static yesman.epicfight.generated.LangKeys.*;

@OnlyIn(Dist.CLIENT)
public class EpicParCoolConfigurations extends Screen {
	private static final List<String> STAMINA_TYPE_IDS = List.of(
		StaminaTypes.NONE_STAMINA.id().toString(),
		StaminaTypes.PARCOOL_STAMINA.id().toString(),
		StaminaTypes.HUNGER_STAMINA.id().toString(),
		"epicfight:epicfight"
	);

    @Nullable
	private final Screen parentScreen;
    private final AnchoredButton saveButton;
    private final AnchoredButton discardButton;

    protected final WidgetTable widgetTable;
    protected final TextBox textBox;
    @Nullable
    protected SettingTitle hoveringSettingTitle = null;

	public EpicParCoolConfigurations(@Nullable final ModContainer mod, @Nullable Screen screen) {
		super(Component.translatable("gui.epicparcool.config_screen.title"));
		
		this.parentScreen = screen;
        this.minecraft = screen == null ? Minecraft.getInstance() : screen.getMinecraft();

        this.saveButton = AnchoredButton.buttonBuilder(
            Component.translatable(GUI_WIDGET_COMMON_SAVE),
            button -> {
                List<Runnable> toSave = new ArrayList<>();
                List<Runnable> toDiscard = new ArrayList<>();
                ClientConfig.checkUnsaved(toSave, toDiscard);

                for (Runnable runnable : toSave) {
                    runnable.run();
                }

                this.minecraft.setScreen(this.parentScreen);
            }
        )
        .xParams(115, 80)
        .yParams(10, 20)
        .horizontalAnchorType(AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH)
        .verticalAnchorType(AnchoredWidget.VerticalAnchorType.BOTTOM_HEGIHT)
        .theme(AnchoredButton.BuiltInTheme.BLACK)
        .highlihgtFontWhen(AbstractWidget::isHoveredOrFocused)
        .alpha(0.6F)
        .build();

        this.discardButton = AnchoredButton.buttonBuilder(
            Component.translatable(GUI_WIDGET_COMMON_DISCARD),
            button -> {
                List<Runnable> toSave = new ArrayList<>();
                List<Runnable> toDiscard = new ArrayList<> ();
                ClientConfig.checkUnsaved(toSave, toDiscard);

                if (!toSave.isEmpty() && !toDiscard.isEmpty()) {
                    this.minecraft.setScreen(new MessageScreen<>(
                        "",
                        Component.translatable(GUI_MESSAGE_SETTINGS_DISCARD_CHANGES_NOTIFICATION),
                        this,
                        button$2 -> {
                            toDiscard.forEach(Runnable::run);
                            this.minecraft.setScreen(this.parentScreen);
                        },
                        button$2 -> {
                            this.minecraft.setScreen(this);
                        },
                        180,
                        0
                    ).autoCalculateHeight());
                } else {
                    this.minecraft.setScreen(this.parentScreen);
                }
            }
        )
        .xParams(10, 100)
        .yParams(10, 20)
        .horizontalAnchorType(AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH)
        .verticalAnchorType(AnchoredWidget.VerticalAnchorType.BOTTOM_HEGIHT)
        .theme(AnchoredButton.BuiltInTheme.BLACK)
        .highlihgtFontWhen(AbstractWidget::isHoveredOrFocused)
        .alpha(0.6F)
        .build();

        widgetTable = new WidgetTable(this, 10, 0, 5, 10, AnchoredWidget.HorizontalAnchorType.LEFT_RIGHT, AnchoredWidget.VerticalAnchorType.TOP_BOTTOM, 21);
        textBox = new TextBox(this.minecraft.font, 10, 0, 5, 40, AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH, AnchoredWidget.VerticalAnchorType.TOP_BOTTOM, Component.empty());

        widgetTable
            .newRow()
                .addWidget(
                    new SettingTitle(
                        minecraft.font,
                        this.widgetTable.nextX(4),
                        125,
                        0,
                        15,
                        AnchoredWidget.HorizontalAnchorType.LEFT_RIGHT,
                        AnchoredWidget.VerticalAnchorType.TOP_HEIGHT,
                        "gui.epicparcool.widget.config_screen.stamina_source",
                        this.textBox::setMessage,
                        this::getHoveringSettingTitle,
                        this::setHoveringSettingTitle,
                        this::getFocusedWidgetFromTable
                    )
                )
                .addWidget(
                    new ComboBox<>(
                        this,
                        minecraft.font,
                        10,
                        100,
                        0,
                        15,
                        AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH,
                        AnchoredWidget.VerticalAnchorType.TOP_HEIGHT,
                        () -> ParCool.getConfig().server().staminaType.get(),
                        value -> ParCool.getConfig().server().staminaType.set(value),
                        8,
                        Component.translatable("gui.epicparcool.widget.config_screen.stamina_source"),
                        STAMINA_TYPE_IDS,
                        value -> Component.translatable("gui.epicparcool.widget.stamina_source." + ResourceLocation.parse(value).getPath()).getString()
                    )
                );

        this.widgetTable.initialize(false);
	}
	
	@Override
	protected void init() {
        this.addRenderableWidget(this.saveButton);
        this.addRenderableWidget(this.discardButton);
        this.addRenderableWidget(this.widgetTable);
        this.addRenderableWidget(this.textBox);

        this.repositionElements();
	}

    @Override
    public void repositionElements() {
        ScreenRectangle rectangle = this.getRectangle();
        this.widgetTable.setX2(Math.round((rectangle.right() - rectangle.left()) * 0.3F));
        this.widgetTable.relocate(rectangle);
        this.textBox.setX2(Math.round((rectangle.right() - rectangle.left()) * 0.27F));
        this.textBox.relocate(rectangle);
        this.saveButton.relocate(rectangle);
        this.discardButton.relocate(rectangle);
    }

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Component warning = Component.translatable("gui.epicparcool.config_screen.warn");
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawString(this.font, warning, 6, this.height - 50, 16777215);
	}

    @Nullable
    public GuiEventListener getFocusedWidgetFromTable() {
        if (this.getFocused() == this.widgetTable && this.widgetTable.getSelected() instanceof WidgetTable.WidgetEntry widgetEntry) {
            return widgetEntry.getFocused();
        }

        return null;
    }

    @Nullable
    protected SettingTitle getHoveringSettingTitle() {
        return this.hoveringSettingTitle;
    }

    protected void setHoveringSettingTitle(@Nullable SettingTitle settingTitle) {
        this.hoveringSettingTitle = settingTitle;
    }

	@Override
	public void onClose() {
        List<Runnable> toSave = new ArrayList<>();
        List<Runnable> toDiscard = new ArrayList<>();
        ClientConfig.checkUnsaved(toSave, toDiscard);

        if (!toSave.isEmpty() && !toDiscard.isEmpty()) {
            this.minecraft.setScreen(new MessageScreen<> (
                "",
                Component.translatable(GUI_MESSAGE_SETTINGS_UNSAVED_CHANGES_NOTIFICATION),
                this,
                button -> {
                    toDiscard.forEach(Runnable::run);
                    this.minecraft.setScreen(this.parentScreen);
                },
                button -> {
                    this.minecraft.setScreen(this);
                },
                180,
                0
            ).autoCalculateHeight());
        } else {
            this.minecraft.setScreen(this.parentScreen);
        }
	}
}
