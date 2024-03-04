package com.alicornlunaa.selene_engine.util;

import com.badlogic.gdx.Input.Keys;

public class DefaultControls {
    public static final String[] names = {
        "PAUSE_GAME",

        "SHIP_TRANSLATE_UP",
        "SHIP_TRANSLATE_DOWN",
        "SHIP_TRANSLATE_LEFT",
        "SHIP_TRANSLATE_RIGHT",
        "SHIP_ROLL_LEFT",
        "SHIP_ROLL_RIGHT",
        "SHIP_INCREASE_THROTTLE",
        "SHIP_DECREASE_THROTTLE",
        "SHIP_FULL_THROTTLE",
        "SHIP_NO_THROTTLE",
        "SHIP_TOGGLE_RCS",
        "SHIP_TOGGLE_SAS",

        "PLAYER_UP",
        "PLAYER_DOWN",
        "PLAYER_LEFT",
        "PLAYER_RIGHT",
        "PLAYER_ROLL_LEFT",
        "PLAYER_ROLL_RIGHT",
        "PLAYER_SPRINT",
        "PLAYER_NOCLIP",

        "OPEN_ORBITAL_MAP",

        "EDITOR_ROTATE",
        "EDITOR_FLIP_X",
        "EDITOR_FLIP_Y",

        "CONSOLE",
        "DEBUG_OVERLAY"
    };
    public static final int[] keys = {
        Keys.ESCAPE,
        
        Keys.W,
        Keys.S,
        Keys.A,
        Keys.D,
        Keys.Q,
        Keys.E,
        Keys.SHIFT_LEFT,
        Keys.CONTROL_LEFT,
        Keys.Z,
        Keys.X,
        Keys.R,
        Keys.T,
        
        Keys.W,
        Keys.S,
        Keys.A,
        Keys.D,
        Keys.Q,
        Keys.E,
        Keys.SHIFT_LEFT,
        Keys.V,
        Keys.M,

        Keys.R,
        Keys.F,
        Keys.C,

        Keys.F1,
        Keys.F3
    };
}
