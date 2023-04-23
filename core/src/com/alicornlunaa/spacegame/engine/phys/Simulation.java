package com.alicornlunaa.spacegame.engine.phys;

import java.util.HashMap;

import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.badlogic.gdx.utils.Array;

/** Holds every PhysWorld and coordinates them */
public class Simulation {
    
    // Variables
    private Array<BaseEntity> entities = new Array<>();
    private Array<PhysWorld> physWorlds = new Array<>();
    private HashMap<BaseEntity, PhysWorld> containers = new HashMap<>();

    // Constructor
    public Simulation(){}

    // Functions
    public Array<BaseEntity> getEntities(){ return entities; }

    public PhysWorld getWorld(int index){ return physWorlds.get(index); }

    public PhysWorld addWorld(float physScale){
        physWorlds.add(new PhysWorld(physScale));
        return physWorlds.peek();
    }

    public PhysWorld addWorld(PhysWorld world){
        physWorlds.add(world);
        return physWorlds.peek();
    }

    public void addEntity(int index, BaseEntity e){ addEntity(physWorlds.get(index), e); }

    public void addEntity(PhysWorld world, BaseEntity e){
        if(!entities.contains(e, true)){
            // Initialize new entity
            entities.add(e);
            containers.put(e, world);
            world.getEntities().add(e);
            e.setWorld(world);
            return;
        }

        containers.get(e).getEntities().removeValue(e, true);
        containers.put(e, world);
        world.getEntities().add(e);
        e.setWorld(world);
    }

    public void update(){
        for(PhysWorld w : physWorlds){
            w.update();
        }
    }

}
