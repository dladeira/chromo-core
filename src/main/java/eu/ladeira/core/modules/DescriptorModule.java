package eu.ladeira.core.modules;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftSilverfish;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import eu.ladeira.core.Chromo;
import eu.ladeira.core.Database;
import eu.ladeira.core.LadeiraModule;
import eu.ladeira.core.guilds.Guild;
import eu.ladeira.core.guilds.GuildModule;
import eu.ladeira.core.guilds.endlock.EndLockModule;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;

public class DescriptorModule extends LadeiraModule implements Listener {

	public HashMap<Player, PlayerDescriptor> descriptors;
	private Database db;

	public DescriptorModule(final Database db, Plugin plugin) {
		this.db = db;
		descriptors = new HashMap<>();

		for (Player online : Bukkit.getOnlinePlayers()) {
			descriptors.put(online, new PlayerDescriptor(online));
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player online : descriptors.keySet()) {
					PlayerDescriptor descriptor = descriptors.get(online);

					if (!online.getGameMode().equals(GameMode.SPECTATOR)) {
						int reputation = db.getPlayerInt(online.getUniqueId(), "reputation");
						descriptor.setText(0, db.getName(online.getUniqueId()));
						descriptor.setText(1, ChatColor.GRAY + "Rep: " + ReputationModule.getReputationColor(reputation) + reputation);
						Guild playerGuild = GuildModule.getGuild(online.getUniqueId());
						if (playerGuild != null) {
							descriptor.setText(2, ChatColor.GRAY + "Guild: " + ChatColor.WHITE + playerGuild.getName());
						} else {
							descriptor.setText(2, ChatColor.GRAY + "Guild: " + ChatColor.WHITE + "NONE");
						}

						Material standingIn = online.getWorld().getBlockAt(online.getLocation()).getType();

						for (int y = -2; y <= 2; y++) {
							Block block = online.getWorld().getBlockAt(online.getLocation().add(new Location(online.getWorld(), 0, y, 0)));
							
							if (block.getType().equals(Material.END_PORTAL)) {
								online.eject();
								online.teleport(((EndLockModule) Chromo.getModule(EndLockModule.class)).getEndLocation());
							}
						}

						if (standingIn.equals(Material.NETHER_PORTAL) || standingIn.equals(Material.END_PORTAL)) {
							online.eject();
						} else {
							if (online.getPassengers().size() < 1 && online.getHealth() > 0) {

								descriptor.reload();
							}
						}
					} else {
						descriptors.get(online).removeEntities();
					}
					descriptor.removeForPlayer(online);
				}
			}
		}.runTaskTimer(plugin, 10, 10);

	}

	@Override
	public void onDisable() {
		for (Player online : descriptors.keySet()) {
			descriptors.get(online).remove();
		}
	}

	@EventHandler
	public void addDescriptor(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		descriptors.put(player, new PlayerDescriptor(player));
	}

	@EventHandler
	public void removeDescriptor(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		PlayerDescriptor descriptor = descriptors.get(player);
		if (descriptor != null) {
			descriptor.remove();
		}
		descriptors.remove(player);
	}

	@EventHandler
	public void removeNameOnWorldSwitch(PlayerTeleportEvent e) {
		Player player = e.getPlayer();
		if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName())) {
			descriptors.get(player).removeEntities();
		}
	}

	private class PlayerDescriptor {
		private Player player;
		private HashMap<Integer, Layer> layers;

		public PlayerDescriptor(Player player) {
			this.player = player;
			layers = new HashMap<>();

			Layer nameLayer;
			nameLayer = new Layer(db.getName(player.getUniqueId()), player.getLocation());
			nameLayer.rideEntity(player);
			layers.put(0, nameLayer);
		}

		public void setText(int layerIndex, String text) {
			Layer layer = layers.get(layerIndex);
			if (layer != null) {
				layer.setText(text);
			} else {
				layers.put(layerIndex, new Layer(text, player.getLocation()));
				reload();
			}
		}

		public void reload() {
			ArrayList<String> texts = new ArrayList<>();
			for (Integer layerIndex : layers.keySet()) {
				Layer layer = layers.get(layerIndex);
				texts.add(layer.getText());
				layer.remove();
			}

			layers = new HashMap<>();
			Layer lastLayer = null;
			int counter = 0;
			for (String text : texts) {
				Layer layer = new Layer(text, player.getLocation());
				layers.put(counter++, layer);

				if (lastLayer == null) {
					layer.rideEntity(player);
				} else {
					layer.rideEntity(lastLayer.stand);
				}

				lastLayer = layer;
			}
		}

		public void remove() {
			removeEntities();

			layers = new HashMap<>();
		}

		public void removeEntities() {
			for (Integer layerIndex : layers.keySet()) {
				Layer layer = layers.get(layerIndex);
				layer.remove();
			}
		}

		public void removeForPlayer(Player player) {
			for (Integer layerIndex : layers.keySet()) {
				Layer layer = layers.get(layerIndex);
				layer.sendRemovePacket(player);
			}
		}

		private class Layer {

			private String name;
			private Silverfish silverfish;
			private ArmorStand stand;

			public Layer(String name, Location location) {
				this.name = name;
				silverfish = getSilverfish(location);
				stand = getStand(location);
				stand.setCustomName(name);
			}

			public void setText(String name) {
				this.name = name;
				stand.setCustomName(name);
			}

			public String getText() {
				return this.name;
			}

			public void rideEntity(Entity entity) {
				entity.addPassenger(silverfish);
				silverfish.addPassenger(stand);
			}

			public Silverfish getSilverfish(Location location) {
				Silverfish fish = (Silverfish) location.getWorld().spawnEntity(location, EntityType.SILVERFISH);

				fish.setAI(false);
				fish.setInvisible(true);
				fish.setInvulnerable(true);
				fish.setSilent(true);

				return fish;
			}

			public ArmorStand getStand(Location location) {
				ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

				stand.setInvulnerable(true);
				stand.setInvisible(true);
				stand.setMarker(true);
				stand.setSmall(true);
				stand.setCustomNameVisible(true);

				return stand;
			}

			public void sendRemovePacket(Player player) {
				((CraftPlayer) player).getHandle().b.a(new PacketPlayOutEntityDestroy(((CraftSilverfish) silverfish).getEntityId()));
				((CraftPlayer) player).getHandle().b.a(new PacketPlayOutEntityDestroy(((CraftArmorStand) stand).getEntityId()));
			}

			public void remove() {
				stand.remove();
				silverfish.remove();
			}
		}
	}
}
