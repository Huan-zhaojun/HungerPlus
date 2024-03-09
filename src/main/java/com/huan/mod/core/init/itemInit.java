package com.huan.mod.core.init;

import com.huan.mod.common.item.bigStomachPotionItem;
import com.huan.mod.hunger_plus;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


public class itemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, hunger_plus.MOD_ID);

    public static final RegistryObject<Item> eatableDirt = ITEMS.register("eatable_dirt",
            () -> new Item(new Item.Properties().food(
                            new Food.Builder().nutrition(1).saturationMod(1).alwaysEat()
                                    .effect(new EffectInstance(Effects.CONFUSION, 15 * 20, 2), 1).build())
                    .tab(ItemGroup.TAB_FOOD)) {
                @Override
                public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {
                    if (entityLiving instanceof ServerPlayerEntity) {
                        ServerPlayerEntity player = (ServerPlayerEntity) entityLiving;
                        /*if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) {//创造模式扣饥饿值上限不生效
                            return this.isEdible() ? entityLiving.eat(worldIn, stack) : stack;
                        }*/
                        FoodStats foodStats = player.getFoodData();
                        try {
                            //通过反射获取新Mixin类实例的成员变量并修改值
                            Method setMaxFoodLevel = foodStats.getClass().getMethod("setMaxFoodLevel", int.class);
                            Field maxFoodLevel = foodStats.getClass().getField("maxFoodLevel");
                            if ((int) maxFoodLevel.get(foodStats) > 2) {
                                setMaxFoodLevel.invoke(foodStats, ((int) maxFoodLevel.get(foodStats) - 2));
                            } else {
                                setMaxFoodLevel.invoke(foodStats, 1);
                            }
                        } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException |
                                 IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return this.isEdible() ? entityLiving.eat(worldIn, stack) : stack;
                }

                @Override
                public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
                    IFormattableTextComponent iFormattableTextComponent = new TranslationTextComponent("text.eatable_dirt.effect");
                    tooltip.add(iFormattableTextComponent.withStyle(TextFormatting.DARK_GREEN));
                }
            });

    public static final RegistryObject<Item> bigStomachPotion_small = ITEMS.register("big_stomach_potion_small",
            () -> new bigStomachPotionItem(2,1));
    public static final RegistryObject<Item> bigStomachPotion_medium = ITEMS.register("big_stomach_potion_medium",
            () -> new bigStomachPotionItem(8,2));
    public static final RegistryObject<Item> bigStomachPotion_large = ITEMS.register("big_stomach_potion_large",
            () -> new bigStomachPotionItem(20,3));
}