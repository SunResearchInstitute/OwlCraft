package services.headpat.owlcraft.external.brigadier.types.ArgumentTypes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerArgumentType implements ArgumentType<Player> {
	@Contract(value = " -> new", pure = true)
	public static @NotNull PlayerArgumentType player() {
		return new PlayerArgumentType();
	}

	@Override
	public Player parse(@NotNull StringReader reader) throws CommandSyntaxException {
		return Bukkit.getPlayer(reader.readString());
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
		Bukkit.getOnlinePlayers().forEach(player -> {
			if (player.getName().toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
				builder.suggest(player.getName());
		});
		return builder.buildFuture();
	}

	@Override
	public Collection<String> getExamples() {
		return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
	}
}
