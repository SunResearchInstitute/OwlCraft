package services.headpat.owlcraft.external.brigadier.types;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import services.headpat.owlcraft.utils.Utils;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BrigadierExecutor implements TabExecutor {
	protected CommandDispatcher<CommandSender> commandDispatcher;

	public BrigadierExecutor(LiteralArgumentBuilder<CommandSender> argumentBuilder) {
		commandDispatcher = new CommandDispatcher<>();
		commandDispatcher.register(argumentBuilder);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		try {
			this.commandDispatcher.execute(Utils.getBrigadierString(command, args), sender);
		} catch (CommandSyntaxException e) {
			Utils.sendUsageMessage(sender, this.commandDispatcher.getAllUsage(this.commandDispatcher.getRoot(), sender, true));
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		String commandArgs = String.join(" ", Utils.addBeginningString(command.getName(), args));
		Suggestions suggestions = commandDispatcher.getCompletionSuggestions(commandDispatcher.parse(commandArgs, sender)).join();
		return suggestions.getList().stream().map(Suggestion::getText).collect(Collectors.toList());
	}
}
