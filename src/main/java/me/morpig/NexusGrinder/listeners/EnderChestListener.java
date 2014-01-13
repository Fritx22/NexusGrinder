package me.morpig.NexusGrinder.listeners;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import me.morpig.NexusGrinder.NexusGrinderTeam;
import me.morpig.NexusGrinder.PlayerMeta;

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
	private HashMap<NexusGrinderTeam, Location> chests = new HashMap<NexusGrinderTeam, Location>();
	private HashMap<String, Inventory> inventories = new HashMap<String, Inventory>();

	@EventHandler
	public void onChestOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (e.getClickedBlock().getType() != Material.ENDER_CHEST)
			return;

		Block clicked = e.getClickedBlock();
		Player player = e.getPlayer();
		NexusGrinderTeam team = PlayerMeta.getMeta(player).getTeam();
		if (team == NexusGrinderTeam.NONE || !chests.containsKey(team))
			return;
		e.setCancelled(true);
		if (chests.get(team).equals(clicked.getLocation())) {
			openEnderChest(player);
		} else {
			NexusGrinderTeam owner = getTeamWithChest(clicked.getLocation());
			if (owner != NexusGrinderTeam.NONE) {
				openEnemyEnderChest(player, owner);
			}
		}
	}

	public void setEnderChestLocation(NexusGrinderTeam team, Location loc) {
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

	
	private void openEnemyEnderChest(Player player, NexusGrinderTeam owner) {
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

	private NexusGrinderTeam getTeamWithChest(Location loc) {
		for (Entry<NexusGrinderTeam, Location> entry : chests.entrySet())
			if (entry.getValue().equals(loc))
				return entry.getKey();
		return NexusGrinderTeam.NONE;
	}
}
