package net.owlcraft.core.spells.implementation.space;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.owlcraft.core.OwlCraft;
import net.owlcraft.core.spells.Spell;
import net.owlcraft.core.spells.SpellContext;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TeleportAnchor extends Spell {
    private final ShapelessRecipe recipe;
    private static final Component MESSAGE_SET = Component.text("Teleport anchor set!").decorate(TextDecoration.ITALIC).color(NamedTextColor.RED);
    private static final Component MESSAGE_UNSET = Component.text("Teleport anchor's gone!").decorate(TextDecoration.ITALIC).color(NamedTextColor.RED);

    public TeleportAnchor() {
        recipe = this.createGlyphRecipe(null, 1, true, NamedTextColor.BLUE, new ItemStack(Material.GOLD_INGOT, 1), new ItemStack(Material.ENDER_PEARL, 1));
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
    public List<ShapelessRecipe> getGlyphRecipes() {
        ArrayList<ShapelessRecipe> recipes = new ArrayList<>();
        recipes.add(recipe);
        return recipes;
    }

    @Override
    public boolean ignoreIsActive() {
        return true;
    }

    @Override
    public boolean deactivateOnDeath() {
        return false;
    }

    @Override
    public boolean activateSpell(Player player, int level, ItemStack glyphStack) {
        if (this.spellManager.isActive(this, player)) {
            if (player.isSneaking()) {
                this.spellManager.setInactive(this, player, true);
                return false;
            }
            SpellContext<Location> ctx = this.spellManager.getContext(this, player, Location.class);
            Location newLoc = ctx.getContext();
            Location oldLoc = player.getLocation();
            player.teleport(newLoc);
            newLoc.setDirection(oldLoc.getDirection());

            oldLoc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, oldLoc, 35, 0.2, 0.2, 0.2);
            oldLoc.getWorld().playSound(oldLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 20, 1.2f);
            newLoc.getWorld().spawnParticle(Particle.PORTAL, newLoc, 35, 0.2, 0.2, 0.2);
            newLoc.getWorld().playSound(newLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 20, 1.8f);
            if (glyphStack.getAmount() == 1) {
                this.spellManager.setInactive(this, player, true);
            }
            return true;
        } else {
            Location loc = player.getLocation().clone();
            loc.getWorld().playSound(loc, Sound.BLOCK_ENDER_CHEST_OPEN, 20, 1.2f);
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), () -> loc.getWorld()
                    .spawnParticle(Particle.REVERSE_PORTAL, loc, 20, 0.1, 0.1, 0.1), 20, 10);
            this.spellManager.setActive(this, player, new SpellContext<>(loc, () -> {
                loc.getWorld().playSound(loc, Sound.BLOCK_ENDER_CHEST_OPEN, 20, 0.6f);
                task.cancel();
                player.sendMessage(MESSAGE_UNSET);
            }));
            player.sendMessage(MESSAGE_SET);
            return false;
        }
    }
}
