package com.alicornlunaa.selene_engine.core;

import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.util.Assets;
import com.alicornlunaa.selene_engine.util.Assets.Reloadable;
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
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public abstract class BaseEntity implements IEntity, Disposable, Reloadable {

    // Variables
    private Array<IComponent> components = new Array<>();

    private TransformComponent transform = new TransformComponent();

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
            transform.position.set(body.getPosition().cpy().scl(physScale));
            transform.rotation = body.getAngle();
        }

        Matrix3 matrix = new Matrix3();
        matrix.idt();
        matrix.translate(transform.position);
        matrix.rotateRad(transform.rotation);

        return matrix;
    }

    public Vector2 getPosition(){
        if(body != null){
            transform.position.set(body.getPosition().cpy().scl(physScale));
            transform.rotation = body.getAngle();
        }

        return transform.position;
    }

    public Vector2 getCenter(){
        if(body != null){
            return body.getWorldCenter().cpy().scl(physScale);
        }

        return getPosition();
    }

    public float getRotation(){
        if(body != null){
            transform.position.set(body.getPosition().cpy().scl(physScale));
            transform.rotation = body.getAngle();
        }

        return transform.rotation;
    }

    public Vector2 getVelocity(){
        if(body == null) return null;
        return transform.velocity.set(body.getLinearVelocity());
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
            transform.position.set(x, y);
        }
    }

    public void setPosition(Vector2 p){ setPosition(p.x, p.y); }

    public void setRotation(float rads){
        if(body != null){
            body.setTransform(getPosition().cpy().scl(1 / physScale), rads);
        } else {
            transform.rotation = rads;
        }
    }

    public void setVelocity(float x, float y){
        if(body == null) return;
        body.setLinearVelocity(x, y);
    }

    public void setVelocity(Vector2 vel){ setVelocity(vel.x, vel.y); }


    // Component system
    @Override
    public IComponent addComponent(IComponent component){
        components.add(component);
        return component;
    }

    @Override
    public boolean hasComponent(Class<? extends IComponent> componentType) {
        return getComponent(componentType) != null;
    }

    @Override
    public boolean hasComponents(Class<?>... componentTypes) {
        boolean[] hasType = new boolean[componentTypes.length];

        for(IComponent component : components){
            for(int i = 0; i < componentTypes.length; i++){
                if(hasType[i] == false && componentTypes[i].isAssignableFrom(component.getClass())){
                    hasType[i] = true;
                }
            }
        }

        for(boolean b : hasType){
            if(!b){
                return false;
            }
        }
        
        return true;
    }

    @Override
    public IComponent getComponent(Class<? extends IComponent> componentType) {
        for(IComponent component : components){
            if(componentType.isAssignableFrom(component.getClass())){
                return component;
            }
        }

        return null;
    }

    @Override
    public IComponent[] getComponents(Class<? extends IComponent> componentType) {
        Array<IComponent> out = new Array<>(IComponent.class);

        for(IComponent component : components){
            if(componentType.isAssignableFrom(component.getClass())){
                out.add(component);
            }
        }

        return out.toArray();
    }

    @Override
    public void dispose(){
        for(IComponent component : components){
            if(component instanceof Disposable){
                Disposable disposableComponent = (Disposable)component;
                disposableComponent.dispose();
            }
        }
    }

    @Override
    public void reload(Assets assets){
        for(IComponent component : components){
            if(component instanceof Reloadable){
                Reloadable disposableComponent = (Reloadable)component;
                disposableComponent.reload(assets);
            }
        }
    }

    // Functions
    public void afterWorldChange(PhysWorld world){}
    
    // Depreciated functions for backwards compat
    public float getX(){ return getPosition().x; }
    public float getY(){ return getPosition().y; }
    public void setX(float x){ setPosition(x, getPosition().y); }
    public void setY(float y){ setPosition(getPosition().x, y); }

}
