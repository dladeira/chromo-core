package eu.ladeira.core.modules;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import eu.ladeira.core.Database;
import eu.ladeira.core.LadeiraCore;
import eu.ladeira.core.LadeiraModule;
import eu.ladeira.guilds.LGuilds;
import net.md_5.bungee.api.ChatColor;

public class ScoreboardManager implements LadeiraModule {

	Plugin plugin;
	Database db;

	public ScoreboardManager(Database db, Plugin plugin) {
		this.db = db;
		this.plugin = plugin;

		startRunnable();
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
	}

	@Override
	public String cmdName() {
		return null;
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
					obj.getScore(ChatColor.WHITE + "Reputation: " + ChatColor.GRAY + ReputationManager.getReputationColor(reputation) + reputation).setScore(score--);
					obj.getScore(ChatColor.WHITE + "Playtime: " + ChatColor.GRAY + playtime + "h").setScore(score--);
					obj.getScore(ChatColor.WHITE + "Online: " + ChatColor.GRAY + Bukkit.getOnlinePlayers().size() + "/" + db.getTotalPlayerCount()).setScore(score--);
					obj.getScore(ChatColor.RESET + "").setScore(score--);

					if (LadeiraCore.hasExternalModule("LGuilds")) {
						LGuilds guilds = (LGuilds) LadeiraCore.getExternalModule("LGuilds");
						score = guilds.addGuildToObjective(online, obj, score);
					}

					online.setScoreboard(board);
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}
}
