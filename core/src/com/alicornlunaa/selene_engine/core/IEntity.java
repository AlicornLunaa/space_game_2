package com.alicornlunaa.selene_engine.core;

import com.alicornlunaa.selene_engine.ecs.IComponent;

public interface IEntity {
    /**
     * Adds a component to the entity
     * @param component
     * @return component
     */
    <T extends IComponent> T addComponent(T component);

    /**
     * Returns true or false depending on if the class has the type of component
     * @param componentType
     * @return boolean
     */
    boolean hasComponent(Class<? extends IComponent> componentType);

    /**
     * Returns true or false depending on if the class has all of the components
     * @param componentTypes
     * @return boolean
     */
    boolean hasComponents(Class<?>... componentTypes);

    /**
     * Returns the first component that matches the type provided
     * @param componentType
     * @return component
     */
    <T extends IComponent> T getComponent(Class<T> componentType);

    /**
     * Returns all the components that match the type provided
     * @param componentType
     * @return component
     */
    <T extends IComponent> T[] getComponents(Class<T> componentType);
}
