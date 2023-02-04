package com.alicornlunaa.spacegame.util;

public class Constants {

    // Physics constants
    public static final float TIME_STEP = 1/60.f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;
    public static final int MAX_PREDICTION_STEPS = 2;
    
    // Scaling constants
    public static final float PPM = 128.0f;
    public static final float PLANET_PPM = 128.0f;
    public static final float SHIP_PPM = 16.0f;

    // Math constants
    public static final float GRAVITY_CONSTANT = 10;//1.0f;
    public static final float DRAG_COEFFICIENT = 0.004f;
    public static final float CONVERSION_FACTOR = (1.f / 3.125f) * 0.2f;
    
    // Map constants
    public static final int ORBIT_RESOLUTION = 512;
    public static final int PATCHED_CONIC_LIMIT = 2;
    public static final int PATCHED_CONIC_STEPS = 256;
    public static final float MAP_VIEW_ZOOM_SENSITIVITY = 0.2f;
    public static final float MAP_VIEW_MIN_ZOOM = 1;
    public static final float MAP_VIEW_MAX_ZOOM = 3000;
    public static final float MAP_VIEW_SIMPLE_ICONS_CELESTIAL = 1000;
    public static final float MAP_VIEW_SIMPLE_ICONS_ENTS = 20;

    // Debug constants
    public static boolean DEBUG = true;

}
