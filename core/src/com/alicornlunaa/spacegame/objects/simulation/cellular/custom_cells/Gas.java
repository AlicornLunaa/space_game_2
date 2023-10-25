package com.alicornlunaa.spacegame.objects.simulation.cellular.custom_cells;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.Action;
import com.alicornlunaa.spacegame.objects.simulation.cellular.actions.CreateAction;
import com.alicornlunaa.spacegame.util.Vector2i;
import com.badlogic.gdx.utils.Array;

public class Gas extends CellBase {
    // Variables
    // private final int SPREAD_RATE = 5;
    private float density = 1.0f;

    // Constructor
    public Gas() {
        super("stone");
    }
    
    // Functions
    public float getDensity(){
        return density;
    }

    public void step(CellWorld world, Array<Action> changes){
        Array<Vector2i> positions = new Array<>();
        positions.add(new Vector2i(-1, 0));
        positions.add(new Vector2i(0, -1));
        positions.add(new Vector2i(1, 0));
        positions.add(new Vector2i(0,  1));
        positions.shuffle();
        
        float averageGasDensity = density;
        for(int i = 0; i < positions.size; i++){
            Vector2i v = positions.get(i);
            CellBase cell = world.getTile(getX() + v.x, getY() + v.y);

            if(cell == null){
                // Create new gas
                CreateAction<Gas> action = new CreateAction<>(getX() + v.x, getY() + v.y, Gas.class);
                action.getCell().density = 0;
                changes.add(action);
            } else if(cell instanceof Gas){
                averageGasDensity += ((Gas)cell).density;
            } else {
                positions.removeIndex(i);
                i--;
            }
        }

        averageGasDensity /= positions.size;
        for(int i = 0; i < positions.size; i++){
            Vector2i v = positions.get(i);
            CellBase cell = world.getTile(getX() + v.x, getY() + v.y);

            if(cell instanceof Gas){
                // Create new gas
                Gas g = (Gas)cell;
                g.density = averageGasDensity;
            }
        }
    }
}
