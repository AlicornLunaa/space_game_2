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

    public static int SHIP_TRANSLATE_UP = Keys.W;
    public static int SHIP_TRANSLATE_DOWN = Keys.S;
    public static int SHIP_TRANSLATE_LEFT = Keys.A;
    public static int SHIP_TRANSLATE_RIGHT = Keys.D;
    public static int SHIP_ROLL_LEFT = Keys.Q;
    public static int SHIP_ROLL_RIGHT = Keys.E;
    public static int SHIP_INCREASE_THROTTLE = Keys.SHIFT_LEFT;
    public static int SHIP_DECREASE_THROTTLE = Keys.CONTROL_LEFT;
    public static int SHIP_TOGGLE_RCS = Keys.R;
    public static int SHIP_TOGGLE_SAS = Keys.T;

    public static int EDITOR_ROTATE = Keys.R;
    public static int EDITOR_FLIP_X = Keys.F;
    public static int EDITOR_FLIP_Y = Keys.C;

    public static int CONSOLE_OPEN = Keys.F1;
    public static int DEBUG_TOGGLE = Keys.F3;

    public static float GUI_SCALE = 1.f;

    public static void save(String filename){
        FileHandle file = Gdx.files.local(filename);

        JSONObject obj = new JSONObject();
        obj.put("SHIP_TRANSLATE_UP", SHIP_TRANSLATE_UP);
        obj.put("SHIP_TRANSLATE_DOWN", SHIP_TRANSLATE_DOWN);
        obj.put("SHIP_TRANSLATE_LEFT", SHIP_TRANSLATE_LEFT);
        obj.put("SHIP_TRANSLATE_RIGHT", SHIP_TRANSLATE_RIGHT);
        obj.put("SHIP_ROLL_LEFT", SHIP_ROLL_LEFT);
        obj.put("SHIP_ROLL_RIGHT", SHIP_ROLL_RIGHT);

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
            SHIP_TRANSLATE_UP = obj.getInt("MOVE_FORWARD");
            SHIP_TRANSLATE_DOWN = obj.getInt("MOVE_BACKWARD");
            SHIP_TRANSLATE_LEFT = obj.getInt("MOVE_LEFT");
            SHIP_TRANSLATE_RIGHT = obj.getInt("MOVE_RIGHT");

            SHIP_ROLL_LEFT = obj.getInt("ROLL_LEFT");
            SHIP_ROLL_RIGHT = obj.getInt("ROLL_RIGHT");
            
            GUI_SCALE = obj.getInt("GUI_SCALE");
        } catch(GdxRuntimeException|JSONException e){
            System.out.println("Error reading the control schema");
            e.printStackTrace();

            save(filename);
        }
    }

}
