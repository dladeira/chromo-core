package eu.ladeira.chromo.modules;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import eu.ladeira.chromo.Database;
import eu.ladeira.chromo.LadeiraModule;
import eu.ladeira.chromo.Logger;

public class SpawnManager extends LadeiraModule implements Listener, CommandExecutor {

	private Database db;
	private Location location;

	public SpawnManager(Database db) {
		location = Database.deserialize(db.getSetting("spawn"));
		this.db = db;
	}

	@Override
	public void onDisable() {
		if (location != null) {
			db.setSetting("spawn", Database.serialize(location));
		}
	}
	
	@Override
	public String getCmdName() {
		return "spawn";
	}
	
	@Override
	public CommandExecutor getExecutor() {
		return this;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Logger.sendError("This command can only be used by players");
		}
		Player player = (Player) sender;
		
		if (!player.isOp()) {
			player.setHealth(0);
			return false;
		}
		
		if (args.length < 1) {
			Logger.sendError(player, "Invalid command usage");
			return false;
		}

		String rootCmd = args[0];

		switch (rootCmd) {
		case "set":
			Logger.sendInfo(player, "Spawn has been set to your location");
			db.setSetting("spawn", Database.serialize(player.getLocation()));
			location = player.getLocation();
			return true;
		case "get":
			if (location != null) {
				Logger.sendInfo(player, "Spawn is set to " + Database.serializePretty(location));
			} else {
				Logger.sendInfo(player, "Spawn is not set");
			}
			return true;
		case "teleport":
			if (location != null) {
				Logger.sendInfo(player, "Teleported to spawn");
				player.eject();
				player.teleport(location);
			} else {
				Logger.sendError(player, "Spawn has not been defined");
			}
			return true;
		default:
			Logger.sendError(player, "Unrecognized option " + rootCmd);
			return false;
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void respawnAtSpawn(PlayerRespawnEvent e) {
		if (!e.isBedSpawn() && location != null) {
			e.setRespawnLocation(getSpawn());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		if (!player.hasPlayedBefore()) {
			player.teleport(getSpawn());
		}
	}

	public Location getSpawn() {
		if (location != null) {
			return location;
		}

		Logger.sendError("Spawn is undefined");
		return Bukkit.getWorld("world").getSpawnLocation();
	}
}
