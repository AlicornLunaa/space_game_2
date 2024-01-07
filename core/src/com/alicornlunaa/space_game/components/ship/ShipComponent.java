package com.alicornlunaa.space_game.components.ship;

import com.badlogic.ashley.core.Component;

public class ShipComponent implements Component {
    // Variables
    public boolean controlEnabled = false;
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
