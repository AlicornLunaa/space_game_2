package com.alicornlunaa.space_game.cell_simulation.actions;

import com.alicornlunaa.space_game.cell_simulation.Simulation;
import com.alicornlunaa.space_game.cell_simulation.tiles.AbstractTile;

public class CreateAction extends AbstractAction {
    // Variables
    public AbstractTile tile;
    public int x, y;

    // Constructor
    public CreateAction(AbstractTile tile, int x, int y){
        this.tile = tile;
        this.x = x;
        this.y = y;
    }

    // Functions
    @Override
    public boolean commit(Simulation simulation) {
        simulation.tiles[simulation.getIndex(x, y)] = tile;
        return true;
    }
}
