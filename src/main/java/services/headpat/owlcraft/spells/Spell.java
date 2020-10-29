package services.headpat.owlcraft.spells;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.spells.events.SpellTargetingEvent;
import services.headpat.owlcraft.utils.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Spell {
	public static final NamespacedKey SPELL_NAME_KEY = new NamespacedKey(OwlCraft.getInstance(), "spell_name");
	public static final NamespacedKey SPELL_LEVEL_KEY = new NamespacedKey(OwlCraft.getInstance(), "spell_level");
	private static final Set<EntityType> ENTITY_BLACKLIST = new HashSet<>();

	static {
		ENTITY_BLACKLIST.add(EntityType.AREA_EFFECT_CLOUD);
		ENTITY_BLACKLIST.add(EntityType.ARMOR_STAND);
		ENTITY_BLACKLIST.add(EntityType.DROPPED_ITEM);
		ENTITY_BLACKLIST.add(EntityType.EXPERIENCE_ORB);
		ENTITY_BLACKLIST.add(EntityType.FALLING_BLOCK);
		ENTITY_BLACKLIST.add(EntityType.FISHING_HOOK);
		ENTITY_BLACKLIST.add(EntityType.MINECART);
		ENTITY_BLACKLIST.add(EntityType.MINECART_CHEST);
		ENTITY_BLACKLIST.add(EntityType.MINECART_COMMAND);
		ENTITY_BLACKLIST.add(EntityType.MINECART_FURNACE);
		ENTITY_BLACKLIST.add(EntityType.MINECART_HOPPER);
		ENTITY_BLACKLIST.add(EntityType.MINECART_MOB_SPAWNER);
		ENTITY_BLACKLIST.add(EntityType.MINECART_TNT);
		ENTITY_BLACKLIST.add(EntityType.PAINTING);
		ENTITY_BLACKLIST.add(EntityType.PRIMED_TNT);
	}

	@Getter
	SpellManager spellManager = null;

	/**
	 * Checks if an entity is blacklisted from being affected from powers by default.
	 *
	 * @param target The entity in question.
	 * @return Whether or not the entity is blacklisted.
	 */
	public static boolean isBlacklisted(@NotNull Entity target) {
		return ENTITY_BLACKLIST.contains(target.getType());
	}

	/**
	 * Checks if an entity is a suitable target for a power.
	 *
	 * @param what            The entity using the power.
	 * @param target          The target entity.
	 * @param ignoreBlacklist Whether to consult the entity blacklist or not.
	 * @return Whether or not the entity is a suitable target.
	 */
	public boolean isTargetable(Entity what, Entity target, boolean ignoreBlacklist) {
		if (!ignoreBlacklist && Spell.isBlacklisted(target)) {
			return (false);
		}
		if (target.isDead()) {
			return (false);
		}
		if (target instanceof Player) {
			GameMode gamemode = ((Player) target).getGameMode();
			if ((gamemode == GameMode.CREATIVE) || (gamemode == GameMode.SPECTATOR)) {
				return (false);
			}
		}
		if ((target instanceof Tameable) && (what instanceof AnimalTamer)) {
			if (((Tameable) target).isTamed() && what.equals(((Tameable) target).getOwner())) {
				return (false);
			}
		}

		SpellTargetingEvent event = new SpellTargetingEvent(this, what, target);
		Bukkit.getPluginManager().callEvent(event);
		return (!(event.isCancelled()));
	}

	/**
	 * Checks if an entity is a suitable target for a power.
	 *
	 * @param entity The entity using the power.
	 * @param target The target entity.
	 * @return Whether or not the entity is a suitable target.
	 */
	public boolean isTargetable(Entity entity, Entity target) {
		return (this.isTargetable(entity, target, false));
	}

	/**
	 * Gets the power's name.
	 *
	 * @return The power's name.
	 */
	public abstract String getName();

	/**
	 * Gets a brief description of how this power works.
	 *
	 * @return The power's description.
	 */
	public abstract String getDescription();

	public abstract List<Recipe> getRecipes();

	public abstract boolean activateSpell(@NotNull Entity entity, int level, @Nullable ItemStack glyphStack);

	protected ItemStack createGlyph(@NotNull String size, int level, ChatColor loreChatColor) {
		ItemStack item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + size + (!size.equals("") ? " " : "") + this.getName() + " Glyph");
		meta.setLore(Utils.wrapLore(this.getDescription(), loreChatColor));
		Utils.addBaseFlags(meta);
		meta.getPersistentDataContainer().set(Spell.SPELL_NAME_KEY, PersistentDataType.STRING, this.getName());
		meta.getPersistentDataContainer().set(Spell.SPELL_LEVEL_KEY, PersistentDataType.INTEGER, level);
		item.setItemMeta(meta);
		return item;
	}

	protected ShapelessRecipe createGlyphRecipe(String size, int level, ChatColor loreChatColor, ItemStack @NotNull ... ingredients) {
		ItemStack stack = this.createGlyph(size, level, loreChatColor);
		NamespacedKey namespacedKey = new NamespacedKey(OwlCraft.getInstance(), size.toLowerCase() + (!size.equals("") ? "_" : "") + getName().replace(" ", "").toLowerCase() + "_glyph");
		ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, stack);
		recipe.addIngredient(Material.PAPER);
		recipe.addIngredient(Material.INK_SAC);
		Arrays.stream(ingredients).forEach(recipe::addIngredient);
		return recipe;
	}
}
