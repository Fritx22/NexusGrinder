/*******************************************************************************
 * Copyright 2014 stuntguy3000 (Luke Anderson) and coasterman10.
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 ******************************************************************************/
package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.manager.VotingManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VoteCommand implements CommandExecutor {
    private final VotingManager manager;

    public VoteCommand(VotingManager manager) {
        this.manager = manager;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        if (!manager.isRunning())
            sender.sendMessage(ChatColor.RED + "Map voting is over.");
        else if (args.length == 0)
            listMaps(sender);
        else if (!manager.vote(sender, args[0])) {
            sender.sendMessage(ChatColor.RED + "Invalid selection!");
            listMaps(sender);
        }
        return true;
    }

    private void listMaps(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "Maps up for voting:");
        int count = 0;
        for (String map : manager.getMaps().values()) {
            count ++;
            sender.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.AQUA + "[" + count + "] " + ChatColor.GRAY + map);
        }
    }
}
