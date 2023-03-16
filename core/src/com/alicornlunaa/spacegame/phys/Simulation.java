package com.alicornlunaa.spacegame.phys;

import com.badlogic.gdx.utils.Array;

/** Holds every PhysWorld and coordinates them */
public class Simulation {
    
    // Variables
    private Array<PhysWorld> physWorlds = new Array<>();

    // Constructor
    public Simulation(){
        // // Initialize world 0
        // physWorlds.add(new PhysWorld(Constants.PPM));
    }

    // Functions
    public PhysWorld getWorld(int index){ return physWorlds.get(index); }

    public PhysWorld addWorld(float physScale){
        physWorlds.add(new PhysWorld(physScale));
        return physWorlds.peek();
    }

    public PhysWorld addWorld(PhysWorld world){
        physWorlds.add(world);
        return physWorlds.peek();
    }

    public void update(){
        for(PhysWorld w : physWorlds){
            w.update();
        }
    }

}
