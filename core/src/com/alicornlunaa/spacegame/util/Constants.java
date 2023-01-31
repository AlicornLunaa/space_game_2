package com.alicornlunaa.spacegame.util;

public class Constants {

    public static final float TIME_STEP = 1/60.f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;
    public static final int MAX_PREDICTION_STEPS = 2;
    public static final int PATCHED_CONIC_LIMIT = 1;
    public static final int PATCHED_CONIC_STEPS = 256;
    
    public static final int ORBIT_RESOLUTION = 512;
    
    public static final float PPM = 128.0f;
    public static final float PLANET_PPM = 128.0f;
    public static final float SHIP_PPM = 16.0f;

    public static final float GRAVITY_CONSTANT = 1.0f;
    public static final float DRAG_COEFFICIENT = 0.004f;
    public static final float CONVERSION_FACTOR = (1.f / 3.125f) * 0.2f;

    public static boolean DEBUG = true;

}
