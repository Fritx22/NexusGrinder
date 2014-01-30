package me.morpig.NexusGrinder.commands;

import me.morpig.NexusGrinder.NexusGrinder;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCommand implements CommandExecutor {
	
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "Classes pertain only to players");
		} else {
			Player player = (Player) sender;
			NexusGrinder.Util.showClassSelector(player, "Select Class");
		}
		return false;
	}
}
