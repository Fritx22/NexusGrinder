package net.coasterman10.Annihilation.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventoryFurnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.object.GameTeam;
import net.coasterman10.Annihilation.object.PlayerMeta;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.TileEntityFurnace;

public class EnderFurnaceListener implements Listener {
	private HashMap<GameTeam, Location> locations;
	private HashMap<String, VirtualFurnace> furnaces;

	public EnderFurnaceListener(Annihilation plugin) {
		locations = new HashMap<GameTeam, Location>();
		furnaces = new HashMap<String, VirtualFurnace>();
		
		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				for (VirtualFurnace f : furnaces.values())
					f.h();
			}
		}, 0L, 1L);
	}

	public void setFurnaceLocation(GameTeam team, Location loc) {
		locations.put(team, loc);
	}

	@EventHandler
	public void onFurnaceOpen(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Block b = e.getClickedBlock();
		if (b.getType() != Material.FURNACE)
			return;

		Location loc = b.getLocation();
		Player player = e.getPlayer();
		GameTeam team = PlayerMeta.getMeta(player).getTeam();
		if (team == null || !locations.containsKey(team))
			return;

		if (locations.get(team).equals(loc)) {
			e.setCancelled(true);
			EntityPlayer handle = ((CraftPlayer) player).getHandle();
			handle.openFurnace(getFurnace(player));
			player.sendMessage(ChatColor.DARK_AQUA
					+ "This is your team's Ender Furnace. Any items you store or smelt here are safe from all other players.");
		}
	}

	@EventHandler
	public void onFurnaceBreak(BlockBreakEvent e) {
		if (locations.values().contains(e.getBlock().getLocation()))
			e.setCancelled(true);
	}

	private VirtualFurnace getFurnace(Player player) {
		if (!furnaces.containsKey(player.getName())) {
			EntityPlayer handle = ((CraftPlayer) player).getHandle();
			furnaces.put(player.getName(), new VirtualFurnace(handle));
		}
		return furnaces.get(player.getName());
	}

	private class VirtualFurnace extends TileEntityFurnace {
		public VirtualFurnace(EntityHuman entity) {
			world = entity.world;
		}

		@Override
		public boolean a(EntityHuman entity) {
			return true;
		}

		@Override
		public int p() {
			return 0;
		}

		@Override
		public net.minecraft.server.v1_7_R1.Block q() {
			return net.minecraft.server.v1_7_R1.Blocks.FURNACE;
		}

		@Override
		public void update() {

		}

		@Override
		public InventoryHolder getOwner() {
			return new InventoryHolder() {
				public Inventory getInventory() {
					return new CraftInventoryFurnace(VirtualFurnace.this);
				}
			};
		}
	}
}
