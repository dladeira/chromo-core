package eu.ladeira.core;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import eu.ladeira.core.modules.AlertsModule;
import eu.ladeira.core.modules.DescriptorModule;
import eu.ladeira.core.modules.GuildModule;
import eu.ladeira.core.modules.ReputationModule;
import eu.ladeira.core.modules.ScoreboardModule;
import eu.ladeira.core.modules.SpawnManager;

public class Chromo extends JavaPlugin {
	
	private static Plugin plugin;
	private static Database db;
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	public static Database getDatabase() {
		return db;
	}
	
	private ArrayList<LadeiraModule> modules;
	
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
		
		for (LadeiraModule module : modules) {
			if (module instanceof Listener) {
				registerEvents((Listener) module);
			}
			
			if (module instanceof CommandExecutor) {
				getCommand(module.cmdName()).setExecutor((CommandExecutor) module); 
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
