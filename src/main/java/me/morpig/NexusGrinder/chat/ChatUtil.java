package me.morpig.NexusGrinder.chat;

import me.morpig.NexusGrinder.NexusGrinderTeam;
import me.morpig.NexusGrinder.PlayerMeta;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatUtil {
	private static final String DARK_AQUA = ChatColor.DARK_AQUA.toString();
	private static final String GRAY = ChatColor.GRAY.toString();
	private static final String RED = ChatColor.RED.toString();

	private static boolean roman = false;

	public static void setRoman(boolean b) {
		roman = b;
	}

	public static void broadcast(String message) {
		Bukkit.broadcastMessage(message);
	}

	public static void nexusDestroyed(NexusGrinderTeam attacker,
			NexusGrinderTeam victim, Player p) {
		broadcast(GRAY + "===============[ " + victim.color().toString() + "Nexus Destroyed"
				+ GRAY + " ]===============");
		broadcast(attacker.color().toString() + p.getName() + GRAY + " from " + attacker.coloredName() + GRAY + 
				" destroyed " + victim.coloredName() + "'s" + GRAY + " Nexus!");
		broadcast(GRAY + "===============================================");
	}

	public static String nexusBreakMessage(Player breaker,
			NexusGrinderTeam attacker, NexusGrinderTeam victim) {
		return colorizeName(breaker, attacker) + GRAY + " has damaged the "
				+ victim.coloredName() + " team's Nexus!";
	}

	private static String colorizeName(Player player, NexusGrinderTeam team) {
		return team.color() + player.getName();
	}

	public static void phaseMessage(int phase) {
		broadcast(GRAY + "==========[ " + DARK_AQUA + "Progress" + GRAY
				+ " ]==========");
		broadcast(GRAY + "Phase " + translateRoman(phase) + " has started");
		switch (phase) {
		case 1:
			broadcast(GRAY + "Each nexus is invincible until Phase " + translateRoman(2));
			break;
		case 2:
			broadcast(GRAY + "Each nexus is no longer invincible");
			break;
		case 3:
			broadcast(GRAY + "Diamonds will now spawn in the middle");
			break;
		case 4:
			break;
		case 5:
			broadcast(RED + "Double nexus damage");
		}
		broadcast(GRAY + "==============================");
	}

	public static void winMessage(NexusGrinderTeam winner) {
		broadcast(GRAY + "================[ " + winner.color().toString()  + "End Game" + GRAY
				+ " ]================");
		broadcast(GRAY + "Team " +  winner.coloredName() + GRAY + " Wins Annihilation! Restarting game...");
		broadcast(GRAY + "==========================================");
	}

	public static String formatDeathMessage(Player victim, Player killer,
			String original) {
		NexusGrinderTeam killerTeam = PlayerMeta.getMeta(killer).getTeam();
		String killerColor = killerTeam != null ? killerTeam.color().toString()
				: ChatColor.DARK_PURPLE.toString();
		String killerName = killerColor + killer.getName() + ChatColor.GRAY;

		String message = ChatColor.GRAY + formatDeathMessage(victim, original);
		message = message.replace(killer.getName(), killerName);

		return message;
	}

	public static String formatDeathMessage(Player victim, String original) {
		NexusGrinderTeam victimTeam = PlayerMeta.getMeta(victim).getTeam();
		String victimColor = victimTeam != null ? victimTeam.color().toString()
				: ChatColor.DARK_PURPLE.toString();
		String victimName = victimColor + victim.getName() + ChatColor.GRAY;

		String message = ChatColor.GRAY + original;
		message = message.replace(victim.getName(), victimName);

		return message;
	}

	public static String translateRoman(int number) {
		if (!roman)
			return String.valueOf(number);
		
		switch (number) {
		case 0:
			return "0";
		case 1:
			return "I";
		case 2:
			return "II";
		case 3:
			return "III";
		case 4:
			return "IV";
		case 5:
			return "V";
		case 6:
			return "VI";
		case 7:
			return "VII";
		case 8:
			return "VIII";
		case 9:
			return "IX";
		case 10:
			return "X";
		default:
			return String.valueOf(number);
		}
	}
}
