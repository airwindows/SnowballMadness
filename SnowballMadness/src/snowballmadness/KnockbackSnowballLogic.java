/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package snowballmadness;

import java.util.*;
import com.google.common.base.*;
import com.google.common.collect.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 *
 * Entity attacker, attacked; float knockback; attacked.setVelocity(
 * attacked.getVelocity().add(attacked.getLocation().toVector().subtract(
 *
 * attacker.getLocation().toVector()).normalize().multiply(knockback)));
 *
 * @author christopherjohnson
 */
public class KnockbackSnowballLogic extends SnowballLogic {

    private final double strength, poweredStrength;

    public KnockbackSnowballLogic(double strength) {
        this(strength, strength);
    }

    public KnockbackSnowballLogic(double strength, double poweredStrength) {
        this.strength = strength;
        this.poweredStrength = poweredStrength;
    }

    public static KnockbackSnowballLogic fromMaterial(Material weaponUsed) {
        switch (weaponUsed) {
            case BONE:
                return new KnockbackSnowballLogic(8);

            case FENCE:
                return new KnockbackSnowballLogic(10);

            case COBBLE_WALL:
                return new KnockbackSnowballLogic(16);

            case NETHER_FENCE:
                return new KnockbackSnowballLogic(24);

            case BLAZE_ROD:
                return new KnockbackSnowballLogic(12) {
                    @Override
                    public double damage(Snowball snowball, SnowballInfo info, Entity target, double proposedDamage) {
                        target.setFireTicks(target.getFireTicks() + (int) (100 * info.power));
                        return super.damage(snowball, info, target, proposedDamage);
                    }
                };

            case FEATHER:
                //you could have knocked me over with a feather!
                return new KnockbackSnowballLogic(2, 32);

            default:
                return new KnockbackSnowballLogic(4);
        }
    }

    @Override
    public double damage(Snowball snowball, SnowballInfo info, Entity target, double proposedDamage) {
        double effectiveStrength = info.power > 8 ? poweredStrength : strength;
        effectiveStrength *= info.power;

        // hows this for de-obfuscated?
        Vector velocity = target.getVelocity().clone();
        velocity.add(target.getLocation().toVector());
        velocity.subtract(snowball.getLocation().toVector());
        velocity.normalize();
        velocity.multiply(effectiveStrength);

        //power also determines how much air time you're getting. No power means little air time,
        double boost = Math.abs(velocity.getX()) + Math.abs(velocity.getZ());
        velocity.setY(boost * info.power);

        target.setVelocity(velocity);
        return 0;
    }
}
