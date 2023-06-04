package com.alicornlunaa.spacegame.components;

import java.util.ArrayList;
import java.util.HashMap;

import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.objects.simulation.orbits.GenericConic;
import com.alicornlunaa.spacegame.objects.simulation.orbits.OrbitPropagator;

public class OrbitComponent implements IComponent {

    // Variables
    private Universe universe;
    private IEntity entity;

    private ArrayList<GenericConic> conics = new ArrayList<>();
    private HashMap<Celestial, GenericConic> celestialConics = new HashMap<>();

    public boolean visible = false;

    // Constructor
    public OrbitComponent(Universe universe, IEntity entity){
        this.universe = universe;
        this.entity = entity;
    }

    // Functions
    private void integrateCelestialConics(){
        for(Celestial child : universe.getCelestials()){
            celestialConics.put(child, OrbitPropagator.getConic(child.getCelestialParent(), child));
        }
    }

    
    
}
