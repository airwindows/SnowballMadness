/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package snowballmadness;

import com.google.common.base.*;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;

/**
 * This logic takes in a material to place and possibly a second material (for
 * placing in the air block just above the material) and a number specifying how
 * many blocks down the embedding is to go. If nothing is specified, we assume 1
 * block. Higher numbers attempt to place that many blocks under the point of
 * impact. -1 means we go down to void.
 *
 *
 * @author christopherjohnson, somewhat
 */
public class BlockEmbedSnowballLogic extends SnowballLogic {

    private final Material toPlace;
    private final Material toCap;
    private final float embedDepth;

    public BlockEmbedSnowballLogic(Material toPlace, Material toCap, float embedDepth) {
        this.toPlace = Preconditions.checkNotNull(toPlace);
        this.toCap = Preconditions.checkNotNull(toCap);
        this.embedDepth = embedDepth;
    }

    @Override
    public void hit(Snowball snowball, SnowballInfo info) {
        super.hit(snowball, info);

        Location loc = snowball.getLocation().clone();
        Block block = loc.getBlock();

        if (block.getType() == Material.AIR && block.getY() > 1) {
            loc.setY(loc.getY() - 1);
            block = loc.getBlock();
        }

        if (block.getType() != Material.AIR) {
            loc.setY(loc.getY() + 1);
            block = loc.getBlock();
        }

        if (block.getType() == Material.AIR) {
            block.setType(toCap); //we set the cap and prepare to step downward
            loc.setY(loc.getY() - 1);
            block = loc.getBlock();
        }

        float decrement = embedDepth;

        while (!((loc.getY() < 0) || (decrement == 0) || (decrement > 0 && block.getType() == Material.BEDROCK))) {
            //bail if: loc.Y is less than zero OR embedDepth is exactly zero 
            //  OR  (embedDepth > 0 and the block type is bedrock)
            block.setType(toPlace);
            //just stepped down, it's above zero and either a replaceable block
            //or bedrock with embedDepth negative. place that sucker!            
            decrement = decrement - 1;
            loc.setY(loc.getY() - 1);
            block = loc.getBlock();
            //step down and go back to re-check the while
        }

    }
}
