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
package net.coasterman10.Annihilation.maps;

import net.coasterman10.Annihilation.maps.MapLoader;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class GameMap {
    private World world;
    private MapLoader mapLoader;

    public GameMap(MapLoader mapLoader) {
        this.mapLoader = mapLoader;
    }

    public boolean loadIntoGame(String worldName) {
        mapLoader.loadMap(worldName);

        WorldCreator wc = new WorldCreator(worldName);
        wc.generator(new VoidGenerator());
        world = Bukkit.createWorld(wc);

        return true;
    }

    public String getName() {
        return world.getName();
    }

    public World getWorld() {
        return world;
    }
}
