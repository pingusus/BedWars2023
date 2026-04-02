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
    
    static {
        try {
            String packageName = Bukkit.getServer().getClass().getPackage().getName();
            nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
            is1_8 = nmsVersion.startsWith("v1_8");
            Bukkit.getLogger().info("[BedWars] TabSupport initialized for " + nmsVersion + " (1.8 mode: " + is1_8 + ")");
        } catch (Exception e) {
            Bukkit.getLogger().warning("[BedWars] Failed to detect server version for TabSupport");
            nmsVersion = "unknown";
            is1_8 = false;
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
            if (is1_8) {
                setHeaderFooter_1_8(player, header, footer);
            } else {
                setHeaderFooter_Modern(player, header, footer);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[BedWars] Failed to set tab header/footer for " + player.getName() + ": " + e.getMessage());
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
            Bukkit.getLogger().warning("[BedWars] Failed to set tab prefix for " + player.getName() + ": " + e.getMessage());
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
            Bukkit.getLogger().warning("[BedWars] Failed to remove tab prefix for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    // ==================== 1.8.8 IMPLEMENTATION ====================
    
    /**
     * Set header/footer using NMS packets for 1.8.8
     * Uses reflection to maintain compatibility across different 1.8 builds
     */
    private static void setHeaderFooter_1_8(Player player, String header, String footer) throws Exception {
        if (header == null) header = "";
        if (footer == null) footer = "";
        
        header = ChatColor.translateAlternateColorCodes('&', header);
        footer = ChatColor.translateAlternateColorCodes('&', footer);
        
        // Get CraftPlayer handle using reflection
        Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = craftPlayer.getClass().getField("playerConnection").get(craftPlayer);
        
        // Get NMS classes
        Class<?> packetClass = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
        Class<?> chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
        
        // Create new packet instance
        Object packet = packetClass.newInstance();
        
        // Serialize header and footer to IChatBaseComponent
        Method serializeMethod = chatSerializer.getMethod("a", String.class);
        Object headerComponent = serializeMethod.invoke(null, "{\"text\":\"" + escapeJson(header) + "\"}");
        Object footerComponent = serializeMethod.invoke(null, "{\"text\":\"" + escapeJson(footer) + "\"}");
        
        // Set packet fields
        // In 1.8, field "a" is header, field "b" is footer
        Field headerField = packetClass.getDeclaredField("a");
        Field footerField = packetClass.getDeclaredField("b");
        headerField.setAccessible(true);
        footerField.setAccessible(true);
        headerField.set(packet, headerComponent);
        footerField.set(packet, footerComponent);
        
        // Send packet to player
        Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
        sendPacketMethod.invoke(playerConnection, packet);
    }
    
    /**
     * Get NMS class by name with version support
     */
    private static Class<?> getNMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + nmsVersion + "." + className);
    }
    
    /**
     * Escape special JSON characters to prevent malformed packets
     */
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "")
                   .replace("\t", "    ");
    }
    
    // ==================== MODERN VERSION IMPLEMENTATION ====================
    
    /**
     * Set header/footer using Bukkit API for 1.9+
     * This method is much simpler as Bukkit provides native support
     */
    private static void setHeaderFooter_Modern(Player player, String header, String footer) {
        if (header == null) header = "";
        if (footer == null) footer = "";
        
        header = ChatColor.translateAlternateColorCodes('&', header);
        footer = ChatColor.translateAlternateColorCodes('&', footer);
        
        player.setPlayerListHeaderFooter(header, footer);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if server is running 1.8.x
     * 
     * @return true if server is 1.8.x
     */
    public static boolean is1_8() {
        return is1_8;
    }
    
    /**
     * Get the NMS version string
     * 
     * @return version string (e.g., "v1_8_R3")
     */
    public static String getNMSVersion() {
        return nmsVersion;
    }
}
