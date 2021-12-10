package eu.ladeira.core.guilds;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import eu.ladeira.core.Chromo;
import eu.ladeira.core.modules.PermissionModule;

public class EventClaimActions implements Listener {

	private PermissionModule perms;

	public EventClaimActions() {
		this.perms = (PermissionModule) Chromo.getModule(PermissionModule.class);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerAttackMember(EntityDamageByEntityEvent e) {
			Player attacker = null;
			
			if (e.getDamager() instanceof Player) {
				attacker = (Player) e.getDamager(); 
			} else if (e.getDamager() instanceof Projectile) {
				if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
					attacker = (Player) ((Projectile) e.getDamager()).getShooter();
				}
			}
			
			if (attacker != null) {
				perms.canAttackEntity(attacker, e.getEntity());
			}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void interactEvent(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		if (e.getClickedBlock() == null) { // Hit the air
			return;
		}

		if (e.getClickedBlock().getType().isInteractable()) { // Interacted with a door
			e.setCancelled(!perms.canInteract(player, e.getClickedBlock()));
		}
	}

	public void blockPlaceEvent(BlockPlaceEvent e) {
		e.setCancelled(!perms.canModifyTerrain(e.getPlayer(), e.getBlock().getLocation()));
	}

	public void blockBreakEvent(BlockBreakEvent e) {
		e.setCancelled(!perms.canModifyTerrain(e.getPlayer(), e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void entityInteract(PlayerInteractAtEntityEvent e) {
		Player player = e.getPlayer();
		Guild chunkGuild = GuildModule.getGuild(e.getRightClicked().getLocation().getChunk());

		if (chunkGuild != null && !(chunkGuild.hasMember(player.getUniqueId()) || chunkGuild.isAllied(player))) {
			e.setCancelled(true);
		}

		if (GuildModule.isServerClaimed(player.getLocation().getChunk())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void entityInteract(EntityDamageByEntityEvent e) {
		Player player = null;
		if (e.getDamager() instanceof Player) {
			player = (Player) e.getDamager();
		} else if (e.getDamager() instanceof Projectile) {
			if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
				player = (Player) ((Projectile) e.getDamager()).getShooter();
			}
		}

		if (player != null) {

			if (!(e.getEntity() instanceof Player)) {
				Entity damaged = e.getEntity();
				Chunk chunk = damaged.getLocation().getChunk();
				Guild chunkGuild = GuildModule.getGuild(chunk);

				if (e.getEntity() instanceof Monster) {
					return;
				}

				if (chunkGuild != null) {
					e.setCancelled(!(chunkGuild.hasMember(player.getUniqueId()) || chunkGuild.isAllied(player)));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		Player player = e.getPlayer();
		Location location = e.getBlock().getLocation();
		Chunk chunk = location.getChunk();
		Guild chunkGuild = GuildModule.getGuild(chunk);


		if (GuildModule.isServerClaimed(chunk)) {
			e.setCancelled(true);
		}

		if (chunkGuild != null && !(chunkGuild.hasMember(player.getUniqueId()) || chunkGuild.isAllied(player))) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockForm(BlockFormEvent e) {
		Location location = e.getBlock().getLocation();

		if (GuildModule.getGuild(location.getChunk()) != null) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onTntExplode(BlockExplodeEvent e) {
		Location location = e.getBlock().getLocation();

		if (GuildModule.getGuild(location.getChunk()) != null) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onCreeperExplode(EntityExplodeEvent e) {
		Location location = e.getLocation();

		if (GuildModule.getGuild(location.getChunk()) != null) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onHangingBreak(HangingBreakByEntityEvent e) {
		Player player;
		if (e.getRemover() instanceof Player) {
			player = (Player) e.getRemover();
		} else if (e.getRemover() instanceof Projectile && ((Projectile) e.getRemover()).getShooter() instanceof Player) {
			player = (Player) ((Projectile) e.getRemover()).getShooter();
		} else {
			return;
		}

		Guild chunkGuild = GuildModule.getGuild(e.getEntity().getLocation().getChunk());

		if (chunkGuild != null) {
			e.setCancelled(!(chunkGuild.hasMember(player.getUniqueId()) || chunkGuild.isAllied(player)));
		}
	}
}
