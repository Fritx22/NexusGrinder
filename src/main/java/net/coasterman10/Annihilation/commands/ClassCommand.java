package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.Annihilation;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "Classes pertain only to players");
		} else {
			Player player = (Player) sender;
			Annihilation.Util.showClassSelector(player);
		}
		return false;
	}
}
