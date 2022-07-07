package services.headpat.owlcraft.spells.utils;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import services.headpat.owlcraft.OwlCraft;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BeamUtil {
    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param targetable         predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param defaultBeam        Use default beam provided?
     * @param color              What color default beam?
     */
    public static boolean createBeamAndTeleportToLocation(Player player, double distance, FluidCollisionMode fluidCollisionMode, TriConsumer<BukkitTask, MutableInt, Location> action, Predicate<Entity> targetable, boolean defaultBeam, Color color) {
        return createBeamAndTeleportToLocation(player, distance, 1.0, fluidCollisionMode, action, targetable, defaultBeam, color);
    }

    /**
     * @param player      Player to create the beam from.
     * @param distance    Distance the beam should travel.
     * @param action      Action to perform on each block.
     * @param targetable  predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param defaultBeam Use default beam provided?
     * @param color       What color default beam?
     */
    public static boolean createBeamAndTeleportToLocation(Player player, double distance, TriConsumer<BukkitTask, MutableInt, Location> action, Predicate<Entity> targetable, boolean defaultBeam, Color color) {
        return createBeamAndTeleportToLocation(player, distance, 1.0, FluidCollisionMode.NEVER, action, targetable, defaultBeam, color);
    }

    /**
     * @param player      Player to create the beam from.
     * @param distance    Distance the beam should travel.
     * @param action      Action to perform on each block.
     * @param defaultBeam Use default beam provided?
     * @param color       What color default beam?
     */
    public static boolean createBeamAndTeleportToLocation(Player player, double distance, TriConsumer<BukkitTask, MutableInt, Location> action, boolean defaultBeam, Color color) {
        return createBeamAndTeleportToLocation(player, distance, 1.0, FluidCollisionMode.NEVER, action, null, defaultBeam, color);
    }

    /**
     * @param player   Player to create the beam from.
     * @param distance Distance the beam should travel.
     * @param action   Action to perform on each block.
     * @param color    What color default beam?
     */
    public static boolean createBeamAndTeleportToLocation(Player player, double distance, TriConsumer<BukkitTask, MutableInt, Location> action, Color color) {
        return createBeamAndTeleportToLocation(player, distance, 1.0, FluidCollisionMode.NEVER, action, null, true, color);
    }

    /**
     * @param player        Player to create the beam from.
     * @param distance      Distance the beam should travel.
     * @param blocksPerTick How many blocks the beam should travel per tick.
     * @param action        Action to perform on each block.
     * @param color         What color default beam?
     */
    public static boolean createBeamAndTeleportToLocation(Player player, double distance, double blocksPerTick, TriConsumer<BukkitTask, MutableInt, Location> action, Color color) {
        return createBeamAndTeleportToLocation(player, distance, blocksPerTick, FluidCollisionMode.NEVER, action, null, true, color);
    }

    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param blocksPerTick      How many blocks the beam should travel per tick.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param targetable         predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param defaultBeam        Use default beam provided?
     * @param color              What color default beam?
     */
    public static boolean createBeamAndTeleportToLocation(Player player, double distance, double blocksPerTick, FluidCollisionMode fluidCollisionMode, TriConsumer<BukkitTask, MutableInt, Location> action, Predicate<Entity> targetable, boolean defaultBeam, Color color) {
        Location src = player.getLocation();
        Vector direction = src.getDirection();
        Vector velocity = player.getVelocity();
        if ((velocity.getX() != 0.0) || (velocity.getZ() != 0.0)) {
            direction = velocity.setY(0.0).normalize();
        }
        double halfWidth = player.getWidth() / 2.0;
        double halfHeight = player.getHeight() / 2.0;
        src.add(0.0, halfHeight, 0.0);
        RayTraceResult result;
        if (targetable != null) {
            result = src.getWorld().rayTrace(src, direction, distance, fluidCollisionMode, true, halfHeight, targetable);
        } else {
            result = src.getWorld().rayTraceBlocks(src, direction, distance, FluidCollisionMode.NEVER, true);
        }
        Location dest;
        if (result != null) {
            dest = result.getHitPosition().toLocation(src.getWorld()).setDirection(player.getLocation().getDirection()).subtract(direction.multiply(0.5));
        } else {
            dest = src.clone().add(direction.multiply(distance));
        }

        Location to = dest.clone().subtract(0.0, halfHeight, 0.0);
        Vector speed = player.getVelocity();

        src = player.getLocation().clone();
        if (!(player.teleport(to))) {
            return false;
        } else {
            Location location = src.clone().add(0, 1, 0);
            Vector dir = src.getDirection();

            double distanceSquared = src.distanceSquared(dest);
            Vector blockDist = dir.clone().multiply(blocksPerTick);

            MutableInt iteration = new MutableInt(0);
            Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), task -> {
                iteration.add(1);
                double dist = iteration.toInteger() * blocksPerTick;
                if (((dist * dist) > distanceSquared) || !(OwlCraft.getInstance().getSpellManager().isCapable(player))) {
                    return;
                }
                if (defaultBeam) {
                    location.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0.0, 0.0, 0.0, 1, new Particle.DustOptions(color, 2));
                    location.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 8, 0.0, 0.0, 0.0, 1);
                }
                action.accept(task, iteration, location);


                location.add(blockDist);
            }, 0, 1);
        }
        return true;
    }

    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param blocksPerTick      How many blocks the beam should travel per tick.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param targetable         predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param color              What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, double blocksPerTick, FluidCollisionMode fluidCollisionMode, BiConsumer<MutableInt, Location> action, Predicate<Entity> targetable, Color color) {
        return createBeam(player, distance, blocksPerTick, fluidCollisionMode, action, targetable, true, color);
    }

    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param blocksPerTick      How many blocks the beam should travel per tick.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param color              What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, double blocksPerTick, FluidCollisionMode fluidCollisionMode, BiConsumer<MutableInt, Location> action, Color color) {
        return createBeam(player, distance, blocksPerTick, fluidCollisionMode, action, null, true, color);
    }

    /**
     * @param player        Player to create the beam from.
     * @param distance      Distance the beam should travel.
     * @param blocksPerTick How many blocks the beam should travel per tick.
     * @param action        Action to perform on each block.
     * @param color         What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, double blocksPerTick, BiConsumer<MutableInt, Location> action, Color color) {
        return createBeam(player, distance, blocksPerTick, FluidCollisionMode.NEVER, action, null, true, color);
    }

    /**
     * @param player   Player to create the beam from.
     * @param distance Distance the beam should travel.
     * @param action   Action to perform on each block.
     * @param color    What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, BiConsumer<MutableInt, Location> action, Color color) {
        return createBeam(player, distance, 1.0, FluidCollisionMode.NEVER, action, null, true, color);
    }

    /**
     * @param player     Player to create the beam from.
     * @param distance   Distance the beam should travel.
     * @param action     Action to perform on each block.
     * @param targetable predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param color      What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, BiConsumer<MutableInt, Location> action, Predicate<Entity> targetable, Color color) {
        return createBeam(player, distance, 1.0, FluidCollisionMode.NEVER, action, targetable, true, color);
    }

    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param blocksPerTick      How many blocks the beam should travel per tick.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param targetable         predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param defaultBeam        Use default beam provided?
     * @param color              What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, double blocksPerTick, FluidCollisionMode fluidCollisionMode, BiConsumer<MutableInt, Location> action, Predicate<Entity> targetable, boolean defaultBeam, Color color) {
        Location src = player.getLocation();
        Vector direction = src.getDirection();
        Vector velocity = player.getVelocity();
        if ((velocity.getX() != 0.0) || (velocity.getZ() != 0.0)) {
            direction = velocity.setY(0.0).normalize();
        }
        double halfWidth = player.getWidth() / 2.0;
        double halfHeight = player.getHeight() / 2.0;
        src.add(0.0, halfHeight, 0.0);

        RayTraceResult result;
        if (targetable != null) {
            result = src.getWorld().rayTrace(src, direction, distance, fluidCollisionMode, true, halfHeight, targetable);
        } else {
            result = src.getWorld().rayTraceBlocks(src, direction, distance, FluidCollisionMode.NEVER, true);
        }
        Location dest;
        if (result != null) {
            dest = result.getHitPosition().toLocation(src.getWorld()).setDirection(player.getLocation().getDirection()).subtract(direction.multiply(0.5));
        } else {
            dest = src.clone().add(direction.multiply(distance));
        }

        Location location = src.clone().add(0, 1, 0);
        Vector dir = src.getDirection();

        double distanceSquared = src.distanceSquared(dest);
        Vector blockDist = dir.clone().multiply(blocksPerTick);

        MutableInt iteration = new MutableInt(0);
        return Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), () -> {
            iteration.add(1);
            double dist = iteration.toInteger() * blocksPerTick;
            if (((dist * dist) > distanceSquared) || !(OwlCraft.getInstance().getSpellManager().isCapable(player))) {
                return;
            }
            if (defaultBeam) {
                location.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0.0, 0.0, 0.0, 1, new Particle.DustOptions(color, 2));
                location.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 8, 0.0, 0.0, 0.0, 1);
            }
            action.accept(iteration, location);

            location.add(blockDist);
        }, 0, 1);
    }
}

