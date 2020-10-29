package services.headpat.owlcraft.spells.implementation.fire;

import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.spells.Spell;
import services.headpat.owlcraft.spells.SpellContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class FireballSpell extends Spell {
	private final ShapelessRecipe glyphRecipe;

	public FireballSpell() {
		glyphRecipe = this.createGlyphRecipe("", 1, ChatColor.DARK_PURPLE, new ItemStack(Material.TNT, 2));
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
	public boolean activateSpell(@NotNull Entity entity, int level, @Nullable ItemStack glyphStack) {
		if (!(entity instanceof Player)) {
			return false;
		}
		((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10, 1, false, false, false));


		Location location = entity.getLocation().add(0, 1.5, 0);
		Vector direction = entity.getLocation().getDirection().normalize();
		AtomicReference<Location> lastLocation = new AtomicReference<>();

		MutableInt iteration = new MutableInt(0);
		BukkitTask task = Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), () -> {
			lastLocation.set(location.clone());

			iteration.add(1);
			double d = (iteration.toInteger() - 1) * 0.5;

			if ((iteration.toInteger() > (10)) || !(this.getSpellManager().isCapable(entity))) {
				this.getSpellManager().setInactive(this, entity);
				return;
			}

			double x = direction.getX() * (d * d);
			double y = direction.getY() * (d * d);
			double z = direction.getZ() * (d * d);
			location.add(x, y, z);

			entity.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0.0, 0.0, 0.0, 1, new Particle.DustOptions(Color.RED, 2));
			entity.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 8, 0.0, 0.0, 0.0, 1);
			double d2 = iteration.toInteger() * 0.5;
			double x2 = direction.getX() * (d2 * d2);
			double y2 = direction.getY() * (d2 * d2);
			double z2 = direction.getZ() * (d2 * d2);
			Location loc = location.clone().add(x2, y2, z2);
			if (!loc.getBlock().isEmpty() || iteration.toInteger() > 9) {
				lastLocation.get().getWorld().createExplosion(lastLocation.get(), 4f, true, false);
				this.getSpellManager().setInactive(this, entity);
				glyphStack.setAmount(glyphStack.getAmount() - 1);
				return;
			}

			location.subtract(x, y, z);
		}, 0, 2);
		this.getSpellManager().setActive(this, entity, new SpellContext<>(task::cancel));
		return false;
	}
}
