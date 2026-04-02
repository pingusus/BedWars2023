package com.tomkeuper.bedwars.sidebar;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.support.TabSupport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class BoardManager {

    private static HashMap<Player, Boolean> hasBoard = new HashMap<>();

    public static void giveBoard(Player player, IArena arena) {
        if (player == null) return;
        
        hasBoard.put(player, true);
        
        updateTab(player, arena);
    }

    public static void removeBoard(Player player) {
        if (player == null) return;
        
        hasBoard.remove(player);
        TabSupport.removeTabPrefix(player);
    }

    public static void updateTab(Player player, IArena arena) {
        if (player == null || !player.isOnline()) return;
        
        String header = Language.getMsg(player, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_LOBBY);
        String footer = Language.getMsg(player, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_LOBBY);
        
        if (arena != null) {
            switch (arena.getStatus()) {
                case playing:
                    header = Language.getMsg(player, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_PLAYING);
                    footer = Language.getMsg(player, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_PLAYING);
                    break;
                case waiting:
                    header = Language.getMsg(player, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_WAITING);
                    footer = Language.getMsg(player, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_WAITING);
                    break;
                case starting:
                    header = Language.getMsg(player, Messages.FORMATTING_SCOREBOARD_TAB_HEADER_STARTING);
                    footer = Language.getMsg(player, Messages.FORMATTING_SCOREBOARD_TAB_FOOTER_STARTING);
                    break;
            }
        }
        
        TabSupport.setHeaderFooter(player, header, footer);
    }

    public static boolean hasBoard(Player player) {
        return hasBoard.containsKey(player) && hasBoard.get(player);
    }

    public static void setPrefix(Player player, String prefix, String suffix) {
        if (player == null || !player.isOnline()) return;
        TabSupport.setTabPrefix(player, prefix, suffix);
    }
}
