package eu.ladeira.core.modules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import eu.ladeira.core.Database;
import eu.ladeira.core.LadeiraCore;
import eu.ladeira.core.LadeiraModule;
import eu.ladeira.guilds.Guild;
import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("deprecation")
public class Alerts implements LadeiraModule, Listener {

	private Database db;
	
	public Alerts(Database db) {
		this.db = db;
	}
	
	@Override
	public void onDisable() {
	}

	@Override
	public String cmdName() {
		return null;
	}

	@EventHandler
	public void broadcastPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Bukkit.broadcastMessage(ChatColor.WHITE + player.getName() + ChatColor.GRAY + " has joined");
		e.setJoinMessage("");
	}
	
	@EventHandler
	public void broadcastPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		Bukkit.broadcastMessage(ChatColor.WHITE + player.getName() + ChatColor.GRAY + " has left");
		e.setQuitMessage("");
	}
	
	@EventHandler
	public void displayPlayerInfoChat(PlayerChatEvent e) {
		Player player = e.getPlayer();
		int rep = db.getPlayerInt(player.getUniqueId(), "reputation");
		String repColor = ReputationManager.getReputationColor(rep);
		String name = player.getName();
		
		if (LadeiraCore.hasExternalModule("LGuilds")) {
			String guild = "";
			
			Guild playerGuild = Guild.getGuild(player.getUniqueId());
			
			if (playerGuild != null) {
				guild = playerGuild.getName() + " ";
			}
			
			for (Player online : Bukkit.getOnlinePlayers()) {
				Guild onlineGuild = Guild.getGuild(online.getUniqueId());
				if (onlineGuild != null && guild.equals(onlineGuild.getName() + " ")) {
					online.sendMessage(ChatColor.GREEN + guild + repColor + "[" + rep + "] " + db.getName(player.getUniqueId()) + ChatColor.GRAY + ": " + e.getMessage());
				} else {
					online.sendMessage(ChatColor.RED + guild + repColor + "[" + rep + "] " + db.getName(player.getUniqueId()) + ChatColor.GRAY + ": " + e.getMessage());
				}
			}
		} else {
			Bukkit.broadcastMessage(repColor + "[" + rep + "] " + db.getName(player.getUniqueId()) + ChatColor.GRAY + ": " + e.getMessage());
		}

		e.setCancelled(true);
	}
}
