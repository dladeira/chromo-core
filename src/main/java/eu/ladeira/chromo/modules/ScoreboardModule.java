package eu.ladeira.chromo.modules;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import eu.ladeira.chromo.Chromo;
import eu.ladeira.chromo.Database;
import eu.ladeira.chromo.LadeiraModule;
import eu.ladeira.chromo.guilds.Guild;
import eu.ladeira.chromo.guilds.GuildModule;
import net.md_5.bungee.api.ChatColor;

public class ScoreboardModule extends LadeiraModule {

	Plugin plugin;
	Database db;

	public ScoreboardModule(Database db, Plugin plugin) {
		this.db = db;
		this.plugin = plugin;

		startRunnable();
	}

	public void startRunnable() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player online : Bukkit.getOnlinePlayers()) {
					UUID uuid = online.getUniqueId();
					db.setPlayer(uuid, "playtime", db.getPlayerInt(uuid, "playtime") + 1);

					Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
					Objective obj = board.registerNewObjective("test", "dummy", ChatColor.GRAY + "-----| " + ChatColor.YELLOW + "" + "ChromoMC" + ChatColor.GRAY + " |-----");
					obj.setDisplaySlot(DisplaySlot.SIDEBAR);

					int score = 20;
					int reputation = db.getPlayerInt(uuid, "reputation");
					double rawPlaytime = (double) Double.valueOf(db.getPlayerInt(uuid, "playtime")) / 3600;
					float playtime = ((float) Math.round(rawPlaytime * 10) / 10);

					obj.getScore("").setScore(score--);
					obj.getScore(ChatColor.WHITE + "Username: " + ChatColor.GRAY + online.getName()).setScore(score--);
					obj.getScore(ChatColor.WHITE + "Reputation: " + ChatColor.GRAY + ReputationModule.getReputationColor(reputation) + reputation).setScore(score--);
					obj.getScore(ChatColor.WHITE + "Playtime: " + ChatColor.GRAY + playtime + "h").setScore(score--);
					obj.getScore(ChatColor.WHITE + "Online: " + ChatColor.GRAY + Bukkit.getOnlinePlayers().size() + "/" + db.getTotalPlayerCount()).setScore(score--);
					obj.getScore(ChatColor.RESET + "").setScore(score--);

					score = addGuildsToObjective(online, obj, score);

					online.setScoreboard(board);
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}

	public int addGuildsToObjective(Player online, Objective obj, int score) {
		Guild guild = GuildModule.getGuild(online.getUniqueId());
		// Guild information
		if (guild != null) {
			obj.getScore(ChatColor.GRAY + "-- " + ChatColor.RED + guild.getName()).setScore(score--);

			// Display members
			for (int x = 0; x < guild.getMaxMembers(); x++) {
				if (guild.getMembers().size() > x) {
					UUID memberUUID = guild.getMembers().get(x);
					int reputation = Chromo.getDatabase().getPlayerInt(memberUUID, "reputation");
					String memberString = Chromo.getDatabase().getName(memberUUID) + ChatColor.GRAY + " (" + ReputationModule.getReputationColor(reputation) + reputation + ChatColor.GRAY + ")";

					if (Bukkit.getPlayer(memberUUID) != null) {
						memberString = ChatColor.GREEN + " • " + memberString;
					} else {
						memberString = ChatColor.RED + " • " + memberString;
					}
					obj.getScore(memberString).setScore(score--);
				} else {
					obj.getScore(ChatColor.GRAY + " • " + "Empty " + x).setScore(score--);
				}

			}
			
			obj.getScore(ChatColor.RESET + "" + ChatColor.RESET + "" + ChatColor.RESET).setScore(score--);
			
			if (guild.allyCount() > 0) {
				obj.getScore(ChatColor.GRAY + "-- " + ChatColor.GOLD + "Allies").setScore(score--);
				for (int x : guild.getAllies()) {
					obj.getScore(ChatColor.GRAY + " • " + GuildModule.getGuild(x).getName()).setScore(score--);
				}
			} else {
				obj.getScore(ChatColor.GRAY + "-- No Allies :(").setScore(score--);
			}

			obj.getScore(ChatColor.RESET + "" + ChatColor.RESET).setScore(score--);
			obj.getScore(ChatColor.WHITE + "Chunks: " + ChatColor.GRAY + guild.getChunks().size() + "/" + guild.getMaxChunks()).setScore(score--);
			obj.getScore(ChatColor.WHITE + "Territories: " + ChatColor.GRAY + guild.getTerritories() + "/" + guild.getMaxTerritories()).setScore(score--);
		} else {
			obj.getScore(ChatColor.GRAY + "-- No guild").setScore(score--);
		}

		return score;
	}
}
