package com.tomkeuper.bedwars.support.version;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TabHandler_v1_8_R3 implements ITabHandler {

    @Override
    public void setHeaderFooter(Player player, String header, String footer) {
        if (player == null) return;
        
        try {
            header = ChatColor.translateAlternateColorCodes('&', header);
            footer = ChatColor.translateAlternateColorCodes('&', footer);
            
            // Get CraftPlayer handle
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object connection = craftPlayer.getClass().getField("playerConnection").get(craftPlayer);
            
            // Create packet
            Class<?> packetClass = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
            Object packet = packetClass.newInstance();
            
            // Create IChatBaseComponent
            Class<?> chatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
            Method serializeMethod = chatSerializer.getMethod("a", String.class);
            
            Object headerComponent = serializeMethod.invoke(null, "{\"text\":\"" + escapeJson(header) + "\"}");
            Object footerComponent = serializeMethod.invoke(null, "{\"text\":\"" + escapeJson(footer) + "\"}");
            
            // Set packet fields
            Field headerField = packetClass.getDeclaredField("a");
            Field footerField = packetClass.getDeclaredField("b");
            headerField.setAccessible(true);
            footerField.setAccessible(true);
            
            headerField.set(packet, headerComponent);
            footerField.set(packet, footerComponent);
            
            // Send packet
            Method sendPacket = connection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
            sendPacket.invoke(connection, packet);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        
        // 1.8.8 has 16 character limit
        if (prefix != null && prefix.length() > 16) {
            prefix = prefix.substring(0, 16);
        }
        if (suffix != null && suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
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
    
    private Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server.v1_8_R3." + name);
    }
    
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n");
    }
}
