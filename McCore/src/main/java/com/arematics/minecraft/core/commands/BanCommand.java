package com.arematics.minecraft.core.commands;

import com.arematics.minecraft.core.annotations.Perm;
import com.arematics.minecraft.core.annotations.SubCommand;
import com.arematics.minecraft.core.command.CoreCommand;
import com.arematics.minecraft.core.server.CorePlayer;
import com.arematics.minecraft.core.utils.ArematicsExecutor;
import com.arematics.minecraft.data.global.model.Ban;
import com.arematics.minecraft.data.global.model.User;
import com.arematics.minecraft.data.service.BanService;
import com.arematics.minecraft.data.service.UserService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
@Perm(permission = "punishment-ban", description = "Permission to ban a player permanent")
public class BanCommand extends CoreCommand {
    private static final int KARMA_POINTS = 100;

    private final BanService service;
    private final UserService userService;

    @Autowired
    public BanCommand(BanService banService, UserService userService){
        super("ban");
        registerLongArgument("reason");
        this.service = banService;
        this.userService = userService;
    }

    @SubCommand("{player} {reason}")
    public void banPlayer(CorePlayer player, User target, String reason) {
        if(target.getRank().isInTeam()){
            player.warn("Player " + target.getLastName() + " could not be banned").handle();
            return;
        }
        Ban ban;
        try{
            ban = service.getBan(target.getUuid());
            if(ban.getBannedUntil() == null || ban.getBannedUntil().after(Timestamp.valueOf(LocalDateTime.now()))) {
                player.warn("Player " + target.getLastName() + " is currently banned").handle();
                return;
            }
            ban.setBannedUntil(null);
        }catch (RuntimeException re){
            ban = new Ban(target.getUuid(), player.getUUID(), reason, Timestamp.valueOf(LocalDateTime.now()), null);
        }
        Player targetPlayer = Bukkit.getPlayer(target.getUuid());
        Ban finalBan = ban;
        ArematicsExecutor.syncRun(() -> {
            if(targetPlayer != null) targetPlayer.kickPlayer("§cYou have been banned permanent \n§b" + finalBan.getReason());
        });
        player.info("Player " + target.getLastName() + " has been banned").handle();
        service.save(ban);
        target.setKarma(target.getKarma() - BanCommand.KARMA_POINTS);
        userService.update(target);
    }
}
