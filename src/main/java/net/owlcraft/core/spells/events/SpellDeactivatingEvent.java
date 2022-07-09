package net.owlcraft.core.spells.events;

import lombok.Getter;
import lombok.Setter;
import net.owlcraft.core.spells.Spell;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class SpellDeactivatingEvent extends SpellEvent implements Cancellable {
    @Getter
    @Setter
    private boolean cancelled = false;
    @Getter
    private final boolean cleaning;

    public SpellDeactivatingEvent(Spell spell, Player player, boolean cleaning) {
        super(spell, player);
        this.cleaning = cleaning;
    }

    /* Generated for Bukkit */
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return (handlers);
    }

    @Override
    public HandlerList getHandlers() {
        return (handlers);
    }
}
