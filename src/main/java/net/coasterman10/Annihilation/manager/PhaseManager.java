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
package net.coasterman10.Annihilation.manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.Util;
import net.coasterman10.Annihilation.bar.BarUtil;
import net.coasterman10.Annihilation.chat.ChatUtil;
import net.coasterman10.Annihilation.object.GameTeam;

public class PhaseManager {
    private long time;
    private long startTime;
    private long phaseTime;
    private int phase;
    private boolean isRunning;

    private final Annihilation plugin;

    private int taskID;

    public PhaseManager(Annihilation plugin, int start, int period) {
        this.plugin = plugin;
        startTime = start;
        phaseTime = period;
        phase = 0;
    }

    public void start() {
        if (!isRunning) {
            BukkitScheduler scheduler = plugin.getServer().getScheduler();
            taskID = scheduler.scheduleSyncRepeatingTask(plugin,
                    new Runnable() {
                        public void run() {
                            onSecond();
                        }
                    }, 20L, 20L);
            isRunning = true;
        }

        time = -startTime;

        for (Player p : Bukkit.getOnlinePlayers())
            BarUtil.setMessageAndPercent(p, ChatColor.GREEN + "Starting in "
                    + -time, 1F);

        plugin.getSignHandler().updateSigns(GameTeam.RED);
        plugin.getSignHandler().updateSigns(GameTeam.BLUE);
        plugin.getSignHandler().updateSigns(GameTeam.GREEN);
        plugin.getSignHandler().updateSigns(GameTeam.YELLOW);
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            Bukkit.getServer().getScheduler().cancelTask(taskID);
        }
    }

    public void reset() {
        stop();
        time = -startTime;
        phase = 0;
    }

    public long getTime() {
        return time;
    }

    public long getRemainingPhaseTime() {
        if (phase == 5) {
            return phaseTime;
        }
        if (phase >= 1) {
            return time % phaseTime;
        }
        return -time;
    }

    public int getPhase() {
        return phase;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void onSecond() {
        time++;

        if (getRemainingPhaseTime() == 0) {
            phase++;
            plugin.advancePhase();
        }

        float percent;
        String text;

        if (phase == 0) {
            percent = (float) -time / (float) startTime;
            text = ChatColor.GREEN + "Starting in " + -time;
        } else {
            if (phase == 5)
                percent = 1F;
            else
                percent = (float) getRemainingPhaseTime() / (float) phaseTime;
            text = Util.getPhaseColor(phase) + "Phase " + ChatUtil.translateRoman(phase)
                    + ChatColor.DARK_GRAY + " | " + ChatColor.WHITE
                    + timeString(time);

            plugin.getSignHandler().updateSigns(GameTeam.RED);
            plugin.getSignHandler().updateSigns(GameTeam.BLUE);
            plugin.getSignHandler().updateSigns(GameTeam.GREEN);
            plugin.getSignHandler().updateSigns(GameTeam.YELLOW);
        }

        for (Player p : Bukkit.getOnlinePlayers())
            BarUtil.setMessageAndPercent(p, text, percent);

        plugin.onSecond();
    }

    public static String timeString(long time) {
        long hours = time / 3600L;
        long minutes = (time - hours * 3600L) / 60L;
        long seconds = time - hours * 3600L - minutes * 60L;
        return String.format(ChatColor.WHITE + "%02d" + ChatColor.GRAY + ":"
                + ChatColor.WHITE + "%02d" + ChatColor.GRAY + ":"
                + ChatColor.WHITE + "%02d", hours, minutes, seconds);
    }
}
