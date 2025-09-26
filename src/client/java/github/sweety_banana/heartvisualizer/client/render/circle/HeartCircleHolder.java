package github.sweety_banana.heartvisualizer.client.render.circle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface HeartCircleHolder {
    HeartCircleRender heartVisualizer$getHeartCycleRender();
}
