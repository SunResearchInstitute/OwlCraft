package net.owlcraft.core.spells;

import net.owlcraft.core.OwlCraft;
import net.owlcraft.core.spells.events.SpellCastEvent;
import net.owlcraft.core.spells.events.SpellDeactivatingEvent;
import net.owlcraft.core.spells.events.SpellTargetingEvent;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.BiPredicate;

public class SpellManager implements Listener {
    private final Map<String, Spell> spells = new HashMap<>();
    private final Map<String, ShapelessRecipe> glyphs = new HashMap<>();

    private final Map<Player, Map<Spell, SpellContext<?>>> activeSpells = new HashMap<>();
    private final Map<Spell, Map<Player, SpellContext<?>>> activeUsers = new HashMap<>();

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
        List<ShapelessRecipe> recipes = spell.getGlyphRecipes();
        if (recipes != null)
            recipes.forEach(Bukkit::addRecipe);
        this.spells.put(spell.getName(), spell);
        spell.getGlyphRecipes().forEach(recipe -> glyphs.put(recipe.getKey().getKey(), recipe));
        spell.spellManager = this;
    }

    public Spell getSpell(String spell) {
        return this.spells.get(spell);
    }

    public ShapelessRecipe getGlyph(String glyph) {
        return this.glyphs.get(glyph);
    }


    public Collection<Spell> getSpells() {
        return (Collections.unmodifiableCollection(this.spells.values()));
    }

    public Collection<ShapelessRecipe> getGlyphs() {
        return (Collections.unmodifiableCollection(this.glyphs.values()));
    }

    public boolean isCapable(Player player) {
        GameMode gamemode = player.getGameMode();
        if (((gamemode == GameMode.CREATIVE) && !player.hasPermission("owlcraft.creative.spells")) || (gamemode == GameMode.SPECTATOR)) {
            return (false);
        }
        return (player.isValid());
    }


    public void setActive(Spell spell, Player player, SpellContext<?> context, Collection<Entity> targets) {
        Map<Spell, SpellContext<?>> activeSpells = this.activeSpells.computeIfAbsent(player, k -> new HashMap<>());
        Map<Player, SpellContext<?>> activeUsers = this.activeUsers.computeIfAbsent(spell, k -> new HashMap<>());
        activeSpells.put(spell, context);
        activeUsers.put(player, context);
        if (targets != null) {
            if (context == null) {
                throw new IllegalStateException("Can't add targets with to a null context!");
            }
            context.getTargets().addAll(targets);
        }
    }

    public void setActive(Spell spell, Player player, SpellContext<?> context) {
        this.setActive(spell, player, context, null);
    }


    public void setActive(Spell spell, Player player, Collection<Entity> targets) {
        this.setActive(spell, player, new SpellContext<>(null), targets);
    }

    public void setActive(Spell spell, Player player) {
        this.setActive(spell, player, null, null);
    }

    public void setInactive(Spell spell, Player player, boolean clean) {
        SpellDeactivatingEvent event = new SpellDeactivatingEvent(spell, player, clean);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        Map<Spell, SpellContext<?>> activeSpell = this.activeSpells.get(player);
        if (activeSpell == null) {
            return;
        }
        SpellContext<?> context = activeSpell.remove(spell);
        if (activeSpell.isEmpty()) {
            this.activeSpells.remove(player);
        }
        if (clean && (context != null)) {
            context.clean();
        }
        Map<Player, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
        activeUsers.remove(player);
        if (activeUsers.isEmpty()) {
            this.activeUsers.remove(spell);
        }
    }

    public SpellContext<?> getContext(Spell spell, Player player) {
        Map<Spell, SpellContext<?>> activeSpell = this.activeSpells.get(player);
        if (activeSpell == null) {
            return (null);
        }
        return (activeSpell.get(spell));
    }

    public <T> SpellContext<T> getContext(Spell spell, Player player, Class<T> contextClass) {
        return ((SpellContext<T>) this.getContext(spell, player));
    }

    public boolean isActive(Spell spell, Player player) {
        Map<Spell, SpellContext<?>> activeSpells = this.activeSpells.get(player);
        if (activeSpells == null) {
            return (false);
        }
        return (activeSpells.containsKey(spell));
    }

    public Map<Spell, SpellContext<?>> getActiveSpells(Player player) {
        Map<Spell, SpellContext<?>> activeSpells = this.activeSpells.get(player);
        if (activeSpells == null) {
            return (new HashMap<>());
        }
        return (new HashMap<>(activeSpells));
    }

    public Map<Entity, SpellContext<?>> getActiveUsers(Spell spell) {
        Map<Player, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
        if (activeUsers == null) {
            return (new HashMap<>());
        }
        return (new HashMap<>(activeUsers));
    }

    public Set<Entity> getTargets(Spell spell) {
        Set<Entity> targets = new HashSet<>();
        Map<Player, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
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
        Map<Player, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
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
        Map<Player, SpellContext<?>> activeUsers = this.activeUsers.get(spell);
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

    public boolean activateGlyph(Spell spell, Player player, int level, ItemStack stack) {
        if (!(this.isCapable(player))) {
            return (false);
        }
        if (this.isActive(spell, player) && !spell.ignoreIsActive()) {
            return (false);
        }

        SpellCastEvent event = new SpellCastEvent(spell, level, stack, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        level = event.getLevel();

        return spell.activateSpell(player, level, stack);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onSpellTargeting(SpellTargetingEvent event) {
        EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(event.getPlayer(), event.getTarget(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, 5d);
        Bukkit.getPluginManager().callEvent(damageEvent);
        event.setCancelled(damageEvent.isCancelled());
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        for (Spell spell : this.getActiveSpells(event.getEntity()).keySet()) {
            this.setInactive(spell, event.getEntity(), true);
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        for (Spell spell : this.getActiveSpells(event.getPlayer()).keySet()) {
            this.setInactive(spell, event.getPlayer(), true);
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
                    if (this.activateGlyph(this.getSpell(spell), event.getPlayer(), level, event.getItem()))
                        event.getItem().setAmount(event.getItem().getAmount() - 1);
                }
            }
        }
    }
}
