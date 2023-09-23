package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.IComponent;

public class BaseComponent implements IComponent {
    // Variables
    private IEntity entity;

    // Constructor
    public BaseComponent(IEntity entity){
        this.entity = entity;
    }

    // Functions
    public IEntity getEntity(){
        return entity;
    }
}
