package eu.ladeira.core.guilds;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CmdGuild implements CommandExecutor {
	
	private static ArrayList<UUID> override;

	public CmdGuild() {
		override = new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			System.out.println("Only players can run this command");
		}

		Player player = (Player) sender;

		if (args.length < 1) {
			sendInfo(player);
		} else {
			String rootCmd = args[0];

			switch (rootCmd) {
			case "create":
				createGuild(player, args);
				break;
			case "rename":
				renameGuild(player, args);
				break;
			case "disband":
				disbandGuild(player);
				break;
			case "map":
				sendMap(player, args);
				break;
			case "invite":
				inviteMember(player, args);
				break;
			case "join":
				joinGuild(player, args);
				break;
			case "leave":
				leaveGuild(player);
				break;
			case "kick":
				kickMember(player, args);
				break;
			case "claims":
				listClaims(player);
				break;
			case "claim":
				claimChunk(player);
				break;
			case "unclaim":
				unclaimChunk(player);
				break;
			case "claimlock":
				claimLock(player);
				break;
			case "claimunlock":
				claimUnlock(player);
				break;
			case "serverclaim":
				serverClaim(player);
				break;
			case "serverunclaim":
				serverUnclaim(player);
				break;
			case "ally":
				ally(player, args);
				break;
			case "allyaccept":
				allyAccept(player, args);
				break;
			case "enemy":
				enemy(player, args);
				break;
			case "override":
				override(player);
				break;
			case "id":
				player.sendMessage(GuildModule.getGuild(player.getUniqueId()).getId() + "");
				break;
			default:
				player.sendMessage(ChatColor.RED + "ERROR: Invalid option " + ChatColor.WHITE + args[0]);
				break;
			}
		}
		return false;
	}

	private void sendInfo(Player player) {
		player.sendMessage(ChatColor.GREEN + "-----| " + ChatColor.WHITE + "GUILDS" + ChatColor.GREEN + " |-----");
		player.sendMessage(ChatColor.GREEN + "/guild create [name]" + ChatColor.GRAY + " - create a guild");
		player.sendMessage(ChatColor.GREEN + "/guild disband" + ChatColor.GRAY + " - disband your guild ");
		player.sendMessage(ChatColor.GREEN + "/guild map" + ChatColor.GRAY + " - display a map of surrounding claims (facing north)");
		player.sendMessage(ChatColor.GREEN + "/guild map [on:off}" + ChatColor.GRAY + " - automatically send map when walking into a new chunk");
		player.sendMessage(ChatColor.GREEN + "/guild invite [username]" + ChatColor.GRAY + " - invite a member to your guild");
		player.sendMessage(ChatColor.GREEN + "/guild join [name]" + ChatColor.GRAY + " - join a guild you've been invited to");
		player.sendMessage(ChatColor.GREEN + "/guild leave" + ChatColor.GRAY + " - leave your guild");
		player.sendMessage(ChatColor.GREEN + "/guild kick [username]" + ChatColor.GRAY + " - kick a member from your guild");
		player.sendMessage(ChatColor.GREEN + "/guild claims" + ChatColor.GRAY + " - list all the chunks your guild has claimed");
		player.sendMessage(ChatColor.GREEN + "/guild claim" + ChatColor.GRAY + " - claim the chunk you are standing on");
		player.sendMessage(ChatColor.GREEN + "/guild unclaim" + ChatColor.GRAY + " - unclaim the chunk you are standing on");
	}

	private void createGuild(Player player, String[] args) {
		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid command usage");
			return;
		}

		String guildName = "";

		int argIndex = 0;
		for (String arg : args) {
			if (argIndex++ < 1) {
				continue;
			}

			guildName += arg;

			if (argIndex < args.length) {
				guildName += " ";
			}
		}

		Guild existingGuild = GuildModule.getGuild(player.getUniqueId());
		if (existingGuild != null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are already in " + ChatColor.AQUA + existingGuild.getName());
			return;
		}

		if (guildName.length() > 24) {
			player.sendMessage(ChatColor.RED + "ERROR: Max guild name is 24 characters");
			return;
		}

		if (GuildModule.addGuild(new Guild(guildName, player.getUniqueId()))) {
			player.sendMessage(ChatColor.GRAY + "Created guild " + ChatColor.WHITE + guildName);
		} else {
			player.sendMessage(ChatColor.RED + "ERROR: A guild with the same name already exists");
		}

		return;
	}

	private void renameGuild(Player player, String[] args) {
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid command usage");
			return;
		}

		Guild guild = GuildModule.getGuild(player.getUniqueId());

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not in a guild");
			return;
		}

		if (!guild.isLeader(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not the guild leader");
			if (override.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Overriden");
			} else {
				return;
			}
		}

		String guildName = "";
		int argIndex = 0;
		for (String arg : args) {
			if (argIndex++ < 1) {
				continue;
			}

			guildName += arg;

			if (argIndex < args.length) {
				guildName += " ";
			}
		}

		if (guildName.length() > 24) {
			player.sendMessage(ChatColor.RED + "ERROR: Max guild name is 24 characters");
			return;
		}

		if (guild.rename(guildName)) {
			player.sendMessage(ChatColor.GRAY + "Renamed guild to " + ChatColor.WHITE + guildName);
		} else {
			player.sendMessage(ChatColor.RED + "ERROR: A guild with the same name already exists");
		}
		return;
	}

	private void disbandGuild(Player player) {
		Guild guild = GuildModule.getGuild(player.getUniqueId());
		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not in a guild");
			return;
		}

		if (!guild.isLeader(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not the team leader");

			if (override.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Overriden");
			} else {
				return;
			}
		}

		guild.broadcast(ChatColor.LIGHT_PURPLE + "ALERT: Your team has been disbanded by the leader");
		GuildModule.deleteGuild(guild);
		return;
	}

	public void sendMap(Player player) {
		Guild guild = GuildModule.getGuild(player.getUniqueId());

		int range = 3;

		player.sendMessage(ChatColor.WHITE + "-| Map |-");
		for (int z = -range; z < range + 1; z++) {
			String msg = "";
			for (int x = -range; x < range + 1; x++) {
				Chunk chunk = player.getWorld().getChunkAt(player.getLocation().getChunk().getX() + x, player.getLocation().getChunk().getZ() + z);
				Guild chunkGuild = GuildModule.getGuild(chunk);

				if (x == 0 & z == 0) {
					msg += ChatColor.WHITE + "+";
					continue;
				}

				if (chunkGuild == null) {
					msg += ChatColor.GRAY + "+";
					continue;
				}

				if (guild != null && chunkGuild.getName().equals(guild.getName())) {
					msg += ChatColor.GREEN + "+";
				} else {
					msg += ChatColor.RED + "+";
				}
			}

			player.sendMessage(msg);
		}

		player.sendMessage(ChatColor.WHITE + "-------");
	}

	public void sendMap(Player player, Location loc) {
		Guild guild = GuildModule.getGuild(player.getUniqueId());

		int range = 3;

		player.sendMessage(ChatColor.WHITE + "-| Map |-");
		for (int z = -range; z < range + 1; z++) {
			String msg = "";
			for (int x = -range; x < range + 1; x++) {
				Chunk chunk = player.getWorld().getChunkAt(loc.getChunk().getX() + x, loc.getChunk().getZ() + z);
				Guild chunkGuild = GuildModule.getGuild(chunk);

				if (x == 0 & z == 0) {
					msg += ChatColor.WHITE + "+";
					continue;
				}

				if (chunkGuild == null) {
					msg += ChatColor.GRAY + "+";
					continue;
				}

				if (guild != null && chunkGuild.getName().equals(guild.getName())) {
					msg += ChatColor.GREEN + "+";
				} else {
					msg += ChatColor.RED + "+";
				}
			}

			player.sendMessage(msg);
		}

		player.sendMessage(ChatColor.WHITE + "-------");
	}

	public void sendMap(Player player, String[] args) {
		Guild guild = GuildModule.getGuild(player.getUniqueId());

		int range = 3;

		if (args.length > 1) {
			String arg = args[1];

			switch (arg) {
			case "on":
				EventSendMap.addMapUpdate(player);
				player.sendMessage(ChatColor.GRAY + "Enabling automatic map display");
				return;
			case "off":
				EventSendMap.removeMapUpdate(player);
				player.sendMessage(ChatColor.GRAY + "Disabling automatic map display");
				return;
			default:
				player.sendMessage(ChatColor.RED + "Unrecognized option " + ChatColor.WHITE + arg);
				return;
			}
		}

		player.sendMessage(ChatColor.WHITE + "-| Map |-");
		for (int z = -range; z < range + 1; z++) {
			String msg = "";
			for (int x = -range; x < range + 1; x++) {
				Chunk chunk = player.getWorld().getChunkAt(player.getLocation().getChunk().getX() + x, player.getLocation().getChunk().getZ() + z);
				Guild chunkGuild = GuildModule.getGuild(chunk);

				if (x == 0 & z == 0) {
					msg += ChatColor.WHITE + "+";
					continue;
				}

				if (chunkGuild == null) {
					msg += ChatColor.GRAY + "+";
					continue;
				}

				if (guild != null && chunkGuild.getName().equals(guild.getName())) {
					msg += ChatColor.GREEN + "+";
				} else {
					msg += ChatColor.RED + "+";
				}
			}

			player.sendMessage(msg);
		}

		player.sendMessage(ChatColor.WHITE + "-------");
	}

	@SuppressWarnings("deprecation")
	private void inviteMember(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID uuid = player.getUniqueId();
		Guild guild = GuildModule.getGuild(uuid);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not in a guild");
			return;
		}

		if (!guild.isLeader(uuid)) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not the team leader");
			if (override.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Overriden");
			} else {
				return;
			}
		}

		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid command usage");
			return;
		}

		// Prevent server from crashing
		if (!args[1].matches("([A-z0-9_])+")) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid username");
			return;
		}

		UUID inviteeUUID = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
		Player invitee = Bukkit.getPlayerExact(args[1]);

		if (args[1] == player.getName()) {
			player.sendMessage(ChatColor.RED + "ERROR: You can't invite yourself");
			return;
		}

		if (GuildModule.getGuild(inviteeUUID) != null) {
			player.sendMessage(ChatColor.RED + "ERROR: Player already in a team");
			return;
		}

		if (guild.getMembers().size() >= guild.getMaxMembers()) {
			player.sendMessage(ChatColor.RED + "ERROR: Max team size reached (" + guild.getMaxMembers() + ")");
			if (override.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Overriden");
			} else {
				return;
			}
		}

		if (guild.isInvited(inviteeUUID)) {
			player.sendMessage(ChatColor.RED + "ERROR: Player has already been invited");
			return;
		}

		guild.inviteMember(inviteeUUID);
		guild.broadcast(ChatColor.GRAY + "Invited " + ChatColor.WHITE + args[1] + ChatColor.GRAY + ", they have " + ChatColor.WHITE + guild.getInvitationDuration() + ChatColor.GRAY + " seconds to join");

		if (invitee != null) {
			TextComponent invitation = new TextComponent(ChatColor.GRAY + "You have been invited to " + ChatColor.WHITE + guild.getName() + ChatColor.GRAY + ", you have 120 seconds to join");
			TextComponent accept = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[JOIN]");
			accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild join " + guild.getName()));

			invitee.spigot().sendMessage(invitation, new TextComponent(" "), accept);
		}

		return;
	}

	private void joinGuild(Player player, String[] args) {
		UUID uuid = player.getUniqueId();

		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "Invalid command usage");
			return;
		}

		String guildName = "";

		int argIndex = 0;
		for (String arg : args) {
			if (argIndex++ < 1) {
				continue;
			}

			guildName += arg;

			if (argIndex < args.length) {
				guildName += " ";
			}
		}

		Guild guild = GuildModule.getGuild(guildName);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: This guild does not exist");
			return;
		}

		if (GuildModule.getGuild(uuid) != null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are already in a guild");
			return;
		}

		if (!guild.isInvited(uuid)) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not invited to this guild!");
			if (override.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Overriden");
			} else {
				return;
			}
		}

		guild.addMember(uuid);
		guild.broadcast(ChatColor.LIGHT_PURPLE + "ALERT: " + ChatColor.WHITE + player.getName() + ChatColor.LIGHT_PURPLE + " has joined the team");

		return;
	}

	private void leaveGuild(Player player) {
		UUID uuid = player.getUniqueId();
		Guild guild = GuildModule.getGuild(uuid);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not in a guild");
			return;
		}

		if (guild.isLeader(uuid)) {
			player.sendMessage(ChatColor.RED + "ERROR: You are the leader of the guild, disband it instead");
			return;
		}

		guild.broadcast(ChatColor.LIGHT_PURPLE + "ALERT: " + ChatColor.WHITE + player.getName() + ChatColor.LIGHT_PURPLE + " has left the guild");
		guild.removeMember(player.getUniqueId());
	}

	private void kickMember(Player player, String[] args) {
		UUID uuid = player.getUniqueId();
		Guild guild = GuildModule.getGuild(uuid);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not in a guild");
			return;
		}

		if (!guild.isLeader(uuid)) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not the guild leader");
			if (override.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Overriden");
			} else {
				return;
			}
		}

		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "ERROR: Please specify a player");
		}

		// Prevent server from crashing
		if (!args[1].matches("([A-z0-9_])+")) {
			player.sendMessage(ChatColor.RED + "ERROR: Invalid username");
			return;
		}

		OfflinePlayer kick = Bukkit.getPlayerExact(args[1]);
		UUID kickUUID = kick.getUniqueId();

		if (!guild.hasMember(kickUUID)) {
			player.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.WHITE + kick.getName() + ChatColor.RED + " is not in your guild");
		}

		guild.broadcast(ChatColor.LIGHT_PURPLE + "ALERT: " + ChatColor.WHITE + kick.getName() + ChatColor.LIGHT_PURPLE + " has been kicked from the guild");
		guild.removeMember(kickUUID);
	}

	private void listClaims(Player player) {
		UUID uuid = player.getUniqueId();
		Guild guild = GuildModule.getGuild(uuid);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not in a guild");
			return;
		}

		player.sendMessage(ChatColor.GRAY + "-----| " + ChatColor.WHITE + "CLAIMS" + ChatColor.GRAY + " |-----");
		for (String claim : guild.getChunks()) {
			player.sendMessage(ChatColor.GRAY + claim);
		}
		player.sendMessage(ChatColor.GRAY + "------------------");
	}

	private void claimChunk(Player player) {
		UUID uuid = player.getUniqueId();
		Guild guild = GuildModule.getGuild(uuid);
		Chunk chunk = player.getLocation().getChunk();
		Guild chunkGuild = GuildModule.getGuild(chunk);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not in a guild");
			return;
		}

		Guild enemyChunkGuild = guild.chunkGetCloseEnemy(chunk);

		if (GuildModule.isClaimLocked(chunk)) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is claim locked");
			return;
		}

		if (GuildModule.isServerClaimed(chunk)) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is claimed by the server");
			return;
		}

		if (GuildModule.getGuild(chunk) != null) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is claimed by " + ChatColor.WHITE + chunkGuild.getName());
			return;
		}

		if (guild.getChunks().size() >= guild.getMaxChunks()) {
			player.sendMessage(ChatColor.RED + "ERROR: Max chunks reached");
			return;
		}

		if (enemyChunkGuild != null) {
			player.sendMessage(ChatColor.RED + "ERROR: Chunk within two chunks of " + ChatColor.WHITE + enemyChunkGuild.getName());
			if (override.contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Overriden");
			} else {
				return;
			}
		}

		if (!guild.isChunkLinked(chunk) && guild.getTerritories() >= guild.getMaxTerritories()) {
			player.sendMessage(ChatColor.RED + "ERROR: Max territories reached");
			return;
		}

		guild.claimChunk(chunk);
		player.sendMessage(ChatColor.GRAY + "Claimed this chunk");
	}

	private void unclaimChunk(Player player) {
		UUID uuid = player.getUniqueId();
		Guild guild = GuildModule.getGuild(uuid);
		Chunk chunk = player.getLocation().getChunk();
		Guild chunkGuild = GuildModule.getGuild(chunk);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: You are not in a guild");
			return;
		}

		if (chunkGuild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is not claimed");
			return;
		}

		if (!chunkGuild.getName().equals(guild.getName())) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is claimed by " + ChatColor.WHITE + chunkGuild.getName());
			return;
		}

		guild.unclaimChunk(chunk);
		player.sendMessage(ChatColor.GRAY + "Unclaimed this chunk");
	}

	private void claimLock(Player player) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permissions");
			return;
		}

		Chunk chunk = player.getLocation().getChunk();
		String rawChunk = chunk.getX() + ":" + chunk.getZ() + ":" + chunk.getWorld().getName();

		if (GuildModule.isClaimLocked(chunk)) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is already claim locked");
			return;
		}

		player.sendMessage(ChatColor.GRAY + "Claim locked chunk at " + ChatColor.WHITE + rawChunk);
		GuildModule.addClaimLocked(rawChunk);
	}

	private void claimUnlock(Player player) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permissions");
			return;
		}

		Chunk chunk = player.getLocation().getChunk();
		String rawChunk = chunk.getX() + ":" + chunk.getZ() + ":" + chunk.getWorld().getName();

		if (!GuildModule.isClaimLocked(chunk)) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is not claim locked");
			return;
		}

		player.sendMessage(ChatColor.GRAY + "Claim unlocked chunk at " + ChatColor.WHITE + rawChunk);
		GuildModule.removeClaimLocked(rawChunk);
	}

	private void serverClaim(Player player) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permissions");
			return;
		}

		Chunk chunk = player.getLocation().getChunk();
		String rawChunk = chunk.getX() + ":" + chunk.getZ() + ":" + chunk.getWorld().getName();

		if (GuildModule.isClaimLocked(chunk)) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is claim locked");
			return;
		}

		if (GuildModule.getGuild(chunk) != null) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is claimed");
			return;
		}

		player.sendMessage(ChatColor.GRAY + "Server claimed chunk at " + ChatColor.WHITE + rawChunk);
		GuildModule.addServerClaimed(rawChunk);
	}

	private void serverUnclaim(Player player) {
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permissions");
			return;
		}

		Chunk chunk = player.getLocation().getChunk();
		String rawChunk = chunk.getX() + ":" + chunk.getZ() + ":" + chunk.getWorld().getName();

		if (!GuildModule.isServerClaimed(chunk)) {
			player.sendMessage(ChatColor.RED + "ERROR: This chunk is not claimed by the server");
			return;
		}

		player.sendMessage(ChatColor.GRAY + "Server unclaimed chunk at " + ChatColor.WHITE + rawChunk);
		GuildModule.removeServerClaimed(rawChunk);
	}

	private void override(Player player) {
		UUID uuid = player.getUniqueId();
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "ERROR: Missing permissions");
		}

		if (override.contains(uuid)) {
			player.sendMessage(ChatColor.GRAY + "No longer overriding");
			override.remove(uuid);
			return;
		}

		player.sendMessage(ChatColor.GRAY + "Overriding restrictions");
		override.add(uuid);
		return;
	}

	public static boolean isOverriding(Player player) {
		return override.contains(player.getUniqueId());
	}
	
	public void ally(Player player, String[] args) {
		UUID uuid = player.getUniqueId();
		Guild playerGuild = GuildModule.getGuild(uuid);
		
		if (playerGuild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: Not currently in a guild");
		}
		String guildName = "";
		int argIndex = 0;
		for (String arg : args) {
			if (argIndex++ < 1) {
				continue;
			}

			guildName += arg;

			if (argIndex < args.length) {
				guildName += " ";
			}
		}

		Guild guild = GuildModule.getGuild(guildName);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: This guild does not exist");
			return;
		}
		
		if (guild.getId() == playerGuild.getId()) {
			player.sendMessage(ChatColor.RED + "ERROR: You can't ally yourself");
			return;
		}
		
		player.sendMessage(ChatColor.GRAY + "Sent an invitation to the guild leader");
		playerGuild.inviteAlly(guild);
		Player leader = Bukkit.getPlayer(guild.getLeader());
		if (leader != null) {
			TextComponent invitation = new TextComponent(ChatColor.GRAY + "Your guild has recieved a ally invitation from " + ChatColor.WHITE + playerGuild.getName() + ChatColor.GRAY + ", you have 120 seconds to accept");
			TextComponent accept = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[ACCEPT]");
			accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/guild allyaccept " + playerGuild.getName()));

			leader.spigot().sendMessage(invitation, new TextComponent(" "), accept);
		}
	}
	
	public void enemy(Player player, String[] args) {
		UUID uuid = player.getUniqueId();
		Guild playerGuild = GuildModule.getGuild(uuid);
		
		if (playerGuild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: Not currently in a guild");
		}
		String guildName = "";
		int argIndex = 0;
		for (String arg : args) {
			if (argIndex++ < 1) {
				continue;
			}

			guildName += arg;

			if (argIndex < args.length) {
				guildName += " ";
			}
		}

		Guild guild = GuildModule.getGuild(guildName);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: This guild does not exist");
			return;
		}
		
		if (guild.getId() == playerGuild.getId()) {
			player.sendMessage(ChatColor.RED + "ERROR: You can't enemy yourself");
			return;
		}
		
		if (!playerGuild.isAlly(guild.getId())) {
			player.sendMessage(ChatColor.RED + "ERROR: This guild is not allied");
			return;
		}
		
		playerGuild.broadcast(ChatColor.LIGHT_PURPLE + "IMPORTANT -- You are now enemies with " + ChatColor.WHITE + guildName);
		guild.broadcast(ChatColor.LIGHT_PURPLE + "IMPORTANT -- You are now enemies with " + ChatColor.WHITE + playerGuild.getName());
		guild.enemyGuild(playerGuild.getId());
		playerGuild.enemyGuild(guild.getId());
	}
	
	public void allyAccept(Player player, String[] args) {
		UUID uuid = player.getUniqueId();
		Guild playerGuild = GuildModule.getGuild(uuid);
		
		if (playerGuild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: Not currently in a guild");
		}
		String guildName = "";
		int argIndex = 0;
		for (String arg : args) {
			if (argIndex++ < 1) {
				continue;
			}

			guildName += arg;

			if (argIndex < args.length) {
				guildName += " ";
			}
		}

		Guild guild = GuildModule.getGuild(guildName);

		if (guild == null) {
			player.sendMessage(ChatColor.RED + "ERROR: This guild does not exist");
			return;
		}
		
		if (!guild.isAllyInvited(playerGuild)) {
			player.sendMessage(ChatColor.RED + "ERROR: This guild has not sent you an ally invitation");
			return;
		}
		
		if (guild.isAlly(playerGuild.getId())) {
			player.sendMessage(ChatColor.RED + "ERROR: This guild is already an ally");
			return;
		}
		
		playerGuild.broadcast(ChatColor.LIGHT_PURPLE + "IMPORTANT -- You are now allies with " + ChatColor.WHITE + guildName);
		guild.broadcast(ChatColor.LIGHT_PURPLE + "IMPORTANT -- You are now allies with " + ChatColor.WHITE + playerGuild.getName());
		guild.allyGuild(playerGuild.getId());
		playerGuild.allyGuild(guild.getId());
	}
}
