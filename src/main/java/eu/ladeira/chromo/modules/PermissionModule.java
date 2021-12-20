package eu.ladeira.chromo.modules;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import eu.ladeira.chromo.LadeiraModule;
import eu.ladeira.chromo.guilds.Guild;
import eu.ladeira.chromo.guilds.GuildModule;

public class PermissionModule extends LadeiraModule {

	private ArrayList<UUID> overriding;
	
	public PermissionModule() {
		overriding = new ArrayList<>();
	}
	
	public boolean canAttackEntity(Player player, Entity entity) {
		UUID playerUUID = player.getUniqueId();
		
		if (overriding.contains(player.getUniqueId())) {
			return true;
		}
		
		if (entity instanceof Player) {
			Player attacked = (Player) entity;
			Guild playerGuild = GuildModule.getGuild(playerUUID);
			
			if (playerGuild != null) {
				if (playerGuild.hasMember(attacked.getUniqueId())) {
					return false;
				}
			}
			
			return true;
		}
		
		if (!(entity instanceof Monster)) {
			Guild entityGuild = GuildModule.getGuild(entity.getLocation().getChunk());
			if (entityGuild != null) {
				if (entityGuild.hasMember(playerUUID)) {
					return true;
				}
				
				if (entityGuild.isAllied(player)) {
					return true;
				}
				
				return false;
			}
		}
		
		return true;
	}
	
	public boolean canModifyTerrain(Player player, Location location) {
		Chunk chunk = location.getChunk();
		Guild chunkGuild = GuildModule.getGuild(chunk);
		
		if (overriding.contains(player.getUniqueId())) {
			return true;
		}
		
		if (chunkGuild != null) {
			if (chunkGuild.hasMember(player.getUniqueId())) {
				return true;
			}
			
			if (chunkGuild.isAllied(player)) {
				return true;
			}
			
			return false;
		}
		
		return true;
	}
	
	public boolean canInteract(Player player, Location location) {
		return canModifyTerrain(player, location);
	}
	
	public ArrayList<UUID> getOverriding() {
		return this.overriding;
	}
	
	public void addOverrding(UUID uuid) {
		this.overriding.add(uuid);
	}
	
	public void removeOverriding(UUID uuid) {
		this.overriding.remove(uuid);
	}
	
	public boolean isOverriding(UUID uuid) {
		return this.overriding.contains(uuid);
	}
}
