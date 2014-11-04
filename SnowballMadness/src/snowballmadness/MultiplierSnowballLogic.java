package snowballmadness;

import com.google.common.base.Preconditions;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.*;

/**
 * This snowball logic spawns a cluster of additional snowballs at impact, and
 * these may have a second logic attached to them (and that can be a multiplier
 * too, for total chaos!)
 *
 * @author DanJ
 */
public class MultiplierSnowballLogic extends SnowballLogic {

    private final int numberOfSnowballs;
    private final InventorySlice inventory;

    public MultiplierSnowballLogic(int numberOfSnowballs, InventorySlice inventory) {
        this.numberOfSnowballs = numberOfSnowballs;
        this.inventory = Preconditions.checkNotNull(inventory);
    }

    @Override
    public void hit(Snowball snowball, SnowballInfo info) {
        super.hit(snowball, info);

        World world = snowball.getWorld();
        LivingEntity shooter = snowball.getShooter();
        Location source = snowball.getLocation().clone();
        source.setY(source.getY() + 0.25);

        Vector bounce = snowball.getVelocity().clone();
        bounce.setY(-(bounce.getY()));
        //we are not going to amplify the bounce because the initial velocity should
        //be what's amplified. Thus we needn't amplify it again.

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
            vector.setY(0.25);

            vector.multiply(new Vector(info.speed, 1.0, info.speed));

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

    @Override
    public String toString() {
        return String.format("%s -> (x%d) %s",
                super.toString(),
                numberOfSnowballs,
                createLogic(inventory));
    }
}
