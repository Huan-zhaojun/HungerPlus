package com.huan.mod.mixin;

import com.huan.mod.Network.Networking;
import com.huan.mod.Network.SendPack;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
    }
    @Shadow
    protected FoodStats foodData;

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (!level.isClientSide) {
            ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
            if (player == null) return;
            int maxFoodLevel;
            int foodLevel = foodData.getFoodLevel();
            try {
                maxFoodLevel = (int) foodData.getClass().getMethod("getMaxFoodLevel").invoke(foodData);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            //同步客户端的玩家食物状态数据
            Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> player)
                    , new SendPack(maxFoodLevel,foodLevel,uuid));
        }
    }
}
