package net.owlcraft.spells.implementation.ice;

import net.kyori.adventure.text.format.NamedTextColor;
import net.owlcraft.OwlCraft;
import net.owlcraft.spells.Spell;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import services.headpat.spigotextensions.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

public class Ice extends Spell {
    private final ShapelessRecipe smallGlyphRecipe;
    private final ShapelessRecipe mediumGlyphRecipe;
    private final ShapelessRecipe largeGlyphRecipe;

    public Ice() {
        smallGlyphRecipe = this.createGlyphRecipe("Small", 1, true, NamedTextColor.DARK_PURPLE, new ItemStack(Material.SNOW_BLOCK, 2));
        ItemStack medium = smallGlyphRecipe.getResult();
        medium.setAmount(4);
        mediumGlyphRecipe = this.createGlyphRecipe("Medium", 2, false, NamedTextColor.DARK_PURPLE, medium);
        ItemStack large = mediumGlyphRecipe.getResult();
        large.setAmount(4);
        largeGlyphRecipe = this.createGlyphRecipe("Large", 3, false, NamedTextColor.DARK_PURPLE, large);
    }

    @Override
    public String getName() {
        return "Ice";
    }

    @Override
    public String getDescription() {
        return "Freeze enemies nearby.";
    }

    @Override
    public List<ShapelessRecipe> getGlyphRecipes() {
        ArrayList<ShapelessRecipe> recipes = new ArrayList<>();
        recipes.add(smallGlyphRecipe);
        recipes.add(mediumGlyphRecipe);
        recipes.add(largeGlyphRecipe);
        return recipes;
    }

    @Override
    public boolean activateSpell(Player player, int level, ItemStack glyphStack) {
        int ticks = MiscUtils.timeToTicks(0, 3) + (level * 20);

        int targetCnt = 0;

        for (Entity target : player.getNearbyEntities(4 + level, 4 + level, 4 + level)) {
            if (target instanceof LivingEntity && this.isTargetable(player, target)
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
            player.sendMessage(ChatColor.RED + "No targets found!");
            return false;
        }
    }
}
