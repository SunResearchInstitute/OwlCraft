package services.headpat.owlcraft.spells.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import services.headpat.owlcraft.spells.Spell;

public class SpellTargetingEvent extends SpellEvent implements Cancellable {
    @Getter
    @Setter
    private boolean cancelled = false;

    @Getter
    protected final Entity target;

    public SpellTargetingEvent(Spell spell, Entity what, Entity target) {
        super(spell, what);
        this.target = target;
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
