package com.arematics.minecraft.core.permissions;

import com.arematics.minecraft.core.messaging.Messages;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PermissionData implements PermConsumer {
    private final CommandSender sender;
    private final String permission;
    private Consumer<CommandSender> permitted = CommandSender::getName;

    @Override
    public PermissionData ifPermitted(Consumer<CommandSender> permitted){
        this.permitted = permitted;
        return this;
    }

    public void orElse(Consumer<CommandSender> orElse){
        if(StringUtils.isBlank(permission) || Permissions.hasPermission(sender, permission))
            permitted.accept(sender);
        else orElse.accept(sender);
    }

    public void submit(){
        orElse(s -> Messages.create("cmd_noperms").WARNING().to(s).handle());
    }
}
