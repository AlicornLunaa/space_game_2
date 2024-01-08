package com.alicornlunaa.space_game.components.ship;

import com.alicornlunaa.space_game.grid.Grid;
import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GridComponent implements Component {
    // Variables
    public Grid grid = new Grid();

    // Constructor
    public GridComponent(){}

    public GridComponent(String path){
        this();

        // Load grid from file
        try {
            // Read filedata
            FileHandle file = Gdx.files.local(path);
            grid = Grid.unserialize(file.readBytes());
        } catch (GdxRuntimeException e){
            System.out.println("Error reading ship");
            e.printStackTrace();
        }
    }
}
