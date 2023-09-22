package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.ScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.components.RailsComponent;
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
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
    // Variables
    private TransformComponent transform = getComponent(TransformComponent.class);
    private BodyComponent bodyComponent;
    private RailsComponent railsComponent = addComponent(new RailsComponent());

    private float radius = 100.f;

    // Constructors
    public Celestial(PhysWorld world, float radius, float x, float y){
        // Initialize self
        this.radius = radius;
        transform.position.x = x;
        transform.position.y = y;
        
        // Initialize body
        CircleShape shape = new CircleShape();
        shape.setRadius(radius / world.getPhysScale());

        bodyComponent = addComponent(new BodyComponent(world, new BodyDef()));
        bodyComponent.body.setType(BodyType.DynamicBody);
        bodyComponent.body.createFixture(shape, 100.0f);
        bodyComponent.sync(transform);

        shape.dispose();

        // Initialize components
        addComponent(new CustomSpriteComponent() {
            @Override
            public void render(Batch batch) {
                if(!Constants.DEBUG) return;
                batch.end();
                App.instance.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                App.instance.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                App.instance.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

                App.instance.shapeRenderer.setColor(Color.RED);
                App.instance.shapeRenderer.circle(0, 0, getSphereOfInfluence(), 500);
                App.instance.shapeRenderer.setColor(Color.YELLOW);
                App.instance.shapeRenderer.circle(0, 0, getRadius(), 500);

                if(railsComponent.conic != null){
                    Constants.DEBUG = false;
                    railsComponent.conic.draw(App.instance.shapeRenderer, 1);
                    Constants.DEBUG = true;
                }
                
                App.instance.shapeRenderer.end();
                batch.begin();
            }
        });
        addComponent(new ScriptComponent(this) {
            @Override
            public void start() {}

            @Override
            public void render() {}

            @Override
            public void update() {
                if(railsComponent.conic == null) return;

                railsComponent.elapsedTime += Gdx.graphics.getDeltaTime();
                double anomaly = railsComponent.conic.timeToMeanAnomaly(railsComponent.elapsedTime);

                if(!Double.isNaN(anomaly)){
                    transform.position.set(railsComponent.conic.getPosition(anomaly).scl(bodyComponent.world.getPhysScale()));
                    transform.position.add(railsComponent.conic.getParent().getComponent(TransformComponent.class).position);

                    transform.velocity.set(railsComponent.conic.getVelocity(anomaly));
                    transform.velocity.add(railsComponent.conic.getParent().getComponent(TransformComponent.class).velocity);
                    
                    bodyComponent.sync(transform);
                }
            }
        });
    }

    public Celestial(PhysWorld world, Celestial parent, float radius, float x, float y, float vx, float vy){
        // Create celestial that orbits around another
        this(world, radius, x, y);
        transform.velocity.set(vx, vy);
        bodyComponent.sync(transform);

        // Initialize rails
        railsComponent.conic = new EllipticalConic(parent, this);
    }

    public Celestial(PhysWorld world, Celestial parent, float radius, float semiMajorAxis, float eccentricity, float periapsis, float trueAnomaly, float inclination){
        // Create celestial that orbits around another
        this(world, radius, parent.getComponent(TransformComponent.class).position.x + semiMajorAxis, parent.getComponent(TransformComponent.class).position.y);
        railsComponent.conic = new EllipticalConic(parent, this, semiMajorAxis, eccentricity, periapsis, trueAnomaly, inclination);
    }

    // Functions
    public float getRadius(){
        return radius;
    }

    public float getSphereOfInfluence(){
        if(railsComponent.conic != null){
            return (float)((railsComponent.conic.getSemiMajorAxis() * bodyComponent.world.getPhysScale()) * Math.pow(bodyComponent.body.getMass() / railsComponent.conic.getParent().getComponent(BodyComponent.class).body.getMass(), 2.f/5.f));
        }

        return radius * 4000;
    }
}
