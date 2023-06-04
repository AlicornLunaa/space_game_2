package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.spacegame.objects.simulation.Universe;

public class OrbitComponent implements IComponent {

    // Variables
    public Universe universe;
    public IEntity entity;

    public boolean visible = false;
    
}
