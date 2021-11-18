package services.headpat.owlcraft.spells;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Triple;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataType;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.spells.events.SpellCastEvent;
import services.headpat.owlcraft.spells.events.SpellTargetingEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;

@SuppressWarnings("unused")
public class SpellManager implements Listener {
	private final Map<String, Spell> spells = new HashMap<>();

	private final Map<Entity, Map<Spell, SpellContext<?>>> activeSpells = new HashMap<>();
	private final Map<Spell, Map<Entity, SpellContext<?>>> activeUsers = new HashMap<>();

	public SpellManager() {
		Bukkit.getPluginManager().registerEvents(this, OwlCraft.getInstance());
	}


	/**
	 * Register spells.
	 *
	 * @param spell Spell to register.
	 */
	public void add(Spell spell) {
		if (spell instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) spell, OwlCraft.getInstance());
		}
		List<Recipe> recipes = spell.getRecipes();
		if (recipes != null)
			recipes.forEach(Bukkit::addRecipe);
		this.spells.put(spell.getName(), spell);
		spell.spellManager = this;
	}

	public Spell get(String spell) {
		return (this.spells.get(spell));
	}

	public Collection<Spell> getSpells() {
		return (Collections.unmodifiableCollection(this.spells.values()));
	}

	public boolean isCapable(Entity entity) {
		if (entity instanceof Player) {
			GameMode gamemode = ((Player) entity).getGameMode();
			if (((gamemode == GameMode.CREATIVE) && !entity.hasPermission("owlcraft.creative.spells")) || (gamemode == GameMode.SPECTATOR)) {
				return (false);
			}
		}
		return (entity.isValid());
	}


	public void setActive(Spell spell, Entity entity, SpellContext<?> context, Collection<Entity> targets) {
		Map<Spell, SpellContext<?>> activeSpells = this.activeSpells.computeIfAbsent(entity, k -> new HashMap<>());
		Map<Entity, SpellContext<?>> activeUsers = this.activeUsers.computeIfAbsent(spell, k -> new HashMap<>());
		activeSpells.put(spell, context);
		activeUsers.put(entity, context);
		if (targets != null) {
			if (context == null) {
				throw new IllegalStateException("Can't add targets with to a null context!");
			}
			context.getTargets().addAll(targets);
		}
	}

	public void setActive(Spell spell, Entity entity, SpellContext<?> context) {
		this.setActive(spell, entity, context, null);
	}


	public void setActive(Spell spell, Entity entity, Collection<Entity> targets) {
		this.setActive(spell, entity, new SpellContext<>(null), targets);
	}

	public void setActive(Spell spell, Entity entity) {
		this.setActive(spell, entity, null, null);
	}

	public void setInactive(Spell spell, Entity entity, boolean clean) {
		Map<Spell, SpellContext<?>> activeSpell = this.activeSpells.get(entity);
		if (activeSpell == null) {
			return;
		}
		SpellContext<?> context = activeSpell.remove(spell);
		if (activeSpell.isEmpty()) {
			this.activeSpells.remove(entity);
		}
		if (clean && (context != null)) {
			context.clean();
		}
		Map<Entity, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
		activeUsers.remove(entity);
		if (activeUsers.isEmpty()) {
			this.activeUsers.remove(spell);
		}
	}

	public void setInactive(Spell spell, Entity entity) {
		this.setInactive(spell, entity, true);
	}

	public @Nullable
	SpellContext<?> getContext(Spell spell, Entity entity) {
		Map<Spell, SpellContext<?>> activeSpell = this.activeSpells.get(entity);
		if (activeSpell == null) {
			return (null);
		}
		return (activeSpell.get(spell));
	}

	@SuppressWarnings("unchecked")
	public <T> SpellContext<T> getContext(Spell spell, Entity entity, Class<T> contextClass) {
		return ((SpellContext<T>) this.getContext(spell, entity));
	}

	public boolean isActive(Spell spell, Entity entity) {
		Map<Spell, SpellContext<?>> activeSpells = this.activeSpells.get(entity);
		if (activeSpells == null) {
			return (false);
		}
		return (activeSpells.containsKey(spell));
	}

	public Map<Spell, SpellContext<?>> getActiveSpells(Entity entity) {
		Map<Spell, SpellContext<?>> activeSpells = this.activeSpells.get(entity);
		if (activeSpells == null) {
			return (new HashMap<>());
		}
		return (new HashMap<>(activeSpells));
	}

	public Map<Entity, SpellContext<?>> getActiveUsers(Spell spell) {
		Map<Entity, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
		if (activeUsers == null) {
			return (new HashMap<>());
		}
		return (new HashMap<>(activeUsers));
	}

	public Set<Entity> getTargets(Spell spell) {
		Set<Entity> targets = new HashSet<>();
		Map<Entity, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
		if (activeUsers != null) {
			activeUsers.values().forEach((context) -> {
				if (context != null) {
					targets.addAll(context.getTargets());
				}
			});
		}
		return (targets);
	}

	public List<Triple<Entity, SpellContext<?>, Entity>> getTargetsWithContext(Spell spell) {
		List<Triple<Entity, SpellContext<?>, Entity>> targets = new LinkedList<>();
		Map<Entity, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
		if (activeUsers != null) {
			activeUsers.forEach((user, context) -> {
				if (context != null) {
					context.getTargets().forEach((target) -> targets.add(new ImmutableTriple<>(user, context, target)));
				}
			});
		}
		return (targets);
	}

	public Map<Entity, SpellContext<?>> getTargetingUsers(Spell spell, Entity target, BiPredicate<Entity, SpellContext<?>> filter) {
		Map<Entity, SpellContext<?>> targetingUsers = new HashMap<>();
		Map<Entity, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
		if (activeUsers == null) {
			return (targetingUsers);
		}
		activeUsers.forEach((user, context) -> {
			if (context.getTargets().contains(target) && ((filter == null) || filter.test(user, context))) {
				targetingUsers.put(user, context);
			}
		});
		return (targetingUsers);
	}

	public Map<Entity, SpellContext<?>> getTargetingUsers(Spell spell, Entity target) {
		return (this.getTargetingUsers(spell, target, null));
	}

	public boolean activateGlyph(Spell spell, Entity entity, int level, ItemStack stack) {
		if (!(this.isCapable(entity))) {
			return (false);
		}
		if (this.isActive(spell, entity) && !spell.ignoreIsActive()) {
			return (false);
		}

		SpellCastEvent event = new SpellCastEvent(spell, level, stack, entity);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return (false);
		}
		level = event.getLevel();

		return spell.activateSpell(entity, level, stack);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onSpellTargeting(SpellTargetingEvent event) {
		EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(event.getEntity(), event.getTarget(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.0);
		Bukkit.getPluginManager().callEvent(damageEvent);
		event.setCancelled(damageEvent.isCancelled());
	}

	@EventHandler
	private void onEntityDeath(EntityDeathEvent event) {
		for (Spell spell : this.getActiveSpells(event.getEntity()).keySet()) {
			this.setInactive(spell, event.getEntity());
		}
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent event) {
		for (Spell spell : this.getActiveSpells(event.getPlayer()).keySet()) {
			this.setInactive(spell, event.getPlayer());
		}
	}

	/*
	Glyph Handler
	*/
	@EventHandler
	private void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (event.getItem() != null && event.getItem().hasItemMeta()) {
				if (event.getItem().getItemMeta().getPersistentDataContainer().has(Spell.SPELL_NAME_KEY, PersistentDataType.STRING)) {
					String spell = event.getItem().getItemMeta().getPersistentDataContainer().get(Spell.SPELL_NAME_KEY, PersistentDataType.STRING);
					Integer level = event.getItem().getItemMeta().getPersistentDataContainer().get(Spell.SPELL_LEVEL_KEY, PersistentDataType.INTEGER);
					assert spell != null;
					assert level != null;
					if (this.activateGlyph(this.get(spell), event.getPlayer(), level, event.getItem()))
						event.getItem().setAmount(event.getItem().getAmount() - 1);
				}
			}
		}
	}
}
