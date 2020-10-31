package com.arematics.minecraft.core.commands;

import com.arematics.minecraft.core.annotations.Default;
import com.arematics.minecraft.core.annotations.PluginCommand;
import com.arematics.minecraft.core.annotations.SubCommand;
import com.arematics.minecraft.core.chat.ChatAPI;
import com.arematics.minecraft.core.chat.controller.ChatThemeController;
import com.arematics.minecraft.core.command.CoreCommand;
import com.arematics.minecraft.data.global.model.ChatTheme;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

@PluginCommand(aliases = {"chattheme"})
@Component
public class ThemeCommand extends CoreCommand {

    private final ChatThemeController themeController = ChatAPI.getChatThemeController();

    public ThemeCommand() {
        super("theme");
    }

    @Override
    @Default
    public boolean onDefaultExecute(CommandSender sender) {
        sender.sendMessage("/theme list");
        sender.sendMessage("/theme [theme]");
        return true;
    }


    @SubCommand("list")
    public boolean list(Player player) {
        themeController.getThemes().values().forEach(theme -> {
            player.sendMessage(theme.getThemeKey());
        });
        return true;
    }

    @SubCommand("switch {theme}")
    public boolean switchCmd(Player player, String theme) {
        if (ChatAPI.setTheme(player, theme)) {
            player.sendMessage("theme gewechselt zu " + theme);
        } else {
            player.sendMessage("theme not found");
        }
        return true;
    }

    @SubCommand("inspect {theme}")
    public boolean inspect(Player player, String theme) {
        ChatTheme apiTheme = ChatAPI.getTheme(theme);
        player.sendMessage(apiTheme.getThemeKey());
        player.sendMessage(apiTheme.getFormat());
        apiTheme.getActiveUsers().forEach(user -> {
            player.sendMessage(user.getPlayerId().toString());
        });
        apiTheme.getThemePlaceholders().forEach(themePlaceholder -> {
            player.sendMessage("themeplaceholderkey: " + themePlaceholder.getPlaceholderKey() + " value: " + themePlaceholder.getValue());
            if (themePlaceholder.getClickAction() != null) {
                player.sendMessage("Click : " + themePlaceholder.getClickAction().getValue());
            }
            if (themePlaceholder.getHoverAction() != null) {
                player.sendMessage("Hover : " + themePlaceholder.getHoverAction().getValue());
            }
        });
        apiTheme.getGlobalPlaceholderActions().forEach(globalPlaceholderActions -> {
            player.sendMessage("dynamic key: " + globalPlaceholderActions.getPlaceholderKey() + " value: " + ChatAPI.getPlaceholder(globalPlaceholderActions.getPlaceholderKey()).getValues().get(player).get());
            if (globalPlaceholderActions.getClickAction() != null) {
                player.sendMessage("Click : " + globalPlaceholderActions.getClickAction().getValue());
            }
            if(globalPlaceholderActions.getHoverAction() != null) {
                player.sendMessage("Hover : " + globalPlaceholderActions.getHoverAction().getValue());
            }
        });
        return true;
    }


}
