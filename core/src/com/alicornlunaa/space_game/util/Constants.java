package com.alicornlunaa.space_game.util;

public class Constants {
    // Physics constants
    public static final float TIME_STEP = 1/1024.f;
    public static final int VELOCITY_ITERATIONS = 8;
    public static final int POSITION_ITERATIONS = 4;
    public static final int MAX_PREDICTION_STEPS = 2;
    public static final int CHUNK_LOAD_DISTANCE = 3;
    
    // Simulation constants
    public static final float MIN_FLUID_LEVEL = 0.01f;

    // Player constraints
    public static final float PLAYER_WIDTH = 0.2f;
    public static final float PLAYER_HEIGHT = 0.4f;
    public static final float PLAYER_MOVEMENT_SPEED = 0.4f;
    public static final float PLAYER_JUMP_FORCE = 60.f;
    public static final float PLAYER_ROLL_FORCE = 8.f;
    
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

    // Item constants
    public static final float ITEM_PICKUP_RANGE = 0.34f;

    // Debug constants
    public static boolean DEBUG = true;
}
