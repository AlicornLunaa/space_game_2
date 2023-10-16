package com.alicornlunaa.spacegame.util;

public class Constants {
    // Physics constants
    public static final float TIME_STEP = 1/1024.f;
    public static final int VELOCITY_ITERATIONS = 8;
    public static final int POSITION_ITERATIONS = 4;
    public static final int MAX_PREDICTION_STEPS = 2;
    public static final int CHUNK_LOAD_DISTANCE = 3;
    
    // Scaling constants
    public static final float PPM = 128.0f;
    public static final float HITBOX_LINEUP_FACTOR = 0.02f;

    // Math constants
    public static final float GRAVITY_CONSTANT = 1.f;
    public static final float DRAG_COEFFICIENT = 0.004f;
    public static final float FPI = (float)Math.PI;
    
    // Map constants
    public static final int ORBIT_RESOLUTION = 512;
    public static final int PATCHED_CONIC_LIMIT = 8;
    public static final int PATCHED_CONIC_STEPS = 256;
    public static final float MAP_VIEW_ZOOM_SENSITIVITY = 0.2f;
    public static final float MAP_VIEW_MIN_ZOOM = 1;
    public static final float MAP_VIEW_MAX_ZOOM = 300000;
    public static final float MAP_VIEW_SIMPLE_ICONS_CELESTIAL = 1000;
    public static final float MAP_VIEW_SIMPLE_ICONS_ENTS = 20;

    // Terrain constants
    public static final int CHUNK_SIZE = 16;
    public static final float TILE_SIZE = 0.2f;

    // Debug constants
    public static boolean DEBUG = true;
}
