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

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.maps.MapLoader;
import net.coasterman10.Annihilation.maps.VoidGenerator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapCommand implements CommandExecutor {
    private MapLoader loader;
    private Annihilation plugin;

    public MapCommand(Annihilation plugin, MapLoader loader) {
        this.plugin = plugin;
        this.loader = loader;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cyan = ChatColor.DARK_AQUA.toString();
        String gray = ChatColor.GRAY.toString();
        String red = ChatColor.RED.toString();
        final String green = ChatColor.GREEN.toString();
        String prefix = cyan + "[Annihilation] " + gray;
        
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("edit")) {
                    if (p.hasPermission("annihilation.map.edit")) {
                        loader.loadMap(args[1]);
                        WorldCreator wc = new WorldCreator(args[1]);
                        wc.generator(new VoidGenerator());
                        Bukkit.createWorld(wc);
                        sender.sendMessage(green + "Map " + args[1]
                                + " loaded for editing.");
                        if (sender instanceof Player) {
                            sender.sendMessage(green + "Teleporting...");
                            World w = Bukkit.getWorld(args[1]);
                            Location loc = w.getSpawnLocation();
                            loc.setY(w.getHighestBlockYAt(loc));
                            ((Player) sender).teleport(loc);
                        }
                    } else sender.sendMessage(prefix + red + "You cannot use this command!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("save")) {
                    if (p.hasPermission("annihilation.map.save")) {
                        if (Bukkit.getWorld(args[1]) != null) {
                            Bukkit.getWorld(args[1]).save();
                            final CommandSender s = sender;
                            final String mapName = args[1];
                            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                public void run() {
                                    s.sendMessage(green + "Map " + mapName
                                            + " saved.");
                                    loader.saveMap(mapName);
                                }
                            }, 40L);
                        }
                    } else sender.sendMessage(prefix + red + "You cannot use this command!");
                    return true;
                }
            }
            
            sender.sendMessage(prefix + red + "Syntax: /map <save/edit> <name>");
        } else {
            sender.sendMessage(prefix + red + "Only a player can use the map editing tools!");
        }
        return true;
    }

    
}
