package com.alicornlunaa.selene_engine.util;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

/**
 * KeyBind is just a named Keys object.
 */

public class Inputs extends InputAdapter {
    // Variables
    public HashMap<Integer, Array<String>> binds = new HashMap<>();
    public HashMap<String, Integer> aliases = new HashMap<>();
    public HashMap<String, Boolean> keys = new HashMap<>();

    // Constructor
    public Inputs(){}

    public Inputs(JSONObject obj){
        this();

        for(String key : obj.keySet()){
            bind(key, Keys.valueOf(obj.getString(key)));
        }
    }

    // Functions
    @Override
    public boolean keyDown (int keycode) {
        if(binds.containsKey(keycode)){
            for(String s : binds.get(keycode)){
                keys.put(s, true);
            }
        }

		return false;
	}

    @Override
	public boolean keyUp (int keycode) {
        if(binds.containsKey(keycode)){
            for(String s : binds.get(keycode)){
                keys.put(s, false);
            }
        }
            
		return false;
	}

    public void bind(String name, int keyCode){
        if(keys.containsKey(name))
            return;

        if(binds.containsKey(keyCode)){
            binds.get(keyCode).add(name);
        } else {
            binds.put(keyCode, new Array<String>());
            binds.get(keyCode).add(name);
        }

        aliases.put(name, keyCode);
        keys.put(name, false);
    }

    public void unbind(String name){
        if(!keys.containsKey(name))
            return;

        Array<String> bindedKeys = binds.get(aliases.get(name));

        if(bindedKeys.size == 1){
            binds.remove(aliases.get(name));
        } else {
            bindedKeys.removeValue(name, false);
        }

        aliases.remove(name);
        keys.remove(name);
    }

    public boolean isKeyPressed(String name){
        return keys.getOrDefault(name, false);
    }

    public boolean isKeyJustPressed(String name){
        if(!keys.containsKey(name))
            return false;
        
        return Gdx.input.isKeyJustPressed(aliases.get(name));
    }

    public int getKeyBind(String name){
        return aliases.get(name);
    }

    public JSONObject serialize(){
        JSONObject obj = new JSONObject();

        for(Entry<Integer, Array<String>> bind : binds.entrySet()){
            for(String s : bind.getValue()){
                obj.put(s, Keys.toString(bind.getKey()));
            }
        }

        return obj;
    }

    public void load(FileHandle file){
        if(file == null)
            return;

        keys.clear();
        aliases.clear();
        binds.clear();

        JSONObject obj = new JSONObject(file.readString());

        for(String key : obj.keySet()){
            bind(key, Keys.valueOf(obj.getString(key)));
        }
    }
}
