package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.MoveAction;
import com.badlogic.gdx.utils.Array;

public class Water extends CellBase {
    public Water() {
        super("water");
    }
    
    public void step(CellWorld world, Array<Action> changes){
        // Basic gravity
        if(world.getTile(getX(), getY() - 1) == null) changes.add(new MoveAction(this, getX(), getY() - 1));
        else if(world.getTile(getX() - 1, getY() - 1) == null) changes.add(new MoveAction(this, getX() - 1, getY() - 1));
        else if(world.getTile(getX() + 1, getY() - 1) == null) changes.add(new MoveAction(this, getX() + 1, getY() - 1));
        else if(world.getTile(getX() - 1, getY()) == null && world.getTile(getX() + 1, getY()) != null) changes.add(new MoveAction(this, getX() - 1, getY()));
        else if(world.getTile(getX() + 1, getY()) == null && world.getTile(getX() - 1, getY()) != null) changes.add(new MoveAction(this, getX() + 1, getY()));
    }
}
