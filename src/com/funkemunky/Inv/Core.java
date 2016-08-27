package com.funkemunky.Inv;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Core extends JavaPlugin {
	
	private FileConfiguration Players = null;
	private File PlayersFile = null;
	
	public String prefix = getConfig().getString("Messages.Prefix").replaceAll("&", "§");
	public String noperm = getConfig().getString("Messages.NoPermission");
	public String restoreds = getConfig().getString("Messages.Restored.Sender");
	public String restoredp = getConfig().getString("Messages.Restored.Target");
	public String getAlerts(boolean setting) {
		if(setting == true) {
			return getConfig().getString("Messages.DeathAlerts").replaceAll("%SETTING%", "true");
		} else {
			return getConfig().getString("Messages.DeathAlerts").replaceAll("%SETTING%", "false");
		}
	}
	
	public void reloadPlayers() {
	    if (PlayersFile == null) {
	    PlayersFile = new File(getDataFolder(), "Players.yml");
	    }
	    Players = YamlConfiguration.loadConfiguration(PlayersFile);
	    Reader defConfigStream = null;
		try {
			defConfigStream = new InputStreamReader(this.getResource("Players.yml"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        Players.setDefaults(defConfig);
	    }
	}
	public FileConfiguration getPlayers() {
	    if (Players == null) {
	        reloadPlayers();
	    }
	    return Players;
	}
	public void savePlayers() {
	    if (Players == null || PlayersFile == null) {
	        return;
	    }
	    try {
	        getPlayers().save(PlayersFile);
	    } catch (IOException ex) {
	        getLogger().log(Level.SEVERE, "Could not save config to " + PlayersFile, ex);
	    }
	}
	
	public Set<String> getConfigKeys()
	{
		if (getPlayers().isConfigurationSection("Players."))
		{
			return getPlayers().getConfigurationSection("Players.").getKeys(false);
		}
		return new HashSet<String>();
	}
	
	public void onEnable() {
		
		File file = new File(getDataFolder() + "config.yml");
		
		if(!file.exists()) {
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
		}
		Bukkit.getPluginManager().registerEvents(new Event(this), this);
		 if (PlayersFile == null) {
		        PlayersFile = new File(getDataFolder(), "Players.yml");
		    }
		    if (!PlayersFile.exists()) {   
		    	getLogger().log(Level.WARNING, "No previous players file was found! Creating one now...");
		         saveResource("Players.yml", false);
		         getLogger().log(Level.INFO, "Players file was created!");
		     }
		 getLogger().log(Level.INFO, "InventoryRestore v" + getDescription().getVersion() + " has been enabled with no errors!");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("invrestore")) {
			if(sender instanceof Player) {
				if(p.hasPermission("ir.restore")) {
					if(args.length == 0) {
						p.sendMessage("");
						p.sendMessage("Running InventoryRestore v" + getDescription().getVersion() + " by funkemunky.");
						p.sendMessage("");
						p.sendMessage("Usage: /" + cmd.getName() + " <player>");
						p.sendMessage("");
					} else {
						Player online = Bukkit.getPlayerExact(args[0]);
						if(!(online == null)) {
							if(getPlayers().contains(p.getUniqueId().toString())) {
								Player target = Bukkit.getPlayer(args[0]);
								target.getInventory().clear();
								Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									public void run() {
										target.getInventory().setArmorContents((ItemStack[]) getPlayers().get(p.getUniqueId().toString() + ".Armor"));
										target.getInventory().setContents((ItemStack[]) getPlayers().get(p.getUniqueId().toString() + ".Inv"));
									p.sendMessage(restoreds.replaceAll("&", "§").replaceAll("%PLAYER%", target.getName()).replaceAll("%PREFIX%", prefix));
										target.sendMessage(restoredp.replaceAll("&", "§").replaceAll("%PLAYER%", p.getName()).replaceAll("%PREFIX%", prefix));
									}
								}, 5L);
							} else {
								p.sendMessage(ChatColor.RED + "That player hasn't died yet!");
							}
						} else {
							p.sendMessage(ChatColor.RED + "That player is not online!");
						}
					}
				} else {
					p.sendMessage(noperm.replaceAll("&", "§").replaceAll("%PREFIX%", prefix));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Only players are allowed to use this command!");
			}
		}
		if(cmd.getName().equalsIgnoreCase("irreload")) {
			if(sender instanceof Player) {
				if(sender.hasPermission("ir.reload")) {
					//Check for config
					File file = new File(getDataFolder() + "config.yml");
					if(!file.exists()) {
						getConfig().options().copyDefaults(true);
						saveDefaultConfig();
					}
					//Check for players file.
					if (PlayersFile == null) {
				        PlayersFile = new File(getDataFolder(), "Players.yml");
				    }
				    if (!PlayersFile.exists()) {   
				    	getLogger().log(Level.WARNING, "No previous players file was found! Creating one now...");
				         saveResource("Players.yml", false);
				     }
				    reloadConfig();
				    reloadPlayers();
				    sender.sendMessage(ChatColor.GRAY + "Reloaded InventoryRestore v" + getDescription().getVersion() + "!");
				} else {
					sender.sendMessage(noperm.replaceAll("&", "§").replaceAll("%PREFIX%", prefix));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Only players can use this command!");
			}
		}
		if(cmd.getName().equalsIgnoreCase("deathalerts")) {
			if(sender.hasPermission("ir.alerts")) {
				if(getPlayers().getBoolean(p.getUniqueId().toString() + ".Alerts") != true) {
					getPlayers().set(p.getUniqueId().toString() + ".Alerts", true);
					savePlayers();
					p.sendMessage(getAlerts(getPlayers().getBoolean(p.getUniqueId().toString() + ".Alerts")).replaceAll("&", "§").replaceAll("%PREFIX%", prefix));
				} else {
					getPlayers().set(p.getUniqueId().toString() + ".Alerts", false);
					savePlayers();
					p.sendMessage(getAlerts(getPlayers().getBoolean(p.getUniqueId().toString() + ".Alerts")).replaceAll("&", "§").replaceAll("%PREFIX%", prefix));
				}
			} else {
				p.sendMessage(noperm.replaceAll("&", "§").replaceAll("%PREFIX%", prefix));
			}
		}
		return false;
	}

}
