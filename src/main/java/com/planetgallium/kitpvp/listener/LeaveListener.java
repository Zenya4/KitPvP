package com.planetgallium.kitpvp.listener;

import com.planetgallium.kitpvp.util.CacheManager;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.game.Arena;

public class LeaveListener implements Listener {

	private Game plugin;
	private Arena arena;
	
	public LeaveListener(Game plugin) {
		this.plugin = plugin;
		this.arena = plugin.getArena();
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		
		Player p = e.getPlayer();

		if (Toolkit.inArena(p)) {

			arena.deletePlayer(p);
			plugin.getDatabase().saveAndRemovePlayer(p);

		}
		
	}
	
}
