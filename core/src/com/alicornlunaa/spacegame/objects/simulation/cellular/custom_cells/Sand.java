package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.MoveAction;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.SwapAction;
import com.badlogic.gdx.utils.Array;

public class Sand extends CellBase {
    public Sand() {
        super("sand");
    }
    
    public void step(CellWorld world, Array<Action> changes){
        // Basic gravity
        if(world.getTile(getX(), getY() - 1) == null){
            changes.add(new MoveAction(this, getX(), getY() - 1));
        } else if(world.getTile(getX() - 1, getY() - 1) == null){
            changes.add(new MoveAction(this, getX() - 1, getY() - 1));
        } else if(world.getTile(getX() + 1, getY() - 1) == null){
            changes.add(new MoveAction(this, getX() + 1, getY() - 1));
        } else if(world.getTile(getX(), getY() - 1) instanceof Water){
            changes.add(new SwapAction(this, getX(), getY() - 1));
        }
    }
}
