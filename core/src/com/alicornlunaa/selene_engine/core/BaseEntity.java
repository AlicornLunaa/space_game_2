package com.alicornlunaa.selene_engine.core;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.util.Assets;
import com.alicornlunaa.selene_engine.util.Assets.Reloadable;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Null;

public abstract class BaseEntity implements IEntity, Disposable, Reloadable {

    // Variables
    private Array<IComponent> components = new Array<>();
    private TransformComponent transform;
    protected @Null BodyComponent bodyComponent;

    // Constructors
    public BaseEntity(){
        transform = addComponent(new TransformComponent());
    }

    // Getters
    public Body getBody(){
        if(bodyComponent == null) return null;
        return bodyComponent.body;
    }

    public PhysWorld getWorld(){
        if(bodyComponent == null) return null;
        return bodyComponent.world;
    }
    
    public float getPhysScale(){
        if(bodyComponent == null) return Constants.PPM;
        return bodyComponent.world.getPhysScale();
    }

    public Matrix3 getTransform(){
        if(bodyComponent != null){
            transform.position.set(bodyComponent.body.getPosition().cpy().scl(getPhysScale()));
            transform.rotation = bodyComponent.body.getAngle();
        }

        Matrix3 matrix = new Matrix3();
        matrix.idt();
        matrix.translate(transform.position);
        matrix.rotateRad(transform.rotation);

        return matrix;
    }

    public Vector2 getPosition(){
        if(bodyComponent != null){
            transform.position.set(bodyComponent.body.getPosition().cpy().scl(getPhysScale()));
            transform.rotation = bodyComponent.body.getAngle();
        }

        return transform.position;
    }

    public Vector2 getCenter(){
        if(bodyComponent != null){
            return bodyComponent.body.getWorldCenter().cpy().scl(getPhysScale());
        }

        return getPosition();
    }

    public float getRotation(){
        if(bodyComponent != null){
            transform.position.set(bodyComponent.body.getPosition().cpy().scl(getPhysScale()));
            transform.rotation = bodyComponent.body.getAngle();
        }

        return transform.rotation;
    }

    public Vector2 getVelocity(){
        if(bodyComponent == null) return null;
        return transform.velocity.set(bodyComponent.body.getLinearVelocity());
    }

    // Setters
    public void setPosition(float x, float y){
        if(bodyComponent != null){
            // TODO: TEMP Problem with bug here
            bodyComponent.body.setTransform(x / getPhysScale(), y / getPhysScale(), getRotation());
        } else {
            transform.position.set(x, y);
        }
    }

    public void setPosition(Vector2 p){ setPosition(p.x, p.y); }

    public void setRotation(float rads){
        if(bodyComponent != null){
            bodyComponent.body.setTransform(getPosition().cpy().scl(1 / getPhysScale()), rads);
        } else {
            transform.rotation = rads;
        }
    }

    public void setVelocity(float x, float y){
        if(bodyComponent == null) return;
        bodyComponent.body.setLinearVelocity(x, y);
    }

    public void setVelocity(Vector2 vel){ setVelocity(vel.x, vel.y); }

    // Component system
    @Override
    public <T extends IComponent> T addComponent(T component){
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
    public <T extends IComponent> T getComponent(Class<T> componentType){
        for(IComponent component : components){
            if(componentType.isAssignableFrom(component.getClass())){
                return componentType.cast(component);
            }
        }

        return null;
    }

    @Override
    public <T extends IComponent> T[] getComponents(Class<T> componentType) {
        Array<T> out = new Array<>(componentType);

        for(IComponent component : components){
            if(componentType.isAssignableFrom(component.getClass())){
                out.add(componentType.cast(component));
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
                Reloadable reloadableComponent = (Reloadable)component;
                reloadableComponent.reload(assets);
            }
        }
    }

    // Depreciated functions for backwards compat
    public float getX(){ return getPosition().x; }
    public float getY(){ return getPosition().y; }
    public void setX(float x){ setPosition(x, getPosition().y); }
    public void setY(float y){ setPosition(getPosition().x, y); }

}