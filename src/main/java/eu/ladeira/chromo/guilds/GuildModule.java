package eu.ladeira.chromo.guilds;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;

import eu.ladeira.chromo.Chromo;
import eu.ladeira.chromo.LadeiraModule;

public class GuildModule extends LadeiraModule implements Listener {

	private static ArrayList<Guild> guilds = new ArrayList<>();
	private static ArrayList<String> claimLocked = new ArrayList<>();
	private static ArrayList<String> serverClaimed = new ArrayList<>();

	public GuildModule() {
		Chromo.getDatabase().loadGuilds();
	}
	
	@Override
	public void onDisable() {
		Chromo.getDatabase().saveGuilds();
	}

	@Override
	public String getCmdName() {
		return "guild";
	}
	
	@Override
	public CommandExecutor getExecutor() {
		return new CmdGuild();
	}
	
	@Override
	public ArrayList<Listener> getListeners() {
		ArrayList<Listener> listeners = new ArrayList<>();
		
		listeners.add(new EventSendMap());
		listeners.add(new EventClaimActions());
		
		return listeners;
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
			if (guild.getName().equals(newGuild.getName())) {
				return false;
			}
		}

		guilds.add(newGuild);
		return true;
	}

	public static boolean deleteGuild(Guild delGuild) {
		int index = -1;
		for (Guild guild : guilds) {
			if (guild.getName().equals(delGuild.getName())) {
				index = guilds.indexOf(guild);
				
				ArrayList<Integer> allies = new ArrayList<>();
				for (int ally : guild.getAllies()) {
					allies.add(ally);
				}
				
				for (int ally : allies) {
					GuildModule.getGuild(ally).enemyGuild(guild.getId());
					guild.enemyGuild(ally);
				}
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
	
	public static Guild getGuild(int id) {
		for (Guild guild : guilds) {
			if (guild.getId() == id) {
				return guild;
			}
		}

		return null;
	}
	
	public static int getId() {
		for (int i = 0; i < 1000; i++) {
			boolean foundNumber = false;
			for (Guild guild : guilds) {
				if (guild.getId() == i) {
					foundNumber = true;
				}
			}
			
			if (!foundNumber) {
				return i;
			}
		}
		System.out.println("no numbers lmao");
		return 0;
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
}
