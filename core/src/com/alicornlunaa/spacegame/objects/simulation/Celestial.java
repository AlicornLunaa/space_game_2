package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.components.CelestialComponent;
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
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
        transform.position.x = x;
        transform.position.y = y;
        
        // Initialize body
        CircleShape shape = new CircleShape();
        shape.setRadius(radius / world.getPhysScale());

        BodyComponent bodyComponent = addComponent(new BodyComponent(world, new BodyDef()));
        bodyComponent.body.setType(BodyType.DynamicBody);
        bodyComponent.body.createFixture(shape, 10.0f);
        bodyComponent.sync(transform);

        shape.dispose();

        // Initialize components
        addComponent(new CelestialComponent(this, phys, radius));
        addComponent(new CustomSpriteComponent() {
            private CelestialComponent celestialComponent = getComponent(CelestialComponent.class);

            @Override
            public void render(Batch batch) {
                if(!Constants.DEBUG) return;
                batch.end();
                App.instance.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                App.instance.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                App.instance.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

                App.instance.shapeRenderer.setColor(Color.RED);
                App.instance.shapeRenderer.circle(0, 0, celestialComponent.getSphereOfInfluence(), 500);
                App.instance.shapeRenderer.setColor(Color.YELLOW);
                App.instance.shapeRenderer.circle(0, 0, celestialComponent.getRadius(), 500);

                if(celestialComponent.conic != null){
                    Constants.DEBUG = false;
                    celestialComponent.conic.draw(App.instance.shapeRenderer, 1);
                    Constants.DEBUG = true;
                }
                
                App.instance.shapeRenderer.end();
                batch.begin();
            }
        });
    }

    public Celestial(PhysicsSystem phys, PhysWorld world, Celestial parent, float radius, float x, float y, float vx, float vy){
        // Create celestial that orbits around another
        this(phys, world, radius, x, y);
        
        TransformComponent transform = getComponent(TransformComponent.class);
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        transform.velocity.set(vx, vy);
        bodyComponent.sync(transform);

        // Initialize rails
        CelestialComponent celestialComponent = getComponent(CelestialComponent.class);
        celestialComponent.conic = new EllipticalConic(parent, this);
    }

    public Celestial(PhysicsSystem phys, PhysWorld world, Celestial parent, float radius, float semiMajorAxis, float eccentricity, float periapsis, float trueAnomaly, float inclination){
        // Create celestial that orbits around another
        this(
            phys,
            world,
            radius,
            parent.getComponent(TransformComponent.class).position.x + semiMajorAxis,
            parent.getComponent(TransformComponent.class).position.y
        );
        
        // Initialize rails
        CelestialComponent celestialComponent = getComponent(CelestialComponent.class);
        celestialComponent.conic = new EllipticalConic(parent, this, semiMajorAxis, eccentricity, periapsis, trueAnomaly, inclination);
    }
}
