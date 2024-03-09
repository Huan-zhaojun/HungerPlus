package com.huan.mod;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.IntValue maxFoodLevel_default_Config;
    public static ForgeConfigSpec.IntValue minHealFoodLevel_default_Config;
    public static ForgeConfigSpec.BooleanValue Reset_default_Config;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push("FoodStats");
        maxFoodLevel_default_Config = COMMON_BUILDER.comment("Default maximum hunger value")
                .defineInRange("maxFoodLevel_default", 20, 1, Integer.MAX_VALUE);
        minHealFoodLevel_default_Config = COMMON_BUILDER.comment("Default minimum heal hunger value")
                .defineInRange("minHealFoodLevel_default", 18, 1, Integer.MAX_VALUE);
        Reset_default_Config = COMMON_BUILDER.comment("是否每次进入游戏存档都会重置为初始默认值？","Will it reset to the initial default values every time I enter the game save?"
                        ,"设置为“true”,即使通过游戏中指令修改的值，每次玩家重新进入游戏存档都会重置为上面两个的默认值","Set to \"true\", even if the value is modified through in-game commands, every time the player re-enters the game save, it will reset to the default values of the above two values")
                .define("Reset_default",false);
        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
