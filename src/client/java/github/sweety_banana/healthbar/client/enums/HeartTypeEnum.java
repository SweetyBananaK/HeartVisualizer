package github.sweety_banana.healthbar.client.enums;


import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

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
        if (livingEntity.hasStatusEffect(StatusEffects.WITHER)) tempIcon = "withered_" + tempIcon;
        if (livingEntity.hasStatusEffect(StatusEffects.POISON)) tempIcon = "poisoned_" + tempIcon;
        if (livingEntity.isFrozen()) tempIcon = "frozen_" + tempIcon;
        if (livingEntity.getWorld().getLevelProperties().isHardcore()) tempIcon = "hardcore_" + tempIcon;
        return tempIcon;
    }
}