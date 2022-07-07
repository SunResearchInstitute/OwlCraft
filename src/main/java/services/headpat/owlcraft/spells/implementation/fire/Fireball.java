package services.headpat.owlcraft.spells.implementation.fire;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitTask;
import services.headpat.owlcraft.spells.Spell;
import services.headpat.owlcraft.spells.SpellContext;
import services.headpat.owlcraft.spells.utils.BeamUtil;

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
    public boolean activateSpell(Player player, int level, ItemStack glyphStack) {
        BukkitTask task = BeamUtil.createBeam(player, 14, (mutableInt, location) -> {
            if (!location.getBlock().isPassable()) {
                player.getWorld().createExplosion(location, 5f, true, true, player);
                if (glyphStack != null)
                    glyphStack.setAmount(glyphStack.getAmount() - 1);
                this.getSpellManager().setInactive(this, player, true);
            } else {
                player.getWorld().getNearbyLivingEntities(location, 1.0, 1.0, 1.0, target -> !target.equals(player) || this.isTargetable(player, target))
                        .forEach((target) -> {
                            target.getWorld().createExplosion(target.getLocation(), 5f, true, true, player);
                            if (glyphStack != null)
                                glyphStack.setAmount(glyphStack.getAmount() - 1);
                            this.getSpellManager().setInactive(this, player, true);
                        });
            }
        }, Color.RED);
        this.getSpellManager().setActive(this, player, new SpellContext<>(task::cancel));
        return glyphStack == null;
    }
}
