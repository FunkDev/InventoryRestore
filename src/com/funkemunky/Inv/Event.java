package com.funkemunky.Inv;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import mkremins.fanciful.FancyMessage;

public class Event implements Listener {
	
	private Core plugin;
	public Event(Core plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity().getPlayer();
		 if(plugin.getConfig().getBoolean("DeathMessage") == true) {
			 for(Player player : Bukkit.getOnlinePlayers()){
				 
		            if(player.hasPermission("ir.msg")){
		               if(plugin.getPlayers().getBoolean(player.getUniqueId().toString() + ".Alerts") == true) {
		            	   new FancyMessage("[!] ")
			        		.color(ChatColor.RED)
			        		.then("The player " + player.getName() + "has died! ")
			        		.color(ChatColor.WHITE)
			        		.then("(Click to revive)")
			        		.color(ChatColor.GRAY)
			        		.style(ChatColor.ITALIC)
			        		.command("/invrestore " + player.getName()).send(player);
		               }
		            }
		 
		     }
		 }
		plugin.getPlayers().set(p.getUniqueId().toString() + ".Inv", p.getInventory().getContents());
		plugin.getPlayers().set(p.getUniqueId().toString() + ".Armor", p.getInventory().getArmorContents());
		plugin.savePlayers();
	}

}
