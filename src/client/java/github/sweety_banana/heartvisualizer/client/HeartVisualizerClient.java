package github.sweety_banana.heartvisualizer.client;

import github.sweety_banana.heartvisualizer.client.config.HeartVisualizerConfig;
import github.sweety_banana.heartvisualizer.client.config.HeartVisualizerConfigScreen;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class HeartVisualizerClient implements ClientModInitializer {
    public static final String MOD_ID = "heartvisualizer";

    public static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(HeartVisualizerConfig.class, GsonConfigSerializer::new);
        HeartVisualizerConfig.init();
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MOD_ID + ".openMenu",
                InputUtil.UNKNOWN_KEY.getCode(),
                "key.categories." + MOD_ID
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new HeartVisualizerConfigScreen(MinecraftClient.getInstance().currentScreen));
            }
        });
    }
}
