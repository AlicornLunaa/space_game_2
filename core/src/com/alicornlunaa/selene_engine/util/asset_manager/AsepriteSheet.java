package com.alicornlunaa.selene_engine.util.asset_manager;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class AsepriteSheet implements Disposable {
    // Variables
    private Texture src;
    private Array<TextureRegion> frames = new Array<>();
    private HashMap<String, Array<TextureRegion>> taggedFrames = new HashMap<>();

    // Constructor
    public AsepriteSheet(FileHandle handle){
        // Load path and its frames
        JSONObject obj = new JSONObject(handle.readString());
        JSONObject meta = obj.getJSONObject("meta");
        JSONArray frames = obj.getJSONArray("frames");
        JSONArray frameTags = meta.getJSONArray("frameTags");
        String srcPath = meta.getString("image");

        src = new Texture(handle.sibling(srcPath));
        parseFrames(frames);
        parseTags(frameTags);
    }

    // Functions
    private void parseFrames(JSONArray framesList){
        for(int i = 0; i < framesList.length(); i++){
            JSONObject frameData = framesList.getJSONObject(i);
            JSONObject bounds = frameData.getJSONObject("frame");
            frames.add(new TextureRegion(src, bounds.getInt("x"), bounds.getInt("y"), bounds.getInt("w"), bounds.getInt("h")));
        }
    }

    private void parseTags(JSONArray tags){
        for(int i = 0; i < tags.length(); i++){
            Array<TextureRegion> arr = new Array<>();
            JSONObject tagData = tags.getJSONObject(i);
            String name = tagData.getString("name");
            int from = tagData.getInt("from");
            int to = tagData.getInt("from");

            for(int k = from; k <= to; k++){
                arr.add(frames.get(k));
            }

            taggedFrames.put(name, arr);
        }
    }

    @Override
    public void dispose() {
        src.dispose();
    }

    public TextureRegion getFrame(int index){
        return frames.get(index);
    }

    public TextureRegion getRegion(String name){
        return taggedFrames.get(name).get(0);
    }

    public Array<TextureRegion> getRegions(String name){
        return taggedFrames.get(name);
    }
}
