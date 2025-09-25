package github.sweety_banana.heartvisualizer.client.enums;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;

@Environment(EnvType.CLIENT)
public enum HeartTypeEnum {
    EMPTY("container"),
    RED_FULL("full"),
    RED_HALF("half"),
    YELLOW_FULL("absorbing_full"),
    YELLOW_HALF("absorbing_half");

    public final String icon;

    HeartTypeEnum(String heartIcon) {
        icon = heartIcon;
    }

    public String getStatusIcon(LivingEntity livingEntity){
        String tempIcon = this.icon;
        if (livingEntity instanceof AbstractClientPlayerEntity && livingEntity.getWorld().getLevelProperties().isHardcore()){
            if (this == YELLOW_FULL){
                tempIcon = "absorbing_hardcore_full";
            } else if(this == YELLOW_HALF){
                tempIcon = "absorbing_hardcore_half";
            }
            tempIcon = "hardcore_" + tempIcon;
        }

        if (livingEntity.hasStatusEffect(StatusEffects.WITHER)) tempIcon = "withered_" + tempIcon;
        else if (livingEntity.hasStatusEffect(StatusEffects.POISON)) tempIcon = "poisoned_" + tempIcon;
        else if (livingEntity.isFrozen()) tempIcon = "frozen_" + tempIcon;
        return tempIcon;
    }
}