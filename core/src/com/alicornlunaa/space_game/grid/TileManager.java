package com.alicornlunaa.space_game.grid;

import java.util.HashMap;

import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.grid.tiles.Element;
import com.alicornlunaa.space_game.grid.tiles.SolidTile;
import com.badlogic.gdx.utils.Array;

public class TileManager {
    // Static classes
    public static enum TileCategory {
        // Enumerations
        CONSTRUCTION("Construction", "Basic parts for constructing the majority of the ship.");
        
        // Variables
        public final String name;
        public final String description;
        
        // Constructor
        private TileCategory(String name, String description){
            this.name = name;
            this.description = description;
        }
    };

    // TODO: Try a placable inner class that holds the element or function to spawn tile entity?

    // Variables
    private HashMap<TileCategory, Array<AbstractTile>> parts = new HashMap<>();

    // Constructor
    public TileManager(){
        // Register construction parts
        register(TileCategory.CONSTRUCTION, new SolidTile(Element.STEEL));
    }

    // Functions
    public void register(TileCategory category, AbstractTile tile){
        if(!parts.containsKey(category))
            parts.put(category, new Array<AbstractTile>());

        parts.get(category).add(tile);
    }

    public Array<AbstractTile> getTilesInCategory(TileCategory category){
        return parts.get(category);
    }

    public AbstractTile getTile(TileCategory category, int index){
        return parts.get(category).get(index);
    }
}
