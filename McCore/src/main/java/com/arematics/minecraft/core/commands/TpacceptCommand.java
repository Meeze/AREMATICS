package com.arematics.minecraft.core.commands;

import com.arematics.minecraft.core.annotations.SubCommand;
import com.arematics.minecraft.core.command.CoreCommand;
import com.arematics.minecraft.core.server.CorePlayer;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class TpacceptCommand extends CoreCommand {

    private final TpaCommand tpaCommand;

    @Autowired
    public TpacceptCommand(TpaCommand tpaCommand) {
        super("tpaccept");
        this.tpaCommand = tpaCommand;
    }


    @SubCommand("{player}")
    public void onAccept(CorePlayer sender, CorePlayer player) {
        getTpaCommand().acceptTpa(sender, player);
    }
}
