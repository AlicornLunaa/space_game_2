package com.alicornlunaa.spacegame.objects.simulation.cellular.actions;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.badlogic.gdx.utils.Null;

public class SwapAction extends Action {
    // Variables
    private CellBase cell;
    private int toX, toY;

    // Constructor
    public SwapAction(CellBase cell, int toX, int toY){
        this.cell = cell;
        this.toX = toX;
        this.toY = toY;
    }

    // Functions
    @Override
    public boolean commit(CellWorld world) {
        if(!world.inBounds(toX, toY))
            return false;
            
        @Null CellBase otherCell = world.getTile(toX, toY);
        world.setTile(cell.getX(), cell.getY(), otherCell);
        world.setTile(toX, toY, cell);
        return true;
    }
}
