package eu.ladeira.chromo.modules;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import eu.ladeira.chromo.Database;
import eu.ladeira.chromo.LadeiraModule;
import eu.ladeira.chromo.Logger;
import eu.ladeira.chromo.guilds.Guild;
import eu.ladeira.chromo.guilds.GuildModule;
import net.md_5.bungee.api.ChatColor;

@SuppressWarnings("deprecation")
public class AlertsModule extends LadeiraModule implements Listener {

	private Database db;

	public AlertsModule(Database db) {
		this.db = db;
	}

	@EventHandler
	public void broadcastPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Bukkit.broadcastMessage(ChatColor.WHITE + db.getName(player.getUniqueId()) + ChatColor.GRAY + " has joined");
		e.setJoinMessage("");
	}

	@EventHandler
	public void broadcastPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		Bukkit.broadcastMessage(ChatColor.WHITE + db.getName(player.getUniqueId()) + ChatColor.GRAY + " has left");
		e.setQuitMessage("");
	}

	@EventHandler
	public void displayPlayerInfoChat(PlayerChatEvent e) {
		Player player = e.getPlayer();
		int rep = db.getPlayerInt(player.getUniqueId(), "reputation");
		String repColor = ReputationModule.getReputationColor(rep);
		String name = db.getName(player.getUniqueId());

		String guild = "";

		Guild playerGuild = GuildModule.getGuild(player.getUniqueId());

		if (playerGuild != null) {
			guild = playerGuild.getName() + " ";
		}

		for (Player online : Bukkit.getOnlinePlayers()) {
			Guild onlineGuild = GuildModule.getGuild(online.getUniqueId());
			if (onlineGuild != null && guild.equals(onlineGuild.getName() + " ")) {
				online.sendMessage(ChatColor.GREEN + guild + repColor + "[" + rep + "] " + name + ChatColor.GRAY + ": " + e.getMessage());
			} else {
				online.sendMessage(ChatColor.RED + guild + repColor + "[" + rep + "] " + name + ChatColor.GRAY + ": " + e.getMessage());
			}
		}
		Logger.sendInfo(ChatColor.RED + guild + repColor + "[" + rep + "] " + name + ChatColor.GRAY + ": " + e.getMessage());

		e.setCancelled(true);
	}
}