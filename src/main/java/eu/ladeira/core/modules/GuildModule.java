package eu.ladeira.core.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import eu.ladeira.core.Chromo;
import eu.ladeira.core.LadeiraModule;
import net.md_5.bungee.api.ChatColor;

public class GuildModule implements LadeiraModule, Listener, CommandExecutor {

	// Populated in loadGuilds in Storage class
	private static ArrayList<Guild> guilds = new ArrayList<>();
	private static ArrayList<String> claimLocked = new ArrayList<>();
	private static ArrayList<String> serverClaimed = new ArrayList<>();

	public GuildModule() {
		Chromo.getDatabase().loadGuilds();
	}
	
	public static void addClaimLocked(String chunk) {
		claimLocked.add(chunk);
	}

	public static void removeClaimLocked(String chunk) {
		claimLocked.remove(chunk);
	}

	public static ArrayList<String> getClaimLocked() {
		return claimLocked;
	}

	public static boolean isClaimLocked(Chunk chunk) {
		for (String locked : claimLocked) {
			int chunkX = Integer.parseInt(locked.split(":")[0]);
			int chunkZ = Integer.parseInt(locked.split(":")[1]);
			String world = locked.split(":")[2];

			if (chunkX == chunk.getX() && chunkZ == chunk.getZ() && world.equals(chunk.getWorld().getName())) {
				return true;
			}
		}
		return false;
	}

	public static void addServerClaimed(String chunk) {
		serverClaimed.add(chunk);
	}

	public static void removeServerClaimed(String chunk) {
		serverClaimed.remove(chunk);
	}

	public static ArrayList<String> getServerClaimed() {
		return serverClaimed;
	}

	public static boolean isServerClaimed(Chunk chunk) {
		for (String locked : serverClaimed) {
			int chunkX = Integer.parseInt(locked.split(":")[0]);
			int chunkZ = Integer.parseInt(locked.split(":")[1]);
			String world = locked.split(":")[2];

			if (chunkX == chunk.getX() && chunkZ == chunk.getZ() && world.equals(chunk.getWorld().getName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean addGuild(Guild newGuild) {
		for (Guild guild : guilds) {
			if (guild.getName().equals(newGuild.name)) {
				return false;
			}
		}

		guilds.add(newGuild);
		return true;
	}

	public static boolean deleteGuild(Guild delGuild) {
		int index = -1;
		for (Guild guild : guilds) {
			if (guild.getName().equals(delGuild.name)) {
				index = guilds.indexOf(guild);
			}
		}

		if (index >= 0) {
			guilds.remove(index);
		}

		return index >= 0;
	}

	public static Guild getGuild(UUID uuid) {
		for (Guild guild : guilds) {
			if (guild.hasMember(uuid)) {
				return guild;
			}
		}

		return null;
	}

	public static Guild getGuild(String name) {
		for (Guild guild : guilds) {
			if (guild.getName().equals(name)) {
				return guild;
			}
		}

		return null;
	}

	public static Guild getGuild(Chunk chunk) {
		if (isServerClaimed(chunk)) {
			return new Guild("SERVER");
		}

		for (Guild guild : guilds) {
			if (guild.isClaimed(chunk)) {
				return guild;
			}
		}
		return null;
	}

	public static ArrayList<Guild> getGuilds() {
		return guilds;
	}
	
	@Override
	public void onDisable() {
		Chromo.getDatabase().saveGuilds();
	}

	@Override
	public String cmdName() {
		return "guild";
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return false;
	}

	public static class Guild {

		private String name;
		private int maxMembers = 4;
		private int invitationDuration = 120;
		private UUID leader;
		private ArrayList<UUID> members;
		private HashMap<UUID, Integer> invitations = new HashMap<>();
		private LinkedList<String> chunks = new LinkedList<>();

		// SERVER ONLY
		public Guild(String name) {
			this.name = name;
			this.members = new ArrayList<UUID>();
		}

		public Guild(String name, UUID leader) {
			this(name, leader, new ArrayList<UUID>(), new LinkedList<String>());

			this.addMember(leader);
		}

		public Guild(String name, UUID leader, ArrayList<UUID> members, LinkedList<String> chunks) {
			this.name = name;
			this.leader = leader;
			this.members = members;
			this.chunks = chunks;

			new BukkitRunnable() {
				@Override
				public void run() {
					HashMap<UUID, Integer> newMap = new HashMap<>();
					for (UUID invitee : invitations.keySet()) {
						int time = invitations.get(invitee);
						if (time > 0) {
							newMap.put(invitee, --time);
						}
					}

					invitations = newMap;
				}
			}.runTaskTimer(Chromo.getPlugin(), 20, 20);
		}

		public String getName() {
			return this.name;
		}

		public boolean rename(String name) {
			if (GuildModule.getGuild(name) == null) {
				this.name = name;
				return true;
			}
			return false;
		}

		public UUID getLeader() {
			return this.leader;
		}

		public boolean isLeader(UUID uuid) {
			return this.leader.equals(uuid);
		}

		public int getMaxMembers() {
			return this.maxMembers;
		}

		public ArrayList<UUID> getMembers() {
			return this.members;
		}

		public void addMember(UUID uuid) {
			invitations.remove(uuid);

			members.add(uuid);

			updateChunks();
		}

		public void removeMember(UUID uuid) {
			members.remove(uuid);
			updateChunks();
		}

		public void inviteMember(UUID uuid) {
			invitations.put(uuid, this.invitationDuration);
		}

		public boolean isInvited(UUID uuid) {
			for (UUID invitee : invitations.keySet()) {
				if (invitee.equals(uuid)) {
					return true;
				}
			}
			return false;
		}

		public int getInvitationDuration() {
			return this.invitationDuration;
		}

		public boolean hasMember(UUID uuid) {
			for (UUID memberUUID : this.getMembers()) {
				if (memberUUID.equals(uuid)) {
					return true;
				}
			}
			return false;
		}

		public void broadcast(String msg) {
			for (UUID memberUUID : this.getMembers()) {
				Player member = Bukkit.getPlayer(memberUUID);

				if (member != null) {
					member.sendMessage(msg);
				}
			}
		}

		public LinkedList<String> getChunks() {
			return this.chunks;
		}

		public String parseChunk(Chunk chunk) {
			return chunk.getX() + ":" + chunk.getZ() + ":" + chunk.getWorld().getName();
		}

		public boolean isClaimed(Chunk chunk) {
			for (String claimedChunk : chunks) {
				int chunkX = Integer.parseInt(claimedChunk.split(":")[0]);
				int chunkZ = Integer.parseInt(claimedChunk.split(":")[1]);

				if (chunk.getX() == chunkX && chunk.getZ() == chunkZ && chunk.getWorld().getName().equals(claimedChunk.split(":")[2])) {
					return true;
				}
			}

			return false;
		}

		public boolean claimChunk(Chunk chunk) {
			if (chunks.size() < getMaxChunks() && !GuildModule.isServerClaimed(chunk)) {
				if (!isClaimed(chunk)) {
					chunks.add(parseChunk(chunk));
				}
				return true;
			}
			return false;
		}

		public void unclaimChunk(Chunk chunk) {
			int chunkIndex = -1;
			for (String claimedChunk : chunks) {
				int chunkX = Integer.parseInt(claimedChunk.split(":")[0]);
				int chunkZ = Integer.parseInt(claimedChunk.split(":")[1]);

				if (chunk.getX() == chunkX && chunk.getZ() == chunkZ && chunk.getWorld().getName().equals(claimedChunk.split(":")[2])) {
					chunkIndex = chunks.indexOf(claimedChunk);
				}
			}

			if (chunkIndex >= 0) {
				chunks.remove(chunkIndex);
			}
		}

		public Guild chunkGetCloseEnemy(Chunk chunk) {
			for (int x = -2; x < 3; x++) {
				for (int z = -2; z < 3; z++) {
					Guild chunkGuild = GuildModule.getGuild(chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ() + z));
					if (chunkGuild != null && !chunkGuild.getName().equals(getName())) {
						return chunkGuild;
					}

					if (GuildModule.isServerClaimed(chunk)) {
						return new Guild("SERVER");
					}
				}
			}

			return null;
		}

		public boolean isChunkLinked(Chunk chunk) {

			// This would be stupid...
			if (isClaimed(chunk)) {
				return true;
			}

			for (int x = -1; x < 3; x += 2) {
				if (isClaimed(chunk.getWorld().getChunkAt(chunk.getX() + x, chunk.getZ()))) {
					return true;
				}
			}

			for (int z = -1; z < 3; z += 2) {
				if (isClaimed(chunk.getWorld().getChunkAt(chunk.getX(), chunk.getZ() + z))) {
					return true;
				}
			}

			return false;
		}

		public ArrayList<String> getLinkedChunks(Chunk sourceChunk) {
			ArrayList<String> foundChunks = new ArrayList<>();
			LinkedList<Chunk> todoChunks = new LinkedList<Chunk>();
			foundChunks.add(sourceChunk.getX() + ":" + sourceChunk.getZ() + ":" + sourceChunk.getWorld().getName());
			todoChunks.add(sourceChunk);

			while (todoChunks.size() > 0) {
				Chunk currentChunk = todoChunks.getFirst();

				for (int x = -1; x < 3; x += 2) {
					Chunk loopChunk = currentChunk.getWorld().getChunkAt(currentChunk.getX() + x, currentChunk.getZ());
					if (isClaimed(loopChunk) && !foundChunks.contains(parseChunk(loopChunk))) {
						foundChunks.add(parseChunk(loopChunk));
						todoChunks.add(loopChunk);
					}
				}

				for (int z = -1; z < 3; z += 2) {
					Chunk loopChunk = currentChunk.getWorld().getChunkAt(currentChunk.getX(), currentChunk.getZ() + z);
					if (isClaimed(loopChunk) && !foundChunks.contains(parseChunk(loopChunk))) {
						foundChunks.add(parseChunk(loopChunk));
						todoChunks.add(loopChunk);
					}
				}

				todoChunks.removeFirst();
			}

			return foundChunks;
		}

		public ArrayList<String> getLinkedChunks(String chunk) {
			int chunkX = Integer.parseInt(chunk.split(":")[0]);
			int chunkZ = Integer.parseInt(chunk.split(":")[1]);

			return getLinkedChunks(Bukkit.getWorld(chunk.split(":")[2]).getChunkAt(chunkX, chunkZ));
		}

		public int getTerritories() {
			LinkedList<String> chunksLeft = new LinkedList<>(chunks);
			int territories = 0;

			while (chunksLeft.size() > 0) {
				territories++;

				String rawChunk = chunksLeft.getFirst();

				int chunkX = Integer.parseInt(rawChunk.split(":")[0]);
				int chunkZ = Integer.parseInt(rawChunk.split(":")[1]);

				Chunk chunk = Bukkit.getWorld(rawChunk.split(":")[2]).getChunkAt(chunkX, chunkZ);

				chunksLeft.removeAll(getLinkedChunks(chunk));
			}

			return territories;
		}

		public void updateChunks() {
			int chunksOverLimit = chunks.size() - getMaxChunks();

			while (chunksOverLimit-- > 0) {
				broadcast(ChatColor.LIGHT_PURPLE + "ALERT: Chunk at " + ChatColor.WHITE + chunks.getLast() + ChatColor.LIGHT_PURPLE + " has been unclaimed");
				chunks.removeLast();
			}

			updateTerritories();
		}

		public void updateTerritories() {
			if (getTerritories() > getMaxTerritories()) {
				ArrayList<String> removedChunks = getLinkedChunks(chunks.getFirst());

				chunks.removeAll(removedChunks);

				for (String removedChunk : removedChunks) {
					broadcast(ChatColor.LIGHT_PURPLE + "ALERT: Chunk at " + ChatColor.WHITE + removedChunk + ChatColor.LIGHT_PURPLE + " has been unclaimed (territory max)");
				}
			}
		}

		public int getMaxChunks() {
			int size = this.getMembers().size() * 16;

			for (UUID member : members) {
				int rep = Chromo.getDatabase().getPlayerInt(member, "reputation");

				if (rep > 0) {
					rep -= rep % 3;

					size += rep / 3;
				} else if (rep < 0) {
					rep = Math.abs(rep);
					size -= rep;
				}
			}
			return size;
		}

		public int getMaxTerritories() {
			int size = 2;

			for (UUID member : members) {
				int rep = Chromo.getDatabase().getPlayerInt(member, "reputation");

				if (rep > 0) {
					rep -= rep % 10;

					size += rep / 10;
				} else if (rep < 0) {
					rep = Math.abs(rep);
					rep -= rep % 10;

					size -= rep / 10;
				}
			}
			return size;
		}
	}
}
