package eu.ladeira.chromo.endlock;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class CmdEndLock implements CommandExecutor {
	
	EndLockModule module;
	
	public CmdEndLock(final EndLockModule module, Plugin plugin) {
		this.module = module;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (module.isEndLocked()) {
					announceEndInfo();
				}
			}
		}.runTaskTimer(plugin, 3600 * 20, 3600 * 20);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			System.out.println("Only players can run this command");
		}

		Player player = (Player) sender;

		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "ERROR: Specify a command");
			return false;
		}

		String rootCmd = args[0];

		switch (rootCmd) {
		case "open":
			openIn(player, args);
			break;
		case "set":
			openInSet(player, args);
			break;
		case "teleport":
			teleport(player);
			break;
		case "location":
			setLocation(player);
			break;
		case "lock":
			lock(player);
			break;
		case "unlock":
			unlock(player);
			break;
		default:
			player.sendMessage(ChatColor.RED + "ERROR: Unrecognized option " + ChatColor.WHITE + rootCmd);
			break;
		}

		return false;
	}

	private void openIn(Player player, String[] args) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permission");
			return;
		}

		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid time specified");
			return;
		}

		if (module.getEndLocation() == null) {
			player.sendMessage(ChatColor.RED + "ERROR: End Location not set");
			return;
		}

		int time = 0;

		try {
			time = Integer.parseInt(args[1]);
		} catch (Exception e) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid time specified");
			return;
		}

		Date date = new Date(System.currentTimeMillis() + (time * 1000));
		module.setEndLock(date);
		player.sendMessage(ChatColor.GRAY + "Set the end lock time");
		announceEndInfo();
	}

	private void openInSet(Player player, String[] args) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permission");
			return;
		}

		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid time specified");
			return;
		}

		if (module.getEndLocation() == null) {
			player.sendMessage(ChatColor.RED + "ERROR: End Location not set");
			return;
		}

		long time = 0;

		String arg = "";

		for (int i = 1; i < args.length; i++) {
			arg += args[i];
			if (!(i >= args.length - 1)) {
				arg += " ";
			}
		}

		System.out.println(arg);

		try {
			SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			time = dtf.parse(arg).getTime();
		} catch (Exception e) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid time specified");
			return;
		}

		Date date = new Date(time);
		module.setEndLock(date);
		player.sendMessage(ChatColor.GRAY + "Set the end lock time");
		announceEndInfo();
	}

	private void teleport(Player player) {
		if (!module.isEndLocked() && !module.isEndPvpEnabled()) {
			player.eject();
			player.teleport(module.getEndLocation());
			player.sendMessage(ChatColor.GRAY + "Teleported to the end");
		} else {
			player.sendMessage(ChatColor.RED + "ERROR: End teleport is disabled");
		}
	}

	private void setLocation(Player player) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permission");
			return;
		}

		module.setEndLocation(player.getLocation());
		player.sendMessage(ChatColor.GRAY + "Set end location");
	}

	private void lock(Player player) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "Missing permission");
			return;
		}

		if (module.isHardLocked()) {
			player.sendMessage(ChatColor.RED + "ERROR: End already hard locked");
			return;
		}

		module.setHardLocked(true);
	}

	private void unlock(Player player) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permission");
			return;
		}

		if (!module.isHardLocked()) {
			player.sendMessage(ChatColor.RED + "ERROR: End not hard locked");
			return;
		}

		module.setHardLocked(false);
	}

	public void announceEndInfo() {
		if (!module.isHardLocked()) {
			Bukkit.broadcastMessage(ChatColor.GRAY + "--- " + ChatColor.WHITE + "PSA" + ChatColor.GRAY + " ---");
			Bukkit.broadcastMessage(
					ChatColor.WHITE + "  The end will be unlocked at " + ChatColor.WHITE + module.getEndUnlockText());
			Bukkit.broadcastMessage(ChatColor.GRAY + "----------");
		}
	}
}