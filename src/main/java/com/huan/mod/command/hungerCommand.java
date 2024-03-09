package com.huan.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.FoodStats;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class hungerCommand {
    public hungerCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("hunger")
                .requires((commandSource -> commandSource.hasPermission(2)))
                .then(Commands.literal("setMaxFoodLevel")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("value", IntegerArgumentType.integer(1)).executes((c -> {
                                            try {
                                                return setMaxFoodLevel(c, EntityArgument.getPlayers(c, "targets"), IntegerArgumentType.getInteger(c, "value"), false);
                                            } catch (ReflectiveOperationException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }))
                                        .then(Commands.argument("sustained", BoolArgumentType.bool())
                                                .executes((c -> {
                                                    try {
                                                        return setMaxFoodLevel(c, EntityArgument.getPlayers(c, "targets"), IntegerArgumentType.getInteger(c, "value"), BoolArgumentType.getBool(c, "sustained"));
                                                    } catch (ReflectiveOperationException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }))))
                        ))
                .then(Commands.literal("setMinHealFoodLevel")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("value", IntegerArgumentType.integer(1)).executes((c -> {
                                            try {
                                                return setMinHealFoodLevel(c, EntityArgument.getPlayers(c, "targets"), IntegerArgumentType.getInteger(c, "value"), false);
                                            } catch (ReflectiveOperationException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }))
                                        .then(Commands.argument("sustained", BoolArgumentType.bool())
                                                .executes((c -> {
                                                    try {
                                                        return setMinHealFoodLevel(c, EntityArgument.getPlayers(c, "targets"), IntegerArgumentType.getInteger(c, "value"), BoolArgumentType.getBool(c, "sustained"));
                                                    } catch (ReflectiveOperationException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }))))
                        ))
                .then(Commands.literal("setFoodLevel")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(c -> {
                                            try {
                                                return setFoodLevel(c, EntityArgument.getPlayers(c, "targets"), IntegerArgumentType.getInteger(c, "value"));
                                            } catch (ReflectiveOperationException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }))))
                .then(Commands.literal("setSaturation")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(c -> {
                                            try {
                                                return setSaturation(c, EntityArgument.getPlayers(c, "targets"), IntegerArgumentType.getInteger(c, "value"));
                                            } catch (ReflectiveOperationException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })))
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.literal("maxFoodLevel").executes(c -> {
                                    try {
                                        return get(c, EntityArgument.getPlayers(c, "targets"), "getMaxFoodLevel", "commands.hunger.setMaxFoodLevel.success2");
                                    } catch (ReflectiveOperationException e) {
                                        throw new RuntimeException(e);
                                    }
                                }))
                                .then(Commands.literal("minHealFoodLevel").executes(c -> {
                                    try {
                                        return get(c, EntityArgument.getPlayers(c, "targets"), "getMinHealFoodLevel", "commands.hunger.setMinHealFoodLevel.success2");
                                    } catch (ReflectiveOperationException e) {
                                        throw new RuntimeException(e);
                                    }
                                }))
                                .then(Commands.literal("foodLevel").executes(c -> {
                                    try {
                                        return getFoodLevel(c, EntityArgument.getPlayers(c, "targets"), "commands.hunger.setFoodLevel");
                                    } catch (ReflectiveOperationException e) {
                                        throw new RuntimeException(e);
                                    }
                                }))
                                .then(Commands.literal("saturationLevel").executes(c -> getSaturationLevel(c, EntityArgument.getPlayers(c, "targets"), "commands.hunger.setSaturation"))))
                )
        );
    }

    public static int setMaxFoodLevel(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> players, int value, boolean bool) throws ReflectiveOperationException {
        CommandSource source = context.getSource();
        ArrayList<String> namesList = new ArrayList<>();
        boolean preBool = false;
        int playerNum = 0, maxDisplayNum = 50;
        for (ServerPlayerEntity player : players) {
            player.getFoodData().getClass().getMethod("setMaxFoodLevel", int.class).invoke(player.getFoodData(), value);
            preBool = player.getPersistentData().getBoolean("maxFoodLevel_sustained");
            player.getPersistentData().putBoolean("maxFoodLevel_sustained", bool);
            namesList.add(player.getDisplayName().getString());
            playerNum++;
        }

        //输出修改了哪些玩家的值
        TranslationTextComponent ttc1 = new TranslationTextComponent("commands.hunger.setMaxFoodLevel.success1");
        String str = namesList.stream().limit(maxDisplayNum).collect(Collectors.joining(","));
        TranslationTextComponent ttc2 = new TranslationTextComponent("");
        TranslationTextComponent ttc3 = new TranslationTextComponent("commands.hunger.setMaxFoodLevel.success2");
        if (playerNum > maxDisplayNum) ttc2 = new TranslationTextComponent("text.AndSoOn");
        source.sendSuccess(ttc1.append(str).append(ttc2).append(ttc3).append(String.valueOf(value)), true);

        //输出修改的值是否因为死亡而重置
        if (preBool != bool) {
            if (bool) source.sendSuccess(new TranslationTextComponent("commands.hunger.sustained"), true);
            else source.sendSuccess(new TranslationTextComponent("commands.hunger.sustained.NO"), true);
        }
        return 0;
    }

    public static int setMinHealFoodLevel(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> players, int value, boolean bool) throws ReflectiveOperationException {
        CommandSource source = context.getSource();
        ArrayList<String> namesList = new ArrayList<>();
        boolean preBool = false;
        int playerNum = 0, maxDisplayNum = 50;
        for (ServerPlayerEntity player : players) {
            player.getFoodData().getClass().getMethod("setMinHealFoodLevel", int.class).invoke(player.getFoodData(), value);
            preBool = player.getPersistentData().getBoolean("minHealFoodLevel_sustained");
            player.getPersistentData().putBoolean("minHealFoodLevel_sustained", bool);
            namesList.add(player.getDisplayName().getString());
            playerNum++;
        }

        //输出修改了哪些玩家的值
        TranslationTextComponent ttc1 = new TranslationTextComponent("commands.hunger.setMinHealFoodLevel.success1");
        String str = namesList.stream().limit(maxDisplayNum).collect(Collectors.joining(","));
        TranslationTextComponent ttc2 = new TranslationTextComponent("");
        TranslationTextComponent ttc3 = new TranslationTextComponent("commands.hunger.setMinHealFoodLevel.success2");
        if (playerNum > maxDisplayNum) ttc2 = new TranslationTextComponent("text.AndSoOn");
        source.sendSuccess(ttc1.append(str).append(ttc2).append(ttc3).append(String.valueOf(value)), true);

        //输出修改的值是否因为死亡而重置
        if (preBool != bool) {
            if (bool) source.sendSuccess(new TranslationTextComponent("commands.hunger.sustained"), true);
            else source.sendSuccess(new TranslationTextComponent("commands.hunger.sustained.NO"), true);
        }
        return 0;
    }

    public static int setFoodLevel(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> players, int value) throws ReflectiveOperationException {
        CommandSource source = context.getSource();
        ArrayList<String> namesList_success = new ArrayList<>();
        ArrayList<String> namesList_fail = new ArrayList<>();
        int playerNum_success = 0, playerNum_fail = 0, maxDisplayNum = 50;
        for (ServerPlayerEntity player : players) {
            FoodStats foodData = player.getFoodData();
            int maxFoodLevel = (int) foodData.getClass().getMethod("getMaxFoodLevel").invoke(foodData);
            if (value > maxFoodLevel) {
                namesList_fail.add(player.getDisplayName().getString());
                playerNum_fail++;
            } else {
                foodData.setFoodLevel(value);
                namesList_success.add(player.getDisplayName().getString());
                playerNum_success++;
            }
        }

        //输出失败修改了哪些玩家
        if (playerNum_fail > 0) {
            TranslationTextComponent ttc1 = new TranslationTextComponent("commands.hunger.set.fail");
            String str = namesList_fail.stream().limit(maxDisplayNum).collect(Collectors.joining(","));
            TranslationTextComponent ttc2 = new TranslationTextComponent("");
            TranslationTextComponent ttc3 = new TranslationTextComponent("commands.hunger.setFoodLevel.failCause");
            if (playerNum_fail > maxDisplayNum) ttc2 = new TranslationTextComponent("text.AndSoOn");
            source.sendSuccess(ttc1.append(str).append(ttc2).append(ttc3), true);
        }

        //输出成功修改了哪些玩家的值
        if (playerNum_success > 0) {
            TranslationTextComponent ttc1 = new TranslationTextComponent("commands.hunger.setMinHealFoodLevel.success1");
            String str = namesList_success.stream().limit(maxDisplayNum).collect(Collectors.joining(","));
            TranslationTextComponent ttc2 = new TranslationTextComponent("");
            TranslationTextComponent ttc3 = new TranslationTextComponent("commands.hunger.setFoodLevel");
            if (playerNum_success > maxDisplayNum) ttc2 = new TranslationTextComponent("text.AndSoOn");
            source.sendSuccess(ttc1.append(str).append(ttc2).append(ttc3).append(String.valueOf(value)), true);
        }
        return 0;
    }

    public static int setSaturation(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> players, int value) throws ReflectiveOperationException {
        CommandSource source = context.getSource();
        ArrayList<String> namesList_success = new ArrayList<>();
        ArrayList<String> namesList_fail = new ArrayList<>();
        int playerNum_success = 0, playerNum_fail = 0, maxDisplayNum = 50;
        for (ServerPlayerEntity player : players) {
            FoodStats foodData = player.getFoodData();
            int foodLevel = foodData.getFoodLevel();
            if (value > foodLevel) {
                namesList_fail.add(player.getDisplayName().getString());
                playerNum_fail++;
            } else {
                foodData.setSaturation(value * 1.0f);
                namesList_success.add(player.getDisplayName().getString());
                playerNum_success++;
            }
        }

        //输出失败修改了哪些玩家
        if (playerNum_fail > 0) {
            TranslationTextComponent ttc1 = new TranslationTextComponent("commands.hunger.set.fail");
            String str = namesList_fail.stream().limit(maxDisplayNum).collect(Collectors.joining(","));
            TranslationTextComponent ttc2 = new TranslationTextComponent("");
            TranslationTextComponent ttc3 = new TranslationTextComponent("commands.hunger.setSaturation.failCause");
            if (playerNum_fail > maxDisplayNum) ttc2 = new TranslationTextComponent("text.AndSoOn");
            source.sendSuccess(ttc1.append(str).append(ttc2).append(ttc3), true);
        }

        //输出成功修改了哪些玩家的值
        if (playerNum_success > 0) {
            TranslationTextComponent ttc1 = new TranslationTextComponent("commands.hunger.setMinHealFoodLevel.success1");
            String str = namesList_success.stream().limit(maxDisplayNum).collect(Collectors.joining(","));
            TranslationTextComponent ttc2 = new TranslationTextComponent("");
            TranslationTextComponent ttc3 = new TranslationTextComponent("commands.hunger.setSaturation");
            if (playerNum_success > maxDisplayNum) ttc2 = new TranslationTextComponent("text.AndSoOn");
            source.sendSuccess(ttc1.append(str).append(ttc2).append(ttc3).append(String.valueOf(value)), true);
        }
        return 0;
    }

    public static int get(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> players, String method, String text) throws ReflectiveOperationException {
        CommandSource source = context.getSource();
        HashMap<Integer, ArrayList<String>> map = new HashMap<>();
        for (ServerPlayerEntity player : players) {
            FoodStats foodData = player.getFoodData();
            int i = (int) foodData.getClass().getMethod(method).invoke(foodData);
            if (map.get(i) == null) {
                ArrayList<String> list = new ArrayList<>();
                list.add(player.getDisplayName().getString());
                map.put(i, list);
            } else {
                map.get(i).add(player.getDisplayName().getString());
            }
        }

        sendSuccess_1(map, source, text);

        return 0;
    }

    public static int getFoodLevel(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> players, String text) throws ReflectiveOperationException{
        CommandSource source = context.getSource();
        HashMap<Integer, ArrayList<String>> map = new HashMap<>();
        for (ServerPlayerEntity player : players) {
            FoodStats foodData = player.getFoodData();
            int i = foodData.getFoodLevel();
            if (map.get(i) == null) {
                ArrayList<String> list = new ArrayList<>();
                list.add(player.getDisplayName().getString());
                map.put(i, list);
            } else {
                map.get(i).add(player.getDisplayName().getString());
            }
        }

        sendSuccess_1(map, source, text);

        return 0;
    }

    public static int getSaturationLevel(CommandContext<CommandSource> context, Collection<ServerPlayerEntity> players, String text) {
        CommandSource source = context.getSource();
        HashMap<Float, ArrayList<String>> map = new HashMap<>();
        for (ServerPlayerEntity player : players) {
            FoodStats foodData = player.getFoodData();
            float f = foodData.getSaturationLevel();
            if (map.get(f) == null) {
                ArrayList<String> list = new ArrayList<>();
                list.add(player.getDisplayName().getString());
                map.put(f, list);
            } else {
                map.get(f).add(player.getDisplayName().getString());
            }
        }

        sendSuccess_2(map, source, text);

        return 0;
    }

    private static void sendSuccess_1(HashMap<Integer, ArrayList<String>> map, CommandSource source, String str) {
        int num = 0;
        for (Integer i : map.keySet()) {
            ArrayList<String> namesList = map.get(i);
            source.sendSuccess(new StringTextComponent(namesList.stream().limit(10).collect(Collectors.joining(",")))
                    .append(new TranslationTextComponent(str)
                            .append(String.valueOf(i))), true);
            num++;
            if (num >= 5) {
                source.sendSuccess(new StringTextComponent("......"), true);
            }
        }
    }

    private static void sendSuccess_2(HashMap<Float, ArrayList<String>> map, CommandSource source, String str) {
        int num = 0;
        for (float i : map.keySet()) {
            ArrayList<String> namesList = map.get(i);
            source.sendSuccess(new StringTextComponent(namesList.stream().limit(10).collect(Collectors.joining(",")))
                    .append(new TranslationTextComponent(str)
                            .append(String.valueOf(i))), true);
            num++;
            if (num >= 5) {
                source.sendSuccess(new StringTextComponent("......"), true);
            }
        }
    }
}
