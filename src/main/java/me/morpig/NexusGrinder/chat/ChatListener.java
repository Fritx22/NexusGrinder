package me.morpig.NexusGrinder.chat;

import me.morpig.NexusGrinder.NexusGrinder;
import me.morpig.NexusGrinder.object.GameTeam;
import me.morpig.NexusGrinder.object.PlayerMeta;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
	private final NexusGrinder plugin;

	public ChatListener(NexusGrinder plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
	}
	
	public void onPlayerChat(final AsyncPlayerChatEvent e) {
		Player sender = e.getPlayer();
		PlayerMeta meta = PlayerMeta.getMeta(sender);
		GameTeam team = meta.getTeam();
		boolean isAll = false;
		boolean dead = !meta.isAlive() && plugin.getPhase() > 0;
		String msg = e.getMessage();
		
		if (e.getMessage().startsWith("!")) {
			isAll = true;
			msg = msg.substring(1);
		}
		
		if (team == GameTeam.NONE)
			isAll = true;
		
		if (isAll)
			ChatUtil.allMessage(team, sender, msg, dead);
		else {
			ChatUtil.allMessage(team, sender, msg, dead);
		}
	}
}
