package com.alicornlunaa.selene_engine.systems;

import com.alicornlunaa.selene_engine.components.ShapeDrawableComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;

public class ShapeRenderSystem implements ISystem {
	// Variables
	private ShapeRenderer renderer = App.instance.shapeRenderer;

	// Functions
	@Override
	public void beforeUpdate() {}

	@Override
	public void afterUpdate() {}

	@Override
	public void update(IEntity entity) {}

	@Override
	public void beforeRender() {
		renderer.setProjectionMatrix(App.instance.camera.combined);
		renderer.setTransformMatrix(new Matrix4());
		renderer.begin(ShapeType.Filled);
	}

	@Override
	public void afterRender() {
		renderer.end();
	}

	@Override
	public void render(IEntity entity) {
		ShapeDrawableComponent drawable = entity.getComponent(ShapeDrawableComponent.class);
		drawable.draw(renderer);
	}

	@Override
	public boolean shouldRunOnEntity(IEntity entity) {
		return entity.hasComponents(ShapeDrawableComponent.class);
	}
}
