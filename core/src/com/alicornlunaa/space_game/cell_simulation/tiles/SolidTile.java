package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;

public class SolidTile extends AbstractTile {
    // Constructor
    public SolidTile(Element element) {
        super(element, State.SOLID);
    }

    // Functions
    @Override
    public void update(Simulation simulation, int currX, int currY){
        super.update(simulation, currX, currY);
        
        if(checkTileSwap(simulation, currX, currY, currX, currY - 1)) return;
        if(checkTileSwap(simulation, currX, currY, currX - 1, currY - 1)) return;
        if(checkTileSwap(simulation, currX, currY, currX + 1, currY - 1)) return;
    }
}
