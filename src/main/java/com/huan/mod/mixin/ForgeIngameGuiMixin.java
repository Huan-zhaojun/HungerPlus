package com.huan.mod.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effects;
import net.minecraft.util.FoodStats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

import static net.minecraftforge.client.gui.ForgeIngameGui.right_height;

@Mixin(value = ForgeIngameGui.class/*,remap = false*/)
@OnlyIn(Dist.CLIENT)
public abstract class ForgeIngameGuiMixin extends IngameGui {

    public ForgeIngameGuiMixin(Minecraft mcIn) {
        super(mcIn);
    }

    @Shadow(remap = false)
    protected abstract boolean pre(RenderGameOverlayEvent.ElementType type, MatrixStack mStack);

    @Shadow(remap = false)
    protected abstract void post(RenderGameOverlayEvent.ElementType type, MatrixStack mStack);

    /**
     * @author 焕昭君
     * @reason 修改饥饿值上限后血条渲染也需要随之改变
     */
    @Overwrite(remap = false)
    public void renderFood(int width, int height, MatrixStack mStack) throws ReflectiveOperationException {
        if (!this.pre(RenderGameOverlayEvent.ElementType.FOOD, mStack)) {
            this.minecraft.getProfiler().push("food");
            PlayerEntity player = (PlayerEntity) this.minecraft.getCameraEntity();
            RenderSystem.enableBlend();
            int left = width / 2 + 91;
            int top = height - right_height;
            right_height += 10;

            if (this.minecraft.player == null) return;
            FoodStats stats = this.minecraft.player.getFoodData();//获得玩家的饱食状态
            int level = stats.getFoodLevel();
            int maxLevel = (int) stats.getClass().getMethod("getMaxFoodLevel").invoke(stats);

            if (maxLevel >= 1000) maxLevel = 1000;//设置最大渲染数，避免卡顿
            for (int num = (int) Math.ceil(maxLevel / 2.0); num > 0; num -= 10) {//10个图标一组
                for (int i = 0; i < (num - 10 > 0 ? 10 : num); ++i) {//判断是否不满一组10个
                    int idx = (i + (((int) Math.ceil(maxLevel / 2.0)) - num)) * 2 + 1;//用于后面计算是画整只还是半只鸡腿的图标
                    int x = left - i * 8 - 9;
                    int y = top;
                    int icon = 16;
                    byte background = 0;

                    if (this.minecraft.player.hasEffect(Effects.HUNGER)) {
                        icon += 36;
                        background = 13;
                    }

                    if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (level * 3 + 1) == 0) {
                        y = top + (this.random.nextInt(3) - 1);
                    }

                    blit(mStack, x, y, 16 + background * 9, 27, 9, 9);//画空鸡腿的图标

                    if (idx < level)//画整个鸡腿的图标
                        blit(mStack, x, y, icon + 36, 27, 9, 9);
                    else if (idx == level)//画半只鸡腿的图标
                        blit(mStack, x, y, icon + 45, 27, 9, 9);
                }
                top -= 9;//往上一排排渲染图标
            }

            RenderSystem.disableBlend();
            this.minecraft.getProfiler().pop();
            this.post(ElementType.FOOD, mStack);
        }
    }
}
