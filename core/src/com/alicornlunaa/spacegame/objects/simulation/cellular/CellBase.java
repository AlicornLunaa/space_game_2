package com.alicornlunaa.spacegame.objects.simulation.cellular;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// Base cell for automata simulation
public class CellBase {
    // Variables
    protected TextureRegion texture;

    // Constructor
    public CellBase(String textureName){
        texture = App.instance.atlas.findRegion("tiles/" + textureName);
    }

    // Functions
}
