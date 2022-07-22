package me.WildAbbee.EscapeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EscapeEvent extends JavaPlugin implements Listener {

	public static EscapeEvent inst;
	public final HashMap<Player, Location> checkpoints = new HashMap<>();
	public final HashMap<Player, ArrayList<Location>> eggs = new HashMap<>();
	public final HashMap<Player, Long> lastMessages = new HashMap<>();
	public final HashMap<Player, Integer> deaths = new HashMap<>();
	public int amount = 1;
	private ItemStack phoenixItem;

	public void onEnable() {
		phoenixItem = new ItemStack(Material.LEATHER);
		final ItemMeta meta = phoenixItem.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Stuffed Bird Toy");
		meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&fAn &6overwhelming power"),
				ChatColor.translateAlternateColorCodes('&', "&fradiating &6divinity"),
				ChatColor.translateAlternateColorCodes('&', "&flays dormant.")));
		phoenixItem.setItemMeta(meta);

		inst = this;
		this.getServer().getPluginManager().registerEvents(this, this);

		getCommand("lokireset").setExecutor(new ResetCommand());
		getCommand("updateplayers").setExecutor(new UpdatePlayersCommand());

		new BukkitRunnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {

					if (p.getGameMode() != GameMode.ADVENTURE) {
						continue;
					}

					p.setMaxHealth(1);

					if (p.getInventory().containsAtLeast(phoenixItem, 1)) {
						continue;
					}

					p.getInventory().addItem(phoenixItem);
				}
			}
		}.runTaskTimer(this, 40, 40);
	}

	// for thiemo xoxo
	@EventHandler
	void onGameModeUpdate(PlayerGameModeChangeEvent e) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (e.getNewGameMode() == GameMode.ADVENTURE) e.getPlayer().hidePlayer(p);
			else e.getPlayer().showPlayer(p);
		}
	}

	// called once
	public void hidePlayers() {
		for (Player p : Bukkit.getOnlinePlayers()) {

			if (p.getGameMode() != GameMode.ADVENTURE) {
				for (Player p2 : Bukkit.getOnlinePlayers()) {
					p.showPlayer(p2);
				}
				continue;
			}

				for (Player p2 : Bukkit.getOnlinePlayers()) {
					p.hidePlayer(p2);
				}
			
		}
	}

	@EventHandler
	void onInteractWithSign(PlayerInteractEvent e) {

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (e.getClickedBlock().getType() != Material.WALL_SIGN
				&& e.getClickedBlock().getType() != Material.SIGN_POST) {
			return;
		}

		final Sign sign = (Sign) e.getClickedBlock().getState();

		if (sign.getLine(0) == null) {
			return;
		}

		final Player player = e.getPlayer();

		if (sign.getLine(0).equalsIgnoreCase("[Checkpoint]")) {
			// checkpoint
			this.checkpoints.put(player, player.getLocation());
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&6&lCHECKPOINT! &fYou will respawn at this location."));
			return;
		} else if (sign.getLine(0).replace(" ", "").equalsIgnoreCase("[EasterEgg]")
				&& e.getPlayer().getGameMode() == GameMode.ADVENTURE) {

			// easter egg
			this.eggs.putIfAbsent(player, new ArrayList<>());
			ArrayList<Location> playerEggs = this.eggs.get(player);

			// already found
			if (playerEggs.contains(sign.getLocation())) {
				player.sendMessage(
						ChatColor.translateAlternateColorCodes('&', "&c&lSILLY! &fYou already found this easter egg."));
				return;
			}

			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&6&lEASTER EGG! &f" + sign.getLine(1) + " " + sign.getLine(2) + " " + sign.getLine(3)));
			playerEggs.add(sign.getLocation());
			eggs.put(player, playerEggs);

			if (playerEggs.size() >= amount) {
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&6&lAMAZING! &e" + player.getName()
						+ " &fhas found all " + playerEggs.size() + " easter eggs!"));
			}

			return;
		} else if (sign.getLine(0).replace(" ", "").equalsIgnoreCase("[Finish]")
				&& e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
			deaths.putIfAbsent(player, 0);
			final int deathCount = deaths.get(player);
			
			int survivalPlayers = 1;
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getGameMode() == GameMode.SURVIVAL) survivalPlayers++;
			}
			
			Bukkit.broadcastMessage(
					ChatColor.translateAlternateColorCodes('&', "&6&lESCAPE! &e" + e.getPlayer().getName()
							+ " &fhas escaped &4Loki's Labyrinth &fwith &c" + deathCount + " deaths&f! &7" + survivalPlayers + Utils.th(survivalPlayers)));
			player.setGameMode(GameMode.SURVIVAL);
			player.getInventory().clear();
			player.setAllowFlight(true);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lPHOENIX &fYour phoenix has evolved and grants you flight and fire immunity!"));

		}

	}

	@EventHandler
	void onLeave(PlayerQuitEvent e) {
		checkpoints.remove(e.getPlayer());
		eggs.remove(e.getPlayer());
		lastMessages.remove(e.getPlayer());
		deaths.remove(e.getPlayer());
	}

	@EventHandler
	void onDrop(PlayerDropItemEvent e) {
		if (e.getPlayer().getGameMode() == GameMode.ADVENTURE) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&6&lPHOENIX &fThe &6phoenix &fis displeased with your actions."));
			e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 3));
		}
	}

	@EventHandler
	void onDamage(EntityDamageEvent e) {
		if ((e.getCause() == DamageCause.SUFFOCATION || e.getCause() == DamageCause.FALL)
				&& e.getEntity() instanceof Player) {
			e.setCancelled(true);
		} else if (e.getEntity() instanceof Player && ((Player)e.getEntity()).getGameMode() == GameMode.ADVENTURE) {
			e.setCancelled(true);
			final Location location = checkpoints.get(e.getEntity());
			final Player player = (Player) e.getEntity();


			this.lastMessages.putIfAbsent(player, 0l);
			if (this.lastMessages.get(player) + 1000 < System.currentTimeMillis()) {
				deaths.putIfAbsent(player, 0);
				deaths.put(player, deaths.get(player) + 1);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&6&lPHOENIX &fThe &6legendary &fpower of the &6phoenix &fgrants you another life."));
				
				if (location != null) {
					player.teleport(location);
				} else {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp loki " + player.getName());
				}
			}

			this.lastMessages.put(player, System.currentTimeMillis());
			player.setFireTicks(0);
			player.setMaxHealth(1);
		}
		e.setCancelled(true);
	}

	@EventHandler
	void onInvClick(InventoryClickEvent e) {
		if (e.getInventory().getType() == InventoryType.DISPENSER
				&& e.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
			e.setCancelled(true);
		}
	}
}
