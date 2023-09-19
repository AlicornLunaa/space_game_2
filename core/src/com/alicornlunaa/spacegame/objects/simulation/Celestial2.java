package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Null;

/**
 * A celstial is one of the super massive objects in space, ex. star, planet, moon
 * Each celestial will have its own box2d world which entities will be added to
 * when they are in the sphere of influence, and removed when they leave
 */
public class Celestial2 extends BaseEntity {
    // Static functions
    private static ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Variables
    private TransformComponent transform = getComponent(TransformComponent.class);
    private BodyComponent bodyComponent;

    private @Null EllipticalConic conicRails;
    private float elapsedTime = 0.0f;

    private float radius = 100.f;

    // Constructors
    public Celestial2(PhysWorld world, float radius, float x, float y){
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
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

                shapeRenderer.setColor(Color.RED);
                shapeRenderer.circle(0, 0, getSphereOfInfluence(), 500);
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.circle(0, 0, getRadius(), 500);

                if(conicRails != null){
                    Constants.DEBUG = false;
                    conicRails.draw(shapeRenderer, 1);
                    Constants.DEBUG = true;
                }
                
                shapeRenderer.end();
                batch.begin();
            }
        });
        addComponent(new IScriptComponent() {
            @Override
            public void start() {}

            @Override
            public void render() {}

            @Override
            public void update() {
                if(conicRails == null) return;

                double anomaly = conicRails.timeToMeanAnomaly(elapsedTime);

                if(!Double.isNaN(anomaly)){
                    transform.position.set(conicRails.getPosition(anomaly).scl(bodyComponent.world.getPhysScale()));
                    transform.position.add(conicRails.getParent().getComponent(TransformComponent.class).position);
                    transform.velocity.scl(0);
                    // transform.velocity.set(conicRails.getVelocity(anomaly));
                    // transform.velocity.add(conicRails.getParent().getComponent(TransformComponent.class).velocity);
                    bodyComponent.sync(transform);
                }
            }
        });
    }

    public Celestial2(PhysWorld world, Celestial2 parent, float radius, float x, float y, float vx, float vy){
        // Create celestial that orbits around another
        this(world, radius, x, y);
        transform.velocity.set(vx, vy);
        bodyComponent.sync(transform);

        // Initialize rails
        conicRails = new EllipticalConic(parent, this);
    }

    // Functions
    public EllipticalConic getConic(){
        return conicRails;
    }

    public float getRadius(){
        return radius;
    }

    public float getSphereOfInfluence(){
        if(conicRails != null){
            return (float)((conicRails.getSemiMajorAxis() * bodyComponent.world.getPhysScale()) * Math.pow(bodyComponent.body.getMass() / conicRails.getParent().getComponent(BodyComponent.class).body.getMass(), 2.f/5.f));
        }

        return radius * 4;
    }
    
    public void update(float delta){
        elapsedTime += delta;
    }
}
