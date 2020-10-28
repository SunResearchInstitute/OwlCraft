package services.headpat.owlcraft.spells.events;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityEvent;
import services.headpat.owlcraft.spells.Spell;

public abstract class SpellEvent extends EntityEvent {
	@Getter
	protected final Spell spell;

	public SpellEvent(Spell spell, Entity ent) {
		super(ent);
		this.spell = spell;
	}
}
