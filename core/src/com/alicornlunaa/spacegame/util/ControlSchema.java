package com.alicornlunaa.spacegame.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/*
 * The control schema allows for gets to get names and be rebound without
 * having to think of the keys in code.
 */
public class ControlSchema {

    public static int PAUSE_GAME = Keys.ESCAPE;

    public static int MOVE_FORWARD = Keys.W;
    public static int MOVE_BACKWARD = Keys.S;
    public static int MOVE_LEFT = Keys.A;
    public static int MOVE_RIGHT = Keys.D;
    public static int ROLL_LEFT = Keys.Q;
    public static int ROLL_RIGHT = Keys.E;

    public static int EDITOR_ROTATE = Keys.R;
    public static int EDITOR_FLIP_X = Keys.F;
    public static int EDITOR_FLIP_Y = Keys.C;

    public static float GUI_SCALE = 1.f;

    public static void save(String filename){
        FileHandle file = Gdx.files.local(filename);

        JSONObject obj = new JSONObject();
        obj.put("MOVE_FORWARD", MOVE_FORWARD);
        obj.put("MOVE_BACKWARD", MOVE_BACKWARD);
        obj.put("MOVE_LEFT", MOVE_LEFT);
        obj.put("MOVE_RIGHT", MOVE_RIGHT);
        obj.put("ROLL_LEFT", ROLL_LEFT);
        obj.put("ROLL_RIGHT", ROLL_RIGHT);
        obj.put("GUI_SCALE", GUI_SCALE);

        try {
            file.writeString(obj.toString(2), false);
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error saving the control schema");
            e.printStackTrace();
        }
    }

    public static void fromFile(String filename){
        FileHandle file = Gdx.files.local(filename);

        if(!file.exists()){
            ControlSchema.save(filename);
        }

        try {
            JSONObject obj = new JSONObject(file.readString());
            MOVE_FORWARD = obj.getInt("MOVE_FORWARD");
            MOVE_BACKWARD = obj.getInt("MOVE_BACKWARD");
            MOVE_LEFT = obj.getInt("MOVE_LEFT");
            MOVE_RIGHT = obj.getInt("MOVE_RIGHT");
            ROLL_LEFT = obj.getInt("ROLL_LEFT");
            ROLL_RIGHT = obj.getInt("ROLL_RIGHT");
            GUI_SCALE = obj.getInt("GUI_SCALE");
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error reading the control schema");
            e.printStackTrace();

            save(filename);
        }
    }

}
