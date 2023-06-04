package com.alicornlunaa.selene_engine.core;

import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.util.Assets;
import com.alicornlunaa.selene_engine.util.Assets.Reloadable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

@SuppressWarnings("unused")
public abstract class BaseEntity implements IEntity, Disposable, Reloadable {

    // Variables
    private Array<IComponent> components = new Array<>();
    private TransformComponent transform = addComponent(new TransformComponent());

    // Constructors
    public BaseEntity(){}

    // Functions
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
