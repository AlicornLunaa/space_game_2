package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.DeleteAction;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.EmplaceAction;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.Vector2i;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public abstract class Gas extends CellBase {
    // Variables
    private final double MIN_PRESSURE = 1e-4;

    private int spreadFactor = 15;
    private float pressure = 1.f;

    // Private functions
    private void scanPossibleCells(CellWorld world, Array<Action> changes, Array<Gas> flowingCells, Array<Vector2i> positions){
        for(Vector2i v : positions){
            CellBase possibleCell = world.getTile(v.x, v.y);

            if(possibleCell == null){
                // No cell exists here, create it
                Gas cell = this.cpy();
                cell.pressure = 0.0f;
                possibleCell = cell;
                changes.add(new EmplaceAction(cell, v.x, v.y));
            } else if(!(possibleCell instanceof Gas)) {
                // Flow is blocked
                break;
            }
            
            // Cast to Gas
            Gas possibleGasCell = (Gas)possibleCell;

            // Add this cell to the possible cells because its a Gas and valid for transfer
            if(possibleGasCell.pressure < pressure)
                flowingCells.add(possibleGasCell);
        }
    }

    private void balanceFluidLevel(CellWorld world, Array<Action> changes){
        // Get cells to transfer to
        Array<Vector2i> upCellPositions = getLine(getX(), getY(), getX(), getY() + spreadFactor);
        Array<Vector2i> downCellPositions = getLine(getX(), getY(), getX(), getY() - spreadFactor);
        Array<Vector2i> leftCellPositions = getLine(getX(), getY(), getX() - spreadFactor, getY());
        Array<Vector2i> rightCellPositions = getLine(getX(), getY(), getX() + spreadFactor, getY());

        // Get possible cells to transfer to
        Array<Gas> flowingCells = new Array<>();
        scanPossibleCells(world, changes, flowingCells, upCellPositions);
        scanPossibleCells(world, changes, flowingCells, downCellPositions);
        scanPossibleCells(world, changes, flowingCells, leftCellPositions);
        scanPossibleCells(world, changes, flowingCells, rightCellPositions);

        // Calculate flow between all possible cells
        float averagePressure = pressure;
        
        for(Gas cell : flowingCells){
            averagePressure += cell.pressure;
        }

        averagePressure /= (flowingCells.size + 1);
        
        // Execute the flow
        for(Gas cell : flowingCells)
            cell.pressure = averagePressure;

        pressure = averagePressure;
    }

    // Constructor
    public Gas(String textureName, int spreadFactor, float pressure) {
        super(textureName);
        this.spreadFactor = spreadFactor;
        this.pressure = pressure;
    }
    
    // Functions
    public abstract Gas cpy();

    public void step(CellWorld world, Array<Action> changes){
        // Prevent stupidly small water cells
        if(pressure <= MIN_PRESSURE){
            changes.add(new DeleteAction(this));
            return;
        }
        
        // Simulate gas pressure
        balanceFluidLevel(world, changes);
    }

    public void draw(Batch batch){
        batch.setColor(0.5f, 0.5f, 0.5f, pressure);
        batch.draw(
            texture,
            getX() * Constants.TILE_SIZE, getY() * Constants.TILE_SIZE,
            0, 0,
            Constants.TILE_SIZE, Constants.TILE_SIZE,
            1, 1,
            0
        );
    }
}
