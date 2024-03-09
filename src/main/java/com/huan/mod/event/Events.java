package com.huan.mod.event;

import com.huan.mod.Network.Networking;
import com.huan.mod.command.hungerCommand;
import com.huan.mod.hunger_plus;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = hunger_plus.MOD_ID)
public class Events {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        new hungerCommand(dispatcher);
    }

    @SubscribeEvent //保存玩家死前的饥饿值设置
    public static void onPlayerCloneEvent(PlayerEvent.Clone event) throws ReflectiveOperationException {
        PlayerEntity player = event.getPlayer();
        PlayerEntity originalPlayer = event.getOriginal();
        boolean maxFoodLevel_sustained = originalPlayer.getPersistentData().getBoolean("maxFoodLevel_sustained");//如果未存值，默认为false
        boolean minHealFoodLevel_sustained = originalPlayer.getPersistentData().getBoolean("minHealFoodLevel_sustained");
        if (!event.getOriginal().level.isClientSide) {
            if (maxFoodLevel_sustained) {//最大饥饿值设置不变
                int maxFoodLevel = (int) originalPlayer.getFoodData().getClass().getMethod("getMaxFoodLevel").invoke(originalPlayer.getFoodData());
                player.getFoodData().getClass().getMethod("setMaxFoodLevel", int.class).invoke(player.getFoodData(), maxFoodLevel);
            }
            if (minHealFoodLevel_sustained) {//设置不变
                int minHealFoodLevel = (int) originalPlayer.getFoodData().getClass().getMethod("getMinHealFoodLevel").invoke(originalPlayer.getFoodData());
                player.getFoodData().getClass().getMethod("setMinHealFoodLevel", int.class).invoke(player.getFoodData(), minHealFoodLevel);
            }

            player.getPersistentData().putBoolean("maxFoodLevel_sustained", maxFoodLevel_sustained);
            player.getPersistentData().putBoolean("minHealFoodLevel_sustained", minHealFoodLevel_sustained);
        }

        //死后复活回满饥饿值
        int maxFoodLevel = (int) player.getFoodData().getClass().getMethod("getMaxFoodLevel").invoke(player.getFoodData());
        player.getFoodData().setFoodLevel(maxFoodLevel);
    }

    @SubscribeEvent //当玩家破坏草方法、泥土时解锁相应的配方
    public static void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
        ServerPlayerEntity player;
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            player = (ServerPlayerEntity) event.getPlayer();
            Block block = event.getState().getBlock();
            if (Blocks.DIRT.equals(block) || Blocks.GRASS_BLOCK.equals(block)) {
                player.awardRecipesByKey(new ResourceLocation[]{
                        new ResourceLocation("hunger_plus:eatable_dirt_from_dirt"),//hunger_plus:
                        new ResourceLocation("hunger_plus:eatable_dirt_from_grass_block")});
            }
        }
    }
    @SubscribeEvent //当玩家捡起草方法、泥土时解锁相应的配方
    public static void onPlayer(PlayerEvent.ItemPickupEvent event) {
        ServerPlayerEntity player;
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            player = (ServerPlayerEntity) event.getPlayer();
            Item item = event.getStack().getItem();
            if (Items.DIRT.equals(item) || Items.GRASS_BLOCK.equals(item)) {
                player.awardRecipesByKey(new ResourceLocation[]{
                        new ResourceLocation("hunger_plus:eatable_dirt_from_dirt"),//hunger_plus:
                        new ResourceLocation("hunger_plus:eatable_dirt_from_grass_block")});
            }
        }
    }
}
