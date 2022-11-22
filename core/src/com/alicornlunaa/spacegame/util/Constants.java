package com.alicornlunaa.spacegame.util;

public class Constants {

    public static final float TIME_STEP = 1/60.f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;
    public static final int MAX_PREDICTION_STEPS = 2;
    public static float TIME_WARP = 0;
    
    public static final int ORBIT_RESOLUTION = 128;
    
    public static final float PPM = 128.0f;
    public static final float PLANET_PPM = 128.0f;
    public static final float SHIP_PPM = 16.0f;

    public static final float GRAVITY_CONSTANT = 0.6f;
    public static final float DRAG_COEFFICIENT = 0.004f;

    public static boolean DEBUG = false;

}
