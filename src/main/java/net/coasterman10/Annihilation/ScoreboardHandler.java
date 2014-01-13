package net.coasterman10.Annihilation;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardHandler {
	public Scoreboard sb;
	public Objective obj;
	
	public HashMap<String, Score> scores = new HashMap<String, Score>();
	public HashMap<String, Team> teams = new HashMap<String, Team>();
	
	public void update() {
		for (Player p : Bukkit.getOnlinePlayers())
			p.setScoreboard(sb);
	}
	
	public void resetScoreboard(String objName) {
		sb = null;
		obj = null;
		
		scores.clear();
		teams.clear();
		
		for (Player p : Bukkit.getOnlinePlayers())
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		
		sb = Bukkit.getScoreboardManager().getNewScoreboard();
		obj = sb.registerNewObjective("anni", "dummy");
		
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(objName);
		
		setTeam(AnnihilationTeam.RED);
		setTeam(AnnihilationTeam.BLUE);
		setTeam(AnnihilationTeam.GREEN);
		setTeam(AnnihilationTeam.YELLOW);
	}
	
	public void setTeam(AnnihilationTeam t) {
		teams.put(t.name(), sb.registerNewTeam(t.name()));
		Team sbt = teams.get(t.name());
		sbt.setAllowFriendlyFire(false);
		sbt.setCanSeeFriendlyInvisibles(true);
		sbt.setPrefix(t.color().toString());
	}
}
