package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Null;

public class TrailSystem implements ISystem {
    // Variables
    private ShapeRenderer renderer = App.instance.shapeRenderer;
    private @Null IEntity relativeEntity = null;

    // Functions
    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {}

    @Override
    public void beforeRender() {
        // Start render
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
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        TrailComponent trailComponent = entity.getComponent(TrailComponent.class);

        renderer.setColor(trailComponent.color);

        for(int i = 0; i < trailComponent.points.size - 1; i++){
            renderer.rectLine(trailComponent.points.get(i), trailComponent.points.get(i + 1), 5 * ((float)i / trailComponent.points.size));
        }

        if(trailComponent.points.size < trailComponent.length){
            // Size isnt filled, add like normal
            trailComponent.points.add(transform.position.cpy());
        } else {
            // Size is filled, add and then remove the first
            trailComponent.points.removeIndex(0);
            trailComponent.points.add(transform.position.cpy());
        }
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponents(TrailComponent.class);
    }
}
