package services.headpat.owlcraft.spells.implementation.fire;

import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.spells.Spell;
import services.headpat.owlcraft.spells.SpellContext;

import java.util.ArrayList;
import java.util.List;

public class FireballSpell extends Spell {
    private final ShapelessRecipe glyphRecipe;

    public FireballSpell() {
        glyphRecipe = this.createGlyphRecipe(null, 1, true, NamedTextColor.DARK_PURPLE, new ItemStack(Material.TNT, 2));
    }

    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public String getDescription() {
        return "Create a fiery explosion!";
    }

    @Override
    public List<Recipe> getRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();
        recipes.add(glyphRecipe);
        return recipes;
    }

    @Override
    public boolean activateSpell(Entity entity, int level, ItemStack glyphStack) {
        if (!(entity instanceof Player)) {
            return false;
        }

        Location location = entity.getLocation().add(0, 1.5, 0);
        Vector direction = entity.getLocation().getDirection().normalize();

        MutableInt iteration = new MutableInt(0);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), () -> {
            iteration.add(1);
            double d = (iteration.toInteger() - 1) * 0.5;

            if ((iteration.toInteger() > (10)) || !(this.getSpellManager().isCapable(entity))) {
                this.getSpellManager().setInactive(this, entity, true);
                return;
            }

            double x = direction.getX() * (d * d);
            double y = direction.getY() * (d * d);
            double z = direction.getZ() * (d * d);
            location.add(x, y, z);

            entity.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0.0, 0.0, 0.0, 1, new Particle.DustOptions(Color.RED, 2));
            entity.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 8, 0.0, 0.0, 0.0, 1);

            if (!location.getBlock().isPassable()) {
                entity.getWorld().createExplosion(location, 5f, true, true, entity);
                if (glyphStack != null)
                    glyphStack.setAmount(glyphStack.getAmount() - 1);
                this.getSpellManager().setInactive(this, entity, true);
            } else {
                entity.getWorld().getNearbyEntities(location, 1.0, 1.0, 1.0).forEach((target) -> {
                    if (target.equals(entity) || !(target instanceof LivingEntity) || !(this.isTargetable(entity, target))) {
                        return;
                    }

                    target.getWorld().createExplosion(target.getLocation(), 5f, true, true, entity);
                    if (glyphStack != null)
                        glyphStack.setAmount(glyphStack.getAmount() - 1);
                    this.getSpellManager().setInactive(this, entity, true);
                });
            }

            location.subtract(x, y, z);
        }, 0, 2);
        this.getSpellManager().setActive(this, entity, new SpellContext<>(task::cancel));
        return glyphStack == null;
    }
}
