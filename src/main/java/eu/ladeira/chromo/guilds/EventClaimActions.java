package eu.ladeira.chromo.guilds;

import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import eu.ladeira.chromo.Chromo;
import eu.ladeira.chromo.modules.PermissionModule;

public class EventClaimActions implements Listener {

	private PermissionModule perms;

	public EventClaimActions() {
		this.perms = (PermissionModule) Chromo.getModule(PermissionModule.class);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerAttackEntity(EntityDamageByEntityEvent e) {
			Player attacker = null;
			
			if (e.getDamager() instanceof Player) {
				attacker = (Player) e.getDamager(); 
			} else if (e.getDamager() instanceof Projectile) {
				if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
					attacker = (Player) ((Projectile) e.getDamager()).getShooter();
				}
			}
			
			if (attacker != null) {
				e.setCancelled(!perms.canAttackEntity(attacker, e.getEntity()));
			}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void interactEvent(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		if (e.getClickedBlock() == null) { // Hit the air
			return;
		}
		
		Material mainHand = player.getInventory().getItemInMainHand().getType();
		if (mainHand.isBlock() || mainHand.equals(Material.ARMOR_STAND) || mainHand.name().toLowerCase().contains("shulker")) {
			if (!perms.canModifyTerrain(player, e.getClickedBlock().getLocation())) {
				e.setCancelled(true);
				return;
			}
		}
		
		if (e.getClickedBlock().getType().isInteractable()) { // Interacted with a door
			if (!perms.canInteract(player, e.getClickedBlock().getLocation())) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockPlaceEvent(BlockPlaceEvent e) {
		e.setCancelled(!perms.canModifyTerrain(e.getPlayer(), e.getBlock().getLocation()));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void blockBreakEvent(BlockBreakEvent e) {
		e.setCancelled(!perms.canModifyTerrain(e.getPlayer(), e.getBlock().getLocation()));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onHangingPlace(HangingPlaceEvent e) {
		e.setCancelled(!perms.canModifyTerrain(e.getPlayer(), e.getBlock().getLocation()));
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onHangingBreak(HangingBreakByEntityEvent e) {
		Player attacker = null;
		
		if (e.getRemover() instanceof Player) {
			attacker = (Player) e.getRemover(); 
		} else if (e.getRemover() instanceof Projectile) {
			if (((Projectile) e.getRemover()).getShooter() instanceof Player) {
				attacker = (Player) ((Projectile) e.getRemover()).getShooter();
			}
		}
		
		if (attacker != null) {
			e.setCancelled(!perms.canAttackEntity(attacker, e.getEntity()));
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void entityInteract(PlayerInteractAtEntityEvent e) {
		e.setCancelled(!perms.canInteract(e.getPlayer(), e.getRightClicked().getLocation()));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		e.setCancelled(!perms.canModifyTerrain(e.getPlayer(), e.getBlock().getLocation()));
	}

	// PREVENT GRIEFING
	
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
}
