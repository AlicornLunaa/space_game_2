package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.badlogic.gdx.utils.Array;

public class Water extends CellBase {
    // Variables
    private final int SPREAD_RATE = 5;

    // Constructor
    public Water() {
        super("water");
    }
    
    // Functions
    public void step(CellWorld world, Array<Action> changes){
        int randOrder = (Math.random() > 0.5) ? 1 : -1;

        
    }
}
