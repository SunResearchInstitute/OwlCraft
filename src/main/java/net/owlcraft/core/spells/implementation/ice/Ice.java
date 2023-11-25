package net.owlcraft.core.spells.implementation.ice;

import dev.sunresearch.spigotextensions.utils.MiscUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.owlcraft.core.OwlCraft;
import net.owlcraft.core.spells.Spell;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
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

        for (LivingEntity target : player.getLocation().getNearbyLivingEntities(4, 4, 4, (livingEntity -> this.isTargetable(player, livingEntity) && !livingEntity.hasPotionEffect(PotionEffectType.INVISIBILITY)))) {
            targetCnt++;
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, level - 1, false, false, true));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ticks, 255, false, false, true));
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(OwlCraft.getInstance(), () -> {
                if (!target.isDead() && target.isValid())
                    target.getWorld().spawnParticle(Particle.REDSTONE, target.getLocation(), 15, 0.5, 0.5, 0.5, new Particle.DustOptions(Color.AQUA, 2));
            }, 0, 10);
            Bukkit.getScheduler().runTaskLaterAsynchronously(OwlCraft.getInstance(), task::cancel, ticks);
        }

        if (targetCnt > 0) {
            return true;
        } else {
            player.sendRichMessage("<red>No targets found!");
            return false;
        }
    }
}
