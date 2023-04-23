package com.alicornlunaa.spacegame.engine.core;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface IEntity {
    
    /**
     * Called every physics tick
     */
    void update();

    /**
     * Called every rendering tick
     * @param batch The batch to render to
     */
    void render(Batch batch);

}
