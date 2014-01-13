package me.morpig.NexusGrinder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.morpig.NexusGrinder.bar.BarUtil;

public class RestartTimer {
	private final NexusGrinder plugin;
	private long time;
	private long delay;
	private int taskID;

	public RestartTimer(NexusGrinder plugin, final long delay) {
		this.plugin = plugin;
		this.delay = delay;
	}

	public void start(final long gameTime) {
		time = delay;
		final String totalTime = PhaseTimer.timeString(gameTime);
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
				new Runnable() {
					@Override
					public void run() {
						if (time <= 0) {
							plugin.reset();
							stop();
							return;
						}
						String message = ChatColor.GOLD + "Total time: "
								+ ChatColor.WHITE + totalTime + " | "
								+ ChatColor.GREEN + "Restarting in "
								+ time;
						float percent = (float) time / (float) delay;
						for (Player p : Bukkit.getOnlinePlayers())
							BarUtil.setMessageAndPercent(p, message, percent);
						time--;
					}
				}, 0L, 20L);
	}

	private void stop() {
		Bukkit.getScheduler().cancelTask(taskID);
	}
}
