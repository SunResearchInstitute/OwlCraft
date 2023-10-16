package net.owlcraft.core.spells.implementation.space;

import net.kyori.adventure.text.format.NamedTextColor;
import net.owlcraft.core.spells.Spell;
import net.owlcraft.core.spells.SpellContext;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import dev.sunresearch.spigotextensions.utils.MiscUtils;

public class SafetyHover extends Spell implements Listener {
    private final ShapelessRecipe recipe;

    public SafetyHover() {
        recipe = this.createGlyphRecipe(null, 1, true, NamedTextColor.DARK_PURPLE, new ItemStack(Material.TNT, 2));
    }

    @Override
    public String getName() {
        return "Safety Hover";
    }

    @Override
    public String getDescription() {
        return "Make yourself hover before you impact the ground!";
    }

    @Override
    public List<ShapelessRecipe> getGlyphRecipes() {
        ArrayList<ShapelessRecipe> recipes = new ArrayList<>();
        recipes.add(recipe);
        return recipes;
    }

    @Override
    public boolean activateSpell(Player player, int level, ItemStack glyphStack) {
        if (player.hasPotionEffect(PotionEffectType.LEVITATION) || player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            return false;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, MiscUtils.timeToTicks(0, 2), 255, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, MiscUtils.timeToTicks(0, 6), 0, false, false, true));

        this.getSpellManager().setActive(this, player, new SpellContext<>(() -> {
            player.removePotionEffect(PotionEffectType.LEVITATION);
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }));

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityPotionEffectEvent(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!(this.getSpellManager().isActive(this, player))) {
                return;
            }
            if (event.getOldEffect() != null && (event.getOldEffect().getType().equals(PotionEffectType.SLOW_FALLING))) {
                this.getSpellManager().setInactive(this, player, true);
            }
        }
    }



}
