package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.MoveAction;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.SwapAction;
import com.badlogic.gdx.utils.Array;

public class Sand extends CellBase {
    // Variables
    // private final int DEPTH_RATE = 1;
    // private final int SPREAD_RATE = 2;

    // Constructor
    public Sand() {
        super("sand");
    }
    
    // Functions
    public void step(CellWorld world, Array<Action> changes){
        int randOrder = (Math.random() > 0.5) ? 1 : -1;

        if(world.getTile(getX(), getY() - 1) == null){
            changes.add(new MoveAction(this, getX(), getY() - 1));
        } else if(world.getTile(getX() + randOrder, getY() - 1) == null){
            changes.add(new MoveAction(this, getX() + randOrder, getY() - 1));
        } else if(world.getTile(getX() - randOrder, getY() - 1) == null){
            changes.add(new MoveAction(this, getX() - randOrder, getY() - 1));
        }
    }
}
