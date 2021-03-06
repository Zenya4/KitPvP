package com.planetgallium.kitpvp;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.planetgallium.kitpvp.api.EventListener;
import com.planetgallium.kitpvp.command.*;
import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.listener.*;
import com.planetgallium.kitpvp.util.*;

public class Game extends JavaPlugin implements Listener {
	
	private static Game instance;
	private static String prefix;

	private Arena arena;
	private Database database;
	private Resources resources = new Resources(this);
	
	private String updateVersion = "Error";
	public String storageType;
	private boolean needsUpdate = false;
	private boolean hasPlaceholderAPI = false;
	
	@Override
	public void onEnable() {
		
		instance = this;

		resources.load();
		prefix = resources.getMessages().getString("Messages.General.Prefix");
		database = new Database(this, "Storage.MySQL");
		arena = new Arena(this, resources);

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(this, this);
		pm.registerEvents(new EventListener(this), this);
		pm.registerEvents(new ArenaListener(this), this);
		pm.registerEvents(new JoinListener(this), this);
		pm.registerEvents(new LeaveListener(this), this);
		pm.registerEvents(new ArrowListener(this), this);
		pm.registerEvents(new DeathListener(this), this);
		pm.registerEvents(new HitListener(this), this);
		pm.registerEvents(new AttackListener(this), this);
		pm.registerEvents(new ItemListener(this), this);
		pm.registerEvents(new SoupListener(this), this);
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new SignListener(this), this);
		pm.registerEvents(new AliasCommand(this), this);
		pm.registerEvents(new AbilityListener(this), this);
		pm.registerEvents(new TrackerListener(this), this);
		pm.registerEvents(new MenuListener(this), this);
		pm.registerEvents(getArena().getKillStreaks(), this);
		
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	    getCommand("kitpvp").setExecutor(new MainCommand(this));
	    
		Bukkit.getConsoleSender().sendMessage(Toolkit.translate("&7[&b&lKIT-PVP&7] &7Enabling &bKitPvP &7version &b" + this.getDescription().getVersion() + "&7..."));
		
		if (resources.getConfig().getString("Storage.Type").equalsIgnoreCase("mysql")) {
			storageType = "mysql";
			
			database.setup();
			database.holdConnection();
			database.createData();
		} else {
			storageType = "yaml";
		}
		
		new Metrics(this);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				checkUpdate();
				
			}
			
		}.runTaskAsynchronously(this);
		
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			Bukkit.getConsoleSender().sendMessage(Toolkit.translate("[&b&lKIT-PVP&7] &7Discovered &bPlaceholderAPI&7, now hooking into it."));
			new Placeholders(this).register();
			hasPlaceholderAPI = true;
		}
		
		Bukkit.getConsoleSender().sendMessage(Toolkit.translate("&7[&b&lKIT-PVP&7] &aDone!"));
		
	}

	@Override
	public void onDisable() {
		for (Player all : Bukkit.getOnlinePlayers()) {
			database.saveAndRemovePlayer(all);
		}
	}

	private void checkUpdate() {

		Updater.of(this).resourceId(27107).handleResponse((versionResponse, version) -> {
			switch (versionResponse) {
				case FOUND_NEW:
					Bukkit.getConsoleSender().sendMessage(Toolkit.translate("&7[&b&lKIT-PVP&7] &aNew version found! Please update to v" + version + " on the Spigot page."));
					needsUpdate = true;
					updateVersion = version;
					break;
				case LATEST:
					Bukkit.getConsoleSender().sendMessage(Toolkit.translate("&7[&b&lKIT-PVP&7] &7No new update found. You are on the latest version."));
					break;
				case UNAVAILABLE:
					Bukkit.getConsoleSender().sendMessage(Toolkit.translate("&7[&b&lKIT-PVP&7] &cUnable to perform an update check."));
					break;
			}
		}).check();
		
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		
		Player p = e.getPlayer();
		
		if (Toolkit.getMainHandItem(p).getType() == XMaterial.matchXMaterial(resources.getConfig().getString("Items.Leave.Material")).get().parseMaterial()) {
			
			if (resources.getConfig().getBoolean("Items.Leave.Enabled")) {
					
				if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
					if (resources.getConfig().getBoolean("Items.Leave.BungeeCord.Enabled")) {
						
				        ByteArrayDataOutput out = ByteStreams.newDataOutput();
				        out.writeUTF("Connect");
				        
				        String server = resources.getConfig().getString("Items.Leave.BungeeCord.Server");
				        
				        out.writeUTF(server);
				        p.sendPluginMessage(this, "BungeeCord", out.toByteArray());
				        
					}
					
				}

				e.setCancelled(true);
				
			}

		}
		
	}

	public boolean hasPlaceholderAPI() { return hasPlaceholderAPI; }

	public boolean needsUpdate() { return needsUpdate; }
	
	public String getUpdateVersion() { return updateVersion; }
	
	public static Game getInstance() { return instance; }
	
	public Arena getArena() { return arena; }
	
	public Database getDatabase() { return database; }
	
	public static String getPrefix() { return prefix; }
	
	public Resources getResources() { return resources; }
	
}
