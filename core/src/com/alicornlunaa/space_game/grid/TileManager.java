package com.alicornlunaa.space_game.grid;

import java.util.HashMap;

import com.alicornlunaa.space_game.grid.TileManager.PickableTile.TileSpawner;
import com.alicornlunaa.space_game.grid.entities.ControlSeatTile;
import com.alicornlunaa.space_game.grid.entities.DoorTile;
import com.alicornlunaa.space_game.grid.entities.ThrusterTile;
import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.grid.tiles.Element;
import com.alicornlunaa.space_game.grid.tiles.GasTile;
import com.alicornlunaa.space_game.grid.tiles.LiquidTile;
import com.alicornlunaa.space_game.grid.tiles.SolidTile;
import com.alicornlunaa.space_game.grid.tiles.TileElement;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.badlogic.gdx.utils.Array;

public class TileManager {
    // Static classes
    public static enum TileCategory {
        // Enumerations
        CONSTRUCTION("Construction", "Basic parts for constructing the majority of the ship."),
        THRUSTERS("Thrusters", "Tiles which can move the ship in a direction."),
        CONTROL("Control", "Tiles which can orient the ship.");
        
        // Variables
        public final String name;
        public final String description;
        
        // Constructor
        private TileCategory(String name, String description){
            this.name = name;
            this.description = description;
        }
    };

    public static class PickableTile {
        // Interface
        public static interface TileSpawner {
            AbstractTile spawn(AbstractTile template);
        }

        // Variables
        private final TileSpawner spawnFunc;
        public final AbstractTile tile;

        // Constructor
        public PickableTile(final TileElement tile){
            // Tile element default spawner
            this.spawnFunc = new TileSpawner() {
                @Override
                public AbstractTile spawn(AbstractTile template) {
                    AbstractTile newTile;

                    switch(tile.state){
                    case SOLID:
                        newTile = new SolidTile(tile.element);
                        newTile.rotation = template.rotation;
                        break;
                        
                    case LIQUID:
                        newTile = new LiquidTile(tile.element);
                        newTile.rotation = template.rotation;
                        break;
                    
                    case GAS:
                        newTile = new GasTile(tile.element);
                        newTile.rotation = template.rotation;
                        break;
                        
                    default:
                        newTile = null;
                        break;
                    }

                    return newTile;
                }
            };

            this.tile = tile;
        }

        public PickableTile(TileEntity tile, TileSpawner spawnFunc){
            // Tile entity, needs custom spawner
            this.spawnFunc = spawnFunc;
            this.tile = tile;
        }

        // Functions
        public AbstractTile spawn(){
            return spawnFunc.spawn(tile);
        }
    };

    // Variables
    private HashMap<TileCategory, Array<PickableTile>> parts = new HashMap<>();

    // Constructor
    public TileManager(){
        // Register construction parts
        register(TileCategory.CONSTRUCTION, new PickableTile(new SolidTile(Element.STEEL)));
        register(TileCategory.THRUSTERS, new PickableTile(new SolidTile(Element.WATER)));
        register(TileCategory.THRUSTERS, new PickableTile(new ThrusterTile(0), new TileSpawner() {
            @Override
            public AbstractTile spawn(AbstractTile template) {
                return new ThrusterTile(template.rotation);
            }
        }));
        register(TileCategory.CONTROL, new PickableTile(new SolidTile(Element.SAND)));
        register(TileCategory.CONTROL, new PickableTile(new DoorTile(0), new TileSpawner() {
            @Override
            public AbstractTile spawn(AbstractTile template) {
                return new DoorTile(template.rotation);
            }
        }));
        register(TileCategory.CONTROL, new PickableTile(new ControlSeatTile(0), new TileSpawner() {
            @Override
            public AbstractTile spawn(AbstractTile template) {
                return new ControlSeatTile(template.rotation);
            }
        }));
    }

    // Functions
    public void register(TileCategory category, PickableTile tile){
        if(!parts.containsKey(category))
            parts.put(category, new Array<PickableTile>());

        parts.get(category).add(tile);
    }

    public Array<PickableTile> getTilesInCategory(TileCategory category){
        return parts.get(category);
    }

    public PickableTile getTile(TileCategory category, int index){
        return parts.get(category).get(index);
    }

    public HashMap<TileCategory, Array<PickableTile>> getTileMap(){
        return parts;
    }
}
