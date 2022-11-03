package com.alicornlunaa.spacegame.states;

public class ShipState {
    /** Contains a list of all the ship statistics and variables
     * such as fuel level, electricity level, thrust level, RCS,
     * SAS, and other instance based variables
     */
    public boolean rcs = false;
    public boolean sas = false;
    public float throttle = 0;
    public float roll = 0;
}