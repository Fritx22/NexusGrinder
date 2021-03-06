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
package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.Annihilation;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static net.coasterman10.Annihilation.Translation.get;

public class AnnihilationCommand implements CommandExecutor {
    private Annihilation plugin;

    public AnnihilationCommand(Annihilation plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cyan = ChatColor.DARK_AQUA.toString();
        String white = ChatColor.WHITE.toString();
        String gray = ChatColor.GRAY.toString();
        String red = ChatColor.RED.toString();
        String gold = ChatColor.GOLD.toString();
        String yellow = ChatColor.YELLOW.toString();
        String dgray = ChatColor.DARK_GRAY.toString();
        String green = ChatColor.GREEN.toString();
        String prefix = cyan + "[Annihilation] " + gray;
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + gray + "Command Help:");
            sender.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + gray + "/anni " + dgray + "-" + white + " Show plugin information.");
            sender.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + gray + "/anni start " + dgray + "-" + white + " Begin the game.");
        }
        
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("start")) {
                if (sender.hasPermission("annihilation.command.start")) {
                    if (!plugin.startTimer()) {
                        sender.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + red + "The game has already started");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + green + "The game has been started.");
                    }
                } else sender.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + red + "You cannot use this command!");
            }
        }
        return false;
    }
}
