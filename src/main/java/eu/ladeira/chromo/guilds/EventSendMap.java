package eu.ladeira.chromo.guilds;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class EventSendMap implements Listener {

	private static ArrayList<UUID> mapUpdate = new ArrayList<>();
	private CmdGuild guildCmd;

	public EventSendMap() {
		this.guildCmd = new CmdGuild();
	}

	@EventHandler
	public void onPlayerEnterNewChunk(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		Guild playerGuild = GuildModule.getGuild(player.getUniqueId());

		// Player entered new chunk
		if (!e.getFrom().getChunk().equals(e.getTo().getChunk())) {
			if (mapUpdate.contains(player.getUniqueId())) {
				guildCmd.sendMap(player, e.getTo());
			}

			Guild enterGuild = GuildModule.getGuild(e.getTo().getChunk());
			Guild exitGuild = GuildModule.getGuild(e.getFrom().getChunk());

			String exiting = ChatColor.WHITE + "wilderness";
			String entering = ChatColor.WHITE + "wilderness";

			if (enterGuild != null) {
				entering = enterGuild.getName();

				if (playerGuild != null) {
					if (playerGuild.getName().equals(entering)) {
						entering = ChatColor.GREEN + entering;
					} else {
						entering = ChatColor.RED + entering;
					}
				}
			}

			if (exitGuild != null) {
				exiting = exitGuild.getName();

				if (playerGuild != null) {
					if (playerGuild.getName().equals(exiting)) {
						exiting = ChatColor.GREEN + exiting;
					} else {
						exiting = ChatColor.RED + exiting;
					}
				}
			}

			if (!entering.equals(exiting)) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
						TextComponent.fromLegacyText(ChatColor.GRAY + "Entered " + entering));
			}
		}
	}

	@EventHandler
	public void onPlayerLeaveServer(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		removeMapUpdate(player);
	}

	public static void addMapUpdate(Player p) {
		mapUpdate.add(p.getUniqueId());
	}

	public static void removeMapUpdate(Player p) {
		mapUpdate.remove(p.getUniqueId());
	}
}
