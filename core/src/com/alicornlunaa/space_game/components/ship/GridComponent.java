package com.alicornlunaa.space_game.components.ship;

import org.json.JSONException;
import org.json.JSONObject;

import com.alicornlunaa.space_game.grid.Grid;
import com.alicornlunaa.space_game.grid.entities.CustomTile;
import com.alicornlunaa.space_game.grid.tiles.Element;
import com.alicornlunaa.space_game.grid.tiles.SolidTile;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GridComponent implements Component {
    // Variables
    public Grid grid = new Grid();

    // Constructor
    public GridComponent(){
        grid.setTile(1, 0, new CustomTile(1));
        grid.setTile(0, 1, new SolidTile(Element.STEEL));
        grid.setTile(0, 0, new SolidTile(Element.STEEL));
        grid.setTile(0, -1, new SolidTile(Element.STEEL));
        grid.setTile(0, -2, new SolidTile(Element.STEEL));
        grid.setTile(0, -3, new SolidTile(Element.STEEL));
        grid.setTile(0, -4, new SolidTile(Element.STEEL));
        grid.setTile(3, 1, new SolidTile(Element.STEEL));
        grid.setTile(3, 0, new SolidTile(Element.STEEL));
        grid.setTile(3, -1, new SolidTile(Element.STEEL));
        grid.setTile(3, -2, new SolidTile(Element.STEEL));
        grid.setTile(3, -3, new SolidTile(Element.STEEL));
        grid.setTile(3, -4, new SolidTile(Element.STEEL));
    }

    public GridComponent(String path){
        this();

        // Load grid from file
        try {
            // Read filedata
            FileHandle file = Gdx.files.local(path);
            JSONObject data = new JSONObject(file.readString());
        } catch (GdxRuntimeException|JSONException e){
            System.out.println("Error reading ship");
            e.printStackTrace();
        }
    }
}
