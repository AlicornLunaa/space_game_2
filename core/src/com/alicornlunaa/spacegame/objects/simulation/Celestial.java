package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CircleColliderComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.components.GravityComponent;
import com.alicornlunaa.spacegame.components.TrackedEntityComponent;
import com.alicornlunaa.spacegame.components.CelestialComponent;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * A celstial is one of the super massive objects in space, ex. star, planet, moon
 * Each celestial will have its own box2d world which entities will be added to
 * when they are in the sphere of influence, and removed when they leave
 */
public class Celestial extends BaseEntity {
    public Celestial(PhysicsSystem phys, PhysWorld world, float radius, float x, float y){
        // Initialize self
        TransformComponent transform = getComponent(TransformComponent.class);
        transform.position.set(x, y);
        
        // Initialize body component
        BodyComponent bodyComponent = addComponent(new BodyComponent(world, new BodyDef()));
        bodyComponent.body.setType(BodyType.DynamicBody);
        addComponent(new CircleColliderComponent(bodyComponent, radius, 0, 0, 10.f));

        // Initialize components
        addComponent(new CelestialComponent(radius));
        addComponent(new GravityComponent(this));
        addComponent(new TrackedEntityComponent(Color.SKY)).predictFuture = false;
        addComponent(new CustomSpriteComponent() {
            private CelestialComponent celestialComponent = getComponent(CelestialComponent.class);
            private GravityComponent gravityComponent = getComponent(GravityComponent.class);

            @Override
            public void render(Batch batch) {
                if(!Constants.DEBUG) return;
                batch.end();
                App.instance.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                App.instance.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                App.instance.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

                if(gravityComponent != null){
                    App.instance.shapeRenderer.setColor(Color.RED);
                    App.instance.shapeRenderer.circle(0, 0, gravityComponent.getSphereOfInfluence(), 500);
                }

                App.instance.shapeRenderer.setColor(Color.YELLOW);
                App.instance.shapeRenderer.circle(0, 0, celestialComponent.getRadius(), 500);
                
                App.instance.shapeRenderer.end();
                batch.begin();
            }
        });
    }
}
