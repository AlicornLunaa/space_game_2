package com.alicornlunaa.space_game.grid;

import org.json.JSONObject;

import com.alicornlunaa.space_game.grid.entities.CustomTile;
import com.alicornlunaa.space_game.grid.entities.ThrusterTile;
import com.alicornlunaa.space_game.grid.tiles.AbstractTile;
import com.alicornlunaa.space_game.grid.tiles.Element;
import com.alicornlunaa.space_game.grid.tiles.GasTile;
import com.alicornlunaa.space_game.grid.tiles.LiquidTile;
import com.alicornlunaa.space_game.grid.tiles.SolidTile;
import com.alicornlunaa.space_game.grid.tiles.TileElement;
import com.alicornlunaa.space_game.grid.tiles.TileElement.Shape;
import com.alicornlunaa.space_game.grid.tiles.TileElement.State;

public class TileFactory {
    private static Element parseElement(String element) throws Exception {
        switch(element.toLowerCase()){
            case "sand":
                return Element.SAND;
                    
            case "stone":
                return Element.STONE;
            
            case "steel":
                return Element.STEEL;
                
            case "water":
                return Element.WATER;
            
            case "ethanol":
                return Element.ETHANOL;
                
            case "oxygen":
                return Element.OXYGEN;
            
            case "carbon_dioxide":
                return Element.CARBON_DIOXIDE;

            default:
                throw new Exception("Invalid element");
        }
    }
    
    private static State parseState(String state) throws Exception {
        switch(state.toLowerCase()){
            case "solid":
                return State.SOLID;
                    
            case "liquid":
                return State.LIQUID;
            
            case "gas":
                return State.GAS;
                
            case "plasma":
                return State.PLASMA;

            default:
                throw new Exception("Invalid state");
        }
    }

    private static Shape parseShape(String shape) throws Exception {
        switch(shape.toLowerCase()){
            case "square":
                return Shape.SQUARE;
                    
            case "slope":
                return Shape.SLOPE;

            default:
                throw new Exception("Invalid state");
        }
    }

    private static TileElement getTileElement(JSONObject obj) throws Exception {
        Element element = parseElement(obj.getString("element"));
        State state = parseState(obj.getString("state"));
        Shape shape = parseShape(obj.getString("shape"));
        TileElement tile = null;

        switch(state){
            case GAS:
                tile = new GasTile(element);
                break;
            case LIQUID:
                tile = new LiquidTile(element);
                break;
            case SOLID:
                tile = new SolidTile(element);
                break;
            default:
                break;
        }

        if(tile != null){
            tile.shape = shape;
            tile.temperature = obj.getFloat("temperature");
            tile.mass = obj.getFloat("mass");
            tile.floatingPosition.set(obj.getFloat("floating_position_x"), obj.getFloat("floating_position_y"));
            tile.velocity.set(obj.getFloat("velocity_x"), obj.getFloat("velocity_y"));
        }

        return tile;
    }

    public static AbstractTile unserialize(JSONObject obj) throws Exception {
        String tileID = obj.getString("tile_id");
        AbstractTile tile = null;

        if(tileID.startsWith("element_")){
            tile = getTileElement(obj);
        } else {
            switch(tileID){
                case "tile_ent_custom":
                    CustomTile customTile = new CustomTile(obj.getInt("rotation"));
                    tile = customTile;
                    break;
                    
                case "tile_ent_thruster":
                    ThrusterTile thrusterTile = new ThrusterTile(obj.getInt("rotation"));
                    thrusterTile.collider.setScale(1 / 64.f);
                    tile = thrusterTile;
                    break;

                default:
                    throw new Exception("Tile data unreadable " + tileID);
            }
        }

        if(tile != null){
            tile.x = obj.getInt("x_pos");
            tile.y = obj.getInt("y_pos");
            tile.rotation = obj.getInt("rotation");
        }

        return tile;
    }
}
