package github.sweety_banana.heartvisualizer.client.config;

import github.sweety_banana.heartvisualizer.client.render.bar.HeartBarRender;
import github.sweety_banana.heartvisualizer.client.render.count.HeartCountRender;
import github.sweety_banana.heartvisualizer.client.render.cycle.HeartCycleRender;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;


import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class HeartVisualizerConfigScreen extends Screen{
    private final Screen parent;
    private final Screen configScreen;

    public HeartVisualizerConfigScreen(Screen parent) {
        super(Text.of("Heart Visualizer Settings"));
        this.parent = parent;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.heartvisualizer.config"))
                .setSavingRunnable(() -> {
                    AutoConfig.getConfigHolder(HeartVisualizerConfig.class).save();
                    HeartCycleRender.updateConfig();
                    HeartCountRender.updateConfig();
                    HeartBarRender.updateConfig();
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.heartvisualizer.general"));

        //General
        general.addEntry(
                entryBuilder.startTextDescription(Text.translatable("label.heartvisualizer.general"))
                        .setColor(0xFFFFFFFF)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(Text.translatable("option.heartvisualizer.isactive"), HeartVisualizerConfig.INSTANCE.isActive)
                        .setDefaultValue(true)
                        .setSaveConsumer(val -> HeartVisualizerConfig.INSTANCE.isActive = val)
                        .build()
        );

        general.addEntry(
                entryBuilder.startEnumSelector(Text.translatable("option.heartvisualizer.displaytype"), HeartVisualizerConfig.HeartDisplayType.class, HeartVisualizerConfig.INSTANCE.displayType)
                        .setEnumNameProvider(enumVal -> ((HeartVisualizerConfig.HeartDisplayType)enumVal).getDisplayName())
                        .setSaveConsumer(val -> HeartVisualizerConfig.INSTANCE.displayType = val)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(Text.translatable("option.heartvisualizer.showthrough"), HeartVisualizerConfig.INSTANCE.isThrough)
                        .setDefaultValue(false)
                        .setTooltip(Text.translatable("tooltip.heartvisualizer.showthrough"))
                        .setSaveConsumer(val -> HeartVisualizerConfig.INSTANCE.isThrough = val)
                        .build()
        );

        general.addEntry(
                entryBuilder.startIntSlider(Text.translatable("option.heartvisualizer.heartcycle.heartsize"),
                                (int)(HeartVisualizerConfig.INSTANCE.heartSize), 5, 15)
                        .setDefaultValue(9)
                        .setTextGetter(val -> Text.literal(String.valueOf(val)))
                        .setSaveConsumer(val -> {
                            HeartVisualizerConfig.INSTANCE.heartSize = (float) val;
                        })
                        .build()
        );

        //HeartCycle
        general.addEntry(
                entryBuilder.startTextDescription(Text.translatable("label.heartvisualizer.heartcycle"))
                        .setColor(0xFFFFFFFF)
                        .build()
        );

        general.addEntry(
                entryBuilder.startIntSlider(Text.translatable("option.heartvisualizer.heartcycle.rotateduration"),
                                (int)(HeartVisualizerConfig.INSTANCE.heartCircle.rotateDuration / 1000), 2, 20)
                        .setDefaultValue(5)
                        .setTextGetter(val -> Text.literal(val + " s"))
                        .setSaveConsumer(val -> {
                            HeartVisualizerConfig.INSTANCE.heartCircle.rotateDuration = (long) val * 1000;
                        })
                        .build()
        );

        general.addEntry(
                entryBuilder.startIntSlider(Text.translatable("option.heartvisualizer.heartcycle.appearduration"),
                                (int)(HeartVisualizerConfig.INSTANCE.heartCircle.appearDuration), 100, 1000)
                        .setDefaultValue(300)
                        .setTextGetter(val -> Text.literal(Math.round(val / 100.0) * 100 + " ms"))
                        .setSaveConsumer(val -> {
                            HeartVisualizerConfig.INSTANCE.heartCircle.appearDuration = Math.round(val / 100.0) * 100;
                        })
                        .build()
        );

        general.addEntry(
                entryBuilder.startIntSlider(Text.translatable("option.heartvisualizer.heartcycle.disappearduration"),
                                (int)(HeartVisualizerConfig.INSTANCE.heartCircle.disappearDuration), 100, 1000)
                        .setDefaultValue(300)
                        .setTextGetter(val -> Text.literal(Math.round(val / 100.0) * 100 + " ms"))
                        .setSaveConsumer(val -> {
                            HeartVisualizerConfig.INSTANCE.heartCircle.disappearDuration = Math.round(val / 100.0) * 100;
                            HeartCycleRender.updateConfig();
                        })
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(Text.translatable("option.heartvisualizer.heartcycle.isflashing"), HeartVisualizerConfig.INSTANCE.heartCircle.isFlashing)
                        .setDefaultValue(false)
                        .setSaveConsumer(val -> HeartVisualizerConfig.INSTANCE.heartCircle.isFlashing = val)
                        .build()
        );

        this.configScreen = builder.build();
    }


    @Override
    protected void init() {
        super.init();
        if (this.client != null) {
            this.client.setScreen(this.configScreen);
        }
    }
}
