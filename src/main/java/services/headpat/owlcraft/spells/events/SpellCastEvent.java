package services.headpat.owlcraft.spells.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import services.headpat.owlcraft.spells.Spell;

public class SpellCastEvent extends SpellEvent implements Cancellable {
	@Getter
	@Setter
	private boolean cancelled = false;
	@Getter
	@Setter
	private int level;

	public SpellCastEvent(Spell power, int level, Entity what) {
		super(power, what);
		this.level = level;
	}

	/* Generated for Bukkit */
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return (handlers);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return (handlers);
	}
}
