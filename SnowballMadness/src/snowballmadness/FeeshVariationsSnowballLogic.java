package snowballmadness;

import com.google.common.base.Preconditions;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

/**
 * This logic makes a box out of the material you give it, varying the construction method by type. You can specify the material
 * for the walls and the fill, but by default this class will only replace air and liquid blocks.
 *
 * @author christopherjohnson
 */
public class FeeshVariationsSnowballLogic extends SnowballLogic {

    private final Material baseItem;
    private final int variation;

    public FeeshVariationsSnowballLogic(ItemStack trigger) {
        this.baseItem = Preconditions.checkNotNull(trigger.getType());
        this.variation = Preconditions.checkNotNull(trigger.getDurability());
        //we're passing the actual thing. 
        //the block of monster egg turns stone/brick to feesh to be set off
    }

    @Override
    public void hit(Snowball snowball, SnowballInfo info) {
        super.hit(snowball, info);
        int baseTool = 4;
        final double totalEffectiveness = baseTool * info.power;
        final int radius = (int) (Math.sqrt(totalEffectiveness) * baseTool);
        final double distanceSquaredLimit = (radius * (double) radius) + 1.0;
        final int diameter = (int) (radius * 2);

        World world = snowball.getWorld();
        Location snowballLoc = snowball.getLocation().clone();
        final int beginX = snowballLoc.getBlockX() - radius;
        final int beginY = Math.max(2, snowballLoc.getBlockY() - radius);
        final int beginZ = snowballLoc.getBlockZ() - radius;
        final int endX = beginX + diameter;
        final int endY = Math.min(world.getMaxHeight(), beginY + diameter);
        final int endZ = beginZ + diameter;
        final Location locationBuffer = new Location(world, 0, 0, 0);

        for (int x = beginX; x <= endX; ++x) {
            for (int z = beginZ; z <= endZ; ++z) {
                for (int y = beginY; y <= endY; ++y) {
                    locationBuffer.setX(x);
                    locationBuffer.setY(y);
                    locationBuffer.setZ(z);
                    if (snowballLoc.distanceSquared(locationBuffer) <= distanceSquaredLimit) {
                        Block target = world.getBlockAt(x, y, z);
                        if (target.getType() == Material.STONE) {
                            target.setType(Material.MONSTER_EGGS);
                            target.setData((byte) 0);
                        }
                        if (target.getType() == Material.COBBLESTONE) {
                            target.setType(Material.MONSTER_EGGS);
                            target.setData((byte) 1);
                        }
                        if (target.getType() == Material.SMOOTH_BRICK) {
                            byte dataType = target.getData();
                            dataType = (byte) (dataType + 2);
                            target.setType(Material.MONSTER_EGGS);
                            target.setData(dataType);
                        }
                        //there, all possible blocks have been feeshed!
                    }
                }
            }
        }
    }
}
