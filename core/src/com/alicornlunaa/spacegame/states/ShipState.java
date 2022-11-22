package com.alicornlunaa.spacegame.states;

public class ShipState {
    /** Contains a list of all the ship statistics and variables
     * such as fuel level, electricity level, thrust level, RCS,
     * SAS, and other instance based variables
     */
    public boolean debug = false; // Debug drawings
    public boolean rcs = false; // RCS thrusters
    public boolean sas = false; // Stability controller
    public float throttle = 0; // Thruster throttle
    public float roll = 0; // Rotational movement intention
    public float vertical = 0; // Translation movement intention
    public float horizontal = 0; // Translation movement intention
    public float artifRoll = 0; // Artificial roll, used by computer control

    public float rcsStored = 0.0f;
    public float rcsCapacity = 0.0f;
    public float liquidFuelStored = 0.0f;
    public float liquidFuelCapacity = 0.0f;
    public float batteryStored = 0.0f;
    public float batteryCapacity = 0.0f;
}