package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.MoveAction;
import com.alicornlunaa.spacegame.util.Vector2i;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class Sand extends CellBase {
    // Variables
    private final int DEPTH_RATE = 1;
    private final int SPREAD_RATE = 2;

    // Constructor
    public Sand() {
        super("sand");
    }
    
    // Functions
    public void step(CellWorld world, Array<Action> changes){
        @Null Vector2i validTarget = null;
        Array<Vector2i> positions;
        int randOrder = (Math.random() > 0.5) ? 1 : -1;

        if(world.getTile(getX(), getY() - 1) == null){
            changes.add(new MoveAction(this, getX(), getY() - 1));
        } else if(world.getTile(getX() + randOrder, getY() - 1) == null){
            changes.add(new MoveAction(this, getX() + randOrder, getY() - 1));
        } else if(world.getTile(getX() - randOrder, getY() - 1) == null){
            changes.add(new MoveAction(this, getX() - randOrder, getY() - 1));
        }

        // Gravity movement
        // positions = getLine(getX(), getY(), getX(), getY() - DEPTH_RATE);
        // for(Vector2i possiblePosition : positions){
        //     if(world.getTile(possiblePosition.x, possiblePosition.y) != null)
        //         break;

        //     validTarget = possiblePosition;
        // }
        // if(validTarget != null){
        //     // Swap positions to the last valid target position
        //     changes.add(new MoveAction(this, validTarget.x, validTarget.y));
        //     return;
        // }

        // // Sand movement
        // validTarget = null;
        // positions = getLine(getX() - randOrder, getY() - 1, getX() - randOrder, getY() - SPREAD_RATE);
        // for(Vector2i possiblePosition : positions){
        //     if(world.getTile(possiblePosition.x, possiblePosition.y) != null)
        //         break;

        //     validTarget = possiblePosition;
        // }
        // if(validTarget != null){
        //     // Swap positions to the last valid target position
        //     changes.add(new MoveAction(this, validTarget.x, validTarget.y));
        //     return;
        // }

        // validTarget = null;
        // positions = getLine(getX() + randOrder, getY() - 1, getX() + randOrder, getY() - SPREAD_RATE);
        // for(Vector2i possiblePosition : positions){
        //     if(world.getTile(possiblePosition.x, possiblePosition.y) != null)
        //         break;

        //     validTarget = possiblePosition;
        // }
        // if(validTarget != null){
        //     // Swap positions to the last valid target position
        //     changes.add(new MoveAction(this, validTarget.x, validTarget.y));
        //     return;
        // }
    }
}
