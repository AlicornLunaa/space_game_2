package com.alicornlunaa.spacegame.engine.core;

import com.badlogic.gdx.math.Vector2;

public interface IEntity {

    /**
     * Gets the local position, relative to the deepest level of physics world
     * @return Vector2 containing the position
     */
    Vector2 getPosition();

    /**
     * Gets the global position, relative to the origin of the highest level of physics worlds
     * @return Vector2 containing the position
     */
    Vector2 getGlobalPosition();
    
    /**
     * Called every frame for each entity
     * @param delta Time between this frame and the previous frame
     */
    void update(float delta);

    /**
     * Called every engine tick for each entity
     */
    void fixedUpdate();

}
