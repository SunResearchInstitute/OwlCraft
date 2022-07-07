package services.headpat.owlcraft.spells.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import services.headpat.owlcraft.spells.Spell;

public class SpellCastEvent extends SpellEvent implements Cancellable {
    @Getter
    @Setter
    private boolean cancelled = false;
    @Getter()
    @Setter
    private int level;
    private final ItemStack glyphStack;

    public SpellCastEvent(Spell power, int level, ItemStack glyphStack, Player player) {
        super(power, player);
        this.level = level;
        this.glyphStack = glyphStack;
    }

    public ItemStack getGlyphStack() {
        if (glyphStack == null)
            return null;

        return glyphStack.clone();
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
