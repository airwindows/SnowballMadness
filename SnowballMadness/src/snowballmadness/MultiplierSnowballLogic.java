/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package snowballmadness;

import com.google.common.base.Preconditions;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.*;

/**
 * This snowball logic spawns a cluster of additional snowballs at impact, and these may have a second logic attached to them (and
 * that can be a multiplier too, for total chaos!)
 *
 * @author DanJ
 */
public class MultiplierSnowballLogic extends SnowballLogic {

    private static long inFlightSyncDeadline = 0;
    private int inFlightSnowballLimit = 2048;
    private final String targetName;
    private final int numberOfSnowballs;
    private int serverBusy = 5;
    private final InventorySlice inventory;

    public MultiplierSnowballLogic(int numberOfSnowballs, String targetName, InventorySlice inventory) {
        this.numberOfSnowballs = numberOfSnowballs;
        this.targetName = targetName;
        this.inventory = Preconditions.checkNotNull(inventory);
    }

    @Override
    public void hit(Snowball snowball, SnowballInfo info) {
        super.hit(snowball, info);

        long now = System.currentTimeMillis();
        if (inFlightSyncDeadline <= now) {
            inFlightSyncDeadline = now + 2000;
            approximateInFlightCount = inFlight.size();
            serverBusy = snowball.getServer().getOnlinePlayers().size() + 4;
            inFlightSnowballLimit = 2048 / serverBusy;
            //we are scaling back the multiplier snowballs based on how many players are on.
            //one player can fire these with 819 snowballs in the air already,
            //two players it's down to 682, ten players there must be less than 292 snowballs in the air.
        } //this is only used in the multiplier snowball logic

        if (approximateInFlightCount < inFlightSnowballLimit) {
            World world = snowball.getWorld();
            ProjectileSource shooter = snowball.getShooter();
            Location source = snowball.getLocation().clone();
            source.setY(source.getY() + 0.25);

            Vector bounce = snowball.getVelocity().clone();
            bounce.setY(-(bounce.getY()));
            //we are not going to amplify the bounce because the initial velocity should
            //be what's amplified. Thus we needn't amplify it again.

            //here we can override if the snowball is named
            if (targetName == null) {
                //pass right by
            } else {
                //we have a word but is it a player name?
                OfflinePlayer sleepingPlayer = snowball.getServer().getOfflinePlayer(targetName);
                //this should return null if no such player exists, or return the OfflinePlayer if the named player exists
                Player namedPlayer = snowball.getServer().getPlayer(targetName);
                //this should return null if the player named is not online at the moment
                Location targetLocation = snowball.getLocation().add(0, 1000, 0);
                //default behavior is firing straight up if nothing is found

                if (sleepingPlayer != null) {
                    if (namedPlayer != null) {
                        targetLocation = namedPlayer.getBedSpawnLocation();
                    } else {
                        targetLocation = sleepingPlayer.getBedSpawnLocation();
                    }
                    //we've set targetLocation to something
                }
                //now we will replace bounce outright with a vector made of target location - snowball location
                bounce = targetLocation.toVector().subtract(snowball.getLocation().toVector());
                //thus giving us a new trajectory that aims at the target, namely the
                //location of the player's bedspawn, or straight up if there's no such player.
                bounce.setY(bounce.getY() + 1); //ballistic trajectory
                double range = Math.sqrt(bounce.length()) * 0.2;
                bounce.normalize().multiply(range); //it goes very fast, so we scale it
                //end of the named snowball location override
            }


            Snowball skipper = world.spawn(source, Snowball.class);
            skipper.setShooter(shooter);
            skipper.setVelocity(bounce);

            performLaunch(inventory, skipper, info);
            //the purpose of this change is to make the first one in the stack always
            //bounce like a skipping rock, for better distance shots and ICBMs
            //successive snowballs will be directed increasingly randomly

            for (int i = 1; i < numberOfSnowballs; ++i) {
                Snowball secondary = world.spawn(source, Snowball.class);

                Vector vector = Vector.getRandom();
                vector.setX(vector.getX() - 0.5);
                vector.setZ(vector.getZ() - 0.5);
                vector.setY(vector.getY() * 0.25);

                //now we will interpolate between that and bounce.
                double highvalues = i / 16.0; //if this doesn't return a fractional value it won't work
                double lowvalues = 1.0 - highvalues;
                vector.multiply(highvalues);
                //we've just scaled back our randomness based on how near the snowball number
                //is to 16: lower i numbers make the random component low
                vector.add(bounce.multiply(lowvalues));
                //and we add bounce scaled to the inverse of that amount. Lower i numbers make the
                //bounce component high. as you keep adding more i you get more randomness and scatter.

                secondary.setShooter(shooter);
                secondary.setVelocity(vector);

                performLaunch(inventory, secondary, info);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s -> (x%d) %s",
                super.toString(),
                numberOfSnowballs,
                createLogic(inventory));
    }
}
