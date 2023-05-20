package com.alicornlunaa.selene_engine.ecs;

import com.alicornlunaa.selene_engine.core.IEntity;

public interface ISystem {
    /** Ran before the first update */
    void beforeUpdate();

    /** Ran after the first update */
    void afterUpdate();

    /** Called once every physics tick */
    void update(IEntity entity);

    /** Ran before the first render */
    void beforeRender();

    /** Ran after the first render */
    void afterRender();

    /** Called once every frame */
    void render(IEntity entity);

    /**
     * Returns true or false if this system should run on this entity
     * @param entity
     * @return true or false
     */
    boolean shouldRunOnEntity(IEntity entity);
}
