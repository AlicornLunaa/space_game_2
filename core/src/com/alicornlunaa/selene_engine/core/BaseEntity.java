package com.alicornlunaa.selene_engine.core;

import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.util.Assets;
import com.alicornlunaa.selene_engine.util.Assets.Reloadable;
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
    public float getRotation(){
        return transform.rotation;
    }

    public Vector2 getVelocity(){
        return transform.velocity.cpy();
    }

    // Setters
    public void setPosition(float x, float y){
        transform.position.set(x, y);
    }

    public void setPosition(Vector2 p){ setPosition(p.x, p.y); }

    public void setRotation(float rads){
        transform.rotation = rads;
    }

    public void setVelocity(float x, float y){
        transform.velocity.set(x, y);
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
}
