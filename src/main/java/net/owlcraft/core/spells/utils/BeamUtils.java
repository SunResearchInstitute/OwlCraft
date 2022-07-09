package net.owlcraft.core.spells.utils;

import net.owlcraft.core.OwlCraft;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BeamUtils {
    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param targetable         predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param defaultBeam        Use default beam provided?
     * @param color              What color default beam?
     */
    public static Pair<Location, Boolean> createBeamAndTeleportToLocation(Player player, double distance, FluidCollisionMode fluidCollisionMode, TriConsumer<BukkitTask, MutableInt, Location> action, Predicate<Entity> targetable, boolean defaultBeam, Color color) {
        return createBeamAndTeleportToLocation(player, distance, 1, fluidCollisionMode, action, targetable, defaultBeam, color);
    }

    /**
     * @param player      Player to create the beam from.
     * @param distance    Distance the beam should travel.
     * @param action      Action to perform on each block.
     * @param targetable  predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param defaultBeam Use default beam provided?
     * @param color       What color default beam?
     */
    public static Pair<Location, Boolean> createBeamAndTeleportToLocation(Player player, double distance, TriConsumer<BukkitTask, MutableInt, Location> action, Predicate<Entity> targetable, boolean defaultBeam, Color color) {
        return createBeamAndTeleportToLocation(player, distance, 1, FluidCollisionMode.NEVER, action, targetable, defaultBeam, color);
    }

    /**
     * @param player      Player to create the beam from.
     * @param distance    Distance the beam should travel.
     * @param action      Action to perform on each block.
     * @param defaultBeam Use default beam provided?
     * @param color       What color default beam?
     */
    public static Pair<Location, Boolean> createBeamAndTeleportToLocation(Player player, double distance, TriConsumer<BukkitTask, MutableInt, Location> action, boolean defaultBeam, Color color) {
        return createBeamAndTeleportToLocation(player, distance, 1, FluidCollisionMode.NEVER, action, null, defaultBeam, color);
    }

    /**
     * @param player   Player to create the beam from.
     * @param distance Distance the beam should travel.
     * @param action   Action to perform on each block.
     * @param color    What color default beam?
     */
    public static Pair<Location, Boolean> createBeamAndTeleportToLocation(Player player, double distance, TriConsumer<BukkitTask, MutableInt, Location> action, Color color) {
        return createBeamAndTeleportToLocation(player, distance, 1, FluidCollisionMode.NEVER, action, null, true, color);
    }

    /**
     * @param player       Player to create the beam from.
     * @param distance     Distance the beam should travel.
     * @param loopsPerTick How many times the beam should travel per tick.
     * @param action       Action to perform on each block.
     * @param color        What color default beam?
     */
    public static Pair<Location, Boolean> createBeamAndTeleportToLocation(Player player, double distance, int loopsPerTick, TriConsumer<BukkitTask, MutableInt, Location> action, Color color) {
        return createBeamAndTeleportToLocation(player, distance, loopsPerTick, FluidCollisionMode.NEVER, action, null, true, color);
    }

    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param loopsPerTick       How many times the beam should travel per tick.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param targetable         predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param defaultBeam        Use default beam provided?
     * @param color              What color default beam?
     */
    public static Pair<Location, Boolean> createBeamAndTeleportToLocation(Player player, double distance, int loopsPerTick, FluidCollisionMode fluidCollisionMode, TriConsumer<BukkitTask, MutableInt, Location> action, Predicate<Entity> targetable, boolean defaultBeam, Color color) {
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
            return Pair.of(to, false);
        } else {
            Location location = src.clone().add(0, 1, 0);
            Vector dir = src.getDirection();

            double distanceSquared = src.distanceSquared(dest);
            Vector blockDist = dir.clone().multiply(0.5);

            MutableInt iteration = new MutableInt(0);
            Consumer<BukkitTask> bukkitTaskConsumer = task -> {
                iteration.add(1);
                double dist = iteration.toInteger() * 0.5;
                if (((dist * dist) > distanceSquared) || !(OwlCraft.getInstance().getSpellManager().isCapable(player))) {
                    return;
                }
                if (defaultBeam) {
                    location.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0.0, 0.0, 0.0, 1, new Particle.DustOptions(color, 2));
                    location.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 8, 0.0, 0.0, 0.0, 1);
                }
                if (action != null) {
                    action.accept(task, iteration, location);
                }

                location.add(blockDist);
            };
            Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), task -> {
                for (int i = 0; i < loopsPerTick; i++) {
                    bukkitTaskConsumer.accept(task);
                }
            }, 0, 1);
        }
        return Pair.of(to, true);
    }

    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param targetable         predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param cleanup            Actions to perform on task finish.
     * @param defaultBeam        Use default beam provided?
     * @param color              What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, FluidCollisionMode fluidCollisionMode, BiConsumer<MutableInt, Location> action, Predicate<Entity> targetable, Runnable cleanup, boolean defaultBeam, Color color) {
        return createBeam(player, distance, 1, fluidCollisionMode, action, targetable, cleanup, defaultBeam, color);
    }

    /**
     * @param player      Player to create the beam from.
     * @param distance    Distance the beam should travel.
     * @param action      Action to perform on each block.
     * @param targetable  predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param cleanup     Actions to perform on task finish.
     * @param defaultBeam Use default beam provided?
     * @param color       What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, BiConsumer<MutableInt, Location> action, Predicate<Entity> targetable, Runnable cleanup, boolean defaultBeam, Color color) {
        return createBeam(player, distance, 1, FluidCollisionMode.NEVER, action, targetable, cleanup, defaultBeam, color);
    }

    /**
     * @param player      Player to create the beam from.
     * @param distance    Distance the beam should travel.
     * @param action      Action to perform on each block.
     * @param cleanup     Actions to perform on task finish.
     * @param defaultBeam Use default beam provided?
     * @param color       What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, BiConsumer<MutableInt, Location> action, Runnable cleanup, boolean defaultBeam, Color color) {
        return createBeam(player, distance, 1, FluidCollisionMode.NEVER, action, null, cleanup, defaultBeam, color);
    }

    /**
     * @param player      Player to create the beam from.
     * @param distance    Distance the beam should travel.
     * @param action      Action to perform on each block.
     * @param defaultBeam Use default beam provided?
     * @param color       What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, BiConsumer<MutableInt, Location> action, boolean defaultBeam, Color color) {
        return createBeam(player, distance, 1, FluidCollisionMode.NEVER, action, null, null, defaultBeam, color);
    }

    /**
     * @param player   Player to create the beam from.
     * @param distance Distance the beam should travel.
     * @param action   Action to perform on each block.
     * @param cleanup  Actions to perform on task finish.
     * @param color    What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, BiConsumer<MutableInt, Location> action, Runnable cleanup, Color color) {
        return createBeam(player, distance, 1, FluidCollisionMode.NEVER, action, null, cleanup, true, color);
    }

    /**
     * @param player     Player to create the beam from.
     * @param distance   Distance the beam should travel.
     * @param action     Action to perform on each block.
     * @param targetable predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param color      What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, BiConsumer<MutableInt, Location> action, Predicate<Entity> targetable, Color color) {
        return createBeam(player, distance, 1, FluidCollisionMode.NEVER, action, targetable, null, true, color);
    }

    /**
     * @param player   Player to create the beam from.
     * @param distance Distance the beam should travel.
     * @param action   Action to perform on each block.
     * @param color    What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, BiConsumer<MutableInt, Location> action, Color color) {
        return createBeam(player, distance, 1, FluidCollisionMode.NEVER, action, null, null, true, color);
    }

    /**
     * @param player       Player to create the beam from.
     * @param distance     Distance the beam should travel.
     * @param loopsPerTick How many times the beam should travel per tick.
     * @param action       Action to perform on each block.
     * @param cleanup      Actions to perform on task finish.
     * @param color        What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, int loopsPerTick, BiConsumer<MutableInt, Location> action, Runnable cleanup, Color color) {
        return createBeam(player, distance, loopsPerTick, FluidCollisionMode.NEVER, action, null, cleanup, true, color);
    }

    /**
     * @param player             Player to create the beam from.
     * @param distance           Distance the beam should travel.
     * @param loopsPerTick       How many times the beam should travel per tick.
     * @param fluidCollisionMode How the beam should collide with fluids.
     * @param action             Action to perform on each block.
     * @param targetable         predicate to be used for rayTrace set to null to use rayTraceBlocks.
     * @param cleanup            Actions to perform on task finish.
     * @param defaultBeam        Use default beam provided?
     * @param color              What color default beam?
     */
    public static BukkitTask createBeam(Player player, double distance, int loopsPerTick, FluidCollisionMode fluidCollisionMode, BiConsumer<MutableInt, Location> action, Predicate<Entity> targetable, Runnable cleanup, boolean defaultBeam, Color color) {
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
        Vector blockDist = dir.clone().multiply(0.5);

        MutableInt iteration = new MutableInt(0);
        Runnable runnable = () -> {
            iteration.add(1);
            double dist = iteration.toInteger() * 0.5;
            if (((dist * dist) > distanceSquared) || !(OwlCraft.getInstance().getSpellManager().isCapable(player))) {
                if (cleanup != null) {
                    cleanup.run();
                }
                return;
            }
            if (defaultBeam) {
                location.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0.0, 0.0, 0.0, 1, new Particle.DustOptions(color, 2));
                location.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 8, 0.0, 0.0, 0.0, 1);
            }
            if (action != null) {
                action.accept(iteration, location);
            }

            location.add(blockDist);
        };
        return Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), () -> {
            for (int i = 0; i < loopsPerTick; i++) {
                runnable.run();
            }
        }, 0, 1);
    }
}

