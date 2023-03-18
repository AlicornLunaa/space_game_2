package com.alicornlunaa.spacegame.phys;

import java.util.HashMap;

import com.alicornlunaa.spacegame.objects.Entity;
import com.badlogic.gdx.utils.Array;

/** Holds every PhysWorld and coordinates them */
public class Simulation {
    
    // Variables
    private Array<Entity> entities = new Array<>();
    private Array<PhysWorld> physWorlds = new Array<>();
    private HashMap<Entity, PhysWorld> containers = new HashMap<>();

    // Constructor
    public Simulation(){}

    // Functions
    public Array<Entity> getEntities(){ return entities; }

    public PhysWorld getWorld(int index){ return physWorlds.get(index); }

    public PhysWorld addWorld(float physScale){
        physWorlds.add(new PhysWorld(physScale));
        return physWorlds.peek();
    }

    public PhysWorld addWorld(PhysWorld world){
        physWorlds.add(world);
        return physWorlds.peek();
    }

    public void addEntity(int index, Entity e){ addEntity(physWorlds.get(index), e); }

    public void addEntity(PhysWorld world, Entity e){
        if(!entities.contains(e, true)){
            // Initialize new entity
            entities.add(e);
            containers.put(e, world);
            world.getEntities().add(e);
            e.loadBodyToWorld(world, world.getPhysScale());
            return;
        }

        containers.get(e).getEntities().removeValue(e, true);
        containers.put(e, world);
        world.getEntities().add(e);
        e.loadBodyToWorld(world, world.getPhysScale());
    }

    public void update(){
        for(PhysWorld w : physWorlds){
            w.update();
        }
    }

}
