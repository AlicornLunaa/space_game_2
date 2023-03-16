package com.alicornlunaa.spacegame.objects;

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
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public class Entity extends Actor implements Disposable {

    // Variables
    protected @Null Body body = null;
    protected @Null Entity driver = null;
    protected @Null Entity drivingEnt = null;
    protected float physScale = Constants.PPM;

    // Functions
    public void update(float delta){} // Update ran every frame
    public void fixedUpdate(float timestep){} // Update ran every physics tick

    public Body setBody(Body b){
        if(b != null){
            setPosition(b.getPosition().cpy().scl(physScale));
            setRotation((float)Math.toDegrees(b.getAngle()));
        }

        body = b;
        return b;
    }

    public Body getBody(){
        return body;
    }

    public void drive(Entity e){
        drivingEnt = e;
        e.driver = this;

        if(body != null)
            body.setActive(false);
    }

    public void stopDriving(){
        setPosition(drivingEnt.getPosition());
        drivingEnt.driver = null;
        drivingEnt = null;

        if(body != null)
            body.setActive(true);
    }

    public Entity getDriving(){ return drivingEnt; }
    public Entity getDriver(){ return driver; }

    /**
     * Loads a new body to the entity for this world
     * @param e The entity to be loaded
     */
    public void loadBodyToWorld(PhysWorld world, float newPhysScale){
        if(driver != null) driver.loadBodyToWorld(world, newPhysScale);
        if(body == null) return;
        
        // Save all the fixture data
        Array<FixtureDef> fixtures = new Array<>(body.getFixtureList().size);
        Array<Shape> shapes = new Array<>(body.getFixtureList().size);

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

                    vertices[i].scl(getPhysScale());
                    vertices[i].scl(1 / newPhysScale);
                }
                
                copy.set(vertices);
                shapes.add(copy);
                fixtureDef.shape = copy;
            } else if(f.getShape().getType() == Shape.Type.Circle) {
                CircleShape shape = (CircleShape)f.getShape();
                CircleShape copy = new CircleShape();

                copy.setPosition(shape.getPosition().scl(getPhysScale()).scl(1 / newPhysScale));
                copy.setRadius(shape.getRadius() * getPhysScale() / newPhysScale);
                
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
        def.linearVelocity.set(body.getLinearVelocity().cpy().scl(getPhysScale()).scl(1 / newPhysScale));
        def.position.set(body.getPosition().cpy().scl(getPhysScale()).scl(1 / newPhysScale));
        def.type = body.getType();

        setPhysScale(newPhysScale);

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

        afterWorldChange();
    }
    
    protected void afterWorldChange(){}

    public void setPhysScale(float s){ physScale = s; }
    public float getPhysScale(){ return physScale; }
    
    /**
     * @return Transformation matrix to the center of the actor
     */
    public Matrix3 getTransform(){
        if(body != null){
            // Get up-to-date body information
            Vector2 p = body.getPosition();
            setPosition(p.x * physScale, p.y * physScale);
            setRotation((float)Math.toDegrees(body.getAngle()));
        }

        // Get matrix
        Matrix3 trans = new Matrix3();
        trans.idt();
        trans.translate(getX(), getY());
        trans.rotate(getRotation());
        trans.scale(getScaleX(), getScaleY());
        trans.translate(-getOriginX(), -getOriginY());
        return trans;
    }

    /**
     * @return A new vector for this position
     */
    public Vector2 getPosition(){ return new Vector2(getX(), getY()); }
    public void setPosition(Vector2 v){ setPosition(v.x, v.y); }

    public Vector2 localToWorld(Vector2 v){ return v.mul(getTransform()); }
    public Vector2 worldToLocal(Vector2 v){ return v.mul(getTransform().inv()); }

    // Overrides
    @Override
    public float getX(){
        if(drivingEnt != null) return drivingEnt.getX();
        if(body == null) return super.getX();

        float newX = body.getPosition().x * physScale;
        super.setX(newX);
        return newX;
    }

    @Override
    public float getY(){
        if(drivingEnt != null) return drivingEnt.getY();
        if(body == null) return super.getY();

        float newY = body.getPosition().y * physScale;
        super.setY(newY);
        return newY;
    }

    @Override
    public float getRotation(){
        if(drivingEnt != null) return drivingEnt.getRotation();
        if(body == null) return super.getRotation();

        float degrees = (float)Math.toDegrees(body.getAngle());
        super.setRotation(degrees);
        return degrees;
    }
    
    @Override
    public void setX(float x){
        if(drivingEnt != null) return;
        if(body != null) body.setTransform(new Vector2(x / physScale, body.getPosition().y), body.getAngle());
        super.setX(x);
    }
    
    @Override
    public void setY(float y){
        if(drivingEnt != null) return;
        if(body != null) body.setTransform(new Vector2(body.getPosition().x, y / physScale), body.getAngle());
        super.setY(y);
    }

    @Override
    public void setRotation(float degrees){
        if(drivingEnt != null) return;
        if(body != null) body.setTransform(body.getPosition(), (float)Math.toRadians(degrees));
        super.setRotation(degrees);
    }

    @Override
    public void setPosition(float x, float y){
        if(drivingEnt != null) return;
        if(body != null) body.setTransform(x / physScale, y / physScale, body.getAngle());
        super.setPosition(x, y);
    }

    @Override
    public void dispose() {}

}
