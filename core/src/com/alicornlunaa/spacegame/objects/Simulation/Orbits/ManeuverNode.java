package com.alicornlunaa.spacegame.objects.Simulation.Orbits;

import com.badlogic.gdx.math.Vector2;

/**
 * Maneuver nodes allow the player to experiment with orbital burns before
 * actually performing them
 */
public class ManeuverNode {

    // Variables
    private GenericConic targetConic; // Conic to attach the node to
    private Orbit predictedPath; // New path after maneuver

    private double placementMeanAnomaly; // Location on target conic
    private Vector2 deltaVelocity; // Velocity change on target conic

    // Constructor
    public ManeuverNode(GenericConic conic, double placementPoint, Vector2 deltaV){
        targetConic = conic;
        placementMeanAnomaly = placementPoint;
        deltaVelocity = deltaV;
    }

    // Functions
    
    
}
