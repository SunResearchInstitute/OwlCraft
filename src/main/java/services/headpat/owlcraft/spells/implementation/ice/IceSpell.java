package services.headpat.owlcraft.spells.implementation.ice;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.spells.Spell;
import services.headpat.spigotextensions.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class IceSpell extends Spell {
	private final ShapelessRecipe smallGlyphRecipe;
	private final ShapelessRecipe mediumGlyphRecipe;
	private final ShapelessRecipe largeGlyphRecipe;

	public IceSpell() {
		smallGlyphRecipe = this.createGlyphRecipe("Small", 1, true, ChatColor.DARK_PURPLE, new ItemStack(Material.SNOW_BLOCK, 2));
		ItemStack medium = smallGlyphRecipe.getResult();
		medium.setAmount(4);
		mediumGlyphRecipe = this.createGlyphRecipe("Medium", 2, false, ChatColor.DARK_PURPLE, medium);
		ItemStack large = mediumGlyphRecipe.getResult();
		large.setAmount(4);
		largeGlyphRecipe = this.createGlyphRecipe("Large", 2, false, ChatColor.DARK_PURPLE, large);
	}

	@Override
	public @NotNull String getName() {
		return "Ice";
	}

	@Override
	public @NotNull String getDescription() {
		return "Freeze enemies nearby.";
	}

	@Override
	public List<Recipe> getRecipes() {
		ArrayList<Recipe> recipes = new ArrayList<>();
		recipes.add(smallGlyphRecipe);
		recipes.add(mediumGlyphRecipe);
		recipes.add(largeGlyphRecipe);
		return recipes;
	}

	@Override
	public boolean activateSpell(@NotNull Entity entity, int level, @Nullable ItemStack glyphStack) {
		int ticks = MiscUtils.timeToTicks(0, 3) + (level * 20);

		int targetCnt = 0;

		for (Entity target : entity.getNearbyEntities(4 + level, 4 + level, 4 + level)) {
			if (target instanceof LivingEntity && this.isTargetable(entity, target)
					&& !((LivingEntity) target).hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				targetCnt++;

				((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, level - 1, false, false, true));
				((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ticks, 255, false, false, true));
				BukkitTask task = Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), () -> {
					if (!target.isDead())
						target.getWorld().spawnParticle(Particle.REDSTONE, target.getLocation(), 15, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.AQUA, 2));
				}, 0, 10);
				Bukkit.getScheduler().runTaskLater(OwlCraft.getInstance(), task::cancel, ticks);
			}
		}
		if (targetCnt > 0) {
			return true;
		} else {
			entity.sendMessage(ChatColor.RED + "No targets found!");
			return false;
		}
	}
}
