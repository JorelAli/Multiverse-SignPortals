/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.destination.DestinationFactory;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.event.MVDebugModeEvent;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.exceptions.NoMultiverseSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;

public class MVSPVersionListener implements Listener {
    private MultiverseSignPortals plugin;

    public MVSPVersionListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    /**
     * This method is called when Multiverse-Core wants to know what version we are.
     * @param event The Version event.
     */
    @EventHandler
    public void versionEvent(MVVersionEvent event) {
        event.appendVersionInfo(this.plugin.getVersionInfo());
    }

    /**
     * This method is called when a player touches a portal.
     * It's used to handle the intriquite messiness of priority between MV plugins.
     * @param event The PTP event.
     */
    @EventHandler
    public void portalTouchEvent(MVPlayerTouchedPortalEvent event) {
        Logging.finer("Found The TouchedPortal event.");
        Player p = event.getPlayer();
        Location l = event.getBlockTouched();

        PortalDetector detector = new PortalDetector(this.plugin);
        try {
            String destString = detector.getNotchPortalDestination(p, l);

            if (destString != null) {
                MVDestination d = this.plugin.getCore().getDestFactory().getDestination(destString);
                Logging.fine(destString + " ::: " + d);
                if (detector.playerCanGoToDestination(p, d)) {
                    // If the player can go to the destination on the sign...
                    // We're overriding NetherPortals.
                    Logging.fine("Player could go to destination!");
                    event.setCancelled(true);
                    takePlayerToDestination(p, destString);
                } else {
                    Logging.fine("Player could NOT go to destination!");
                }
            }

        } catch (NoMultiverseSignFoundException e) {
            // This will simply act as a notch portal.
            Logging.finer("Did NOT find a Multiverse Sign");
        } catch (MoreThanOneSignFoundException e) {
            this.plugin.getCore().getMessaging().sendMessage(p,
                    String.format("%sSorry %sbut more than 1 sign was found where the second line was [mv] or [multiverse]. Please remove one of the signs.",
                            ChatColor.RED, ChatColor.WHITE), false);
        }
    }

    @EventHandler
    public void debugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
    
    private void takePlayerToDestination(Player player, String destString) {
        if (destString != null) {
            Logging.finer("Found a SignPortal! (" + destString + ")");
            SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
            DestinationFactory df = this.plugin.getCore().getDestFactory();

            MVDestination d = df.getDestination(destString);
            Logging.finer("Found a Destination! (" + d + ")");
            if (true) {
                TeleportResult result = teleporter.safelyTeleport(player, player, d);
                if (result == TeleportResult.FAIL_UNSAFE) {
                    player.sendMessage("The Destination was not safe! (" + ChatColor.RED + d + ChatColor.WHITE + ")");
                }
            } else {
                Logging.finer("Denied permission to go to destination!");
            }
        } else {
            player.sendMessage("The Destination was not set on the sign!");
        }
    }
}
