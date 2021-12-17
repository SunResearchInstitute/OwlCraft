package services.headpat.owlcraft.spells;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.spells.events.SpellTargetingEvent;
import services.headpat.spigotextensions.utils.ChatUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Spell {
	public static final NamespacedKey SPELL_NAME_KEY = new NamespacedKey(OwlCraft.getInstance(), "spell_name");
	public static final NamespacedKey SPELL_LEVEL_KEY = new NamespacedKey(OwlCraft.getInstance(), "spell_level");
	/**
	 * List of entities you cannot target, should not be used directly.
	 * Use {@link #isTargetable}.
	 */
	protected static final Set<EntityType> ENTITY_BLACKLIST = new HashSet<>();

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

	/**
	 * SpellManager instance.
	 */
	@Getter
	public SpellManager spellManager = null;

	/**
	 * Checks if an entity is blacklisted from being affected from powers by default.
	 *
	 * @param target The entity in question.
	 * @return Whether the entity is blacklisted.
	 */
	protected static boolean isBlacklisted(Entity target) {
		return ENTITY_BLACKLIST.contains(target.getType());
	}

	/**
	 * Checks if an entity is a suitable target for a power.
	 *
	 * @param what            The entity using the power.
	 * @param target          The target entity.
	 * @param ignoreBlacklist Whether to consult the entity blacklist or not.
	 * @return Whether the entity is a suitable target.
	 */
	protected boolean isTargetable(Entity what, Entity target, boolean ignoreBlacklist) {
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
	 * @return Whether the entity is a suitable target.
	 */
	protected boolean isTargetable(Entity entity, Entity target) {
		return (this.isTargetable(entity, target, false));
	}

	/**
	 * Gets the spell's name.
	 *
	 * @return The spell's name.
	 */
	public abstract String getName();

	/**
	 * Gets a brief description of how this spell works.
	 *
	 * @return The spell's description.
	 */
	public abstract String getDescription();

	/**
	 * This is unnecessary for spells not being implemented in OwlCraftServer code.
	 *
	 * @return The coven the spell is part of.
	 */
	public String getCoven() {
		return "Covenless";
	}

	public boolean ignoreIsActive() {
		return false;
	}

	/**
	 * @return List of recipes to add when registered.
	 */
	public abstract List<Recipe> getRecipes();

	public abstract boolean activateSpell(Entity entity, int level, ItemStack glyphStack);

	/**
	 * Creates the glyph ItemStack, should not be called directly in most cases.
	 * Use {@link #createGlyphRecipe(String, int, boolean, TextColor, ItemStack...)}.
	 */
	protected ItemStack createGlyph(String size, int level, TextColor loreChatColor) {
		ItemStack item = new ItemStack(Material.PAPER);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(Component.text(size + (!size.equals("") ? " " : "") + this.getName() + " Glyph").color(NamedTextColor.AQUA));
		meta.lore(ChatUtils.createLore(this.getDescription(), loreChatColor));
		services.headpat.owlcraft.utils.Utils.addBaseFlags(meta);
		meta.getPersistentDataContainer().set(Spell.SPELL_NAME_KEY, PersistentDataType.STRING, this.getName());
		meta.getPersistentDataContainer().set(Spell.SPELL_LEVEL_KEY, PersistentDataType.INTEGER, level);
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * @param size                   Prefixes the glyph name. This is usually Small, Medium, or Large. This call also be an empty or null string if there should be no sizes.
	 * @param level                  The level or magnitude of the glyph. This should usually correspond to the size.
	 * @param includeBaseIngredients Whether to include the base ingredients (paper and an ink sac).
	 * @param loreChatColor          Color of the lore.
	 * @param ingredients            Array of ingredients the glyph will use. Every glyph will always require one ink sac and one paper.
	 * @return The glyph recipe with the appropriate metadata.
	 */
	protected ShapelessRecipe createGlyphRecipe(String size, int level, boolean includeBaseIngredients, TextColor loreChatColor, ItemStack... ingredients) {
		if (StringUtils.isBlank(size))
			size = "";
		ItemStack stack = this.createGlyph(size, level, loreChatColor);
		ShapelessRecipe recipe = new ShapelessRecipe(createKey(size, level), stack);
		if (includeBaseIngredients) {
			recipe.addIngredient(Material.PAPER);
			recipe.addIngredient(Material.INK_SAC);
		}
		Arrays.stream(ingredients).forEach(recipe::addIngredient);
		return recipe;
	}

	/**
	 * Creates the glyph NamespacedKey, should not be called directly in most cases.
	 * Use {@link #createGlyphRecipe(String, int, boolean, TextColor, ItemStack...)}.
	 */
	protected NamespacedKey createKey(String size, int level)
	{
		return new NamespacedKey(OwlCraft.getInstance(), size.toLowerCase() + (!size.equals("") ? "_" : "") + getName().replace(" ", "").toLowerCase() + "_glyph");
	}
}
