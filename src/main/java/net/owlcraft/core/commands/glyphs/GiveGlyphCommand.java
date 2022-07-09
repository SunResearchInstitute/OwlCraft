package net.owlcraft.core.commands.glyphs;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.sunresearch.spigotextensions.brigadier.BrigadierExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.owlcraft.core.commands.glyphs.arguments.GlyphArgumentType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static dev.sunresearch.spigotextensions.brigadier.arguments.PlayerArgumentType.getPlayer;
import static dev.sunresearch.spigotextensions.brigadier.arguments.PlayerArgumentType.player;
import static net.owlcraft.core.commands.glyphs.arguments.GlyphArgumentType.getGlyph;

public class GiveGlyphCommand extends BrigadierExecutor {
    public GiveGlyphCommand() {
        super(dispatcher -> dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal("giveglyph")
                .then(RequiredArgumentBuilder.<CommandSender, Player>argument("player", player())
                        .then(RequiredArgumentBuilder.<CommandSender, ShapelessRecipe>argument("glyph", GlyphArgumentType.glyph())
                                .executes(ctx -> {
                                    Player player = getPlayer(ctx, "player");
                                    ItemStack glyphItem = getGlyph(ctx, "glyph").getResult().clone();
                                    glyphItem.setAmount(64);
                                    player.getInventory().addItem(glyphItem);
                                    ctx.getSource().sendMessage(
                                            Component.text("Giving ").color(NamedTextColor.GOLD)
                                                    .append(Component.text("64 ").color(NamedTextColor.RED))
                                                    .append(Component.text("of ").color(NamedTextColor.GOLD))
                                                    .append(glyphItem.displayName().color(NamedTextColor.RED))
                                                    .append(Component.text(" to ").color(NamedTextColor.GOLD))
                                                    .append(player.displayName()));
                                    return 1;
                                })
                                .then(RequiredArgumentBuilder.<CommandSender, Integer>argument("amount", IntegerArgumentType.integer(1, 64))
                                        .suggests((commandContext, builder) -> {
                                            Arrays.asList("1", "64").forEach(s ->
                                            {
                                                if (s.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                                                    builder.suggest(s);
                                            });
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            Player player = getPlayer(ctx, "player");
                                            ItemStack glyphItem = getGlyph(ctx, "glyph").getResult().clone();
                                            int amt = getInteger(ctx, "amount");
                                            glyphItem.setAmount(amt);
                                            player.getInventory().addItem(glyphItem);
                                            ctx.getSource().sendMessage(
                                                    Component.text("Giving ").color(NamedTextColor.GOLD)
                                                            .append(Component.text(glyphItem.getAmount()).color(NamedTextColor.RED))
                                                            .append(Component.text(" of ").color(NamedTextColor.GOLD))
                                                            .append(glyphItem.displayName().color(NamedTextColor.RED))
                                                            .append(Component.text(" to ").color(NamedTextColor.GOLD))
                                                            .append(player.displayName()));
                                            return 1;
                                        })
                                )
                        )
                )
        ));
    }
}
