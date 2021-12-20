package eu.ladeira.chromo;

import java.util.ArrayList;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;

public class LadeiraModule {

	public void onDisable() {

	}

	public CommandExecutor getExecutor() {
		return null;
	}

	public String getCmdName() {
		return null;
	}

	public ArrayList<Listener> getListeners() {
		return new ArrayList<Listener>();
	}
}
