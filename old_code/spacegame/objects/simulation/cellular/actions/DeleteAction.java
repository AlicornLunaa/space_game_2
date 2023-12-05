package com.alicornlunaa.spacegame.objects.simulation.cellular.actions;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;

public class DeleteAction extends Action {
    // Variables
    private CellBase cell;

    // Constructor
    public DeleteAction(CellBase cell){
        this.cell = cell;
    }

    // Functions
    @Override
    public boolean commit(CellWorld world) {
        world.setTile(cell.getX(), cell.getY(), null);
        return true;
    }
}
