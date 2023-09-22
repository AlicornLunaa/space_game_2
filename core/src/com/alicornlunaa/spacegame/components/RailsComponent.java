package com.alicornlunaa.spacegame.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.spacegame.objects.simulation.orbits.EllipticalConic;
import com.badlogic.gdx.utils.Null;

public class RailsComponent implements IComponent {
    public @Null EllipticalConic conic;
    public float elapsedTime = 0.0f;

    public void update(){
        conic = new EllipticalConic(conic.getParent(), conic.getChild());
    }
}
