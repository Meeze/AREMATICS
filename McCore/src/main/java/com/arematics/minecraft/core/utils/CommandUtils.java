package com.arematics.minecraft.core.utils;

public class CommandUtils {

    public static String prettyReplace(String key, String keyValue){
        return "§a\n\n§7" + key + " » " + "§c" + keyValue + "\n" + "%value%";
    }
}
