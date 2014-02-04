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
import net.coasterman10.Annihilation.Util;
import net.coasterman10.Annihilation.object.GameTeam;
import net.coasterman10.Annihilation.object.PlayerMeta;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {
    private final Annihilation plugin;

    public TeamCommand(Annihilation plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        if (args.length == 0)
            listTeams(sender);
        else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can join teams");
            } else {
                joinTeam((Player) sender, args[0]);
            }
        }
        return true;
    }

    private void joinTeam(Player player, String team) {
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (meta.getTeam() != GameTeam.NONE) {
            player.sendMessage(ChatColor.DARK_AQUA + "You cannot switch teams!");
            return;
        }

        GameTeam target;
        try {
            target = GameTeam.valueOf(team.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "\"" + team
                    + "\" is not a valid team name!");
            listTeams(player);
            return;
        }

        if (Util.isTeamTooBig(target)
                && !player.hasPermission("annihilation.team-limit-bypass")) {
            player.sendMessage(ChatColor.RED
                    + "That team is too big, join another team!");
            return;
        }

        if (target.getNexus() != null) {
            if (target.getNexus().getHealth() == 0 && plugin.getPhase() > 1) {
                player.sendMessage(ChatColor.RED
                        + "You cannot join a team without a Nexus!");
                return;
            }
        }

        if (plugin.getPhase() > plugin.lastJoinPhase
                && !player.hasPermission("annhilation.bypass.phaselimiter")) {
            player.kickPlayer(ChatColor.RED
                    + "You cannot join the game in this phase!");
            return;
        }

        player.sendMessage(ChatColor.DARK_AQUA + "You joined "
                + target.coloredName());
        meta.setTeam(target);

        plugin.getScoreboardHandler().teams.get(team.toUpperCase()).addPlayer(
                player);

        if (plugin.getPhase() > 0) {
            Util.sendPlayerToGame(player, plugin);
        }

        plugin.getSignHandler().updateSigns(GameTeam.RED);
        plugin.getSignHandler().updateSigns(GameTeam.BLUE);
        plugin.getSignHandler().updateSigns(GameTeam.GREEN);
        plugin.getSignHandler().updateSigns(GameTeam.YELLOW);
    }

    private void listTeams(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "============[ "
                + ChatColor.DARK_AQUA + "Teams" + ChatColor.GRAY
                + " ]============");
        for (GameTeam t : GameTeam.teams()) {
            int size = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerMeta meta = PlayerMeta.getMeta(p);
                if (meta.getTeam() == t)
                    size++;
            }

            if (size != 1) {
                sender.sendMessage(t.coloredName() + " - " + size + " players");
            } else {
                sender.sendMessage(t.coloredName() + " - " + size + " player");
            }
        }
        sender.sendMessage(ChatColor.GRAY + "===============================");
    }
}
