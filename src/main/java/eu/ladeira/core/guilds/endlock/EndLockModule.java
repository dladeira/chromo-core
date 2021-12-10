package eu.ladeira.core.guilds.endlock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import eu.ladeira.core.Database;
import eu.ladeira.core.LadeiraModule;
import net.md_5.bungee.api.ChatColor;

public class EndLockModule extends LadeiraModule implements Listener {

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	private ScheduledFuture<?> endOpenTask;
	private ScheduledFuture<?> endPvpTask;
	private Date endLock;
	private boolean hardLocked;
	private Location endLocation;

	private Database db;
	private CmdEndLock cmd;

	public EndLockModule(Database db, Plugin plugin) {
		this.db = db;
		this.cmd = new CmdEndLock(this, plugin);

		if (db.getSetting("hardLocked") != null) {
			hardLocked = Integer.parseInt(db.getSetting("hardLocked")) == 1;
		} else {
			hardLocked = false;
		}
		if (db.getSetting("endSpawn") != null)
			endLocation = Database.deserialize(db.getSetting("endSpawn"));
		
		if (db.getSetting("endOpen") != null)
			endLock = new Date(Long.parseLong(db.getSetting("endOpen")));
	}

	@Override
	public void onDisable() {
		db.setSetting("hardLocked", hardLocked ? "1" : "0");

		if (endLocation != null) {
			db.setSetting("endSpawn", Database.serialize(endLocation));
		}
		
		if (endLock != null) {
			db.setSetting("endOpen", String.valueOf(endLock.getTime()));
		}
	}

	@Override
	public ArrayList<Listener> getListeners() {
		ArrayList<Listener> listeners = new ArrayList<>();
		listeners.add(new EndEvents(this));
		return listeners;
	}

	@Override
	public CommandExecutor getExecutor() {
		return cmd;
	}

	@Override
	public String getCmdName() {
		return "endlock";
	}
	
	public boolean setEndLock(Date date) {
		cancelEndLock();
		endLock = date;

		endOpenTask = scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				Bukkit.broadcastMessage(ChatColor.GRAY + "--------------------");
				Bukkit.broadcastMessage(ChatColor.RESET + "");
				Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "THE END HAS BEEN OPENED");
				Bukkit.broadcastMessage(ChatColor.WHITE + "  PvP and claiming are disabled in the end for 1 hour");
				Bukkit.broadcastMessage(ChatColor.WHITE + "  You can teleport to the end anytime for 1 hour");
				Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "  /endlock teleport");
				Bukkit.broadcastMessage(ChatColor.RESET + "");
				Bukkit.broadcastMessage(ChatColor.GRAY + "--------------------");
			}
		}, date.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

		endPvpTask = scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				Bukkit.broadcastMessage(ChatColor.GRAY + "--------------------");
				Bukkit.broadcastMessage(ChatColor.RESET + "");
				Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "End PvP and claiming enabled");
				Bukkit.broadcastMessage(ChatColor.RESET + "");
				Bukkit.broadcastMessage(ChatColor.GRAY + "--------------------");
			}
		}, date.getTime() + (3600 * 1000) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);

		return false;
	}

	public void cancelEndLock() {
		if (endOpenTask != null && !endOpenTask.isCancelled()) {
			endOpenTask.cancel(true);
		}

		if (endPvpTask != null && !endPvpTask.isCancelled()) {
			endPvpTask.cancel(true);
		}
	}

	public String getEndUnlockText() {
		if (hardLocked) {
			return "never";
		}
		
		if (endLock != null) {
			SimpleDateFormat dtf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			return dtf.format(endLock);
		}
		return "UNKNOWN";
	}

	public boolean isEndLocked() {
		if (hardLocked) {
			return true;
		}
		
		Date now = new Date();

		if (endLock == null) {
			return true;
		}

		return now.before(endLock);
	}

	public boolean isEndPvpEnabled() {
		if (hardLocked) {
			return false;
		}
		
		Date now = new Date();

		if (endLock == null) {
			return false;
		}

		return now.after(new Date(endLock.getTime() + (3600 * 1000)));
	}

	public void setHardLocked(boolean lock) {
		if (lock) {
			cancelEndLock();
		}
		
		hardLocked = lock;
	}

	public boolean isHardLocked() {
		return hardLocked;
	}
	
	public void setEndLocation(Location location) {
		this.endLocation = location;
	}
	
	public Location getEndLocation() {
		return endLocation;
	}
}