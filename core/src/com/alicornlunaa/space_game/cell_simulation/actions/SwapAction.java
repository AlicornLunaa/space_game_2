package com.alicornlunaa.space_game.cell_simulation.actions;

import com.alicornlunaa.space_game.cell_simulation.Simulation;
import com.alicornlunaa.space_game.cell_simulation.tiles.AbstractTile;

public class SwapAction extends AbstractAction {
    // Variables
    private int fromX, fromY;
    private int toX, toY;

    // Constructor
    public SwapAction(int fromX, int fromY, int toX, int toY){
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    // Functions
    @Override
    public boolean commit(Simulation simulation) {
        AbstractTile current = simulation.getTile(fromX, fromY);
        AbstractTile target = simulation.getTile(toX, toY);

        simulation.tiles[simulation.getIndex(fromX, fromY)] = target;
        simulation.tiles[simulation.getIndex(toX, toY)] = current;
        return true;
    }
}