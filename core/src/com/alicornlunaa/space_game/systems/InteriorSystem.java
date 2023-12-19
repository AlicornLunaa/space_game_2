package com.alicornlunaa.space_game.systems;

import com.alicornlunaa.selene_engine.ecs.BodyComponent;
import com.alicornlunaa.selene_engine.phys.Collider;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.space_game.components.ship.InteriorComponent;
import com.alicornlunaa.space_game.components.ship.ShipComponent;
import com.alicornlunaa.space_game.components.ship.interior.InteriorCell;
import com.alicornlunaa.space_game.components.ship.parts.Part;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;

public class InteriorSystem extends EntitySystem {
    // Static classes
    private static class InteriorPhysicsListener implements EntityListener {
        // Variables
        private ComponentMapper<ShipComponent> sm = ComponentMapper.getFor(ShipComponent.class);
        private ComponentMapper<InteriorComponent> im = ComponentMapper.getFor(InteriorComponent.class);
        private ComponentMapper<BodyComponent> bm = ComponentMapper.getFor(BodyComponent.class);
        private InteriorSystem system;

        // Constructor
        public InteriorPhysicsListener(InteriorSystem system){
            this.system = system;
        }

        // Functions
        private void assembleInterior(ShipComponent shipComponent, InteriorComponent interior){
            for(InteriorCell cell : interior.cells){
                cell.getCollider().detach();
            }
    
            interior.cells.clear();
    
            Array<Part> partsSorted = new Array<>();
            shipComponent.rootPart.addParts(partsSorted);
            partsSorted.sort();
    
            // Expand cells into each direction, adding based on cell count in each direction
            // Keep count for each direction, add each time
            int x = 0;
            int y = 0;
            Part prev = partsSorted.get(0);
    
            for(Part p : partsSorted){
                if(p.getInteriorSize() == 0) continue;
                int xDirection = (int)((p.getPosition().x - prev.getPosition().x) / Math.abs(p.getPosition().x - prev.getPosition().x));
                int yDirection = (int)((p.getPosition().y - prev.getPosition().y) / Math.abs(p.getPosition().y - prev.getPosition().y));
                
                if(xDirection < 0){
                    x--;
                } else if(xDirection > 0) {
                    x++;
                }
                
                if(yDirection < 0){
                    y--;
                } else if(yDirection > 0) {
                    y++;
                }
    
                interior.cells.add(new InteriorCell(x, y, false, false, false, false));
                prev = p;
            }
        
            // Update the connections on each side
            for(int i = 0; i < interior.cells.size; i++){
                InteriorCell cell = interior.cells.get(i);
                boolean up = false;
                boolean down = false;
                boolean left = false;
                boolean right = false;
    
                // Find neighboring cells
                for(int j = 0; j < interior.cells.size; j++){
                    InteriorCell neighbor = interior.cells.get(j);
    
                    if(neighbor == cell) continue;
                    if(up && down && left && right) break;
    
                    if(!up && neighbor.getX() == cell.getX() && neighbor.getY() == cell.getY() + 1){
                        up = true;
                    }
                    if(!down && neighbor.getX() == cell.getX() && neighbor.getY() == cell.getY() - 1){
                        down = true;
                    }
                    if(!left && neighbor.getX() == cell.getX() - 1 && neighbor.getY() == cell.getY()){
                        left = true;
                    }
                    if(!right && neighbor.getX() == cell.getX() + 1 && neighbor.getY() == cell.getY()){
                        right = true;
                    }
                }
    
                cell.updateConnections(up, down, left, right);
            }
        }

        @Override
        public void entityAdded(Entity entity) {
            ShipComponent shipComp = sm.get(entity);
            InteriorComponent interiorComp = im.get(entity);
            BodyComponent bodyComp = bm.get(entity);

            bodyComp.body = system.world.getBox2DWorld().createBody(bodyComp.bodyDef);
            bodyComp.world = system.world;

            assembleInterior(shipComp, interiorComp);

            bodyComp.colliders.clear();
            for(InteriorCell cell : interiorComp.cells){
                bodyComp.colliders.add(cell.getCollider());
            }

            for(Collider collider : bodyComp.colliders){
                collider.attach(bodyComp.body);
            }
        }

        @Override
        public void entityRemoved(Entity entity) {
            // Remove the body from the world
            BodyComponent bodyComp = bm.get(entity);

            for(Collider collider : bodyComp.colliders){
                collider.detach();
            }
            
            system.world.getBox2DWorld().destroyBody(bodyComp.body);
            bodyComp.body = null;
            bodyComp.world = null;
        }
    }

    // Variables
    private ImmutableArray<Entity> entities;
    private PhysWorld world;

    // Constructor
    public InteriorSystem(PhysWorld world){
        super(2);
        this.world = world;
    }

    // Functions
    @Override
    public void addedToEngine(Engine engine){
        entities = engine.getEntitiesFor(Family.all(InteriorComponent.class).get());
        engine.addEntityListener(Family.all(ShipComponent.class, InteriorComponent.class, BodyComponent.class).get(), new InteriorPhysicsListener(this));
    }

    @Override
    public void update(float deltaTime){
        // Update every entity
        for(int i = 0; i < entities.size(); i++){
        }
    }
}
