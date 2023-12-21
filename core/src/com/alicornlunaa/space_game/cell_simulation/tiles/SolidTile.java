package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;

public class SolidTile extends AbstractTile {
    // Constructor
    public SolidTile(Element element) {
        super(element, State.SOLID);
    }

    // Functions
    private boolean checkTileSwap(Simulation simulation, int fx, int fy, int tx, int ty){
        AbstractTile target = simulation.getTile(tx, ty);

        if(simulation.inBounds(tx, ty) && (target == null || target.state != State.SOLID)){
            simulation.swap(fx, fy, tx, ty);
            return true;
        }

        return false;
    }

    @Override
    public boolean update(Simulation simulation, int currX, int currY){
        if(!super.update(simulation, currX, currY)) return false;
        
        if(checkTileSwap(simulation, currX, currY, currX, currY - 1)) return false;
        if(checkTileSwap(simulation, currX, currY, currX - 1, currY - 1)) return false;
        if(checkTileSwap(simulation, currX, currY, currX + 1, currY - 1)) return false;

        return true;
    }
}
