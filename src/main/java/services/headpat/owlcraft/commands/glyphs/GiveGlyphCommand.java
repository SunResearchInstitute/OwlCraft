package services.headpat.owlcraft.commands.glyphs;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import services.headpat.owlcraft.OwlCraft;
import services.headpat.owlcraft.commands.glyphs.arguments.GlyphArgumentType;
import services.headpat.spigotextensions.brigadier.BrigadierExecutor;

import java.util.Arrays;
import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static services.headpat.owlcraft.commands.glyphs.arguments.GlyphArgumentType.getGlyph;
import static services.headpat.spigotextensions.brigadier.arguments.PlayerArgumentType.getPlayer;
import static services.headpat.spigotextensions.brigadier.arguments.PlayerArgumentType.player;

public class GiveGlyphCommand extends BrigadierExecutor {
    public GiveGlyphCommand() {
        super(dispatcher -> dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal("giveglyph")
                .then(RequiredArgumentBuilder.<CommandSender, Player>argument("player", player())
                        .then(RequiredArgumentBuilder.<CommandSender, ShapelessRecipe>argument("glyph", GlyphArgumentType.glyph())
                                .executes(ctx -> {
                                    Player player = getPlayer(ctx, "player");
                                    ItemStack glyphItem = getGlyph(ctx, "glyph").getResult().clone();
                                    player.getInventory().addItem(glyphItem);
                                    return 1;
                                })
                                .then(RequiredArgumentBuilder.<CommandSender, Integer>argument("amount", IntegerArgumentType.integer(1, 64))
                                        .suggests((commandContext, builder) -> {
                                            List<String> nums = Arrays.asList("1", "64");
                                            nums.forEach(s ->
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
                                            return 1;
                                        })
                                )
                        )
                )
        ));
    }

    public static void registerCommand() {
        GiveGlyphCommand owlCraftServerCommand = new GiveGlyphCommand();
        OwlCraft.getInstance().getCommand("giveglyph").setExecutor(owlCraftServerCommand);
        OwlCraft.getInstance().getCommand("giveglyph").setTabCompleter(owlCraftServerCommand);
    }
}
