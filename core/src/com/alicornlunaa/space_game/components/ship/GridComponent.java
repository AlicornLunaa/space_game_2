package com.alicornlunaa.space_game.components.ship;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GridComponent implements Component {
    // Variables
    

    // Constructor
    public GridComponent(){}

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
