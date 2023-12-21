package com.alicornlunaa.space_game.cell_simulation.tiles;

import com.alicornlunaa.space_game.cell_simulation.Simulation;
import com.alicornlunaa.space_game.util.Constants;

public class LiquidTile extends AbstractTile {
    // Variables
    public int spreadFactor = 1;
    public float viscosity = 0.96f;
    public boolean renderFullBlock = false;

    // Constructor
    public LiquidTile(Element element) {
        super(element, State.LIQUID);
        mass = element.density;
    }

    // Functions
    @Override
    public void update(Simulation simulation, int currX, int currY){
        super.update(simulation, currX, currY);

        // Rendering flags
        renderFullBlock = simulation.getTile(currX, currY + 1) instanceof LiquidTile;

        // Remove water with nothing in it
        if(mass <= Constants.MIN_FLUID_LEVEL){
            simulation.tiles[simulation.getIndex(currX, currY)] = null;
            return;
        }

        // Simulate water flow down
        AbstractTile target = simulation.getTile(currX, currY - 1);

        if(simulation.inBounds(currX, currY - 1) && target == null){
            // Liquid falling with gravity, viscosity does not matter
            simulation.swap(currX, currY, currX, currY - 1);
            return;
        }

        if(target != null && target.element == element && target.mass < target.element.density){
            // Move higher to lower water
            target.mass += (viscosity * mass);
            mass -= (viscosity * mass);
            return;
        }

        // Flow sideways
        AbstractTile left = simulation.getTile(currX - 1, currY);
        AbstractTile right = simulation.getTile(currX + 1, currY);
        float averageMass = mass;
        int numTiles = 1;
        
        if(simulation.inBounds(currX - 1, currY) && left == null){
            // Create new liquid cell
            LiquidTile tile = new LiquidTile(element);
            tile.renderFullBlock = true;
            tile.mass = 0.f;
            left = tile;
            simulation.tiles[simulation.getIndex(currX - 1, currY)] = tile;
        }
        
        if(simulation.inBounds(currX + 1, currY) && right == null){
            // Create new liquid cell
            LiquidTile tile = new LiquidTile(element);
            tile.renderFullBlock = true;
            tile.mass = 0.f;
            right = tile;
            simulation.tiles[simulation.getIndex(currX + 1, currY)] = tile;
        }

        boolean leftValid = left != null && left instanceof LiquidTile && left.element == element && left.mass < mass;
        boolean rightValid = right != null && right instanceof LiquidTile && right.element == element && right.mass < mass;

        if(leftValid){ numTiles++; averageMass += left.mass; };
        if(rightValid){ numTiles++; averageMass += right.mass; }

        if(numTiles > 1){
            averageMass /= numTiles;
            
            float flowAmount = (mass - averageMass) * viscosity; // The amount to flow into other cells is the average minus the current cell
            mass -= flowAmount;
            flowAmount /= (numTiles - 1);
            
            if(leftValid) left.mass += flowAmount;
            if(rightValid) right.mass += flowAmount;
            return;
        }
    }
}
