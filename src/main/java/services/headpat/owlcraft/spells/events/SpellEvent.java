package services.headpat.owlcraft.spells.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import services.headpat.owlcraft.spells.Spell;

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
