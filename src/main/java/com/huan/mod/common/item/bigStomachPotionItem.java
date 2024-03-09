package com.huan.mod.common.item;

import com.google.common.graph.Network;
import com.huan.mod.Network.Networking;
import com.huan.mod.Network.SendPack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.FoodStats;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class bigStomachPotionItem extends PotionItem {
    int effect = 0;
    int size = 0;

    public bigStomachPotionItem(int effect, int size) {
        super(new Item.Properties().stacksTo(8).rarity(Rarity.EPIC).tab(ItemGroup.TAB_BREWING));
        this.effect = effect;
        this.size = size;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return new ItemStack(this);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getDescriptionId();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        if (entityLiving != null && entityLiving instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) entityLiving;
            FoodStats foodStats = player.getFoodData();
            try {
                Method setMaxFoodLevel = foodStats.getClass().getMethod("setMaxFoodLevel", int.class);
                Field maxFoodLevel = foodStats.getClass().getField("maxFoodLevel");
                setMaxFoodLevel.invoke(foodStats, ((int) maxFoodLevel.get(foodStats) + effect));
            } catch (NoSuchMethodException | NoSuchFieldException | InvocationTargetException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (!player.abilities.instabuild) {
                stack.shrink(1);
            }

            if (stack.isEmpty() && !player.abilities.instabuild) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            player.inventory.add(new ItemStack(Items.GLASS_BOTTLE));
        }
        return this.isEdible() ? entityLiving.eat(worldIn, stack) : stack;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        IFormattableTextComponent iFormattableTextComponent1, iFormattableTextComponent2;
        switch (size) {
            case 1:
                iFormattableTextComponent1 = new TranslationTextComponent("text.big_stomach_potion.small");
                tooltip.add(iFormattableTextComponent1.withStyle(TextFormatting.BLUE));
                iFormattableTextComponent2 = new TranslationTextComponent("text.big_stomach_potion.effect_small");
                break;
            case 2:
                iFormattableTextComponent1 = new TranslationTextComponent("text.big_stomach_potion.medium");
                tooltip.add(iFormattableTextComponent1.withStyle(TextFormatting.DARK_PURPLE));
                iFormattableTextComponent2 = new TranslationTextComponent("text.big_stomach_potion.effect_medium");
                break;
            case 3:
                iFormattableTextComponent1 = new TranslationTextComponent("text.big_stomach_potion.large");
                tooltip.add(iFormattableTextComponent1.withStyle(TextFormatting.GOLD));
                iFormattableTextComponent2 = new TranslationTextComponent("text.big_stomach_potion.effect_large");
                break;
            default:
                iFormattableTextComponent1 = new TranslationTextComponent("");
                iFormattableTextComponent2 = new TranslationTextComponent("");

        }
        tooltip.add(iFormattableTextComponent2.withStyle(TextFormatting.GREEN));
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(new ItemStack(this));
        }
    }
}
