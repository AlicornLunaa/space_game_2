package com.alicornlunaa.spacegame.objects.simulation.cellular.actions;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;

public class EmplaceAction extends Action {
    // Variables
    private CellBase cell;
    private int toX, toY;

    // Constructor
    public EmplaceAction(CellBase cell, int toX, int toY){
        this.cell = cell;
        this.toX = toX;
        this.toY = toY;
    }

    // Functions
    public CellBase getCell(){
        return cell;
    }

    @Override
    public boolean commit(CellWorld world) {
        if(!world.inBounds(toX, toY))
            return false;

        if(world.getTile(toX, toY) == null){
            world.setTile(toX, toY, cell); // Read it to new position
            return true;
        }

        return false;
    }
}
