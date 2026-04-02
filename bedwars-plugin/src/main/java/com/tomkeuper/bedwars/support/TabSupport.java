/*
 * BedWars2023 - A bed wars mini-game.
 * Copyright (C) 2024 Tomas Keuper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: contact@fyreblox.com
 */

package com.tomkeuper.bedwars.support;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Tab list support for all Minecraft versions including 1.8.8
 * Provides version-compatible header/footer and team prefix/suffix functionality
 * 
 * This is a fallback utility for servers not using TAB plugin.
 * BedWars2023 primarily uses TAB plugin for scoreboard management.
 */
public class TabSupport {
    
    private static boolean is1_8;
    private static String nmsVersion;
    private static boolean useReflection = false;
    
    static {
        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
            is1_8 = nmsVersion.startsWith("v1_8");
            
            // Check if modern Bukkit API is available
            try {
                Player.class.getMethod("setPlayerListHeaderFooter", String.class, String.class);
                useReflection = false;
            } catch (NoSuchMethodException e) {
                useReflection = true;
            }
            
            Bukkit.getLogger().info("[BedWars] TabSupport initialized for " + nmsVersion + " (1.8 mode: " + is1_8 + ", reflection: " + useReflection + ")");
        } catch (Exception e) {
            Bukkit.getLogger().warning("[BedWars] Failed to detect server version for TabSupport");
            nmsVersion = "unknown";
            is1_8 = false;
            useReflection = true;
        }
    }
    
    /**
     * Set header and footer for player's tab list
     * Works on all versions including 1.8.8
     * 
     * @param player The player to update
     * @param header Header text (supports & color codes)
     * @param footer Footer text (supports & color codes)
     */
    public static void setHeaderFooter(Player player, String header, String footer) {
        if (player == null || !player.isOnline()) return;
        
        try {
            if (is1_8 || useReflection) {
                setHeaderFooter_Reflection(player, header, footer);
            } else {
                setHeaderFooter_Modern(player, header, footer);
            }
        } catch (Exception e) {
            // Silently fail - don't spam console
            // Bukkit.getLogger().warning("[BedWars] Failed to set tab header/footer for " + player.getName());
        }
    }
    
    /**
     * Set player's prefix and suffix in tab list using scoreboard teams
     * Works on all versions with appropriate length limits
     * 
     * @param player The player to update
     * @param prefix Prefix text (supports & color codes, max 16 chars on 1.8)
     * @param suffix Suffix text (supports & color codes, max 16 chars on 1.8)
     */
    public static void setTabPrefix(Player player, String prefix, String suffix) {
        if (player == null || !player.isOnline()) return;
        
        try {
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
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
            
            // Apply prefix
            if (prefix != null) {
                prefix = ChatColor.translateAlternateColorCodes('&', prefix);
                // 1.8.8 has strict 16 character limit for prefix
                if (is1_8 && prefix.length() > 16) {
                    prefix = prefix.substring(0, 16);
                }
                team.setPrefix(prefix);
            }
            
            // Apply suffix
            if (suffix != null) {
                suffix = ChatColor.translateAlternateColorCodes('&', suffix);
                // 1.8.8 has strict 16 character limit for suffix
                if (is1_8 && suffix.length() > 16) {
                    suffix = suffix.substring(0, 16);
                }
                team.setSuffix(suffix);
            }
            
            // Add player to team if not already a member
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    /**
     * Remove player from their tab team and cleanup
     * 
     * @param player The player to cleanup
     */
    public static void removeTabPrefix(Player player) {
        if (player == null) return;
        
        try {
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) return;
            
            String teamName = "BW_" + player.getName();
            if (teamName.length() > 16) {
                teamName = teamName.substring(0, 16);
            }
            
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                team.removeEntry(player.getName());
                team.unregister();
            }
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    // ==================== REFLECTION-BASED IMPLEMENTATION ====================
    
    /**
     * Set header/footer using reflection for maximum compatibility
     * Works on 1.8.8 through modern versions
     */
    private static void setHeaderFooter_Reflection(Player player, String header, String footer) throws Exception {
        if (header == null) header = "";
        if (footer == null) footer = "";
        
        header = ChatColor.translateAlternateColorCodes('&', header);
        footer = ChatColor.translateAlternateColorCodes('&', footer);
        
        // Get CraftPlayer handle
        Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = craftPlayer.getClass().getField("playerConnection").get(craftPlayer);
        
        // Get packet class
        Class<?> packetClass = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
        Object packet = packetClass.newInstance();
        
        // Get chat serializer
        Class<?> chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
        Method serializeMethod = chatSerializer.getMethod("a", String.class);
        
        // Create chat components
        Object headerComponent = 
