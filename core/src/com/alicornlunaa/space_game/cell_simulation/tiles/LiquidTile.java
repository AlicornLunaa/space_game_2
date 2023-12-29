package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class LiquidTile extends AbstractTile {
    // Variables
    public int spreadFactor = 1;
    public float viscosity = 0.975f;
    public boolean renderFullBlock = false;

    // Constructor
    public LiquidTile(Element element) {
        super(element, State.LIQUID);
        mass = element.density;
    }

    // Functions
    // private boolean moveOrCompress(Simulation simulation, int currX, int currY){
    //     AbstractTile above = simulation.getTile(currX, currY + 1);

    //     if(simulation.inBounds(currX, currY + 1)){
    //         if(above == null){
    //             // Just move up
    //             simulation.swap(currX, currY, currX, currY + 2);
    //             return true;
    //         } else if(above.element == element) {
    //             // Same element, compress them into one
    //             above.mass += mass;
    //             mass = 0;
    //             simulation.tiles[simulation.getIndex(currX, currY)] = null;
    //             return true;
    //         } else {
    //             // Different element
    //         }
    //     }

    //     return false;
    // }

    @Override
    public boolean update(Simulation simulation, int currX, int currY){
        if(!super.update(simulation, currX, currY)) return false;

        // Rendering flags
        renderFullBlock = simulation.getTile(currX, currY + 1) instanceof LiquidTile;

        // Skip if no gravity
        if(simulation.gravity.len2() == 0)
            return false;

        // Remove water with nothing in it
        if(mass <= Constants.MIN_FLUID_LEVEL){
            simulation.tiles[simulation.getIndex(currX, currY)] = null;
            return false;
        }

        // Fluid dynamics
        Vector2 downDir = Simulation.getDown(simulation.gravity);
        Vector2 leftDir = downDir.cpy().rotate90(-1);
        
        AbstractTile below = simulation.getTile((int)(currX + downDir.x), (int)(currY + downDir.y));
        AbstractTile left = simulation.getTile((int)(currX + leftDir.x), (int)(currY + leftDir.y));
        AbstractTile right = simulation.getTile((int)(currX - leftDir.x), (int)(currY - leftDir.y));
        Array<AbstractTile> tilesFlowing = new Array<>();
        tilesFlowing.add(this);

        if(simulation.inBounds((int)(currX + downDir.x), (int)(currY + downDir.y))){
            if(below == null){
                // Fall with gravity
                simulation.swap(currX, currY, (int)(currX + downDir.x), (int)(currY + downDir.y));
                return false;
            } /* else if(below.state == State.LIQUID && below.element.density < element.density) {
                // Below is less dense, swap spaces with them
                simulation.swap(currX, currY, currX, currY - 1);
                return false;
            } */ else if(below.element == element && below.mass < element.density){
                // Flow water from high mass to low mass
                below.mass += (viscosity * mass);
                mass -= (viscosity * mass);
                return false;
            }
        }

        if(simulation.inBounds((int)(currX + leftDir.x), (int)(currY + leftDir.y))){
            if(left == null){
                // Create new liquid cell
                LiquidTile tile = new LiquidTile(element);
                tile.mass = 0.f;
                tilesFlowing.add(tile);
                simulation.tiles[simulation.getIndex((int)(currX + leftDir.x), (int)(currY + leftDir.y))] = tile;
            } /* else if(left.state == State.LIQUID && left.element.density > element.density){
                // Left is more dense, move up
                if(moveOrCompress(simulation, currX, currY)) return false;
            } */ else if(left.element == element && left.mass < mass){
                // Flow water from high mass to low mass
                tilesFlowing.add(left);
            }
        }
        
        if(simulation.inBounds((int)(currX - leftDir.x), (int)(currY - leftDir.y))){
            if(right == null){
                // Create new liquid cell
                LiquidTile tile = new LiquidTile(element);
                tile.mass = 0.f;
                tilesFlowing.add(tile);
                simulation.tiles[simulation.getIndex((int)(currX - leftDir.x), (int)(currY - leftDir.y))] = tile;
            } /* else if(right.state == State.LIQUID && right.element.density > element.density){
                // Right is more dense, move up
                if(moveOrCompress(simulation, currX, currY)) return false;
            } */ else if(right.element == element && right.mass < mass){
                // Flow water from high mass to low mass
                tilesFlowing.add(right);
            }
        }

        if(tilesFlowing.size > 1){
            float averageMass = 0.f;

            for(AbstractTile t : tilesFlowing)
                averageMass += t.mass;

            averageMass /= tilesFlowing.size;
            
            float flowAmount = Math.abs(mass - averageMass) * viscosity; // The amount to flow into other cells is the average minus the current cell
            mass -= flowAmount;
            flowAmount /= (tilesFlowing.size - 1);

            for(AbstractTile t : tilesFlowing){
                if(t == this) continue;
                t.mass += flowAmount;
            }

            return false;
        }

        return true;
    }
}
