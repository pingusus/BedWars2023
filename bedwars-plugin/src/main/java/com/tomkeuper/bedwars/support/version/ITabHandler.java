package com.tomkeuper.bedwars.support.version;

import org.bukkit.entity.Player;

public interface ITabHandler {
    /**
     * Set tab header and footer for a player
     */
    void setHeaderFooter(Player player, String header, String footer);
    
    /**
     * Update player's team prefix and suffix in tab
     */
    void setPrefix(Player player, String prefix, String suffix);
    
    /**
     * Hide a player from another player's tab list
     */
    void hidePlayer(Player player, Player toHide);
    
    /**
     * Show a player to another player's tab list
     */
    void showPlayer(Player player, Player toShow);
}
