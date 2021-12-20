package eu.ladeira.chromo;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import eu.ladeira.chromo.endlock.EndLockModule;
import eu.ladeira.chromo.guilds.GuildModule;
import eu.ladeira.chromo.modules.AlertsModule;
import eu.ladeira.chromo.modules.DescriptorModule;
import eu.ladeira.chromo.modules.PermissionModule;
import eu.ladeira.chromo.modules.ReputationModule;
import eu.ladeira.chromo.modules.ScoreboardModule;
import eu.ladeira.chromo.modules.SpawnManager;

public class Chromo extends JavaPlugin {
	
	private static Plugin plugin;
	private static Database db;
	private static ArrayList<LadeiraModule> modules;
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	public static Database getDatabase() {
		return db;
	}
	
	public static LadeiraModule getModule(Class<?> moduleType) {
		for (LadeiraModule module : modules) {
			if (module.getClass().isAssignableFrom(moduleType)) {
				return module;
			}
		}
		
		return null;
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		db = new Database(this);
		
		modules = new ArrayList<>();
		modules.add(new SpawnManager(db));
		modules.add(new ScoreboardModule(db, plugin));
		modules.add(new DescriptorModule(db, plugin));
		modules.add(new ReputationModule(db, plugin));
		modules.add(new AlertsModule(db));
		modules.add(new GuildModule());
		modules.add(new PermissionModule());
		modules.add(new EndLockModule(db, plugin));
		
		for (LadeiraModule module : modules) {
			if (module instanceof Listener) {
				registerEvents((Listener) module);
			}
			
			for (Listener listener : module.getListeners()) {
				registerEvents(listener);
			}
			
			if (module.getExecutor() != null) {
				getCommand(module.getCmdName()).setExecutor((CommandExecutor) module.getExecutor()); 
			}
		}
	}
	
	@Override
	public void onDisable() {
		for (LadeiraModule module : modules) {
			module.onDisable();
		}
		
		plugin = null;
		db = null;
	}
	
	public void registerEvents(Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getPluginManager().registerEvents(listener, plugin);
		}
	}
}
