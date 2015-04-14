package snowballmadness;

import java.util.*;
import com.google.common.base.*;
import com.google.common.collect.Lists;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This class is the base class that hosts the logic that triggers when a snowball hits a target.
 *
 * We keep these in a weak hash map, so it is important that this object (and all subclasses) not hold onto a reference to a
 * Snowball, or that snowball may never be collected.
 *
 * @author DanJ
 */
public abstract class SnowballLogic {

    ////////////////////////////////////////////////////////////////
    // Logic
    //
    /**
     * This is called when the snowball is launched.
     *
     * @param snowball The snowball being launched.
     * @param info Other information about the snowball.
     */
    public void launch(Snowball snowball, SnowballInfo info) {
    }

    /**
     * This method decides whether the snowball should continue to operate; for performance reasons we destroy snowball that have
     * gone too high.
     *
     * This is called before tick() is, but only about 1/16th as often as tick(); snowballs can therefore exist 'outside the
     * reservation' for a short time.
     *
     * @param snowball The snowball that we may destroy.
     * @param info The info about he snowball.
     * @return True to continue with this snowball; false to silently terminate it.
     */
    public boolean shouldContinue(Snowball snowball, SnowballInfo info) {
        Location here = snowball.getLocation();
        double y = here.getY();

        if (y < 0 || y > snowball.getWorld().getMaxHeight()) {
            return false;
        }

        final double maxDistance = 128.0;
        final double maxDistanceSq = maxDistance * maxDistance;

        double distanceSq = info.launchLocation.distanceSquared(here);

        if (distanceSq > maxDistanceSq) {
            return false;
        }

        return true;
    }

    /**
     * this is called every many times every second.
     *
     * @param snowball A snowball that gets a chance to do something.
     * @param info Other information about the snowball.
     */
    public void tick(Snowball snowball, SnowballInfo info) {
    }

    /**
     * This is called when the snowball hits something and returns teh damange to be done (which can be 0).
     *
     * @param snowball The snowball hitting something.
     * @param info Other information about the snowball.
     * @param target The entity that was hit.
     * @param damage The damage the snowball is expected to do..
     * @return The damage teh snowball will do.
     */
    public double damage(Snowball snowball, SnowballInfo info, Entity target, double proposedDamage) {
        return proposedDamage;
    }

    /**
     * This is called when the snowball hits something.
     *
     * @param snowball The snowball hitting something.
     * @param info Other information about the snowball.
     */
    public void hit(Snowball snowball, SnowballInfo info) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    ////////////////////////////////////////////////////////////////
    // Creation
    //

    /**
     * This method creates a new logic, but does not start it. It chooses the logic based on 'hint', which is the stack
     * immediately above the snowball being thrown.
     *
     * @param slice The inventory slice above the snowball in the inventory.
     * @return The new logic, not yet started or attached to a snowball, or null if the snowball will be illogical.
     */
    public static SnowballLogic createLogic(InventorySlice slice) {
        ItemStack hint = slice.getBottomItem();

        if (hint == null) {
            return null;
        }

        switch (hint.getType()) {
            case ARROW:
                return new ArrowSnowballLogic(hint);

            case FIREWORK_CHARGE:
            case BLAZE_ROD:
            case EGG:
            case EXP_BOTTLE:
                return new ProjectileSnowballLogic(hint.getType());

            case COBBLESTONE:
            case BOOKSHELF:
            case BRICK:
            case SAND:
            case GRAVEL:
                return new BlockPlacementSnowballLogic(hint.getType());
            //considering adding data values to smooth brick so it randomizes
            //including mossy, cracked and even silverfish

            case EMERALD_BLOCK:
            case EMERALD_ORE:
                return new FeeshVariationsSnowballLogic(hint);

            case EMERALD:
                //fires poisoned feesh
                return new SpawnSnowballLogic<Silverfish>(Silverfish.class) {
                    @Override
                    protected void initializeEntity(Silverfish spawned, SnowballInfo info) {
                        super.initializeEntity(spawned, info);
                        spawned.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 250));
                        spawned.setTarget(spawned); //seems to trigger only if feesh has a object of its ire.
                    }
                };

            case ENDER_PEARL:
                return new BlockPlacementSnowballLogic(Material.ENDER_CHEST);

            case ANVIL:
                return new AnvilSnowballLogic();

            case DAYLIGHT_DETECTOR:
                return new DaylightFinderSnowballLogic();

            case WATCH:
                return new WatchSnowballLogic();

            case REDSTONE:
                return new StartRainLogic();

            case CACTUS:
                return new StopRainLogic();

            case WATER_BUCKET:
                return new BlockPlacementSnowballLogic(Material.WATER);

            case LAVA_BUCKET:
                return new BlockPlacementSnowballLogic(Material.LAVA);

            case RED_ROSE:
            case YELLOW_FLOWER:
                return new FireworkSnowballLogic(hint);

            case QUARTZ:
            case COAL:
            case COAL_BLOCK:
            case REDSTONE_BLOCK:
            case NETHERRACK:
            case LADDER:
            case VINE:
            case DIAMOND_ORE:
            case ENDER_STONE:
                return BlockEmbedSnowballLogic.fromMaterial(hint.getType());

            case SAPLING:
                return new ArboristSnowballLogic(hint);

            case WOOD_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case DIAMOND_SWORD:
                return new SwordSnowballLogic(slice);

            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE:
                return new PickaxeSnowballLogic(hint.getType());

            case SHEARS:
                return new ShearsSnowballLogic();

            case STICK:
            case BONE:
            case FENCE:
            case COBBLE_WALL:
            case NETHER_FENCE:
                return new KnockbackSnowballLogic(hint.getType());

            case LEAVES:
            case STONE:
            case SMOOTH_BRICK:
            case IRON_FENCE:
                return new BoxSnowballLogic(hint.getType());
            //all structures that can be broken with any pick, but can be
            //large with use of glowstone. Provides a defensive game

            case GLASS:
                return new BoxSnowballLogic(Material.GLASS, Material.AIR);

            case GLASS_BOTTLE:
                return new RingSnowballLogic(Material.GLASS);

            case POTION:
                return PotionInfo.fromItemStack(hint).createPotionLogic();

            case BUCKET:
                return new BoxSnowballLogic(Material.AIR);

            case WEB:
            case WOOD:
            case LOG:
            case MOSSY_COBBLESTONE:
                return new RingSnowballLogic(hint.getType());

            case TORCH:
                return new BoxSnowballLogic(Material.GLASS, Material.STATIONARY_LAVA); //gives you a tiny lava box.
            //Will set delayed fires, glass doesn't replace leaves so they catch.

            case FENCE_GATE:
                return new LinkedTrailSnowballLogic(Material.FENCE);

            case CAULDRON_ITEM:
                return new LinkedTrailSnowballLogic(Material.STATIONARY_WATER);

            case WATER_LILY:
                return new LinkedWaterTrailSnowballLogic(Material.WATER_LILY);

            case TNT:
                return new TNTSnowballLogic(4.0f);

            case SULPHUR:
                return new TNTSnowballLogic(1.4f);

            case FIREWORK:
                return new JetpackSnowballLogic();

            case FLINT_AND_STEEL:
                return new FlintAndSteelSnowballLogic(Material.FIRE);

            case SPIDER_EYE:
                return new ReversedSnowballLogic(1);

            case FERMENTED_SPIDER_EYE:
                return new EchoSnowballLogic(hint.getAmount(), slice.skip(1));

            case APPLE:
                return new SpeededSnowballLogic(1.3, slice.skip(1));

            case MELON:
                return new SpeededSnowballLogic(1.4, slice.skip(1));

            case SUGAR:
                return new SpeededSnowballLogic(1.5, slice.skip(1));

            case BOW:
                return new SpeededSnowballLogic(1.8, slice.skip(1));

            case COOKIE:
                return new SpeededSnowballLogic(2, slice.skip(1));

            case PUMPKIN_PIE:
                return new SpeededSnowballLogic(2.5, slice.skip(1));

            case CAKE:
                return new SpeededSnowballLogic(3, slice.skip(1));
            //the cake is a... lazor!

            case BEACON:
                return new SpeededSnowballLogic(4, slice.skip(1));
            //the beacon is the REAL lazor.

            case GLOWSTONE_DUST:
                return new PoweredSnowballLogic(1.6, slice.skip(1));

            case GLOWSTONE:
                return new PoweredSnowballLogic(3, slice.skip(1));

            case NETHER_STAR:
                return new PoweredSnowballLogic(4, slice.skip(1));
            //nuclear option. Beacon/netherstar designed to be insane
            //overkill but not that cost-effective, plus more unwieldy.

            case SNOW_BALL:
                return new MultiplierSnowballLogic(hint.getAmount(), hint.getItemMeta().getDisplayName(), slice.skip(1));

            case SLIME_BALL:
                return new BouncySnowballLogic(hint.getAmount(), slice.skip(1));

            case QUARTZ_BLOCK:
                return new KapwingSnowballLogic(hint.getAmount(), slice.skip(1));

            case GRASS:
            case DIRT:
                return new RegenerationSnowballLogic();

            case GHAST_TEAR:
                return SpawnSnowballLogic.fromEntityClass(Ghast.class);

            case ENCHANTMENT_TABLE:
                return new EnchantingTableSnowballLogic();

            case GOLD_NUGGET:
                return new ItemDropSnowballLogic(Material.ROTTEN_FLESH) {
                    @Override
                    protected EntityType getEntityToSpawn(Snowball snowball, SnowballInfo info) {
                        if (info.power > 2) {
                            return EntityType.PIG_ZOMBIE;
                        } else {
                            return null;
                        }
                    }
                };

            case DRAGON_EGG:
                return new DeathVortexSnowballLogic();

            case IRON_INGOT:
                return new MagneticSnowballLogic();

            case CARROT_STICK:
            case FISHING_ROD:
            case STRING:
            case OBSIDIAN:
                return new ComeAlongSnowballLogic(hint.getType());

            case LEATHER:
                return new ItemDropSnowballLogic(
                        Material.BOOK,
                        Material.LEATHER_HELMET,
                        Material.LEATHER_CHESTPLATE,
                        Material.LEATHER_LEGGINGS,
                        Material.LEATHER_BOOTS,
                        Material.STICK,
                        Material.WOOD_SWORD,
                        Material.WOOD_PICKAXE,
                        Material.WOOD_AXE,
                        Material.WOOD_SPADE,
                        Material.WOOD_HOE,
                        Material.WORKBENCH,
                        Material.SADDLE);

            case IRON_BLOCK:
                return new ItemDropSnowballLogic(
                        Material.IRON_HELMET,
                        Material.IRON_CHESTPLATE,
                        Material.IRON_LEGGINGS,
                        Material.IRON_BOOTS,
                        Material.IRON_SWORD,
                        Material.IRON_PICKAXE,
                        Material.IRON_AXE,
                        Material.IRON_SPADE,
                        Material.IRON_HOE);

            case GOLD_BLOCK:
                return new ItemDropSnowballLogic(
                        Material.GOLD_HELMET,
                        Material.GOLD_CHESTPLATE,
                        Material.GOLD_LEGGINGS,
                        Material.GOLD_BOOTS,
                        Material.GOLD_SWORD,
                        Material.GOLD_PICKAXE,
                        Material.GOLD_AXE,
                        Material.GOLD_SPADE,
                        Material.GOLD_HOE);

            case DIAMOND_BLOCK:
                return new ItemDropSnowballLogic(
                        Material.DIAMOND_HELMET,
                        Material.DIAMOND_CHESTPLATE,
                        Material.DIAMOND_LEGGINGS,
                        Material.DIAMOND_BOOTS,
                        Material.DIAMOND_SWORD,
                        Material.DIAMOND_PICKAXE,
                        Material.DIAMOND_AXE,
                        Material.DIAMOND_SPADE,
                        Material.DIAMOND_HOE);

            case SADDLE:
                return new ItemDropSnowballLogic(Material.LEASH) {
                    @Override
                    protected EntityType getEntityToSpawn(Snowball snowball, SnowballInfo info) {
                        if (info.power > 2) {
                            return EntityType.HORSE;
                        } else {
                            return null;
                        }
                    }
                };

            //developing ItemDropSnowball to be a randomizer, it won't be
            //heavily used so it can be full of special cases

            case GOLD_INGOT:
                return SpawnSnowballLogic.fromEntityClass(PigZombie.class);

            case EYE_OF_ENDER:
                return SpawnSnowballLogic.fromEntityClass(Enderman.class);

            case MILK_BUCKET:
                return SpawnSnowballLogic.fromEntityClass(Cow.class);
            //get cow if you have ever milked one
            //path to leather -> book -> enchanting table

            case MUSHROOM_SOUP:
                return SpawnSnowballLogic.fromEntityClass(MushroomCow.class);
            //get mooshroom if you can get both mushrooms and wood
            //alternate path to leather -> book -> enchanting table!

            case JACK_O_LANTERN:
                return new SpawnSnowballLogic<Skeleton>(Skeleton.class) {
                    @Override
                    protected void initializeEntity(Skeleton spawned, final SnowballInfo info) {
                        super.initializeEntity(spawned, info);

                        //my skellington army of undead minions!
                        if (info.power > 1) {
                            //you have to use at least some glowstone to get the special stuff to work, but then
                            //the real gains are in speed
                            spawned.setMaxHealth(spawned.getMaxHealth() * info.power);
                            spawned.setHealth(spawned.getMaxHealth());

                            if (info.speed > 1) {
                                //if you speed the snowballs, you get speedyjumpyskeles
                                spawned.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, (int) info.speed));
                                spawned.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, (int) Math.pow(info.speed, 2)));
                            }
                        }
                    }

                    @Override
                    protected void equipEntity(Skeleton spawned, SnowballInfo info) {
                        super.equipEntity(spawned, info);
                        equipSkele(info.plugin, spawned, info);
                    }
                };

            case ROTTEN_FLESH:
                return new SpawnSnowballLogic<Zombie>(Zombie.class) {
                    @Override
                    protected void initializeEntity(Zombie spawned, SnowballInfo info) {
                        super.initializeEntity(spawned, info);

                        //my zombie army of yucky minions!
                        if (info.power > 1) {
                            //you have to use at least some glowstone to get it to work, but then
                            //the real gains are in speed
                            spawned.setMaxHealth(spawned.getMaxHealth() * info.power);
                            spawned.setHealth(spawned.getMaxHealth());
                            if (info.speed > 1) {
                                //if you speed the snowballs, you get speedyjumpyzombies
                                spawned.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, (int) info.speed));
                                spawned.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, (int) Math.pow(info.speed, 2)));
                            }
                        }
                    }

                    @Override
                    protected void equipEntity(Zombie spawned, SnowballInfo info) {
                        super.equipEntity(spawned, info);
                        equipZombie(info.plugin, spawned, info);
                    }
                };


            case SKULL_ITEM:
                SkullType skullType = SkullType.values()[hint.getDurability()];

                return SpawnSnowballLogic.fromSkullType(skullType);
            case FEATHER:
                return new FeatherSnowballLogic();


            default:
                return null;
        }
    }

    ////////////////////////////////////////////////////////////////
    // Event Handling
    //
    /**
     * This method processes a new snowball, executing its launch() method and also recording it so the hit() method can be called
     * later.
     *
     * The shooter may be provided as well; this allows us to launch snowballs from places that are not a player, but associated
     * it with a player anyway.
     *
     * @param inventory The inventory slice that determines the logic type.
     * @param snowball The snowball to be launched.
     * @param info The info record that describes the snowball.
     * @return The logic associated with the snowball; may be null.
     */
    public static SnowballLogic performLaunch(InventorySlice inventory, Snowball snowball, SnowballInfo info) {
        SnowballLogic logic = createLogic(inventory);

        if (logic != null) {
            performLaunch(logic, snowball, info);
        }

        return logic;
    }

    /**
     * This overload of performLaunch takes the logic to associate with the snowball instead of an inventory.
     *
     * @param logic The logic to apply to the snowball; can't be null.
     * @param snowball The snowball to be launched.
     * @param info The info record that describes the snowball.
     */
    public static void performLaunch(SnowballLogic logic, Snowball snowball, SnowballInfo info) {
        logic.start(snowball, info);

        if (info.shouldLogMessages) {
            Bukkit.getLogger().info(String.format("Snowball launched: %s [%d]", logic, inFlight.size()));
        }

        logic.launch(snowball, info);
    }

    /**
     * This method processes the impact of a snowball, and invokes the hit() method on its logic object, if it has one.
     *
     * @param snowball The impacting snowball.
     */
    public static void performHit(Snowball snowball) {
        SnowballLogicData data = getData(Preconditions.checkNotNull(snowball));

        if (data != null) {
            try {
                if (data.info.shouldLogMessages) {
                    Bukkit.getLogger().info(String.format("Snowball hit: %s [%d]", data.logic, inFlight.size()));
                }

                data.logic.hit(snowball, data.info);
            } finally {
                data.logic.end(snowball);
            }
        }
    }

    public static double performDamage(Snowball snowball, Entity target, double damage) {
        SnowballLogicData data = getData(Preconditions.checkNotNull(snowball));

        if (data != null) {
            if (data.info.shouldLogMessages) {
                Bukkit.getLogger().info(String.format("Snowball damage: %s [%d]", data.logic, inFlight.size()));
            }

            return data.logic.damage(snowball, data.info, target, damage);
        }

        return damage;
    }

    /**
     * This method handles a projectile launch; it selects a logic and runs its launch method.
     *
     * @param e The event data.
     */
    public static void onProjectileLaunch(SnowballMadness plugin, ProjectileLaunchEvent e) {
        Projectile proj = e.getEntity();
        LivingEntity shooter = proj.getShooter();

        if (proj instanceof Snowball && shooter instanceof Player) {
            Snowball snowball = (Snowball) proj;
            Player player = (Player) shooter;

            PlayerInventory inv = player.getInventory();
            int heldSlot = inv.getHeldItemSlot();
            ItemStack sourceStack = inv.getItem(heldSlot);

            if (sourceStack == null || sourceStack.getType() == Material.SNOW_BALL) {
                InventorySlice slice = InventorySlice.fromSlot(player, heldSlot).skip(1);
                SnowballLogic logic = performLaunch(slice, snowball,
                        new SnowballInfo(plugin, snowball.getLocation(), player));

                if (logic != null && player.getGameMode() != GameMode.CREATIVE) {
                    replenishSnowball(plugin, inv, heldSlot);
                }
            }
        }
    }

    /**
     * This method calls tick() on each snowball that has any logic. This also checks shouldContinue() on each snowball and
     * removes snowball that shouldn't continue.
     */
    public static void onTick(long tickCount) {
        ArrayList<Map.Entry<Snowball, SnowballLogicData>> toRemove = null;

        // we use the tick count to decide which snowballs to
        // check shouldContinue() on; we increment this so
        // on each tick we can check 1/16th of the snowballs.

        long continuationThrottle = tickCount;

        for (Map.Entry<Snowball, SnowballLogicData> e : inFlight.entrySet()) {
            Snowball snowball = e.getKey();
            SnowballLogic logic = e.getValue().logic;
            SnowballInfo info = e.getValue().info;

            boolean shouldContinue = true;

            if ((continuationThrottle & 0xF) == 0) {
                shouldContinue = logic.shouldContinue(snowball, info);
            }

            if (shouldContinue) {
                logic.tick(snowball, info);
            } else {
                if (toRemove == null) {
                    toRemove = Lists.newArrayList();
                }

                toRemove.add(e);
            }

            ++continuationThrottle;
        }

        if (toRemove != null) {
            for (Map.Entry<Snowball, SnowballLogicData> e : toRemove) {
                Snowball snowball = e.getKey();
                SnowballLogic logic = e.getValue().logic;
                SnowballInfo info = e.getValue().info;

                try {
                    if (info.shouldLogMessages) {
                        Bukkit.getLogger().info(String.format("Snowball has left the reservation: %s [%d]", logic, inFlight.size()));
                    }
                } finally {
                    logic.end(snowball);
                    snowball.remove();
                }
            }
        }
    }

    /**
     * This method handles the damage a snowball does on impact, and can adjust that damage.
     *
     * @param e The damage event.
     */
    public static void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        Entity damagee = e.getEntity();
        Entity damager = e.getDamager();
        double damage = e.getDamage();

        if (damager instanceof Snowball) {
            double newDamage = performDamage((Snowball) damager, damagee, damage);

            if (newDamage != damage) {
                e.setDamage(newDamage);
            }
        }
    }

    /**
     * This method increments the number of snowballs in the slot indicated; but it does this after a brief delay since changes
     * made during the launch are ignored. If the indicated slot contains something that is not a snowball, we don't update it. If
     * it is empty, we put one snowball in there.
     *
     * @param plugin The plugin, used to schedule the update.
     * @param inventory The inventory to update.
     * @param slotIndex The slot to update.
     */
    private static void replenishSnowball(Plugin plugin, final PlayerInventory inventory, final int slotIndex) {

        // ugh. We must delay the inventory update or it won't take.
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack replacing = inventory.getItem(slotIndex);

                if (replacing == null) {
                    inventory.setItem(slotIndex, new ItemStack(Material.SNOW_BALL));
                } else if (replacing.getType() == Material.SNOW_BALL) {
                    int oldCount = replacing.getAmount();
                    int newCount = Math.min(16, oldCount + 1);

                    if (oldCount != newCount) {
                        inventory.setItem(slotIndex, new ItemStack(Material.SNOW_BALL, newCount));
                    }
                }
            }
        }.runTaskLater(plugin, 1);
    }

    private static void equipSkele(Plugin plugin, final Skeleton spawned, final SnowballInfo info) {
        new BukkitRunnable() {
            @Override
            public void run() {

                if (info.power > 1 && info.shooter != null) {
                    //you have to use at least some glowstone to get the special stuff to work.
                    ItemStack gear = info.shooter.getInventory().getHelmet();
                    if (gear == null) {
                        gear = info.shooter.getInventory().getItem(27);
                    }
                    if (gear != null) {
                        spawned.getEquipment().setHelmet(gear);
                        spawned.getEquipment().setHelmetDropChance(1.0f);
                    } else {
                        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                        SkullMeta meta = (SkullMeta) skull.getItemMeta();
                        meta.setOwner(info.shooter.getName());
                        skull.setItemMeta(meta);
                        // OH GOD IT HAS MY FAAAAAAACE!
                        spawned.getEquipment().setHelmet(skull);
                        spawned.getEquipment().setHelmetDropChance(1.0f);
                    }

                    gear = info.shooter.getInventory().getChestplate();
                    if (gear != null) {
                        spawned.getEquipment().setChestplate(gear);
                        spawned.getEquipment().setChestplateDropChance(1.0f);
                    }
                    gear = info.shooter.getInventory().getLeggings();
                    if (gear != null) {
                        spawned.getEquipment().setLeggings(gear);
                        spawned.getEquipment().setLeggingsDropChance(1.0f);
                    }
                    gear = info.shooter.getInventory().getBoots();
                    if (gear != null) {
                        spawned.getEquipment().setBoots(gear);
                        spawned.getEquipment().setBootsDropChance(1.0f);
                    }
                    gear = info.shooter.getInventory().getItem(0);
                    if (gear != null) {
                        spawned.getEquipment().setItemInHand(gear);
                        spawned.getEquipment().setItemInHandDropChance(1.0f);
                    }

                    String mobName = info.shooter.getInventory().getItem(27).getItemMeta().getDisplayName();
                    if (mobName == null) {
                        spawned.setCustomName(info.shooter.getName() + "'s Minion");
                    } else {
                        spawned.setCustomName(mobName);
                    }
                    spawned.setCustomNameVisible(true);
                }

            }
        }.runTaskLater(plugin, 1L);
    }

    private static void equipZombie(Plugin plugin, final Zombie spawned, final SnowballInfo info) {
        new BukkitRunnable() {
            @Override
            public void run() {

                if (info.power > 1 && info.shooter != null) {
                    //you have to use at least some glowstone to get the special stuff to work.
                    ItemStack gear = info.shooter.getInventory().getHelmet();
                    if (gear == null) {
                        gear = info.shooter.getInventory().getItem(27);
                    }
                    if (gear != null) {
                        spawned.getEquipment().setHelmet(gear);
                        spawned.getEquipment().setHelmetDropChance(1.0f);
                    } else {
                        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                        SkullMeta meta = (SkullMeta) skull.getItemMeta();
                        meta.setOwner(info.shooter.getName());
                        skull.setItemMeta(meta);

                        // OH GOD IT HAS MY FAAAAAAACE!
                        spawned.getEquipment().setHelmet(skull);
                        spawned.getEquipment().setHelmetDropChance(1.0f);
                    }
                    gear = info.shooter.getInventory().getChestplate();
                    if (gear != null) {
                        spawned.getEquipment().setChestplate(gear);
                        spawned.getEquipment().setChestplateDropChance(1.0f);
                    }
                    gear = info.shooter.getInventory().getLeggings();
                    if (gear != null) {
                        spawned.getEquipment().setLeggings(gear);
                        spawned.getEquipment().setLeggingsDropChance(1.0f);
                    }
                    gear = info.shooter.getInventory().getBoots();
                    if (gear != null) {
                        spawned.getEquipment().setBoots(gear);
                        spawned.getEquipment().setBootsDropChance(1.0f);
                    }
                    gear = info.shooter.getInventory().getItem(0);
                    if (gear != null) {
                        spawned.getEquipment().setItemInHand(gear);
                        spawned.getEquipment().setItemInHandDropChance(1.0f);
                    }
                    String mobName = info.shooter.getInventory().getItem(27).getItemMeta().getDisplayName();
                    if (mobName == null) {
                        spawned.setCustomName(info.shooter.getName() + "'s Minion");
                    } else {
                        spawned.setCustomName(mobName);
                    }
                    spawned.setCustomNameVisible(true);
                }

            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * This method handles a projectile hit event, and runs the hit method.
     *
     * @param e The event data.
     */
    public static void onProjectileHit(ProjectileHitEvent e) {
        Projectile proj = e.getEntity();

        if (proj instanceof Snowball) {
            performHit((Snowball) proj);
        }
    }
    ////////////////////////////////////////////////////////////////
    // Logic Association
    //
    private final static WeakHashMap<Snowball, SnowballLogicData> inFlight = new WeakHashMap<Snowball, SnowballLogicData>();
    private static int approximateInFlightCount = 0;
    private static long inFlightSyncDeadline = 0;

    /**
     * this class just holds the snowball logic and info for a snowball; the snowball itself must not be kept here, as this is the
     * value of a weak-hash-map keyed on the snowballs. We don't want to keep them alive.
     */
    private final static class SnowballLogicData {

        public final SnowballLogic logic;
        public final SnowballInfo info;

        public SnowballLogicData(SnowballLogic logic, SnowballInfo info) {
            this.logic = logic;
            this.info = info;
        }
    }

    /**
     * This returns the number of snowballs (that have attached logic) that are currently in flight. This may count snowballs that
     * have been unloaded or otherwise destroyed for a time; it is not exact.
     *
     * @return The number of in-flight snowballs.
     */
    public static int getInFlightCount() {
        long now = System.currentTimeMillis();

        if (inFlightSyncDeadline <= now) {
            inFlightSyncDeadline = now + 1000;
            approximateInFlightCount = inFlight.size();
        }
        return approximateInFlightCount;
    }

    /**
     * This returns the logic and shooter for a snowball that has one.
     *
     * @param snowball The snowball of interest; can be null.
     * @return The logic and info of the snowball, or null if it is an illogical snowball or it was null.
     */
    private static SnowballLogicData getData(Snowball snowball) {
        if (snowball != null) {
            return inFlight.get(snowball);
        } else {
            return null;
        }
    }

    /**
     * This method registers the logic so getLogic() can find it. Logics only work once started.
     *
     * @param snowball The snowball being launched.
     * @param info Other information about the snowball.
     */
    public void start(Snowball snowball, SnowballInfo info) {
        inFlight.put(snowball, new SnowballLogicData(this, info));

        approximateInFlightCount++;
    }

    /**
     * This method unregisters this logic so it is no longer invoked; this is done when snowball hits something.
     *
     * @param snowball The snowball to deregister.
     */
    public void end(Snowball snowball) {
        approximateInFlightCount--;
        inFlight.remove(snowball);
    }
    ////////////////////////////////////////////////////////////////
    // Utilitu Methods
    //

    /**
     * This returns the of the nearest non-air block underneath 'location' that is directly over the ground. If 'locaiton' is
     * inside the ground, we'll return a new copy of the same location.
     *
     * @param location The starting location; this is not modified.
     * @return A new location describing the place found.
     */
    public static Location getGroundUnderneath(Location location) {
        Location loc = location.clone();

        for (;;) {
            // just in case we have a shaft to the void, we  need
            // to give up before we reach it.

            if (loc.getBlockY() <= 0) {
                return loc;
            }

            switch (loc.getBlock().getType()) {
                case AIR:
                case WATER:
                case STATIONARY_WATER:
                case LEAVES:
                case LONG_GRASS:
                case DOUBLE_PLANT:
                case LAVA:
                case SNOW:
                case WATER_LILY:
                case RED_ROSE:
                case YELLOW_FLOWER:
                case DEAD_BUSH:
                    loc.add(0, -1, 0);
                    break;
                default:
                    return loc;
            }
        }
    }
}
