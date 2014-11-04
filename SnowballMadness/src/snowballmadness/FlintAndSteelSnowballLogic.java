package snowballmadness;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

/**
 * This logic sets the block or entity at the point of impact on fire
 *
 * @author DanJ
 */
public class FlintAndSteelSnowballLogic extends BlockPlacementSnowballLogic {

    public FlintAndSteelSnowballLogic(Material toPlace) {
        super(toPlace);
    }

    @Override
    public double damage(Snowball snowball, SnowballInfo info, Entity target, double proposedDamage) {
        int extraTicks = (int) (20 * info.power);
        target.setFireTicks(target.getFireTicks() + extraTicks);

        return super.damage(snowball, info, target, proposedDamage);
    }
}
