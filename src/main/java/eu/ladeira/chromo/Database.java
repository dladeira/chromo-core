package eu.ladeira.chromo;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import eu.ladeira.chromo.guilds.Guild;
import eu.ladeira.chromo.guilds.GuildModule;
import net.md_5.bungee.api.ChatColor;

public class Database {

	private static String uri = "mongodb://localhost:27017/ladeiraCore";
	private static MongoClient client;
	private static MongoDatabase db;
	private static MongoCollection<Document> playerCollection;
	private static MongoCollection<Document> serverCollection;
	private static MongoCollection<Document> guildCollection;
	private static Document serverSettings;

	public Database(Plugin plugin) {
		client = MongoClients.create(uri);
		db = client.getDatabase("chromo-core");

		playerCollection = db.getCollection("players");
		serverCollection = db.getCollection("server");
		guildCollection = db.getCollection("guilds");

		// If server settings don't exist create them
		if (serverCollection.countDocuments() < 1) {
			serverCollection.insertOne(new Document());
		}

		serverSettings = serverCollection.find().first();

		final Database database = this;

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player online : Bukkit.getOnlinePlayers()) {
					online.setPlayerListName(database.getName(online.getUniqueId()));
					;
				}
			}
		}.runTaskTimer(plugin, 20, 20);
	}

	public MongoDatabase getDB() {
		return db;
	}

	/*
	 * Server Settings
	 */

	public String getSetting(String key) {
		return serverSettings.getString(key);
	}

	public List<String> getSettingList(String key) {
		return serverSettings.getList(key, String.class);
	}

	public void setSetting(String key, String value) {
		serverCollection.replaceOne(new Document(), serverSettings.append(key, value));
	}

	public void setSetting(String key, List<String> value) {
		serverCollection.replaceOne(new Document(), serverSettings.append(key, value));
	}

	/*
	 * Player
	 */

	public String getPlayerString(UUID uuid, String key) {
		return getPlayer(uuid).getString(key);
	}

	public int getPlayerInt(UUID uuid, String key) {
		return getPlayer(uuid).getInteger(key, 0);
	}

	public String getName(UUID uuid) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

		if (player.isOp()) {
			return ChatColor.RED + "[" + ChatColor.WHITE + "ADMIN" + ChatColor.RED + "] " + player.getName();
		}

		return ChatColor.WHITE + player.getName();
	}

	public void setPlayer(UUID uuid, String key, Object value) {
		playerCollection.replaceOne(new Document("uuid", uuid.toString()), getPlayer(uuid).append(key, value));
	}

	public long getTotalPlayerCount() {
		return playerCollection.countDocuments();
	}

	private Document getPlayer(UUID uuid) {
		Document doc = playerCollection.find(eq("uuid", uuid.toString())).first();

		if (doc == null) {
			playerCollection.insertOne(new Document("_id", new ObjectId()).append("uuid", uuid.toString()));
		}

		return playerCollection.find(eq("uuid", uuid.toString())).first();
	}

	/*
	 * Helper
	 */

	public static String serializePretty(Location loc) {
		long x = Math.round(loc.getX());
		long y = Math.round(loc.getY());
		long z = Math.round(loc.getZ());
		long yaw = Math.round(loc.getYaw());
		long pitch = Math.round(loc.getPitch());
		return x + ":" + y + ":" + z + ":" + yaw + ":" + pitch + ":" + loc.getWorld().getName();
	}

	public static String serialize(Location loc) {
		return loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch() + ":" + loc.getWorld().getName();
	}

	public static Location deserialize(String loc) {
		if (loc == null)
			return null;

		String rawLocation[] = loc.split(":");
		Double x = Double.parseDouble(rawLocation[0]);
		Double y = Double.parseDouble(rawLocation[1]);
		Double z = Double.parseDouble(rawLocation[2]);
		Float yaw = Float.parseFloat(rawLocation[3]);
		Float pitch = Float.parseFloat(rawLocation[4]);
		String world = rawLocation[5];

		return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
	}

	/*
	 * Guild Module
	 */

	public void loadGuilds() {
		MongoCursor<Document> cursor = guildCollection.find().iterator();

		while (cursor.hasNext()) {
			Document guildDoc = cursor.next();

			String name = guildDoc.getString("name");
			UUID leader = UUID.fromString(guildDoc.getString("leader"));
			ArrayList<String> rawMembers = new ArrayList<>(guildDoc.getList("members", String.class));
			ArrayList<UUID> members = new ArrayList<>();
			ArrayList<Integer> allies = new ArrayList<>();
			if (guildDoc.containsKey("allies")) {
				allies = new ArrayList<>(guildDoc.getList("allies", Integer.class));
			}
			LinkedList<String> chunks = new LinkedList<String>(guildDoc.getList("chunks", String.class));
			Integer id = guildDoc.getInteger("id", GuildModule.getId());

			for (String rawMember : rawMembers) {
				members.add(UUID.fromString(rawMember));
			}

			GuildModule.addGuild((new Guild(name, leader, id, members, chunks, allies)));
		}

		cursor.close();

		// Chunk restrictions
		ArrayList<String> claimLocked = (ArrayList<String>) getSettingList("claimLocked");
		ArrayList<String> serverClaimed = (ArrayList<String>) getSettingList("serverClaimed");

		if (claimLocked != null)
			GuildModule.getClaimLocked().addAll(claimLocked);

		if (serverClaimed != null)
			GuildModule.getServerClaimed().addAll(serverClaimed);
	}

	public void saveGuilds() {
		guildCollection.deleteMany(new Document());

		for (Guild guild : GuildModule.getGuilds()) {
			Document guildDoc = new Document("name", guild.getName());
			guildDoc.append("leader", guild.getLeader().toString());

			ArrayList<String> rawMembers = new ArrayList<>();
			for (UUID member : guild.getMembers()) {
				rawMembers.add(member.toString());
			}

			guildDoc.append("members", rawMembers);
			guildDoc.append("chunks", guild.getChunks());
			guildDoc.append("allies", guild.getAllies());
			guildDoc.append("id", guild.getId());

			guildCollection.insertOne(guildDoc);
		}

		// Chunk restrictions
		setSetting("claimLocked", GuildModule.getClaimLocked());
		setSetting("serverClaimed", GuildModule.getServerClaimed());
	}
}