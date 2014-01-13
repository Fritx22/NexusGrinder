package net.coasterman10.Annihilation.maps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.coasterman10.Annihilation.Annihilation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class VotingManager {
	private final Annihilation plugin;
	private final HashSet<String> maps = new HashSet<String>();
	private final HashMap<String, String> votes = new HashMap<String, String>();
	private boolean running = false;
	
	public VotingManager(Annihilation plugin) {
		this.plugin = plugin;
	}
	
	public void start() {
		maps.clear();
		votes.clear();
		
		for (String map : plugin.getMapManager().getRandomMaps()) {
			maps.add(map);
			plugin.getScoreboardHandler().scores.put(
					map, plugin.getScoreboardHandler().obj.getScore(Bukkit.getOfflinePlayer(map)));
			plugin.getScoreboardHandler().scores.get(map).setScore(0);
		}
		
		running = true;
		
		plugin.getScoreboardHandler().update();
	}

	public boolean vote(CommandSender voter, String vote) {
		for (String map : maps) {
			if (vote.equalsIgnoreCase(map)) {
				votes.put(voter.getName(), map);
				plugin.getScoreboardHandler().scores.get(map).setScore(countVotes(map));
				voter.sendMessage(ChatColor.GOLD + "You voted for "
						+ ChatColor.WHITE + map);
				plugin.getScoreboardHandler().update();
				return true;
			}
		}
		voter.sendMessage(vote + ChatColor.RED + " is not a valid map");
		return false;
	}

	public String getWinner() {
		String winner = null;
		Integer highest = -1;
		for (String map : maps) {
			int totalVotes = countVotes(map);
			if (totalVotes > highest) {
				winner = map;
				highest = totalVotes;
			}
		}
		return winner;
	}
	
	public void end() {
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public Set<String> getMaps() {
		return maps;
	}

	private int countVotes(String map) {
		int total = 0;
		for (String vote : votes.values())
			if (vote.equals(map))
				total++;
		return total;
	}
}
