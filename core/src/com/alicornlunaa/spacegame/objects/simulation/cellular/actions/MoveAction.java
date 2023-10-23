package com.alicornlunaa.spacegame.objects.simulation.cellular.actions;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;

public class MoveAction extends Action {
    // Variables
    private CellBase cell;
    private int toX, toY;

    // Constructor
    public MoveAction(CellBase cell, int toX, int toY){
        this.cell = cell;
        this.toX = toX;
        this.toY = toY;
    }

    // Functions
    @Override
    public boolean commit(CellWorld world) {
        if(!world.inBounds(toX, toY))
            return false;

        if(world.getTile(toX, toY) == null){
            world.setTile(cell.getX(), cell.getY(), null); // Remove old cell
            world.setTile(toX, toY, cell); // Read it to new position
            return true;
        }

        return false;
    }
}
