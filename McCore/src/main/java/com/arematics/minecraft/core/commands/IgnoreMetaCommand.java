package com.arematics.minecraft.core.commands;

import com.arematics.minecraft.core.annotations.Perm;
import com.arematics.minecraft.core.annotations.SubCommand;
import com.arematics.minecraft.core.command.CoreCommand;
import com.arematics.minecraft.core.language.LanguageAPI;
import com.arematics.minecraft.core.messaging.Messages;
import com.arematics.minecraft.core.server.CorePlayer;
import com.arematics.minecraft.core.utils.TitleAPI;
import org.springframework.stereotype.Component;

@Component
@Perm(permission = "ignore-meta", description = "Allowed to ignore item meta actions")
public class IgnoreMetaCommand extends CoreCommand {

    public static void setIgnoreMeta(CorePlayer player){
        player.setIgnoreMeta(true);
        TitleAPI.sendTitle(player.getPlayer(),
                LanguageAPI.prepareRawMessage(player.getPlayer(), "ignore_item_meta_enabled"),
                LanguageAPI.prepareRawMessage(player.getPlayer(), "ignore_item_meta_inventory_can_edit"),
                10, 20*5, 10);
        Messages.create("ignore_item_meta_enabled")
                .FAILURE()
                .to(player.getPlayer())
                .handle();
    }

    public static void unsetIgnoreMeta(CorePlayer player){
        player.setIgnoreMeta(false);
        Messages.create("ignore_item_meta_disabled")
                .to(player.getPlayer())
                .handle();
    }

    public IgnoreMetaCommand(){
        super("ignore-meta");
    }

    @SubCommand("toggle")
    public boolean toggleIgnoreMeta(CorePlayer player) {
        if(player.isIgnoreMeta()) disableIgnoreMeta(player);
        else enableIgnoreMeta(player);
        return true;
    }

    @SubCommand("enable")
    public boolean enableIgnoreMeta(CorePlayer player) {
        IgnoreMetaCommand.setIgnoreMeta(player);
        return true;
    }

    @SubCommand("disable")
    public boolean disableIgnoreMeta(CorePlayer player) {
        IgnoreMetaCommand.unsetIgnoreMeta(player);
        return true;
    }
}
