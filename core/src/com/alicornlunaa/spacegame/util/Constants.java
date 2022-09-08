package com.alicornlunaa.spacegame.util;

import java.util.ArrayList;

public class Constants {
    public static final float TIME_STEP = 1/60.f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;
    public static final ArrayList<String> PART_CATEGORIES = new ArrayList<String>(){
        {
            add("Aero");
            add("Cargo");
            add("DataUtilities");
            add("EnvironmentControl");
            add("NuclearReactor");
            add("RCSPort");
            add("ReactionWheel");
            add("SolarPanel");
            add("Structural");
            add("Thruster");
        }
    };
}
