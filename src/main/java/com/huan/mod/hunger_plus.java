package com.huan.mod;

import com.huan.mod.core.init.itemInit;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(hunger_plus.MOD_ID)
public class hunger_plus {
    public static final String MOD_ID = "hunger_plus";

    public hunger_plus(){
        MinecraftForge.EVENT_BUS.register(this);

        itemInit.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
    }
}
