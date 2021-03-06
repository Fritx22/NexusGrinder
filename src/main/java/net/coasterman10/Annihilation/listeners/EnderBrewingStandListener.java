/*
 Copyright 2014 stuntguy3000 (Luke Anderson), coasterman10 and F3DEX22.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 MA 02110-1301, USA.
 */
package net.coasterman10.Annihilation.listeners;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.object.PlayerMeta;
import net.coasterman10.Annihilation.object.TeamEnum;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.TileEntityBrewingStand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryBrewer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;

public class EnderBrewingStandListener implements Listener {
    private HashMap<TeamEnum, Location> locations;
    private HashMap<String, VirtualBrewingStand> brewingStands;

    public EnderBrewingStandListener(Annihilation plugin) {
        locations = new HashMap<TeamEnum, Location>();
        brewingStands = new HashMap<String, VirtualBrewingStand>();

        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
                for (VirtualBrewingStand b : brewingStands.values())
                    b.c();
            }
        }, 0L, 1L);
    }

    public void setBrewingStandLocation(TeamEnum team, Location loc) {
        locations.put(team, loc);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onFurnaceOpen(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block b = e.getClickedBlock();

        if (b.getType() != Material.BREWING_STAND)
            return;

        Location loc = b.getLocation();
        Player player = e.getPlayer();
        TeamEnum team = PlayerMeta.getMeta(player).getTeam();

        if (team == null || !locations.containsKey(team))
            return;

        e.setCancelled(true);
        if (locations.get(team).equals(loc)) {

            EntityPlayer handle = ((CraftPlayer) player).getHandle();
            handle.openContainer(getBrewingStand(player));

            player.sendMessage(ChatColor.DARK_AQUA
                    + "This is your team's Ender Brewing Stand. Any items you brew here are safe from all other players.");
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onFurnaceBreak(BlockBreakEvent e) {
        if (locations.values().contains(e.getBlock().getLocation()))
            e.setCancelled(true);
    }

    private VirtualBrewingStand getBrewingStand(Player player) {
        if (!brewingStands.containsKey(player.getName())) {
            EntityPlayer handle = ((CraftPlayer) player).getHandle();
            brewingStands.put(player.getName(), new VirtualBrewingStand(handle));
        }
        return brewingStands.get(player.getName());
    }

    private class VirtualBrewingStand extends TileEntityBrewingStand {
        public VirtualBrewingStand(EntityHuman entity) {
            world = entity.world;
        }

        @Override
        public boolean a(EntityHuman entity) {
            return true;
        }

        @Override
        public int g() {
            return 0;
        }

        @Override
        public net.minecraft.server.v1_8_R3.Block w() {
            return net.minecraft.server.v1_8_R3.Blocks.BREWING_STAND;
        }

        @Override
        public void update() {

        }

        @Override
        public InventoryHolder getOwner() {
            return new InventoryHolder() {
                public Inventory getInventory() {
                    return new CraftInventoryBrewer(VirtualBrewingStand.this);
                }
            };
        }
    }
}
