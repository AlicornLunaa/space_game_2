package com.alicornlunaa.space_game.util;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/*
 * This class will load all the parts
 */
public class PartManager {
    // Variables
    private HashMap<String, HashMap<String, JSONObject>> parts;

    // Constructor
    public PartManager(){
        parts = new HashMap<String, HashMap<String, JSONObject>>();
    }

    // Functions
    public void load(String filename){
        FileHandle file = Gdx.files.internal(filename);

        if(!file.exists()){
            System.out.println("Cannot read file: " + filename);
            return;
        }

        try {
            JSONArray arr = new JSONArray(file.readString());
            for(int i = 0; i < arr.length(); i++){
                // Part data obtained, add it to the hashmap
                JSONObject partData = arr.getJSONObject(i);
                String id = partData.getString("id");
                String type = partData.getString("type");

                // Add new hashmap category if it does not exist
                if(parts.get(type) == null){
                    parts.put(type, new HashMap<String, JSONObject>());
                }

                parts.get(type).put(id, partData);
                System.out.println("Loaded " + id);
            }
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Cannot read file: " + filename);
            e.printStackTrace();
        }
    }

    public JSONObject get(String type, String id){
        HashMap<String, JSONObject> category = parts.get(type);
        if(category == null) return null;
        return category.get(id);
    }

    public HashMap<String, HashMap<String, JSONObject>> getPartsList(){
        return parts;
    }
}
