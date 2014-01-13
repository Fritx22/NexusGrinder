package me.morpig.NexusGrinder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Sound;

import me.morpig.NexusGrinder.bar.BarUtil;
import me.morpig.NexusGrinder.chat.ChatUtil;

public class PhaseTimer {
	private long time;
	private long startTime;
	private long phaseTime;
	private int phase;
	private boolean isRunning;

	private final NexusGrinder plugin;

	private int taskID;

	public PhaseTimer(NexusGrinder plugin, int start, int period) {
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

		for (Player p : Bukkit.getOnlinePlayers()) {
			BarUtil.setMessageAndPercent(p, ChatColor.GREEN + "Starting in "
					+ -time, 1F);
            p.playSound(p.getLocation(), Sound.NOTE_BASS, 20, 20);
            SoundUtil.playSound(p.getLocation(), Sound.NOTE_PIANO, 20F, 20F, 20F);
            SoundUtil.playSound(p.getLocation(), Sound.NOTE_PLING, 20, 20, 20);
            SoundUtil.playSound(p.getLocation(), Sound.NOTE_BASS_GUITAR, 20, 20, 20);
            SoundUtil.playSound(p.getLocation(), Sound.NOTE_BASS_DRUM, 20, 20, 20);
            SoundUtil.playSound(p.getLocation(), Sound.NOTE_BASS, 20, 20, 20);


        plugin.getSignHandler().updateSigns(NexusGrinderTeam.RED);
		plugin.getSignHandler().updateSigns(NexusGrinderTeam.BLUE);
		plugin.getSignHandler().updateSigns(NexusGrinderTeam.GREEN);
		plugin.getSignHandler().updateSigns(NexusGrinderTeam.YELLOW);
	  }
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
			text = getPhaseColor() + "Phase " + ChatUtil.translateRoman(phase)
					+ ChatColor.DARK_GRAY + " | " + ChatColor.WHITE
					+ timeString(time);

			plugin.getSignHandler().updateSigns(NexusGrinderTeam.RED);
			plugin.getSignHandler().updateSigns(NexusGrinderTeam.BLUE);
			plugin.getSignHandler().updateSigns(NexusGrinderTeam.GREEN);
			plugin.getSignHandler().updateSigns(NexusGrinderTeam.YELLOW);
		}

		for (Player p : Bukkit.getOnlinePlayers())
			BarUtil.setMessageAndPercent(p, text, percent);

		plugin.onSecond();
	}

	private String getPhaseColor() {
		switch (phase) {
		case 1:
			return ChatColor.BLUE.toString();
		case 2:
			return ChatColor.GREEN.toString();
		case 3:
			return ChatColor.YELLOW.toString();
		case 4:
			return ChatColor.GOLD.toString();
		case 5:
			return ChatColor.RED.toString();
		default:
			return ChatColor.WHITE.toString();
		}
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