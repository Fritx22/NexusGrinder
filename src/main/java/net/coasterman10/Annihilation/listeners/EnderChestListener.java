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

import net.coasterman10.Annihilation.object.PlayerMeta;
import net.coasterman10.Annihilation.object.TeamEnum;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

public class EnderChestListener implements Listener {
    private HashMap<TeamEnum, Location> chests = new HashMap<TeamEnum, Location>();
    private HashMap<String, Inventory> inventories = new HashMap<String, Inventory>();

    @SuppressWarnings("unused")
    @EventHandler
    public void onChestOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (e.getClickedBlock().getType() != Material.ENDER_CHEST)
            return;

        Block clicked = e.getClickedBlock();
        Player player = e.getPlayer();
        TeamEnum team = PlayerMeta.getMeta(player).getTeam();

        if (team == TeamEnum.NONE || !chests.containsKey(team))
            return;

        e.setCancelled(true);

        if (chests.get(team).equals(clicked.getLocation())) {
            openEnderChest(player);
        } else {
            TeamEnum owner = getTeamWithChest(clicked.getLocation());
            if (owner != TeamEnum.NONE) {
                openEnemyEnderChest(player, owner);
            }
        }
    }

    public void setEnderChestLocation(TeamEnum team, Location loc) {
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

    @SuppressWarnings("unused")
    @EventHandler
    public void onFurnaceBreak(BlockBreakEvent e) {
        if (chests.values().contains(e.getBlock().getLocation()))
            e.setCancelled(true);
    }


    private void openEnemyEnderChest(Player player, TeamEnum owner) {
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

    private TeamEnum getTeamWithChest(Location loc) {
        for (Entry<TeamEnum, Location> entry : chests.entrySet())
            if (entry.getValue().equals(loc))
                return entry.getKey();
        return TeamEnum.NONE;
    }
}
