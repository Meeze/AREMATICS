package com.arematics.minecraft.core.utils;

import com.arematics.minecraft.core.Boots;
import com.arematics.minecraft.core.CoreBoot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ArematicsExecutor {

    public static void runAsync(Runnable runnable){
        new BukkitRunnable(){
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLaterAsynchronously(Boots.getBoot(CoreBoot.class), 1);
    }

    public static String awaitResult(BiConsumer<AtomicReference<String>, CountDownLatch> consumer) throws InterruptedException {
        AtomicReference<String> res = new AtomicReference<>("");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        consumer.accept(res, countDownLatch);
        countDownLatch.await();
        return res.get();
    }

    public static synchronized BukkitTask syncRun(Runnable runnable){
        return new BukkitRunnable(){
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(Boots.getBoot(CoreBoot.class), 2);
    }

    public static BukkitTask asyncDelayed(Runnable runnable, long time, TimeUnit unit){
        return new BukkitRunnable(){
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLaterAsynchronously(Boots.getBoot(CoreBoot.class), TimeUtils.toTicks(time, unit));
    }

    public static BukkitTask syncDelayed(Runnable runnable, long time, TimeUnit unit){
        return new BukkitRunnable(){
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(Boots.getBoot(CoreBoot.class), TimeUtils.toTicks(time, unit));
    }

    public static void syncRepeat(Runnable runnable, long delay, long period, TimeUnit unit){
        delay = TimeUtils.toTicks(delay, unit);
        period = TimeUtils.toTicks(period, unit);
        new BukkitRunnable(){
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskTimer(Boots.getBoot(CoreBoot.class), delay, period);
    }

    public static void syncRepeat(Consumer<Integer> run, long delay, long period, TimeUnit unit, int times){
        delay = TimeUtils.toTicks(delay, unit);
        period = TimeUtils.toTicks(period, unit);
        new BukkitRunnable(){
            int amount = times;
            @Override
            public void run() {
                run.accept(amount);
                amount--;
                if(amount < 0) this.cancel();
            }
        }.runTaskTimer(Boots.getBoot(CoreBoot.class), delay, period);
    }

    public static void asyncRepeat(Runnable runnable, long delay, long period, TimeUnit unit){
        delay = TimeUtils.toTicks(delay, unit);
        period = TimeUtils.toTicks(period, unit);
        new BukkitRunnable(){
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskTimerAsynchronously(Boots.getBoot(CoreBoot.class), delay, period);
    }

    public static BukkitTask asyncRepeat(Consumer<Integer> run, long delay, long period, TimeUnit unit, int times){
        delay = TimeUtils.toTicks(delay, unit);
        period = TimeUtils.toTicks(period, unit);
        return new BukkitRunnable(){
            int amount = times;
            @Override
            public void run() {
                run.accept(amount);
                amount--;
                if(amount < 0) this.cancel();
            }
        }.runTaskTimerAsynchronously(Boots.getBoot(CoreBoot.class), delay, period);
    }
}
