package com.tomkeuper.bedwars.support.version;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TabHandler_Modern implements ITabHandler {

    @Override
    public void setHeaderFooter(Player player, String header, String footer) {
        if (player == null) return;
        
        header = ChatColor.translateAlternateColorCodes('&', header);
        footer = ChatColor.translateAlternateColorCodes('&', footer);
        
        player.setPlayerListHeaderFooter(header, footer);
    }

    @Override
    public void setPrefix(Player player, String prefix, String suffix) {
        if (player == null) return;
        
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }
        
        String teamName = "BW_" + player.getName();
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }
        
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        
        if (prefix != null) {
            team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
        }
        if (suffix != null) {
            team.setSuffix(ChatColor.translateAlternateColorCodes('&', suffix));
        }
        
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    @Override
    public void hidePlayer(Player player, Player toHide) {
        if (player != null && toHide != null) {
            player.hidePlayer(toHide);
        }
    }

    @Override
    public void showPlayer(Player player, Player toShow) {
        if (player != null && toShow != null) {
            player.showPlayer(toShow);
        }
    }
}
