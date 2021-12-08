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
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventClaimActions implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerAttackMember(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Player) {
				Player attacked = (Player) e.getEntity();
				Player attacker = (Player) e.getDamager();

				if (CmdGuild.isOverriding(attacker)) {
					return;
				}

				Guild attackedGuild = GuildModule.getGuild(attacked.getUniqueId());

				if (attackedGuild != null) {
					e.setCancelled(attackedGuild.hasMember(attacker.getUniqueId()));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerAttackMemberProjectile(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getDamager() instanceof Projectile) {
				Player attacked = (Player) e.getEntity();
				Projectile attackerProj = (Projectile) e.getDamager();

				if (attackerProj.getShooter() instanceof Player) {
					Player attacker = (Player) attackerProj.getShooter();

					if (CmdGuild.isOverriding(attacker)) {
						return;
					}

					Guild attackedGuild = GuildModule.getGuild(attacked.getUniqueId());

					if (attackedGuild != null) {
						e.setCancelled(attackedGuild.hasMember(attacker.getUniqueId()));
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void interactEvent(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		if (CmdGuild.isOverriding(player)) {
			return;
		}

		if (e.getClickedBlock() == null) {
			return;
		}

		Guild chunkGuild = GuildModule.getGuild(e.getClickedBlock().getLocation().getChunk());

		if (chunkGuild != null && !chunkGuild.hasMember(player.getUniqueId())) {
			e.setCancelled(true);
		}

		if (GuildModule.isServerClaimed(player.getLocation().getChunk())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void entityInteract(PlayerInteractAtEntityEvent e) {
		Player player = e.getPlayer();
		Guild chunkGuild = GuildModule.getGuild(e.getRightClicked().getLocation().getChunk());

		if (CmdGuild.isOverriding(player)) {
			return;
		}

		if (chunkGuild != null && !chunkGuild.hasMember(player.getUniqueId())) {
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
			if (CmdGuild.isOverriding(player)) {
				return;
			}

			if (!(e.getEntity() instanceof Player)) {
				Entity damaged = e.getEntity();
				Chunk chunk = damaged.getLocation().getChunk();
				Guild chunkGuild = GuildModule.getGuild(chunk);

				if (e.getEntity() instanceof Monster) {
					return;
				}
				
				if (chunkGuild != null) {
					e.setCancelled(!chunkGuild.hasMember(player.getUniqueId()));
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		Player player = e.getPlayer();
		Location location = e.getBlock().getLocation();
		Chunk chunk = location.getChunk();
		Guild chunkGuild = GuildModule.getGuild(chunk);

		if (CmdGuild.isOverriding(player)) {
			return;
		}

		if (GuildModule.isServerClaimed(chunk)) {
			e.setCancelled(true);
		}

		if (chunkGuild != null && !chunkGuild.hasMember(player.getUniqueId())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
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
		if (!(e.getRemover() instanceof Player)) {
			return;
		}

		Player player = (Player) e.getRemover();
		Guild chunkGuild = GuildModule.getGuild(e.getEntity().getLocation().getChunk());

		if (CmdGuild.isOverriding(player)) {
			return;
		}

		if (chunkGuild != null) {
			e.setCancelled(!chunkGuild.hasMember(player.getUniqueId()));
		}
	}
}
