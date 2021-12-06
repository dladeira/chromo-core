package eu.ladeira.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import eu.ladeira.core.modules.Alerts;
import eu.ladeira.core.modules.DescriptorManager;
import eu.ladeira.core.modules.ReputationManager;
import eu.ladeira.core.modules.ScoreboardManager;
import eu.ladeira.core.modules.SpawnManager;

public class LadeiraCore extends JavaPlugin {
	
	private static HashMap<String, Plugin> externalModules;
	private static String externalModuleList = "SurvivalGuilds";
	
	public static boolean hasExternalModule(String name) {
		return externalModules.containsKey(name);
	}
	
	public static Plugin getExternalModule(String name) {
		return externalModules.get(name);
	}
	
	public Database db;
	private Plugin plugin;
	private ArrayList<LadeiraModule> modules;
	
	@Override
	public void onEnable() {
		plugin = this;
		db = new Database(this);
		externalModules = new HashMap<>();
		
		modules = new ArrayList<>();
		modules.add(new SpawnManager(db));
		modules.add(new ScoreboardManager(db, plugin));
		modules.add(new DescriptorManager(db, plugin));
		modules.add(new ReputationManager(db, plugin));
		modules.add(new Alerts(db));
		
		for (LadeiraModule module : modules) {
			if (module instanceof Listener) {
				registerEvents((Listener) module);
			}
			
			if (module instanceof CommandExecutor) {
				getCommand(module.cmdName()).setExecutor((CommandExecutor) module); 
			}
		}
		
		for (String pluginName : externalModuleList.split(":")) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
			if (plugin != null) {
				externalModules.put(pluginName, plugin);
			}
		}
	}
	
	@Override
	public void onDisable() {
		for (LadeiraModule module : modules) {
			module.onDisable();
		}
		
		plugin = null;
	}
	
	public void registerEvents(Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getPluginManager().registerEvents(listener, plugin);
		}
	}
}
