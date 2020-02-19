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
package net.coasterman10.Annihilation.api;

import net.coasterman10.Annihilation.object.TeamEnum;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NexusDamageEvent extends Event {

    private Player player;
    private TeamEnum team;
    private int damage;
    
    public NexusDamageEvent(Player player, TeamEnum team, int damage) {
        this.player = player;
        this.team = team;
        this.damage = damage;
    }
    
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }
    
    public TeamEnum getTeam() {
        return team;
    }

    public int getNexusDamage() {
        return damage;
    }
}
