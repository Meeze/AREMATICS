package com.arematics.minecraft.core.server;

import com.arematics.minecraft.core.Boots;
import com.arematics.minecraft.core.CoreBoot;
import com.arematics.minecraft.core.chat.controller.ChatThemeController;
import com.arematics.minecraft.core.currency.Currency;
import com.arematics.minecraft.core.items.CoreItem;
import com.arematics.minecraft.core.messaging.MessageInjector;
import com.arematics.minecraft.core.messaging.Messages;
import com.arematics.minecraft.core.pages.Pager;
import com.arematics.minecraft.core.scoreboard.functions.BoardSet;
import com.arematics.minecraft.core.utils.ArematicsExecutor;
import com.arematics.minecraft.core.utils.Inventories;
import com.arematics.minecraft.data.global.model.ChatTheme;
import com.arematics.minecraft.data.global.model.User;
import com.arematics.minecraft.data.mode.model.GameStats;
import com.arematics.minecraft.data.service.GameStatsService;
import com.arematics.minecraft.data.service.InventoryService;
import com.arematics.minecraft.data.service.OnlineTimeService;
import com.arematics.minecraft.data.service.UserService;
import com.arematics.minecraft.data.share.model.OnlineTime;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import lombok.Data;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Data
public class CorePlayer{
    private static Map<UUID, CorePlayer> players = new HashMap<>();

    public static CorePlayer get(Player player){
        if(!players.containsKey(player.getUniqueId()))
            players.put(player.getUniqueId(), new CorePlayer(player));
        return players.get(player.getUniqueId());
    }

    public static void invalidate(Player player){
        if(players.containsKey(player.getUniqueId())) players.get(player.getUniqueId()).unload();
        players.remove(player.getUniqueId());
    }

    public static void unload(Player player){
        players.remove(player.getUniqueId()).unload();
    }

    private final Player player;
    private final Map<Currency, Double> currencies = new HashMap<>();
    private final Pager pager;
    private final BoardSet boardSet;
    private final PlayerRequestSettings requestSettings;
    private boolean ignoreMeta = false;
    private boolean disableLowerInventory = false;
    private boolean disableUpperInventory = false;

    private boolean inFight = false;
    private BukkitTask inFightTask;
    private BukkitTask inTeleport;

    private final GameStatsService service;
    private final UserService userService;
    private final ChatThemeController chatThemeController;
    private final OnlineTimeService onlineTimeService;

    private LocalDateTime lastPatch = null;

    public CorePlayer(Player player){
        this.player = player;
        this.chatThemeController = Boots.getBoot(CoreBoot.class).getContext().getBean(ChatThemeController.class);
        this.pager = new Pager(this);
        this.boardSet = new BoardSet(player);
        this.userService = Boots.getBoot(CoreBoot.class).getContext().getBean(UserService.class);
        this.onlineTimeService = Boots.getBoot(CoreBoot.class).getContext().getBean(OnlineTimeService.class);
        this.requestSettings = new PlayerRequestSettings(this);
        this.service = Boots.getBoot(CoreBoot.class).getContext().getBean(GameStatsService.class);
    }

    private void unload() {
        this.pager.unload();
        this.boardSet.remove();
    }

    public void setInFight(){
        this.inFight = true;
        if(inFightTask != null) inFightTask.cancel();
        this.inFightTask = ArematicsExecutor.asyncDelayed(this::fightEnd, 3, TimeUnit.SECONDS);
    }

    public void fightEnd(){
        if(inFight) this.info("Could log out now").handle();
        this.inFight = false;
    }

    public void patchOnlineTime(){
        if(lastPatch == null) lastPatch = getUser().getLastJoin().toLocalDateTime();
        Duration online = Duration.between(this.lastPatch, LocalDateTime.now());
        patchOnlineTime(online, false);
        patchOnlineTime(online, true);
        lastPatch = LocalDateTime.now();
    }

    public void removeAmountFromHand(int amount){
        ArematicsExecutor.syncRun(() -> syncRemoveFromHand(amount));
    }

    private void syncRemoveFromHand(int amount){
        if(getItemInHand() != null)
            if(!player.getGameMode().equals(GameMode.CREATIVE)){
                int am = player.getItemInHand().getAmount();
                if(amount >= am)
                    player.setItemInHand(new ItemStack(Material.AIR));
                else
                    player.getItemInHand().setAmount(am - amount);
            }
    }

    public void patchOnlineTime(Duration duration, boolean mode){
        OnlineTime time;
        try{
            time = this.onlineTimeService.findByUUID(mode, getUUID());
        }catch (RuntimeException re){
            time = new OnlineTime(getUUID(), 0L, 0L);
        }
        time.setTime(time.getTime() + duration.toMillis());
        this.onlineTimeService.put(mode, time);
    }

    public boolean hasEffect(PotionEffectType type) {
        return this.getPlayer().getActivePotionEffects().stream()
                .filter(effect -> effect.getType() == type)
                .count() >= 1;
    }

    public void equip(CoreItem... items){
        ArematicsExecutor.runAsync(() -> this.equipItems(items));
    }

    private void equipItems(CoreItem... items){
        CoreItem[] drop = noUse(items);
        if(drop.length > 0){
            this.warn("" + drop.length + " items have been dropped").handle();
            Arrays.stream(drop).forEach(this::dropItem);
        }
    }

    public void dropItem(CoreItem drop){
        ArematicsExecutor.syncRun(() -> this.getLocation().getWorld().dropItemNaturally(this.getLocation(), drop));
    }

    private CoreItem[] noUse(CoreItem... item){
        return Arrays.stream(item)
                .filter(this::equipArmor)
                .toArray(CoreItem[]::new);
    }

    private boolean equipArmor(CoreItem item) {
        if(hasEffect(PotionEffectType.INVISIBILITY)) return true;
        if(item.isArmor()){

        }
        return true;
    }

    public void stopTeleport(){
        if(inTeleport != null){
            inTeleport.cancel();
            warn("Your teleport has been cancelled").handle();
            inTeleport = null;
        }
    }

    public void instantTeleport(Location location){
        ArematicsExecutor.syncRun(() -> this.getPlayer().teleport(location));
    }

    public void teleport(Location location){
        if(inTeleport != null){
            inTeleport.cancel();
            warn("Old teleport request cancelled").handle();
        }
        inTeleport = ArematicsExecutor.asyncRepeat((count) -> teleport(count, location),
                0, 1, TimeUnit.SECONDS, getUser().getRank().isInTeam() ? 0 : 3);
    }

    private void teleport(int count, Location location){
        if (count == 0) {
            ArematicsExecutor.syncRun(() -> this.getPlayer().teleport(location));
            inTeleport = null;
        } else {
            this.info("%prefix%Teleport in %seconds%§7...")
                    .DEFAULT()
                    .replace("prefix", "   §cTP » §7")
                    .replace("seconds", "§c" + count)
                    .disableServerPrefix()
                    .handle();
        }
    }

    public boolean isFlagEnabled(RegionQuery query, StateFlag flag){
        return query.testState(this.getLocation(), WorldGuardPlugin.inst().wrapPlayer(this.getPlayer()), flag);
    }

    public InventoryView getView(){
        return player.getOpenInventory();
    }

    public void openInventory(Inventory inventory){
        Inventories.openLowerDisabledInventory(inventory, this);
    }

    public void openTotalBlockedInventory(Inventory inventory){
        Inventories.openTotalBlockedInventory(inventory, this);
    }

    public void openLowerEnabledInventory(Inventory inventory){
        Inventories.openInventory(inventory, this);
    }

    public User getUser(){
        return this.userService.getOrCreateUser(this);
    }

    UserService getUserService(){
        return this.userService;
    }

    public void update(User user){
        this.userService.update(user);
    }

    public void addKarma(int amount){
        User user = getUser();
        user.setKarma(user.getKarma() + amount);
        update(user);
    }

    public void removeKarma(int amount){
        User user = getUser();
        user.setKarma(user.getKarma() - amount);
        update(user);
    }

    public PlayerRequestSettings getRequestSettings(){
        return this.requestSettings;
    }

    public MessageInjector info(String msg){
        return Messages.create(msg)
                .to(this.getPlayer());
    }

    public MessageInjector warn(String msg){
        return Messages.create(msg)
                .WARNING()
                .to(this.getPlayer());
    }

    public MessageInjector failure(String msg){
        return Messages.create(msg)
                .FAILURE()
                .to(this.getPlayer());
    }

    public BoardSet getBoard(){
        return this.boardSet;
    }

    public GameStats getStats(){
        return this.service.getOrCreate(getUUID());
    }

    private void saveStats(GameStats stats){
        this.service.save(stats);
    }

    public void cleanStats(){
        this.service.delete(getStats());
    }

    public void onStats(Consumer<GameStats> execute){
        GameStats stats = getStats();
        execute.accept(stats);
        saveStats(stats);
    }

    public void setKills(int kills){
        onStats(stats -> stats.setKills(kills));
        getBoard().getOrAddBoard("main", "§bSoulPvP")
                .setEntrySuffix("Kills", "§7" + this.getStats().getKills())
                .refresh();
    }

    public void addKill(){
        onStats(stats -> stats.setKills(stats.getKills() + 1));
        getBoard().getOrAddBoard("main", "§bSoulPvP")
                .setEntrySuffix("Kills", "§7" + this.getStats().getKills())
                .refresh();
    }

    public void setDeaths(int deaths){
        onStats(stats -> stats.setDeaths(deaths));
        onStats(stats -> stats.setDeaths(stats.getDeaths() + 1));
        getBoard().getOrAddBoard("main", "§bSoulPvP")
                .setEntrySuffix("Deaths", "§7" + this.getStats().getDeaths())
                .refresh();
    }

    public void addDeath(){
        onStats(stats -> stats.setDeaths(stats.getDeaths() + 1));
        getBoard().getOrAddBoard("main", "§bSoulPvP")
                .setEntrySuffix("Deaths", "§7" + this.getStats().getDeaths())
                .refresh();
    }

    public void setMoney(long money){
        onStats(stats -> stats.setCoins(money));
        getBoard().getOrAddBoard("main", "§bSoulPvP")
                .setEntrySuffix("Coins", "§7" + this.getStats().getCoins())
                .refresh();
    }

    public void addMoney(long amount){
        onStats(stats -> stats.setCoins(stats.getCoins() + amount));
        getBoard().getOrAddBoard("main", "§bSoulPvP")
                .setEntrySuffix("Coins", "§7" + this.getStats().getCoins())
                .refresh();
    }

    public void setBounty(int bounty){
        onStats(stats -> stats.setBounty(bounty));
    }

    public UUID getUUID(){
        return this.player.getUniqueId();
    }

    public CoreItem getItemInHand(){
        return CoreItem.create(player.getItemInHand());
    }

    public Inventory getInventory(InventoryService service, String key) throws RuntimeException{
        return service.getInventory(player.getUniqueId() + "." + key);
    }

    public Inventory getOrCreateInventory(InventoryService service, String key, String title, byte slots){
        return service.getOrCreate(player.getUniqueId() + "." + key, title, slots);
    }

    public Location getLocation() {
        return this.player.getLocation();
    }

    /**
     * sets active theme for chatthemeuser and adds to senders chattheme
     *
     * @param theme which is activated
     */
    public void setTheme(ChatTheme theme) {
        User user = userService.getUserByUUID(getUUID());
        ChatTheme old = chatThemeController.getTheme(user.getActiveTheme().getThemeKey());
        old.getActiveUsers().remove(user);
        user.setActiveTheme(theme);
        theme.getActiveUsers().add(user);
    }
}
