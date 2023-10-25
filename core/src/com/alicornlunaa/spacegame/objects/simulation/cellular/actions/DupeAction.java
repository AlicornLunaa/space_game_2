package com.alicornlunaa.spacegame.objects.simulation.cellular.actions;

import com.alicornlunaa.spacegame.objects.simulation.cellular.CellBase;
import com.alicornlunaa.spacegame.objects.simulation.cellular.CellWorld;

public class DupeAction<T extends CellBase> extends Action {
    // Variables
    private T newCell;
    private int toX, toY;

    // Constructor
    @SuppressWarnings("unchecked")
    public DupeAction(int toX, int toY, Class<? extends CellBase> type, Object... args){
        try {
            this.newCell = (T)type.getDeclaredConstructors()[0].newInstance(args);
        } catch(Exception e){
            e.getStackTrace();
        }

        this.toX = toX;
        this.toY = toY;
    }

    // Functions
    public T getCell(){
        return newCell;
    }

    @Override
    public boolean commit(CellWorld world) {
        if(!world.inBounds(toX, toY))
            return false;

        if(world.getTile(toX, toY) == null){
            world.setTile(toX, toY, newCell); // Remove old cell
            return true;
        }

        return false;
    }
}
