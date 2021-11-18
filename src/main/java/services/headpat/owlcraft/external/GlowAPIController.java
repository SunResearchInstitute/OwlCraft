package services.headpat.owlcraft.external;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.inventivetalent.glow.GlowAPI;
import services.headpat.owlcraft.OwlCraft;

import java.util.*;

public class GlowAPIController {
	private final Map<Entity, GlowData> activeEntities = new HashMap<>();

	public GlowAPIController() {
		Bukkit.getScheduler().runTaskTimer(OwlCraft.getInstance(), this::run, 0, 16);
	}

	private void run() {
		if (this.activeEntities.isEmpty()) {
			return;
		}

		this.activeEntities.forEach((entity, glowData) -> glowData.viewers.forEach((viewer, context) -> {
			GlowAPI.setGlowing(entity, context.next(), viewer);
		}));
	}

	public ColorData setGlowing(Entity entity, GlowAPI.Color color, Player viewer) {
		HashSet<Player> viewers = new HashSet<>();
		viewers.add(viewer);
		HashSet<GlowAPI.Color> pattern = new HashSet<>();
		pattern.add(color);
		return (this.setGlowing(entity, pattern, viewers));
	}

	public ColorData setGlowing(Entity entity, Collection<GlowAPI.Color> pattern, Player viewer) {
		HashSet<Player> viewers = new HashSet<>();
		viewers.add(viewer);
		return (this.setGlowing(entity, pattern, viewers));
	}

	public ColorData setGlowing(Entity entity, GlowAPI.Color color, Collection<? extends Player> viewers) {
		HashSet<GlowAPI.Color> pattern = new HashSet<>();
		pattern.add(color);
		return (this.setGlowing(entity, pattern, viewers));
	}

	public ColorData setGlowing(Entity entity, Collection<GlowAPI.Color> pattern, Collection<? extends Player> viewers) {
		GlowData glowdata = this.activeEntities.get(entity);
		if (glowdata == null) {
			glowdata = new GlowData(entity);
			this.activeEntities.put(entity, glowdata);
		}
		ColorData handle = new ColorData(entity, pattern, viewers);
		glowdata.add(handle);
		return (handle);
	}

	public void unsetGlowing(ColorData handle) {
		if (!(handle.valid)) {
			return;
		}
		GlowData glowdata = this.activeEntities.get(handle.entity);
		if (glowdata == null) {
			return;
		}
		glowdata.remove(handle);
		if (glowdata.patterns.isEmpty()) {
			this.activeEntities.remove(handle.entity);
		}
		handle.valid = false;
	}

	private static class ViewerContext {
		final Entity entity;
		final Player viewer;
		final Set<ColorData> patterns = new LinkedHashSet<>();
		Iterator<ColorData> currentPattern = null;
		Iterator<GlowAPI.Color> currentColor = null;

		ViewerContext(Entity entity, Player viewer) {
			this.entity = entity;
			this.viewer = viewer;
		}

		void add(ColorData pattern) {
			this.currentPattern = null;
			this.currentColor = null;
			this.patterns.add(pattern);
			GlowAPI.setGlowing(this.entity, this.next(), this.viewer);
		}

		void remove(ColorData pattern) {
			this.currentPattern = null;
			this.currentColor = null;
			this.patterns.remove(pattern);
			if (this.patterns.isEmpty()) {
				GlowAPI.setGlowing(this.entity, false, this.viewer);
			} else {
				GlowAPI.setGlowing(this.entity, this.next(), this.viewer);
			}
		}

		GlowAPI.Color next() {
			if ((this.currentColor == null) || !(this.currentColor.hasNext())) {
				if ((this.currentPattern == null) || !(this.currentPattern.hasNext())) {
					this.currentPattern = this.patterns.iterator();
				}
				this.currentColor = this.currentPattern.next().pattern.iterator();
			}
			return (this.currentColor.next());
		}
	}

	private static class GlowData {
		final Entity entity;
		final Set<ColorData> patterns = new LinkedHashSet<>();
		final Map<Player, ViewerContext> viewers = new HashMap<>();

		GlowData(Entity entity) {
			this.entity = entity;
		}

		void add(ColorData pattern) {
			this.patterns.add(pattern);
			for (Player viewer : pattern.viewers) {
				ViewerContext context = this.viewers.get(viewer);
				if (context == null) {
					context = new ViewerContext(this.entity, viewer);
					this.viewers.put(viewer, context);
				}
				context.add(pattern);
			}
		}

		void remove(ColorData pattern) {
			this.patterns.remove(pattern);
			for (Player viewer : pattern.viewers) {
				ViewerContext context = this.viewers.get(viewer);
				if (context == null) {
					continue;
				}
				context.remove(pattern);
				if (context.patterns.isEmpty()) {
					this.viewers.remove(viewer);
				}
			}
		}
	}

	public class ColorData {
		private final Entity entity;
		private final Collection<GlowAPI.Color> pattern;
		private final Collection<? extends Player> viewers;
		private boolean valid = true;

		private ColorData(Entity entity, Collection<GlowAPI.Color> pattern, Collection<? extends Player> viewers) {
			this.entity = entity;
			this.pattern = pattern;
			this.viewers = viewers;
		}

		public void unsetGlowing() {
			GlowAPIController.this.unsetGlowing(this);
		}
	}
}

