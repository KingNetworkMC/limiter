package ru.alexeylesin.kinglimiter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Main extends JavaPlugin implements Listener {
  static Plugin plugin;
  
  public static HashMap<Player, Location> playerFirstWELocation = new HashMap<>();
  
  public static HashMap<Player, Location> playerSecondWELocation = new HashMap<>();
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (sender instanceof Player) {
      Player pl = (Player)sender;
      if (!pl.hasPermission("kinglimiter.use")) {
        pl.sendMessage("§6KingLimiter §8| §fУ вас недостаточно прав.");
        return false;
      } 
      if (cmd.getName().equalsIgnoreCase("kinglimiter")) {
        if (args.length == 0) {
          pl.sendMessage("§6KingLimiter §8| §fСписок команд плагина:");
          pl.sendMessage("§6* §fДобавить предмет, запрещённый в творческом режиме - §e/klt addgm");
          pl.sendMessage("§6* §fУстановить лимит блоков для команды - §e/klt blimit название группы команда лимит");
          pl.sendMessage("§6* §fУстановить лимит для аргумента - §e/klt alimit название_группы команда номер_аргумента лимит");
        } 
        if (args.length == 1)
          if (args[0].equalsIgnoreCase("addgm")) {
            List<Integer> alreadyForb = getConfig().getIntegerList("gm-forbidden");
            alreadyForb.add(Integer.valueOf(pl.getItemInHand().getTypeId()));
            getConfig().set("gm-forbidden", alreadyForb);
            saveConfig();
            pl.sendMessage("§6KingLimiter §8| §fПредмет §aуспешно §fдобавлен.");
          }  
        if (args.length == 4)
          if (args[0].equalsIgnoreCase("blimit")) {
            getConfig().set("block-limit." + args[1] + "." + args[2], Integer.valueOf(Integer.parseInt(args[3])));
            pl.sendMessage("§6KingLimiter §8| §fДля группы §a" + args[1] + " §fустановлен лимит в §a" + args[3] + " §fблоков для команды §a" + args[2] + "§f.");
            saveConfig();
          }  
        if (args.length == 5)
          if (args[0].equalsIgnoreCase("alimit")) {
            getConfig().set("argument-limit." + args[1] + "." + args[2] + "." + args[3], Integer.valueOf(Integer.parseInt(args[4])));
            List<String> limitedCommands = getConfig().getStringList("argument-limit." + args[1] + ".commands-list");
            if (!limitedCommands.contains(args[2]))
              limitedCommands.add(args[2]); 
            getConfig().set("argument-limit." + args[1] + ".commands-list", limitedCommands);
            List<Integer> limitedArgs = getConfig().getIntegerList("argument-limit." + args[1] + "." + args[2] + ".limited");
            if (limitedArgs != null) {
              limitedArgs.add(Integer.valueOf(Integer.parseInt(args[3])));
              getConfig().set("argument-limit." + args[1] + "." + args[2] + ".limited", limitedArgs);
            } else {
              List<Integer> limitedArgs2 = new ArrayList<>();
              limitedArgs2.add(Integer.valueOf(Integer.parseInt(args[3])));
              getConfig().set("argument-limit." + args[1] + "." + args[2] + ".limited", limitedArgs2);
            } 
            pl.sendMessage("§6KingLimiter §8| §fДля группы §a" + args[1] + " §fустановлен лимит для аргумента §a" + args[3] + " §fкоманды §a" + args[2] + " §fв §a" + args[4] + "§f.");
            saveConfig();
          }  
      } 
    } 
    return false;
  }
  
  @EventHandler
  public void onGmLimit(PlayerInteractEvent e) {
    if (e.getPlayer().hasPermission("kinglimiter.ignore"))
      return; 
    if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
      try {
        List<Integer> forbInGm = getConfig().getIntegerList("gm-forbidden");
        for (Integer typeId : forbInGm) {
          if (e.getPlayer().getItemInHand().getTypeId() == typeId.intValue()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§6KingLimiter §8| §fДанный предмет запрещён в творческом режиме.");
          } 
        } 
      } catch (NullPointerException nullPointerException) {} 
  }
  
  @EventHandler
  public void onArgumentLimitCommand(PlayerCommandPreprocessEvent e) {
    String[] split = e.getMessage().split(" ");
    String command = split[0];
    PermissionGroup[] playerLimitsGroups = PermissionsEx.getUser(e.getPlayer()).getGroups();
    PermissionGroup permGroup = null;
    byte b;
    int i;
    PermissionGroup[] arrayOfPermissionGroup1;
    for (i = (arrayOfPermissionGroup1 = playerLimitsGroups).length, b = 0; b < i; ) {
      PermissionGroup group = arrayOfPermissionGroup1[b];
      permGroup = group;
      b++;
    } 
    String permGroupName = permGroup.getName();
    if (getConfig().getStringList("argument-limit." + permGroupName + ".commands-list").contains(command)) {
      List<Integer> limitedArgs = getConfig().getIntegerList("argument-limit." + permGroupName + "." + command + ".limited");
      for (Integer argsNumber : limitedArgs) {
        Integer limitOnThis = Integer.valueOf(getConfig().getInt("argument-limit." + permGroupName + "." + command + "." + argsNumber));
        try {
          Integer arg = Integer.valueOf(Integer.parseInt(split[argsNumber.intValue()]));
          if (arg.intValue() > limitOnThis.intValue()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§6KingLimiter §8| §fВы превысили лимит на аргумент §a" + argsNumber + " §fв команде §a" + command + " §fдля группы §a" + permGroupName + "§f.");
            e.getPlayer().sendMessage("§6KingLimiter §8| §fВаш лимит: " + limitOnThis);
            return;
          } 
        } catch (NumberFormatException numberFormatException) {}
      } 
    } 
  }
  
  @EventHandler
  public void onBlockLimitCommand(PlayerCommandPreprocessEvent e) {
    String[] split = e.getMessage().split(" ");
    PermissionGroup[] playerLimitsGroups = PermissionsEx.getUser(e.getPlayer()).getGroups();
    PermissionGroup permGroup = null;
    byte b;
    int i;
    PermissionGroup[] arrayOfPermissionGroup1;
    for (i = (arrayOfPermissionGroup1 = playerLimitsGroups).length, b = 0; b < i; ) {
      PermissionGroup group = arrayOfPermissionGroup1[b];
      permGroup = group;
      b++;
    } 
    String permGroupName = permGroup.getName();
    if (getConfig().getInt("block-limit." + permGroupName + "." + split[0]) != 0) {
      Integer limit = Integer.valueOf(getConfig().getInt("block-limit." + permGroupName + "." + split[0]));
      try {
        Location firstPos = playerFirstWELocation.get(e.getPlayer());
        Location secPos = playerSecondWELocation.get(e.getPlayer());
        Integer pos1x = Integer.valueOf(firstPos.getBlockX());
        Integer pos1y = Integer.valueOf(firstPos.getBlockY());
        Integer pos1z = Integer.valueOf(firstPos.getBlockZ());
        Integer pos2x = Integer.valueOf(secPos.getBlockX());
        Integer pos2y = Integer.valueOf(secPos.getBlockY());
        Integer pos2z = Integer.valueOf(secPos.getBlockZ());
        Integer changer = Integer.valueOf(0);
        if (pos1x.intValue() > pos2x.intValue()) {
          changer = pos1x;
          pos1x = pos2x;
          pos2x = changer;
        } 
        if (pos1y.intValue() > pos2y.intValue()) {
          changer = pos1y;
          pos1y = pos2y;
          pos2y = changer;
        } 
        if (pos1z.intValue() > pos2z.intValue()) {
          changer = pos1z;
          pos1z = pos2z;
          pos2z = changer;
        } 
        Integer counter = Integer.valueOf(0);
        for (int j = pos1x.intValue(); j <= pos2x.intValue(); j++) {
          for (int k = pos1y.intValue(); k <= pos2y.intValue(); k++) {
            for (int m = pos1z.intValue(); m <= pos2z.intValue(); m++)
              counter = Integer.valueOf(counter.intValue() + 1); 
          } 
        } 
        if (counter.intValue() > limit.intValue()) {
          e.setCancelled(true);
          e.getPlayer().sendMessage(ChatColor.RED + "§6KingLimiter §8| §fВы превысили лимит блоков для группы §c" + permGroupName + "§f.");
        } 
      } catch (NullPointerException nullPointerException) {}
    } 
  }
  
  public void onEnable() {
    Bukkit.getLogger().info("[KingLimiter] Плагин включён.");
    Bukkit.getPluginManager().registerEvents(this, (Plugin)this);
    plugin = (Plugin)this;
    if (!(new File(getDataFolder(), "config.yml")).exists()) {
      saveDefaultConfig();
      getConfig().set("block-limit.admin.//set", Integer.valueOf(1000));
      List<Integer> gmforbidden = new ArrayList<>();
      gmforbidden.add(Integer.valueOf(1));
      getConfig().set("gm-forbidden", gmforbidden);
      List<String> argumentforb = new ArrayList<>();
      argumentforb.add("//sphere");
      getConfig().set("argument-limit.default.commands-list", argumentforb);
      List<Integer> limitedArgs = new ArrayList<>();
      limitedArgs.add(Integer.valueOf(1));
      getConfig().set("argument-limit.default.//sphere.limited", limitedArgs);
      getConfig().set("argument-limit.default.//sphere.1", Integer.valueOf(20));
      saveConfig();
    } 
  }
  
  @EventHandler
  public void onWoodAxeInteract(PlayerInteractEvent e) {
    if (e.getPlayer().getItemInHand().getType() == Material.WOOD_AXE)
      try {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
          playerSecondWELocation.put(e.getPlayer(), e.getClickedBlock().getLocation());
        } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
          playerFirstWELocation.put(e.getPlayer(), e.getClickedBlock().getLocation());
        } 
      } catch (NullPointerException nullPointerException) {} 
  }
  
  @EventHandler
  public void onCommandSelection(PlayerCommandPreprocessEvent e) {
    if (e.getMessage().equalsIgnoreCase("//pos1")) {
      playerFirstWELocation.put(e.getPlayer(), e.getPlayer().getLocation());
    } else if (e.getMessage().equalsIgnoreCase("//pos2")) {
      playerSecondWELocation.put(e.getPlayer(), e.getPlayer().getLocation());
    } 
  }
}
