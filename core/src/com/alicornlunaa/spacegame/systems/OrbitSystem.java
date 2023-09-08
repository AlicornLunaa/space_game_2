package com.alicornlunaa.spacegame.systems;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.OrbitComponent;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class OrbitSystem implements ISystem {

    // Variables
	private App game;
    private ShapeRenderer shapeRenderer;
    public boolean visible = false;

	// Constructor
	public OrbitSystem(App game){
		this.game = game;
		shapeRenderer = new ShapeRenderer();
	}

    // Functions
    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
        OrbitComponent orbitComponent = entity.getComponent(OrbitComponent.class);
        orbitComponent.recalculate();
    }

    @Override
    public void beforeRender() {
        shapeRenderer.setProjectionMatrix(game.gameScene.activeCamera.combined);
        shapeRenderer.begin(ShapeType.Filled);
    }

    @Override
    public void afterRender() {
        shapeRenderer.end();
    }

    @Override
    public void render(IEntity entity) {
        if(!visible) return;
        OrbitComponent orbitComponent = entity.getComponent(OrbitComponent.class);
        orbitComponent.draw(shapeRenderer, 1.5f * game.gameScene.activeCamera.zoom);
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponents(OrbitComponent.class);
    }
    
}
