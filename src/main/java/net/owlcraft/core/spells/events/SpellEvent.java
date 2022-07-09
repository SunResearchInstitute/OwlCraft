package net.owlcraft.core.spells.events;

import lombok.Getter;
import net.owlcraft.core.spells.Spell;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public abstract class SpellEvent extends PlayerEvent {
    @Getter
    protected final Spell spell;

    public SpellEvent(Spell spell, Player player) {
        super(player);
        this.spell = spell;
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
