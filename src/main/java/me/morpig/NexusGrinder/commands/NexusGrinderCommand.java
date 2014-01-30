package me.morpig.NexusGrinder.commands;

import me.morpig.NexusGrinder.NexusGrinder;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NexusGrinderCommand implements CommandExecutor {
	private NexusGrinder plugin;

	public NexusGrinderCommand(NexusGrinder plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String cyan = ChatColor.DARK_AQUA.toString();
		String white = ChatColor.WHITE.toString();
		String gray = ChatColor.GRAY.toString();
		String red = ChatColor.RED.toString();
		String gold = ChatColor.GOLD.toString();
		String yellow = ChatColor.YELLOW.toString();
		String dgray = ChatColor.DARK_GRAY.toString();
		String green = ChatColor.GREEN.toString();
		String prefix = cyan + "[NexusGrinder] " + gray;
		
		if (args.length == 0) {
			sender.sendMessage(prefix + white + "NexusGrinder v" + plugin.getDescription().getVersion() + " by coasterman10 & stuntguy3000.");
			sender.sendMessage(prefix + gold + "Download NexusGrinder at");
			sender.sendMessage(prefix + yellow + "http://dev.bukkit.org/bukkit-plugins/ng/");
			sender.sendMessage(prefix + gray + "Command Help:");
			sender.sendMessage(prefix + gray + "/ng " + dgray + "-" + white + " Show plugin information.");
			sender.sendMessage(prefix + gray + "/ng start " + dgray + "-" + white + " Begin the game.");
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("start")) {
				if (sender.hasPermission("ng.command.start")) {
					if (!plugin.startTimer()) {
						sender.sendMessage(prefix + red + "The game has already started");
					} else {
						sender.sendMessage(prefix + green + "The game has been started.");
					}
				} else sender.sendMessage(prefix + red + "You cannot use this command!");
			}
		}
		return false;
	}
}
