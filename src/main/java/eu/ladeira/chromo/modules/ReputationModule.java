package eu.ladeira.chromo.modules;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import eu.ladeira.chromo.Database;
import eu.ladeira.chromo.LadeiraModule;
import eu.ladeira.chromo.Logger;
import eu.ladeira.chromo.guilds.Guild;
import eu.ladeira.chromo.guilds.GuildModule;
import net.md_5.bungee.api.ChatColor;

public class ReputationModule extends LadeiraModule implements Listener, CommandExecutor {

	public static String getReputationColor(int reputation) {
		if (reputation >= 10) {
			return ChatColor.YELLOW + "";
		} else if (reputation >= 2) {
			return ChatColor.GREEN + "";
		}

		if (reputation <= -25) {
			return ChatColor.LIGHT_PURPLE + "";
		} else if (reputation <= -10) {
			return ChatColor.RED + "";
		}

		return ChatColor.GRAY + "";
	}

	private Database db;
	private HashMap<UUID, HashMap<UUID, Integer>> attackedList = new HashMap<>();
	private int combatTimer = 30;

	@Override
	public String getCmdName() {
		return "reputation";
	}
	
	@Override
	public CommandExecutor getExecutor() {
		return this;
	}
	
	public ReputationModule(Database db, Plugin plugin) {
		this.db = db;

		new BukkitRunnable() {
			@Override
			public void run() {
				for (UUID attacked : attackedList.keySet()) {
					HashMap<UUID, Integer> newMap = new HashMap<>();

					for (UUID attacker : attackedList.get(attacked).keySet()) {
						if (attackedList.get(attacked).get(attacker) - 1 > 0) {
							newMap.put(attacker, attackedList.get(attacked).get(attacker) - 1);
						}
					}
					attackedList.replace(attacked, newMap);
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (args.length < 1) {
				Logger.sendError(player, "Invalid command usage");
				return false;
			}

			if (!player.isOp()) {
				player.sendMessage(ChatColor.RED + "ERROR: Missing permissions");
				return false;
			}

			int reputation = 0;
			try {
				reputation = Integer.parseInt(args[0]);
			} catch (Exception e) {
				Logger.sendError(player, "Invalid reputation amount");
				return false;
			}

			OfflinePlayer target = null;
			UUID targetUUID = null;

			if (args.length == 1) {
				target = player;
				targetUUID = player.getUniqueId();
			} else {
				target = Bukkit.getOfflinePlayer(args[1]);
				targetUUID = target.getUniqueId();
			}

			db.setPlayer(targetUUID, "reputation", db.getPlayerInt(targetUUID, "reputation") + reputation);
			Logger.sendInfo(player, "Changed " + target.getName() + "'s reputation to " + db.getPlayerInt(targetUUID, "reputation"));
			return true;
		} else {
			if (args.length < 2) {
				Logger.sendError("Invalid command usage");
				return false;
			}
			int reputation = 0;
			try {
				reputation = Integer.parseInt(args[0]);
			} catch (Exception e) {
				Logger.sendError("Invalid reputation amount");
				return false;
			}

			OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
			UUID targetUUID = target.getUniqueId();

			db.setPlayer(targetUUID, "reputation", db.getPlayerInt(targetUUID, "reputation") + reputation);
			Logger.sendInfo("Changed " + target.getName() + "'s reputation to " + db.getPlayerInt(targetUUID, "reputation"));
			return true;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKill(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Player) {
				Player attacked = (Player) e.getEntity();
				Player attacker = (Player) e.getDamager();

				
				if (e.isCancelled()) {
					return;
				}
				
				if (!attackedList.containsKey(attacked.getUniqueId())) {
					attackedList.put(attacked.getUniqueId(), new HashMap<UUID, Integer>());
				}

				if (!attackedList.containsKey(attacker.getUniqueId())) {
					attackedList.put(attacker.getUniqueId(), new HashMap<UUID, Integer>());
				}

				if (!attackedList.get(attacker.getUniqueId()).containsKey(attacked.getUniqueId())) {
					attackedList.get(attacked.getUniqueId()).put(attacker.getUniqueId(), combatTimer);
				}

				if (attacked.getHealth() - e.getFinalDamage() <= 0 && !attacked.isBlocking() && !isHoldingTotemOfUndying(attacked)) {
					changeReputation(attacker, attacked, attackedList.get(attacker.getUniqueId()).containsKey(attacked.getUniqueId()));

					if (attackedList.get(attacker.getUniqueId()).containsKey(attacked.getUniqueId())) {
						attackedList.get(attacker.getUniqueId()).remove(attacked.getUniqueId());
					}
				}
			}
		}
	}

	public void changeReputation(Player killer, Player killed, boolean defensive) {
		UUID killerUUID = killer.getUniqueId();
		UUID killedUUID = killed.getUniqueId();

		int killedRep = db.getPlayerInt(killedUUID, "reputation");

		if (killedRep < -20) {
			db.setPlayer(killerUUID, "reputation", db.getPlayerInt(killerUUID, "reputation") + 5);
			killer.sendMessage(ChatColor.WHITE + "+5 rep" + ChatColor.GRAY + " reputation (killed a player with -20 rep)");
			killed.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "HUMBLED");
		} else if (killedRep < -10) {
			db.setPlayer(killerUUID, "reputation", db.getPlayerInt(killerUUID, "reputation") + 1);
			killer.sendMessage(ChatColor.WHITE + "+1 rep" + ChatColor.GRAY + " reputation (killed a player with -10 rep)");
		} else if (!defensive && killedRep > 10) {
			db.setPlayer(killerUUID, "reputation", db.getPlayerInt(killerUUID, "reputation") - 2);
			killer.sendMessage(ChatColor.WHITE + "-2 rep" + ChatColor.GRAY + " reputation (killed a player with +10 rep)");

			Guild killerGuild = GuildModule.getGuild(killerUUID);
			if (killerGuild != null) {
				for (UUID memberUUID : killerGuild.getMembers()) {
					if (!memberUUID.equals(killerUUID)) {
						db.setPlayer(memberUUID, "reputation", -1);
						Player member = Bukkit.getPlayer(memberUUID);
						if (member != null) {
							member.sendMessage(ChatColor.WHITE + "-1" + ChatColor.GRAY + " reputation (" + ChatColor.WHITE + killer.getName() + ChatColor.GRAY + " killed a player with +10 rep");
						}
					}
				}
			}
		} else if (!defensive) {
			db.setPlayer(killerUUID, "reputation", db.getPlayerInt(killerUUID, "reputation") - 1);
			killer.sendMessage(ChatColor.WHITE + "-1 rep" + ChatColor.GRAY + " reputation (killed a player)");
		}

		if (defensive) {
			db.setPlayer(killerUUID, "reputation", db.getPlayerInt(killerUUID, "reputation") + 1);
			killer.sendMessage(ChatColor.WHITE + "+1 rep" + ChatColor.GRAY + " reputation (defended yourself)");
		}

		Guild killerGuild = GuildModule.getGuild(killerUUID);
		Guild killedGuild = GuildModule.getGuild(killedUUID);

		if (killerGuild != null) {
			killerGuild.updateChunks();
		}

		if (killedGuild != null) {
			killedGuild.updateChunks();
		}
	}
	
	public boolean isHoldingTotemOfUndying(Player player) {
		return player.getInventory().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING) || player.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING);
	}
}
