package github.sweety_banana.heartvisualizer.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;


import net.minecraft.text.Text;

public class HeartVisualizerConfigScreen extends Screen{
    private final Screen parent;
    private Screen configScreen;

    protected HeartVisualizerConfigScreen(Screen parent) {
        super(Text.of("Heart Visualizer Settings"));
        this.parent = parent;

        // 创建 Builder
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.heartvisualizer.config"));

        // 创建 EntryBuilder
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 创建分类
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.heartvisualizer.general"));

        // 添加一个开关
        general.addEntry(
                entryBuilder.startBooleanToggle(Text.translatable("option.heartvisualizer.enable_hearts"), HeartVisualizerConfig.INSTANCE.isThrough)
                        .setSaveConsumer(newValue -> HeartVisualizerConfig.INSTANCE.isThrough = newValue)
                        .build()
        );

        // 最终生成屏幕
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
