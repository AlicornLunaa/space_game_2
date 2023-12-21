package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;
import com.badlogic.gdx.math.Vector2;

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

        // Get left and right directions
        Vector2 down = simulation.gravity.cpy().nor();
        Vector2 left = simulation.gravity.cpy().nor().rotate90(-1);
        Vector2 right = simulation.gravity.cpy().nor().rotate90(1);
        
        if(checkTileSwap(simulation, currX, currY, (int)(currX + down.x), (int)(currY + down.y))) return false;
        if(checkTileSwap(simulation, currX, currY, (int)(currX + left.x + down.x), (int)(currY + left.y + down.y))) return false;
        if(checkTileSwap(simulation, currX, currY, (int)(currX + right.x + down.x), (int)(currY + right.x + down.y))) return false;

        return true;
    }
}
