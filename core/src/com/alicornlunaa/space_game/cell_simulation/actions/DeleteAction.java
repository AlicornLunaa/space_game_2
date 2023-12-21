package com.alicornlunaa.space_game.cell_simulation.actions;

import com.alicornlunaa.space_game.cell_simulation.Simulation;

public class DeleteAction extends AbstractAction {
    // Variables
    public int x, y;

    // Constructor
    public DeleteAction(int x, int y){
        this.x = x;
        this.y = y;
    }

    // Functions
    @Override
    public boolean commit(Simulation simulation) {
        simulation.tiles[simulation.getIndex(x, y)] = null;
        return true;
    }
}
