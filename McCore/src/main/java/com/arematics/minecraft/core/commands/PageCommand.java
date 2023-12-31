package com.arematics.minecraft.core.commands;


import com.arematics.minecraft.core.annotations.SubCommand;
import com.arematics.minecraft.core.command.CoreCommand;
import com.arematics.minecraft.core.messaging.Messages;
import com.arematics.minecraft.core.messaging.advanced.Part;
import com.arematics.minecraft.core.messaging.injector.advanced.AdvancedMessageInjector;
import com.arematics.minecraft.core.pages.Pageable;
import com.arematics.minecraft.core.server.CorePlayer;
import org.bukkit.command.CommandSender;
import org.springframework.stereotype.Component;

@Component
public class PageCommand extends CoreCommand {

    public PageCommand() {
        super("page", "pager");
    }

    @Override
    public void onDefaultExecute(CommandSender sender){
        Messages.create("cmd_not_valid")
                .to(sender)
                .setInjector(AdvancedMessageInjector.class)
                .replace("cmd_usage", new Part("\n/page before\n/page next"))
                .handle();
    }

    @SubCommand("{type}")
    public boolean page(CorePlayer player, PageType type){
        return pageFor(player, type, null);
    }

    @SubCommand("{type} {key}")
    public boolean pageFor(CorePlayer player, PageType type, String key){
        Pageable pageable = key == null ? player.getPager().last() : player.getPager().fetch(key);
        if(pageable == null) return true;
        switch (type){
            case BEFORE:
                if(pageable.hasBefore()) pageable.before();
                break;
            case NEXT:
                if(pageable.hasNext()) pageable.next();
                break;
        }
        return true;
    }

    public enum PageType{
        BEFORE(),
        NEXT();
    }
}
