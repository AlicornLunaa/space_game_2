package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.objects.simulation.orbits.Orbit;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class OrbitComponent extends Orbit implements IComponent {

    // Variables
    public boolean visible = true;

    // Constructor
    public OrbitComponent(Universe universe, IEntity entity){
        super(universe, entity);
    }

    // Functions
    public void draw(ShapeRenderer renderer, float lineWidth){
        if(!visible) return;
        super.draw(renderer, lineWidth);
    }
    
}
