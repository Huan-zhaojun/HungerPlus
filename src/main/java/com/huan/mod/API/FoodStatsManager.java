package com.huan.mod.API;

import com.huan.mod.Config;
import com.huan.mod.hunger_plus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.FoodStats;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 这是一个对玩家的食物状态管理的类，
 * 直接向外提供字段、方法
 */
@Mod.EventBusSubscriber(modid = hunger_plus.MOD_ID)
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

    /**
     * 接收其他模组发送的IMC信息并处理<br>
     * 发送消息参数：<br>
     * “senderModId”=发送消息的mod的ModId，不填可能无法回调<br>
     * “method”=“{@link hunger_plus#MOD_ID}:method” 填入调用的接口方法名<br>
     * 例如："hunger_plus:{@link FoodStatsManager#getMaxFoodLevel}"<br>
     * "hunger_plus:{@link FoodStatsManager#setFoodLevel}"<br>
     * “{@link Supplier}<?>thing”=填入调用方法的参数,如果多个参数则顺序填入{@link Object}数组 <br>
     *  若调用方法具有返回值,接收消息的参数“method”=“{@link hunger_plus#MOD_ID}:callback#Name”<br>
     *  下面是一些IMC发送端代码例子：<br>
     *  <code>InterModComms.sendTo(fart_shit_pee.MOD_ID, "hunger_plus", "hunger_plus:getMaxFoodLevel", () -> player);</code><br>
     *  <code>InterModComms.sendTo(yourMod.MOD_ID, "hunger_plus", "hunger_plus:setFoodLevel", (Supplier< Object[]>) () -> new Object[]{player,5});</code><br>
     *  下面是一些IMC发送端接收回调值的代码例子：<br>
     *  <code>InterModComms.getMessages(fart_shit_pee.MOD_ID,s -> s.equals("hunger_plus:callback#getMaxFoodLevel"))</code>
     * @see InterModComms#sendTo(String, String, String, Supplier)
     */
    @SubscribeEvent
    public static void tickReceive(TickEvent.PlayerTickEvent event) {
        if (!event.player.level.isClientSide) {
            List<InterModComms.IMCMessage> list = InterModComms.getMessages(hunger_plus.MOD_ID,
                    s -> s.matches("(" + hunger_plus.MOD_ID + ":)\\w+")).collect(Collectors.toList());
            for (InterModComms.IMCMessage imcMessage : list) {
                String method = imcMessage.getMethod().split(":")[1];
                HashSet<String> methods = (HashSet<String>) Arrays.stream(FoodStatsManager.class.getDeclaredMethods()).map(Method::getName).collect(Collectors.toSet());
                if (methods.contains(method)) {
                    try {
                        if (method.contains("get")) {
                            Object object = FoodStatsManager.class.getMethod(method, PlayerEntity.class)
                                    .invoke(new Object(), (PlayerEntity)imcMessage.getMessageSupplier().get());
                            if (object != null) {
                                InterModComms.sendTo(imcMessage.getSenderModId(), hunger_plus.MOD_ID + ":callback#"+method,
                                        () -> object);
                            }
                        } else if (method.contains("set")) {
                            FoodStatsManager.class.getMethod(method, PlayerEntity.class,int.class)
                                    .invoke(new Object(),
                                            (PlayerEntity)((Object[]) imcMessage.getMessageSupplier().get())[0],
                                            (int)((Object[]) imcMessage.getMessageSupplier().get())[1]);
                        }
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

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

    public static void setFoodLevel(PlayerEntity player, int num) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        foodData.setFoodLevel(num);
    }

    public static void setSaturation(PlayerEntity player, float num) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        foodData.setSaturation(num);
    }

    public static void setMaxFoodLevel(PlayerEntity player, int num) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        foodData.getClass().getMethod("setMaxFoodLevel", int.class).invoke(foodData, num);
    }

    public static void setMinHealFoodLevel(PlayerEntity player, int num) throws ReflectiveOperationException {
        FoodStats foodData = player.getFoodData();
        foodData.getClass().getMethod("setMinHealFoodLevel", int.class).invoke(foodData, num);
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
