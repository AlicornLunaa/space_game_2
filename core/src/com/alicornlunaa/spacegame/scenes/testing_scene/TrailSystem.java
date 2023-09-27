package com.alicornlunaa.spacegame.scenes.testing_scene;

import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Null;

public class TrailSystem implements ISystem {
    // Variables
    private ShapeRenderer renderer = App.instance.shapeRenderer;
    private @Null IEntity referenceEntity = null;
    private @Null TransformComponent referenceTransform = null;
    private boolean refreshTrails = false;

    // Functions
    public void setReferenceEntity(@Null IEntity ref){
        referenceEntity = ref;
        referenceTransform = (ref == null) ? null : ref.getComponent(TransformComponent.class);
        refreshTrails = true;
    }

    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {}

    @Override
    public void beforeRender() {
        // Start render
        Vector2 referencePoint = (referenceEntity == null) ? Vector2.Zero.cpy() : referenceTransform.position.cpy();
        renderer.setProjectionMatrix(App.instance.camera.combined);
        renderer.setTransformMatrix(new Matrix4().translate(referencePoint.x, referencePoint.y, 0));
        renderer.setAutoShapeType(true);
        renderer.begin(ShapeType.Filled);
    }

    @Override
    public void afterRender() {
        renderer.end();

        refreshTrails = false;
    }

    @Override
    public void render(IEntity entity) {
        // Get variables
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        TrailComponent trailComponent = entity.getComponent(TrailComponent.class);
        GravityComponent gravityComponent = entity.getComponent(GravityComponent.class);

        Vector2 referencePoint = (referenceEntity == null) ? Vector2.Zero.cpy() : referenceTransform.position.cpy();

        // Refresh trails if needed
        if(refreshTrails)
            trailComponent.points.clear();

        // Init color
        renderer.setColor(trailComponent.color);

        // Render gravity well
        // TODO: Move this to gravity system
        if(gravityComponent != null){
            renderer.set(ShapeType.Line);
            renderer.circle(transform.position.x - referencePoint.x, transform.position.y - referencePoint.y, gravityComponent.getSphereOfInfluence());
            renderer.set(ShapeType.Filled);
        }

        // Draw the trail so far
        for(int i = 0; i < trailComponent.points.size - 1; i++){
            renderer.rectLine(trailComponent.points.get(i), trailComponent.points.get(i + 1), 5 * ((float)i / trailComponent.points.size));
        }

        // Create new trail points as it moves
        if(trailComponent.points.size < trailComponent.length){
            // Size isnt filled, add like normal
            trailComponent.points.add(transform.position.cpy().sub(referencePoint));
        } else {
            // Size is filled, add and then remove the first
            trailComponent.points.removeIndex(0);
            trailComponent.points.add(transform.position.cpy().sub(referencePoint));
        }
    }

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponents(TrailComponent.class);
    }
}
