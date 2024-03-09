package com.huan.mod.API;

import com.huan.mod.Config;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.FoodStats;

/**
 * 这是一个对玩家的食物状态管理的类，
 * 直接向外提供字段、方法
 */
public class FoodStatsManager {
    private FoodStatsManager() {
    }

    /**
     * 默认值初始值，可通过配置文件、游戏中指令等进行修改
     */
    public static int maxFoodLevel_default = Config.maxFoodLevel_default_Config.get();
    public static int minHealFoodLevel_default = Config.minHealFoodLevel_default_Config.get();

    /**
     * 开启后，即使通过游戏中指令修改的值，每次玩家重新进入存档都会重置为上面两个的默认值
     */
    public static boolean Reset_default = Config.Reset_default_Config.get();

    public static int getMaxFoodLevel(PlayerEntity player) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        return (int) foodData.getClass().getMethod("getMaxFoodLevel").invoke(foodData);
    }

    public static int getMinHealFoodLevel(PlayerEntity player) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        return (int) foodData.getClass().getMethod("getMinHealFoodLevel").invoke(foodData);
    }

    public static int getFoodLevel(PlayerEntity player) {
        FoodStats foodData = player.getFoodData();
        return foodData.getFoodLevel();
    }

    public static float getSaturationLevel(PlayerEntity player) {
        FoodStats foodData = player.getFoodData();
        return foodData.getSaturationLevel();
    }

    /**
     * 设置方法在渲染线程"Render thread"不生效！
     */

    public static void setFoodLevel(PlayerEntity player) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        foodData.getClass().getMethod("setFoodLevel").invoke(foodData);
    }

    public static void setSaturation(PlayerEntity player) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        foodData.getClass().getMethod("setSaturation").invoke(foodData);
    }

    public static void setMaxFoodLevel(PlayerEntity player) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        foodData.getClass().getMethod("setMaxFoodLevel").invoke(foodData);
    }

    public static void setMinHealFoodLevel(PlayerEntity player) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        foodData.getClass().getMethod("setFoodLevel").invoke(foodData);
    }

    /*public static void setMaxFoodLevel_default(int maxFoodLevelDefault) {
        if (Thread.currentThread().getName().equals("Render thread")) return;
        maxFoodLevel_default = maxFoodLevelDefault;
    }

    public static void setMinHealFoodLevel_default(int minHealFoodLevelDefault) {
        if (Thread.currentThread().getName().equals("Render thread")) return;
        minHealFoodLevel_default = minHealFoodLevelDefault;
    }

    public static void setReset_default(boolean reset_default) {
        if (Thread.currentThread().getName().equals("Render thread")) return;
        Reset_default = reset_default;
    }*/
}
