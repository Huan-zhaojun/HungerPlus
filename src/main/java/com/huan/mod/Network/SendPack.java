package com.huan.mod.Network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.FoodStats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.function.Supplier;

public class SendPack {
    private final int maxFoodLevel;
    private final int foodLevel;
    private final UUID uuid;

    //private static final Logger LOGGER = LogManager.getLogger();

    public SendPack(PacketBuffer buffer) {
        maxFoodLevel = buffer.readInt();
        foodLevel = buffer.readInt();
        uuid = buffer.readUUID();
    }

    public SendPack(int maxFoodLevel, int foodLevel, UUID uuid) {
        this.maxFoodLevel = maxFoodLevel;
        this.foodLevel = foodLevel;
        this.uuid = uuid;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(maxFoodLevel);
        buf.writeInt(foodLevel);
        buf.writeUUID(uuid);
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(()->{
            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.isClientSide()) {
                ClientPlayerEntity player = (ClientPlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(uuid);
                if (player == null) return;
                FoodStats foodData = player.getFoodData();
                try {
                    foodData.getClass().getMethod("setMaxFoodLevel", int.class).invoke(foodData, maxFoodLevel);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
                foodData.setFoodLevel(foodLevel);
            }
        });
        context.setPacketHandled(true);
    }
}
