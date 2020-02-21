package net.coasterman10.Annihilation.object;

import com.sun.istack.internal.NotNull;
import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public final class Team {
    private final TeamEnum teamColor;
    //private final Nexus nexus;
    private final HashMap<Player, UUID> players;
    private static byte nextId = 0;
    private final byte id;

    public Team(@NotNull TeamEnum color) {
        this.teamColor = color;
        // TODO: Create the game class
        //Location nexusLocation = Util.parseLocation(Annihilation.getInstance().getConfig().getString("nexuses." + this.teamColor.name().toLowerCase()));
        //this.nexus = new Nexus(teamColor, nexusLocation, 75);
        this.id = nextId++;
        this.players = new HashMap<>();
    }

    public byte getId() {
        return this.id;
    }

    /**
     *
     * @return a copy of the HashMap containing the players
     */
    public Object getPlayers() {
        return players.clone();
    }

    // TODO: Code this class
}
