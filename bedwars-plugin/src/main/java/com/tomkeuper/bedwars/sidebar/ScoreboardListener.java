package com.tomkeuper.bedwars.sidebar;

import com.tomkeuper.bedwars.support.TabSupport;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.events.player.PlayerReSpawnEvent;
import com.tomkeuper.bedwars.support.TabSupport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        IArena a = com.tomkeuper.bedwars.arena.Arena.getArenaByPlayer(p);
        
        String header = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_LOBBY);
        String footer = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_LOBBY);
        
        if (a != null) {
            switch (a.getStatus()) {
                case playing:
                    header = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_PLAYING);
                    footer = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_PLAYING);
                    break;
                case waiting:
                    header = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_WAITING);
                    footer = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_WAITING);
                    break;
                case starting:
                    header = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_STARTING);
                    footer = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_STARTING);
                    break;
            }
        }
        
        TabSupport.setHeaderFooter(p, header, footer);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        TabSupport.removeTabPrefix(p);
    }

    @EventHandler
    public void onJoinArena(PlayerJoinArenaEvent e) {
        Player p = e.getPlayer();
        IArena a = e.getArena();
        
        String header = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_WAITING);
        String footer = Language.getMsg(p, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_WAITING);
        
        TabSupport.setHeaderFooter(p, header, footer);
        
        ITeam t = a.getTeam(p);
        if (t != null) {
            String prefix = t.getColor().chat() + "";
            String suffix = "";
            TabSupport.setTabPrefix(p, prefix, suffix);
        }
    }

    @EventHandler
    public void onLeaveArena(PlayerLeaveArenaEvent e) {
        Player p = e.getPlayer();
        TabSupport.removeTabPrefix(p);
    }

    @EventHandler
    public void onRespawn(PlayerReSpawnEvent e) {
        Player p = e.getPlayer();
        IArena a = e.getArena();
        ITeam t = a.getTeam(p);
        
        if (t != null) {
            String prefix = t.getColor().chat() + "";
            String suffix = "";
            TabSupport.setTabPrefix(p, prefix, suffix);
        }
    }
}
