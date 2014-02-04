package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.Util;

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
			Util.showClassSelector(player, "Select Class");
		}
		return false;
	}
}
