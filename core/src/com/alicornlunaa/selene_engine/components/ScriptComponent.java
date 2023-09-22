package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.ecs.IComponent;

public abstract class ScriptComponent implements IComponent {
    // Variables
    private BaseEntity entity;

    // Constructor
    public ScriptComponent(BaseEntity entity){
        this.entity = entity;
    }

    // Functions
    protected BaseEntity getEntity(){ return entity; }

    /** Only at the start */
    public abstract void start();

    /** Once per physics tick */
    public abstract void update();

    /** Once per frame */
    public abstract void render();
}
