package net.coasterman10.Annihilation.chat;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.AnnihilationTeam;
import net.coasterman10.Annihilation.PlayerMeta;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
	private final Annihilation plugin;

	public ChatListener(Annihilation plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerChat(final AsyncPlayerChatEvent e) {
		String DARK_GRAY = ChatColor.DARK_GRAY.toString();
		String GRAY = ChatColor.GRAY.toString();
		String WHITE = ChatColor.WHITE.toString();

		String username = e.getPlayer().getName();
		AnnihilationTeam team = PlayerMeta.getMeta(e.getPlayer()).getTeam();
		String group;
		String message = e.getMessage();

		if (team == AnnihilationTeam.NONE) {
			String color = ChatColor.DARK_PURPLE.toString();
			group = DARK_GRAY + "[" + color + "Lobby" + DARK_GRAY + "]";

			if (message.startsWith("!"))
				message = message.substring(1);
		} else {
			String color = team.color().toString();
			if (message.startsWith("!")) {
				message = message.substring(1);
				group = DARK_GRAY + "[" + color + "All" + DARK_GRAY + "]";
				username = color + username;
			} else {
				group = GRAY + "[" + color + "Team" + GRAY + "]";
				e.getRecipients().clear();
				e.getRecipients().addAll(team.getPlayers());
			}
			if (!PlayerMeta.getMeta(e.getPlayer()).isAlive()
					&& plugin.getPhase() > 0) {
				group = group + GRAY + " [" + ChatColor.DARK_RED + "DEAD" + GRAY
						+ "]";
			}
		}

		e.setFormat(group + " " + WHITE + username + WHITE + ": " + message);

		if (message.contains("NEXUS")) {
			e.setCancelled(true);

			plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
				public void run() {
					Player player = e.getPlayer();
					player.getWorld().strikeLightning(
							e.getPlayer().getLocation());
					player.sendMessage(ChatColor.DARK_RED
							+ "Instead of shouting at your team, why don't you go defend?");

				}
			});
		}
	}
}
