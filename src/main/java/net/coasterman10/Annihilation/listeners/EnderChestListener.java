package net.coasterman10.Annihilation.listeners;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import net.coasterman10.Annihilation.object.GameTeam;
import net.coasterman10.Annihilation.object.PlayerMeta;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnderChestListener implements Listener {
	private HashMap<GameTeam, Location> chests = new HashMap<GameTeam, Location>();
	private HashMap<String, Inventory> inventories = new HashMap<String, Inventory>();

	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (e.getClickedBlock().getType() != Material.ENDER_CHEST)
			return;

		Block clicked = e.getClickedBlock();
		Player player = e.getPlayer();
		GameTeam team = PlayerMeta.getMeta(player).getTeam();
		if (team == GameTeam.NONE || !chests.containsKey(team))
			return;
		e.setCancelled(true);
		if (chests.get(team).equals(clicked.getLocation())) {
			openEnderChest(player);
		} else {
			GameTeam owner = getTeamWithChest(clicked.getLocation());
			if (owner != GameTeam.NONE) {
				openEnemyEnderChest(player, owner);
			}
		}
	}

	public void setEnderChestLocation(GameTeam team, Location loc) {
		chests.put(team, loc);
	}

	private void openEnderChest(Player player) {
		String name = player.getName();
		if (!inventories.containsKey(name)) {
			Inventory inv = Bukkit.createInventory(null, 9);
			inventories.put(name, inv);
		}
		player.openInventory(inventories.get(name));
	}

	@EventHandler
	public void onFurnaceBreak(BlockBreakEvent e) {
		if (chests.values().contains(e.getBlock().getLocation()))
			e.setCancelled(true);
	}

	
	private void openEnemyEnderChest(Player player, GameTeam owner) {
		LinkedList<Inventory> shuffledInventories = new LinkedList<Inventory>();
		for (Entry<String, Inventory> entry : inventories.entrySet())
			if (PlayerMeta.getMeta(entry.getKey()).getTeam() == owner)
				shuffledInventories.add(entry.getValue());
		Collections.shuffle(shuffledInventories);

		int inventories = Math.min(9, shuffledInventories.size());
		if (inventories == 0)
			return;
		Inventory view = Bukkit.createInventory(null, inventories * 9);
		for (Inventory inv : shuffledInventories.subList(0, inventories)) {
			for (ItemStack stack : inv.getContents())
				if (stack != null)
					view.addItem(stack);
		}
		player.openInventory(view);
	}

	private GameTeam getTeamWithChest(Location loc) {
		for (Entry<GameTeam, Location> entry : chests.entrySet())
			if (entry.getValue().equals(loc))
				return entry.getKey();
		return GameTeam.NONE;
	}
}
