package net.owlcraft.commands.glyphs.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.owlcraft.OwlCraft;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GlyphArgumentType implements ArgumentType<ShapelessRecipe> {
    public static GlyphArgumentType glyph() {
        return new GlyphArgumentType();
    }

    public static ShapelessRecipe getGlyph(@NotNull CommandContext<?> commandContext, String name) {
        return commandContext.getArgument(name, ShapelessRecipe.class);
    }

    @Override
    public ShapelessRecipe parse(StringReader stringReader) throws CommandSyntaxException {
        String str = stringReader.readUnquotedString();
        ShapelessRecipe glyph;
        if (str.equals("random")) {
            Collection<ShapelessRecipe> glyphs = OwlCraft.getInstance().getSpellManager().getGlyphs();
            int index = (int) (Math.random() * glyphs.size());
            glyph = glyphs.stream().toList().get(index);
        } else {
            glyph = OwlCraft.getInstance().getSpellManager().getGlyph(str);
        }

        if (glyph == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Glyph not found.");
        }
        return glyph;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> glyphs = new ArrayList<>(OwlCraft.getInstance().getSpellManager().getGlyphs().stream().map(shapelessRecipe -> shapelessRecipe.getKey().getKey()).toList());
        glyphs.add("random");

        glyphs.forEach(s -> {
            if (s.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                builder.suggest(s);
        });
        return builder.buildFuture();
    }
}

