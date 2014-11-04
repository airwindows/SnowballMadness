/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package snowballmadness;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
/**
 * This logic tracks the snowball in flight, and when it has left an
 * air block, the block is replaced with a material. It might do this on a
 * chunk by chunk basis, tracking each block it's in within a chunk and
 * updating those blocks once the snowball has left that chunk.
 * 
 * Does not apply to the chunk snowball author is in.
 * 
 * When it hits anything, it just stops. Only works while traveling through
 * air blocks. I'm thinking, minecart for 'girders', fencepost for ground
 * fences. Gravel for a not-updated gravel in the air bridge? LinkedTrail
 * should fill in the gaps that would exist in a 'fence' placement, in the event
 * of a corner-to-corner transition. The idea is, linked is ones that should be
 * continuous (girders).
 * 
 * Also passed is whether the trail is in the air, on, or under the surface 
 * (lowest air block available under each placement point, or replace the 
 * ground). The 'surface' variations are fence for a wood fence, packed ice
 * for an ice trail, dead bush for coal blocks embedded in the ground with
 * fire on top of them (scorching the earth). The scan for this should skip
 * past air and leaf blocks so the effect goes under tree canopies.
 * @author christopherjohnson
 */
public class LinkedTrailSnowballLogic {
    
    private final Material toPlace;
    
      public LinkedTrailSnowballLogic(Material toPlace) {
        this.toPlace = Preconditions.checkNotNull(toPlace);
        //we won't need a 'hit' method as we won't care
        //however, this should compile now as it's a real class
        //and does a thing.
    }
    
    
}
