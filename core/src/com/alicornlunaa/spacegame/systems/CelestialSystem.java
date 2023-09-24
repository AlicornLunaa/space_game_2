package com.alicornlunaa.spacegame.systems;

import java.util.HashMap;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.ecs.ISystem;
import com.alicornlunaa.spacegame.components.CelestialComponent;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.objects.simulation.Universe;
import com.alicornlunaa.spacegame.scripts.GravityScript;

public class CelestialSystem implements ISystem {
    // Variables
    private Universe universe;
    private HashMap<IEntity, Celestial> oldParents = new HashMap<>();
    private HashMap<IEntity, Celestial> parents = new HashMap<>();

    // Constructor
    public CelestialSystem(Universe universe){
        this.universe = universe;
    }

    // Functions
    private boolean checkEnterSOI(IEntity entity){
        // Check whether or not this entity has a celestial parent or not
        Celestial entityParent = universe.getParentCelestial(entity); 

        if(parents.get(entity) != entityParent){
            // Transfer to new parent
            BodyComponent entityBodyComponent = entity.getComponent(BodyComponent.class);

            parents.put(entity, entityParent);
            
            Celestial oldEntityParent = oldParents.get(entity);
            if(oldEntityParent != null){
                // Old parent exists, remove from it
                CelestialComponent oldParentCelestialComponent = oldEntityParent.getComponent(CelestialComponent.class);
                BodyComponent oldParentBodyComponent = oldEntityParent.getComponent(BodyComponent.class);
                
                entityBodyComponent.body.setLinearVelocity(entityBodyComponent.body.getLinearVelocity().cpy().add(oldParentBodyComponent.body.getLinearVelocity()));
                entityBodyComponent.body.setTransform(entityBodyComponent.body.getPosition().cpy().add(oldParentBodyComponent.body.getPosition()), entityBodyComponent.body.getAngle());
                oldParentCelestialComponent.children.removeValue(entity, true);
            }

            if(entityParent != null){
                // New parent exists, remove from it
                CelestialComponent newParentCelestialComponent = entityParent.getComponent(CelestialComponent.class);
                BodyComponent newParentBodyComponent = entityParent.getComponent(BodyComponent.class);
                
                entityBodyComponent.body.setLinearVelocity(entityBodyComponent.body.getLinearVelocity().cpy().sub(newParentBodyComponent.body.getLinearVelocity()));
                entityBodyComponent.body.setTransform(entityBodyComponent.body.getPosition().cpy().sub(newParentBodyComponent.body.getPosition()), entityBodyComponent.body.getAngle());
                entityBodyComponent.setWorld(newParentCelestialComponent.influenceWorld);
                newParentCelestialComponent.children.add(entity);
            } else {
                // No new parent, add to the universal world
                entityBodyComponent.setWorld(universe.getUniversalWorld());
            }

            oldParents.put(entity, entityParent);
            return true;
        }

        return false;
    }

    // System functions
    @Override
    public void beforeUpdate() {}

    @Override
    public void afterUpdate() {}

    @Override
    public void update(IEntity entity) {
        // Runs on every celestial, checks entities to see if they can be
        // transferred to their sphere of influence physworld or leave it
        System.out.println(checkEnterSOI(entity));
    }

    @Override
    public void beforeRender() {}

    @Override
    public void afterRender() {}

    @Override
    public void render(IEntity entity) {}

    @Override
    public boolean shouldRunOnEntity(IEntity entity) {
        return entity.hasComponents(TransformComponent.class, BodyComponent.class, GravityScript.class);
    }
}
