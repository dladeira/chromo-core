package eu.ladeira.core;

import static com.mongodb.client.model.Filters.eq;

import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import net.md_5.bungee.api.ChatColor;

public class Database {

	private static String uri = "mongodb://localhost:27017/ladeiraCore";
	private static MongoClient client;
	private static MongoDatabase db;
	private static MongoCollection<Document> playerCollection;
	private static MongoCollection<Document> serverCollection;
	private static Document serverSettings;


	public Database() {
		client = MongoClients.create(uri);
		db = client.getDatabase("ladeira-core");
		
		playerCollection = db.getCollection("players");
		serverCollection = db.getCollection("server");
		
		// If server settings don't exist create them
		if (serverCollection.countDocuments() < 1) {
			serverCollection.insertOne(new Document());
		}
		
		serverSettings = serverCollection.find().first();
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
		return x + ":" + y + ":" + z + ":" + yaw + ":" + pitch + ":"
				+ loc.getWorld().getName();
	}
	
	public static String serialize(Location loc) {
		return loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch() + ":"
				+ loc.getWorld().getName();
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
}