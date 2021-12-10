package eu.ladeira.core.guilds.endlock;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.md_5.bungee.api.ChatColor;

public class EndEvents implements Listener {
	
	private EndLockModule module;
	
	public EndEvents(EndLockModule module) {
		this.module = module;
	}
	
	@EventHandler
	public void onPlayerEndEnter(PlayerPortalEvent e) {
		Player player = e.getPlayer();
		player.sendMessage("triggering event");
		if (e.getCause().equals(TeleportCause.END_PORTAL)) {
			if (module.isEndLocked()) {
				player.sendMessage(ChatColor.GRAY + "The end is locked till " + ChatColor.WHITE + module.getEndUnlockText());
			} else {
				player.teleport(module.getEndLocation());
				player.sendMessage(ChatColor.GRAY + "Teleporting to the end");
			}
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerAttackInEnd(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Player) {
				Player attacked = (Player) e.getEntity();

				if (attacked.getLocation().getWorld().getName().equals("world_the_end")) {
					e.setCancelled(!module.isEndPvpEnabled());
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerAttackInEndProjectile(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Projectile) {
				Player attacked = (Player) e.getEntity();
				Projectile attackerProjectile = (Projectile) e.getDamager();
				if (attackerProjectile.getShooter() instanceof Player) {
					if (attacked.getLocation().getWorld().getName().equals("world_the_end")) {
						e.setCancelled(!module.isEndPvpEnabled());
					}
				}
			}
		}
	}
}
