package net.owlcraft.core.spells.implementation.fire;

import net.kyori.adventure.text.format.NamedTextColor;
import net.owlcraft.core.spells.Spell;
import net.owlcraft.core.spells.SpellContext;
import net.owlcraft.core.spells.utils.BeamUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Fireball extends Spell {
    private final ShapelessRecipe glyphRecipe;

    public Fireball() {
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
    public List<ShapelessRecipe> getGlyphRecipes() {
        ArrayList<ShapelessRecipe> recipes = new ArrayList<>();
        recipes.add(glyphRecipe);
        return recipes;
    }

    @Override
    public boolean deactivateOnDeath() {
        return false;
    }

    @Override
    public boolean activateSpell(Player player, int level, ItemStack glyphStack) {
        MutableObject<Location> lastLoc = new MutableObject<>();
        BukkitTask task = BeamUtils.createBeam(player, 14, 4, (mutableInt, location) -> {
            for (LivingEntity target : player.getWorld().getNearbyLivingEntities(location, 1.0, 1.0, 1.0)) {
                if (target.equals(player) || !this.isTargetable(player, target)) {
                    continue;
                }
                target.getWorld().createExplosion(target.getLocation(), 5f, true, true, player);
                this.getSpellManager().setInactive(this, player, true);
            }
            lastLoc.setValue(location);
        }, () -> {
            player.getWorld().createExplosion(lastLoc.getValue(), 5f, true, true, player);
            this.getSpellManager().setInactive(this, player, false);
        }, Color.RED);
        this.getSpellManager().setActive(this, player, new SpellContext<>(() -> {
            task.cancel();
            if (glyphStack != null)
                glyphStack.setAmount(glyphStack.getAmount() - 1);
        }));
        return glyphStack != null;
    }
}
