package com.alicornlunaa.selene_engine.core;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.util.Assets;
import com.alicornlunaa.selene_engine.util.Assets.Reloadable;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public abstract class BaseEntity implements IEntity, Disposable, Reloadable {

    // Variables
    private Array<IComponent> components = new Array<>();
    private TransformComponent transform = addComponent(new TransformComponent());

    // Constructors
    public BaseEntity(){}

    // Getters
    @Deprecated
    public float getPhysScale(){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        if(bodyComponent == null) return Constants.PPM;
        return bodyComponent.world.getPhysScale();
    }

    @Deprecated
    public Matrix3 getTransform(){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
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

    @Deprecated
    public Vector2 getPosition(){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        if(bodyComponent != null){
            transform.position.set(bodyComponent.body.getPosition().cpy().scl(getPhysScale()));
            transform.rotation = bodyComponent.body.getAngle();
        }

        return transform.position;
    }

    @Deprecated
    public Vector2 getCenter(){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        if(bodyComponent != null){
            return bodyComponent.body.getWorldCenter().cpy().scl(getPhysScale());
        }

        return getPosition();
    }

    @Deprecated
    public float getRotation(){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        if(bodyComponent != null){
            transform.position.set(bodyComponent.body.getPosition().cpy().scl(getPhysScale()));
            transform.rotation = bodyComponent.body.getAngle();
        }

        return transform.rotation;
    }

    @Deprecated
    public Vector2 getVelocity(){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        if(bodyComponent == null) return null;
        return transform.velocity.set(bodyComponent.body.getLinearVelocity());
    }

    // Setters
    @Deprecated
    public void setPosition(float x, float y){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        if(bodyComponent != null){
            // TODO: TEMP Problem with bug here
            bodyComponent.body.setTransform(x / getPhysScale(), y / getPhysScale(), getRotation());
        } else {
            transform.position.set(x, y);
        }
    }

    @Deprecated
    public void setPosition(Vector2 p){ setPosition(p.x, p.y); }

    @Deprecated
    public void setRotation(float rads){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        if(bodyComponent != null){
            bodyComponent.body.setTransform(getPosition().cpy().scl(1 / getPhysScale()), rads);
        } else {
            transform.rotation = rads;
        }
    }

    @Deprecated
    public void setVelocity(float x, float y){
        BodyComponent bodyComponent = getComponent(BodyComponent.class);
        if(bodyComponent == null) return;
        bodyComponent.body.setLinearVelocity(x, y);
    }

    @Deprecated
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
    @Deprecated
    public float getX(){ return getPosition().x; }
    @Deprecated
    public float getY(){ return getPosition().y; }
    @Deprecated
    public void setX(float x){ setPosition(x, getPosition().y); }
    @Deprecated
    public void setY(float y){ setPosition(getPosition().x, y); }
}
