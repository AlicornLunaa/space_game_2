package com.alicornlunaa.spacegame.engine.core;

import com.alicornlunaa.spacegame.phys.PhysWorld;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public abstract class Entity extends Actor implements IEntity {

    // Variables
    private @Null PhysWorld currentWorld = null;
    private @Null Body body = null;
    private float physScale = Constants.PPM;

    // Overrides
    public void afterWorldChange(PhysWorld world){}

    // Private functions
    private void updateTransform(){
        // Update the transform of the object
        if(body != null){
            // A physics body exists, move the entity
            setPosition(body.getPosition().cpy().scl(physScale));
            setRotation((float)Math.toDegrees(body.getAngle()));
        }
    }

    // Getters
    public PhysWorld currentWorld(){ return currentWorld; }
    public Body getBody(){ return body; }
    public float getPhysScale(){ return physScale; }

    public Matrix3 getTransform(){
        updateTransform();

        Matrix3 trans = new Matrix3();
        trans.idt();
        trans.translate(getX(), getY());
        trans.rotate(getRotation());
        trans.scale(getScaleX(), getScaleY());
        trans.translate(-getOriginX(), -getOriginY());

        return trans;
    }

    // Setters
    public Body setBody(Body b){
        body = b;
        updateTransform();
        return b;
    }

    public void setPosition(Vector2 v){ setPosition(v.x, v.y); }

    // Functions
    public void loadToWorld(PhysWorld world){
        // You can only load an entity to a world if it has a body
        if(body == null) return;
        if(world == null) return;

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

        currentWorld = world;
        afterWorldChange(world);
    }

    @Override
    public Vector2 getPosition() { return new Vector2(body.getPosition().x * physScale, body.getPosition().y * physScale); }

    @Override
    public Vector2 getGlobalPosition() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGlobalPosition'");
    }

    // Actor overrides
    @Override
    public void setX(float x){
        if(body != null){
            body.setTransform(x / physScale, body.getPosition().y, body.getAngle());
        }

        super.setX(x);
    }

    @Override
    public void setY(float y){
        if(body != null){
            body.setTransform(body.getPosition().x, y / physScale, body.getAngle());
        }

        super.setY(y);
    }

    @Override
    public void setRotation(float degrees){
        if(body != null){
            body.setTransform(body.getPosition(), (float)Math.toRadians(degrees));
        }

        super.setRotation(degrees);
    }

    @Override
    public void setPosition(float x, float y){
        if(body != null){
            body.setTransform(x / physScale, y / physScale, body.getAngle());
        }

        super.setPosition(x, y);
    }
    
}
