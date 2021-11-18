package services.headpat.owlcraft.spells.implementation.space;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitTask;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.spells.Spell;
import services.headpat.owlcraft.spells.SpellContext;

import java.util.ArrayList;
import java.util.List;

public class TeleportAnchor extends Spell {
	ShapelessRecipe recipe;
	private static final BaseComponent[] MESSAGE_SET = new ComponentBuilder().italic(true).color(net.md_5.bungee.api.ChatColor.RED).append("Teleport anchor set!").create();
	private static final BaseComponent[] MESSAGE_UNSET = new ComponentBuilder().italic(true).color(net.md_5.bungee.api.ChatColor.RED).append("Teleport anchor's gone!").create();

	public TeleportAnchor() {
		recipe = this.createGlyphRecipe(null, 1, true, ChatColor.BLUE, new ItemStack(Material.DIAMOND, 1), new ItemStack(Material.ENDER_PEARL, 1));
	}

	@Override
	public String getName() {
		return "Teleport Anchor";
	}

	@Override
	public String getDescription() {
		return "Create a teleport anchor you can travel back to.";
	}

	@Override
	public List<Recipe> getRecipes() {
		ArrayList<Recipe> recipes = new ArrayList<>();
		recipes.add(recipe);
		return recipes;
	}

	@Override
	public boolean ignoreIsActive() {
		return true;
	}

	@Override
	public boolean activateSpell(Entity entity, int level, ItemStack glyphStack) {
		if (this.spellManager.isActive(this, entity)) {
			if (entity instanceof Player && ((Player) entity).isSneaking()) {
				this.spellManager.setInactive(this, entity);
				return false;
			}
			//TODO: set radius limits!!
			SpellContext<Location> ctx = this.spellManager.getContext(this, entity, Location.class);
			Location newLoc = ctx.getContext();
			Location oldLoc = entity.getLocation();
			newLoc.setDirection(oldLoc.getDirection());
			oldLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, oldLoc, 35, 0.2, 0.2, 0.2);
			oldLoc.getWorld().playSound(oldLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 20, 1.2f);
			entity.teleport(newLoc);
			newLoc.getWorld().spawnParticle(Particle.PORTAL, newLoc, 35, 0.2, 0.2, 0.2);
			newLoc.getWorld().playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 20, 1.8f);
			if (glyphStack.getAmount() == 1) {
				this.spellManager.setInactive(this, entity);
			}
			return true;
		} else {
			Location loc = entity.getLocation().clone();
			loc.getWorld().playSound(loc, Sound.BLOCK_ENDER_CHEST_OPEN, 20, 1.2f);
			BukkitTask task = Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), () -> loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc, 20, 0.1, 0.1, 0.1), 20, 10);
			this.spellManager.setActive(this, entity, new SpellContext<>(loc, () -> {
				loc.getWorld().playSound(loc, Sound.BLOCK_ENDER_CHEST_OPEN, 20, 0.6f);
				task.cancel();
				entity.sendMessage(MESSAGE_UNSET);
			}));
			entity.sendMessage(MESSAGE_SET);
			return false;
		}
	}
}
