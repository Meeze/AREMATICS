package com.arematics.minecraft.core.permissions;

import com.arematics.minecraft.core.Boots;
import com.arematics.minecraft.core.CoreBoot;
import com.arematics.minecraft.data.share.model.Permission;
import com.arematics.minecraft.core.data.share.model.User;
import com.arematics.minecraft.core.data.share.service.UserService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public class Permissions {

    public static boolean isNotAllowed(CommandSender sender, String permission){
        if(sender instanceof Player) {
            UserService service = Boots.getBoot(CoreBoot.class).getContext().getBean(UserService.class);
            User user = service.getOrCreateUser(((Player)sender).getUniqueId());
            return !hasPermission(user, permission);
        }
        return true;
    }

    public static boolean hasPermission(User user, String permission){
        return user.getUserPermissions().stream().anyMatch(hasPerm(permission)) ||
                user.getRank().getPermissions().stream().anyMatch(hasPerm(permission));
    }

    private static Predicate<? super Permission> hasPerm(String permission){
        return perm -> perm.getPermission().equals(permission) ||
                perm.getPermission().equals(permission.split("\\.")[0]);
    }
}
