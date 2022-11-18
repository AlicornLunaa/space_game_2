package com.alicornlunaa.spacegame.objects.Ship.interior;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship.Ship;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

/** Drawable class to render the inside of a ship
 * Will also store all user-created objects
 */
public class Interior {

    // Variables
    private Array<InteriorCell> cells = new Array<>();

    // Constructor
    public Interior(final App game, final Ship ship){
        // Construct interior cells based on the ship
        
    }
    
    // Functions
    public void draw(Batch batch){
        for(InteriorCell c : cells){
            c.draw(batch);
        }
    }
    
}
