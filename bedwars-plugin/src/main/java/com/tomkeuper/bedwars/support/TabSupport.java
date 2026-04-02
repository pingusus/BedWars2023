package com.tomkeuper.bedwars.support;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TabSupport {
    
    private static boolean is1_8;
    private static String nmsVersion;
    
    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
        is1_8 = nmsVersion.startsWith("v1_8");
        Bukkit.getLogger().info("[BedWars] TabSupport initialized for " + nmsVersion);
    }
    
    public static void setHeaderFooter(Player player, String header, String footer) {
        if (player == null || !player.isOnline()) return;
        
        try {
            if (is1_8) {
                setHeaderFooter_1_8(player, header, footer);
            } else {
                setHeaderFooter_Modern(player, header, footer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
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
            
            if (prefix != null) {
                prefix = ChatColor.translateAlternateColorCodes('&', prefix);
                if (is1_8 && prefix.length() > 16) {
                    prefix = prefix.substring(0, 16);
                }
                team.setPrefix(prefix);
            }
            
            if (suffix != null) {
                suffix = ChatColor.translateAlternateColorCodes('&', suffix);
                if (is1_8 && suffix.length() > 16) {
                    suffix = suffix.substring(0, 16);
                }
                team.setSuffix(suffix);
            }
            
            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
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
            e.printStackTrace();
        }
    }
    
    private static void setHeaderFooter_1_8(Player player, String header, String footer) throws Exception {
        if (header == null) header = "";
        if (footer == null) footer = "";
        
        header = ChatColor.translateAlternateColorCodes('&', header);
        footer = ChatColor.translateAlternateColorCodes('&', footer);
        
        Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
        Object playerConnection = craftPlayer.getClass().getField("playerConnection").get(craftPlayer);
        
        Class<?> packetClass = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
        Class<?> chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
        
        Object packet = packetClass.newInstance();
        
        Method serializeMethod = chatSerializer.getMethod("a", String.class);
        Object headerComponent = serializeMethod.invoke(null, "{\"text\":\"" + escapeJson(header) + "\"}");
        Object footerComponent = serializeMethod.invoke(null, "{\"text\":\"" + escapeJson(footer) + "\"}");
        
        Field headerField = packetClass.getDeclaredField("a");
        Field footerField = packetClass.getDeclaredField("b");
        headerField.setAccessible(true);
        footerField.setAccessible(true);
        headerField.set(packet, headerComponent);
        footerField.set(packet, footerComponent);
        
        Method sendPacketMethod = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
        sendPacketMethod.invoke(playerConnection, packet);
    }
    
    private static Class<?> getNMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + nmsVersion + "." + className);
    }
    
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "")
                   .replace("\t", "    ");
    }
    
    private static void setHeaderFooter_Modern(Player player, String header, String footer) {
        if (header == null) header = "";
        if (footer == null) footer = "";
        
        header = ChatColor.translateAlternateColorCodes('&', header);
        footer = ChatColor.translateAlternateColorCodes('&', footer);
        
        player.setPlayerListHeaderFooter(header, footer);
    }
}
