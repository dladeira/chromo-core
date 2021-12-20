package eu.ladeira.chromo;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Logger {

	public static void sendError(Player player, String message) {
		player.sendMessage(ChatColor.RED + "ERROR: " + message);
	}
	
	public static void sendError(String message) {
		Bukkit.getLogger().log(Level.INFO, ChatColor.RED + "ERROR: " + message);
	}
	
	public static void sendInfo(Player player, String message) {
		player.sendMessage(ChatColor.GRAY + "INFO: " + message);
	}
	
	public static void sendInfo(String message) {
		Bukkit.getLogger().log(Level.INFO, ChatColor.GRAY + "INFO: " + message);
	}
}
