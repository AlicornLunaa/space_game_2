package com.alicornlunaa.space_game.grid;

import java.util.HashMap;

import com.alicornlunaa.space_game.grid.TileManager.PickableTile.TileSpawner;
import com.alicornlunaa.space_game.grid.entities.ControlSeatTile;
import com.alicornlunaa.space_game.grid.entities.DoorTile;
import com.alicornlunaa.space_game.grid.entities.RcsPortTile;
import com.alicornlunaa.space_game.grid.entities.ThrusterTile;
import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.grid.tiles.Element;
import com.alicornlunaa.space_game.grid.tiles.GasTile;
import com.alicornlunaa.space_game.grid.tiles.LiquidTile;
import com.alicornlunaa.space_game.grid.tiles.SolidTile;
import com.alicornlunaa.space_game.grid.tiles.TileElement;
import com.alicornlunaa.space_game.grid.tiles.TileEntity;
import com.alicornlunaa.space_game.grid.tiles.TileElement.Shape;
import com.badlogic.gdx.utils.Array;

public class TileManager {
    // Static classes
    public static enum TileCategory {
        // Enumerations
        AERO("Aerodynamics", "These have a great effect on the way air flows around the ship."),
        CARGO("Cargo", "These can be used to store stuff securely."),
        DATA_UTILITIES("Data Utilities", "These can be used to collect data on the environment."),
        ENVIRONMENT("Environment Control", "These can be used to change the environment to your will."),
        NUCLEAR("Nuclear", "big power :)"),
        RCS_PORTS("Rcs Ports", "These can be used to rotate and orient the ship in space."),
        ENERGY("Energy", "These can be used to store or create energy."),
        STRUCTURAL("Structural", "These can be used to construct the basic ship."),
        THRUSTER("Thrusters", "These can be used to move the ship fast.");
        
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
                        newTile = new SolidTile(tile.element, tile.shape);
                        newTile.rotation = template.rotation;
                        ((TileElement)newTile).shape = ((TileElement)newTile).shape;
                        break;
                        
                    case LIQUID:
                        newTile = new LiquidTile(tile.element, tile.shape);
                        newTile.rotation = template.rotation;
                        ((TileElement)newTile).shape = ((TileElement)newTile).shape;
                        break;
                    
                    case GAS:
                        newTile = new GasTile(tile.element, tile.shape);
                        newTile.rotation = template.rotation;
                        ((TileElement)newTile).shape = ((TileElement)newTile).shape;
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
        register(TileCategory.STRUCTURAL, new PickableTile(new SolidTile(Element.STEEL, Shape.SQUARE)));
        register(TileCategory.STRUCTURAL, new PickableTile(new SolidTile(Element.STEEL, Shape.SLOPE)));
        register(TileCategory.STRUCTURAL, new PickableTile(new SolidTile(Element.WATER, Shape.SQUARE)));
        register(TileCategory.STRUCTURAL, new PickableTile(new SolidTile(Element.SAND, Shape.SQUARE)));
        
        register(TileCategory.AERO, new PickableTile(new SolidTile(Element.SAND, Shape.SQUARE)));
        register(TileCategory.CARGO, new PickableTile(new SolidTile(Element.SAND, Shape.SQUARE)));
        register(TileCategory.DATA_UTILITIES, new PickableTile(new SolidTile(Element.SAND, Shape.SQUARE)));
        register(TileCategory.NUCLEAR, new PickableTile(new SolidTile(Element.SAND, Shape.SQUARE)));
        register(TileCategory.ENERGY, new PickableTile(new SolidTile(Element.SAND, Shape.SQUARE)));

        register(TileCategory.THRUSTER, new PickableTile(new ThrusterTile(0), new TileSpawner() {
            @Override
            public AbstractTile spawn(AbstractTile template) {
                return new ThrusterTile(template.rotation);
            }
        }));

        register(TileCategory.ENVIRONMENT, new PickableTile(new DoorTile(0), new TileSpawner() {
            @Override
            public AbstractTile spawn(AbstractTile template) {
                return new DoorTile(template.rotation);
            }
        }));

        register(TileCategory.ENVIRONMENT, new PickableTile(new ControlSeatTile(0), new TileSpawner() {
            @Override
            public AbstractTile spawn(AbstractTile template) {
                return new ControlSeatTile(template.rotation);
            }
        }));

        register(TileCategory.RCS_PORTS, new PickableTile(new RcsPortTile(0), new TileSpawner() {
            @Override
            public AbstractTile spawn(AbstractTile template) {
                return new RcsPortTile(template.rotation);
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
