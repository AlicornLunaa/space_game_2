package com.alicornlunaa.spacegame.engine.core;

import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public abstract class BaseEntity implements IEntity {

    // Variables
    private Matrix3 transform = new Matrix3();
    private Vector2 position = new Vector2();
    private Vector2 velocity = new Vector2();

    private @Null PhysWorld world = null;
    private @Null Body body = null;
    private float physScale = Constants.PPM;
    
    // Constructor
    public BaseEntity(){}

    // Getters
    public Body getBody(){ return body; }

    public PhysWorld getWorld(){ return world; }
    
    public float getPhysScale(){ return physScale; }

    public Matrix3 getTransform(){
        if(body != null){
            transform.idt();
            // TODO: TEMP
            transform.translate(body.getPosition().cpy().scl(physScale));
            transform.rotateRad(body.getAngle());
        }

        return transform;
    }

    public Vector2 getPosition(){
        return getTransform().getTranslation(position);
    }

    public Vector2 getCenter(){
        if(body != null){
            return body.getWorldCenter().cpy().scl(physScale);
        }

        return getPosition();
    }

    public float getRotation(){
        return getTransform().getRotationRad();
    }

    public Vector2 getVelocity(){
        if(body == null) return null;
        return velocity.set(body.getLinearVelocity());
    }

    // Setters
    public Body setBody(Body b){
        // TODO: TEMP physscale
        if(b != null){
            setPosition(b.getPosition().cpy().scl(physScale));
            setRotation(b.getAngle());
        }

        body = b;
        return b;
    }

    public void setWorld(PhysWorld world){
        // You can only load an entity to a world if it has a body
        if(body == null) return;
        if(world == null){ this.world = world; return; }

        // Save all data and prep to load to the new world
        Array<FixtureDef> fixtures = new Array<>(body.getFixtureList().size);
        Array<Shape> shapes = new Array<>(body.getFixtureList().size);
        float newPhysScale = world.getPhysScale();

        for(Fixture f : body.getFixtureList()){
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.density = f.getDensity();
            fixtureDef.filter.set(f.getFilterData());
            fixtureDef.friction = f.getFriction();
            fixtureDef.isSensor = f.isSensor();
            fixtureDef.restitution = f.getRestitution();

            // Recreate the shape
            if(f.getShape().getType() == Shape.Type.Polygon){
                PolygonShape shape = (PolygonShape)f.getShape();
                PolygonShape copy = new PolygonShape();
                Vector2[] vertices = new Vector2[shape.getVertexCount()];

                for(int i = 0; i < shape.getVertexCount(); i++){
                    vertices[i] = new Vector2();
                    shape.getVertex(i, vertices[i]);

                    vertices[i].scl(physScale);
                    vertices[i].scl(1 / newPhysScale);
                }
                
                copy.set(vertices);
                shapes.add(copy);
                fixtureDef.shape = copy;
            } else if(f.getShape().getType() == Shape.Type.Circle) {
                CircleShape shape = (CircleShape)f.getShape();
                CircleShape copy = new CircleShape();

                copy.setPosition(shape.getPosition().scl(physScale).scl(1 / newPhysScale));
                copy.setRadius(shape.getRadius() * physScale / newPhysScale);
                
                shapes.add(copy);
                fixtureDef.shape = copy;
            }

            fixtures.add(fixtureDef);
        }

        // Save body data
        BodyDef def = new BodyDef();
        def.active = body.isActive();
        def.allowSleep = body.isSleepingAllowed();
        def.angle = body.getAngle();
        def.angularDamping = body.getAngularDamping();
        def.angularVelocity = body.getAngularVelocity();
        def.awake = body.isAwake();
        def.bullet = body.isBullet();
        def.fixedRotation = body.isFixedRotation();
        def.gravityScale = body.getGravityScale();
        def.linearDamping = body.getLinearDamping();
        def.linearVelocity.set(body.getLinearVelocity().cpy().scl(physScale).scl(1 / newPhysScale));
        def.position.set(body.getPosition().cpy().scl(physScale).scl(1 / newPhysScale));
        def.type = body.getType();

        physScale = newPhysScale;

        // Delete body from world and recreate it in the new one
        body.getWorld().destroyBody(body);
        setBody(world.getBox2DWorld().createBody(def));

        // Recreate the fixtures
        for(FixtureDef fixtureDef : fixtures){
            body.createFixture(fixtureDef);
        }

        // Remove shapes
        for(Shape s : shapes){
            s.dispose();
        }

        this.world = world;
        afterWorldChange(world);
    }

    public void setPosition(float x, float y){
        if(body != null){
            // TODO: TEMP Problem with bug here
            body.setTransform(x / physScale, y / physScale, getRotation());
        } else {
            getPosition();
            transform.translate(position.x * -1, position.y * -1);
            transform.translate(x, y);
        }
    }

    public void setPosition(Vector2 p){ setPosition(p.x, p.y); }

    public void setRotation(float rads){
        if(body != null){
            body.setTransform(getPosition().cpy().scl(1 / physScale), rads);
        } else {
            float oldRads = getRotation();
            transform.rotateRad(oldRads);
            transform.rotateRad(rads);
        }
    }

    public void setVelocity(float x, float y){
        if(body == null) return;
        body.setLinearVelocity(x, y);
    }

    public void setVelocity(Vector2 vel){ setVelocity(vel.x, vel.y); }

    // Functions
    public void afterWorldChange(PhysWorld world){}
    
    // Depreciated functions for backwards compat
    public float getX(){ return getPosition().x; }
    public float getY(){ return getPosition().y; }
    public void setX(float x){ setPosition(x, getPosition().y); }
    public void setY(float y){ setPosition(getPosition().x, y); }

}
