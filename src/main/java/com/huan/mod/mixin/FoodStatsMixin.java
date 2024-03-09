package com.huan.mod.mixin;

import com.huan.mod.API.FoodStatsManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.UUID;

@Mixin(value = FoodStats.class/*,remap = false*/)
public abstract class FoodStatsMixin {
    @Unique
    public int maxFoodLevel = FoodStatsManager.maxFoodLevel_default;//饥饿度最大上限，默认为原版数值
    @Unique
    public int minHealFoodLevel = FoodStatsManager.minHealFoodLevel_default/*错误maxFoodLevel > 2 ? maxFoodLevel - 2 : 1*/;//能回血的最小饥饿度
    private int foodLevel = maxFoodLevel;//当前饥饿度水平
    @Shadow
    private float saturationLevel = 5.0F;//当前饱和度水平
    @Shadow
    private float exhaustionLevel;//当前消耗度水平
    @Shadow
    private int tickTimer;//计时器，对应游戏刻(一般20刻为一秒)
    @Shadow
    private int lastFoodLevel;//前一刻饥饿水平

    /**
     * @author 焕昭君
     * @reason 修改最大饥饿度上限
     */
    @Overwrite
    public void eat(int foodLevelIn, float foodSaturationModifier) {
        this.foodLevel = Math.min(foodLevelIn + this.foodLevel, maxFoodLevel);
        this.saturationLevel = Math.min(this.saturationLevel + (float) foodLevelIn * foodSaturationModifier * 2.0F, (float) this.foodLevel);
    }

    /**
     * @author 焕昭君
     * @reason 修改了最大饥饿值上限后，整个游戏饥饿运行逻辑改变
     */

    @Overwrite
    public void tick(PlayerEntity player) {
        Difficulty difficulty = player.level.getDifficulty();//获取玩家所在世界的难度级别。
        this.lastFoodLevel = this.foodLevel;//保存当前的食物等级作为先前的食物等级。
        if (this.exhaustionLevel > 4.0F) {//如果食物消耗值大于4.0，说明玩家需要消耗饥饿度或饱和度了。
            this.exhaustionLevel -= 4.0F;//减去4.0的食物消耗值。
            if (this.saturationLevel > 0.0F) {//如果饱和度大于0.0，说明还有剩余的饱和度。
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);//减去1.0的饱和度，但不会让饱和度降到负数。
            } else if (difficulty != Difficulty.PEACEFUL) {//如果没有饱和度，而且难度不是平和（PEACEFUL），则减少饥饿度
                this.foodLevel = Math.max(this.foodLevel - 1, 0);//减去1的饥饿度，但不会让其降到负数。
            }
        }

        boolean flag = player.level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);//获取世界的生命值自然恢复规则是否开启。
        //如果开启了生命值自然恢复规则、饱和度大于0、玩家受伤、饥饿度大于等于最大值。
        if (flag && this.saturationLevel > 0.0F && player.isHurt() && this.foodLevel >= maxFoodLevel) {
            ++this.tickTimer;//增加计时器
            if (this.tickTimer >= 10) {//每半秒，执行以下操作
                float f = Math.min(this.saturationLevel, 6.0F);//取饱和度和6.0中较小的一个值
                player.heal(f / 6.0F);//取上值计算恢复的生命值并给玩家回血
                this.addExhaustion(f);//消耗值增加，模拟消耗食物
                this.tickTimer = 0;//重置计时器
            }
        } else if (flag && this.foodLevel >= minHealFoodLevel && player.isHurt()) {//如果饥饿度不是大于等于最大值并且开启了生命值自然恢复规则、饥饿度大于等于minHealFoodLevel、玩家受伤
            ++this.tickTimer;
            if (this.tickTimer >= 80) {//如果计时器达到80（每四秒）
                player.heal(1.0F);//恢复1.0的生命值
                this.addExhaustion(6.0F);//消耗值增加，模拟消耗食物
                this.tickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {//如果饥饿度小于等于0
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                //如果玩家的生命值大于10.0，或者困难难度，或者生命值大于1.0且普通难度，执行以下操作。
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
                    player.hurt(DamageSource.STARVE, 1.0F);//以1.0的伤害值攻击玩家，模拟饥饿造成的伤害
                }

                this.tickTimer = 0;
            }
        } else {//以上条件都不满足，即饥饿度大于0且小于minHealFoodLevel
            this.tickTimer = 0;
        }

        //test
        /*if (Thread.currentThread().getName().equals("Render thread")) {
            System.out.println("Client 上限:" + maxFoodLevel + "回血:" + minHealFoodLevel + "饥饿:" + foodLevel + "饱和:" + saturationLevel);
        } else {
            System.out.println("Server 上限:" + maxFoodLevel + "回血:" + minHealFoodLevel + "饥饿:" + foodLevel + "饱和:" + saturationLevel);
        }*/
    }

    /**
     * @author 焕昭君
     * @reason 修改饥饿度最大上限
     */

    @Overwrite
    public boolean needsFood() {
        return foodLevel < maxFoodLevel;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void readAdditionalSaveData(CompoundNBT compoundNBT) {
        if (compoundNBT.contains("foodLevel", 99)) {
            this.foodLevel = compoundNBT.getInt("foodLevel");
            this.tickTimer = compoundNBT.getInt("foodTickTimer");
            this.saturationLevel = compoundNBT.getFloat("foodSaturationLevel");
            this.exhaustionLevel = compoundNBT.getFloat("foodExhaustionLevel");
        }
        if (FoodStatsManager.Reset_default) {
            this.maxFoodLevel = FoodStatsManager.maxFoodLevel_default;
            this.minHealFoodLevel = FoodStatsManager.minHealFoodLevel_default;
        } else {
            this.maxFoodLevel = compoundNBT.getInt("maxFoodLevel");
            this.minHealFoodLevel = compoundNBT.getInt("minHealFoodLevel");
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void addAdditionalSaveData(CompoundNBT compoundNBT) {
        compoundNBT.putInt("foodLevel", this.foodLevel);
        compoundNBT.putInt("foodTickTimer", this.tickTimer);
        compoundNBT.putFloat("foodSaturationLevel", this.saturationLevel);
        compoundNBT.putFloat("foodExhaustionLevel", this.exhaustionLevel);
        if (FoodStatsManager.Reset_default) {
            compoundNBT.putInt("maxFoodLevel", FoodStatsManager.maxFoodLevel_default);
            compoundNBT.putInt("minHealFoodLevel", FoodStatsManager.minHealFoodLevel_default);
        } else {
            compoundNBT.putInt("maxFoodLevel", maxFoodLevel);
            compoundNBT.putInt("minHealFoodLevel", minHealFoodLevel);
        }
    }

    @Shadow
    public abstract void addExhaustion(float exhaustion);
    /*必要的映射标记们，删除会可能导致饥饿条渲染不变化*/

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getFoodLevel() {
        return foodLevel;
    }

    @Shadow
    public abstract float getSaturationLevel();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setFoodLevel(int foodLevel) {
        if (foodLevel > maxFoodLevel) {
            System.err.println("foodLevel不能大于maxFoodLevel！");
        }
        this.lastFoodLevel = this.foodLevel;
        this.foodLevel = Math.min(foodLevel, maxFoodLevel);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    @OnlyIn(Dist.CLIENT)
    public void setSaturation(float saturation) {
        if (saturation > foodLevel * 1.0f) {
            System.err.println("saturation不能大于foodLevel！");
        }
        this.saturationLevel = Math.min(saturation, foodLevel);
    }

    @Unique
    public int getMaxFoodLevel() {
        return this.maxFoodLevel;
    }

    @Unique
    public int getMinHealFoodLevel() {
        return this.minHealFoodLevel;
    }

    @Unique
    public int getLastFoodLevel() {
        return this.lastFoodLevel;
    }

    @Unique
    public void setMaxFoodLevel(int maxFoodLevelIn) {
        if (maxFoodLevelIn < 1) {
            System.err.println("maxFoodLevel 最小只能设置为1！");
        }
        this.maxFoodLevel = Math.max(maxFoodLevelIn, 1);
        this.minHealFoodLevel = Math.max(maxFoodLevel - 2, 1);
        this.foodLevel = Math.min(this.maxFoodLevel, this.foodLevel);
        this.saturationLevel = Math.min(this.foodLevel, saturationLevel);
    }

    @Unique
    public void setMinHealFoodLevel(int minHealFoodLevelIn) {
        if (minHealFoodLevelIn < 1) {
            System.err.println("minHealFoodLevel 最小只能设置为1！");
        }
        this.minHealFoodLevel = Math.max(minHealFoodLevelIn, 1);
    }

}
