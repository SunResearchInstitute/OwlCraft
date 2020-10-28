package services.headpat.owlcraft.spells;

import lombok.Getter;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.Set;

public class SpellContext<T> {
	@Getter
	protected T context;

	@Getter
	protected Set<Entity> targets = new HashSet<>();

	protected Runnable cleanup;

	public SpellContext(T context, Runnable cleanup) {
		this.context = context;
		this.cleanup = cleanup;
	}

	public SpellContext(T context) {
		this(context, null);
	}

	public SpellContext(Runnable cleanup) {
		this(null, cleanup);
	}

	public void clean() {
		if (this.cleanup != null) {
			this.cleanup.run();
		}
	}
}
