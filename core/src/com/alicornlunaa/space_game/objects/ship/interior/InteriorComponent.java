package com.alicornlunaa.space_game.objects.ship.interior;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.space_game.components.ship.parts.Part;
import com.alicornlunaa.space_game.objects.ship.Ship;
import com.alicornlunaa.space_game.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;

// Component that allows for building a world within a ship
public class InteriorComponent {
    // Variables
    private final App game;
    private final Ship ship;
    private PhysWorld world;
    private Body body;
    private Array<InteriorCell> cells = new Array<>();

    // Constructor
    public InteriorComponent(final App game, final Ship ship){
        // super(ship);
        this.game = game;
        this.ship = ship;
        world = new PhysWorld(Constants.PPM);

        BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		body = world.getBox2DWorld().createBody(def);
    }

    // Functions
    public void assemble(){
        for(InteriorCell cell : cells){
            cell.collider.detachCollider();
        }

        cells.clear();

        Array<Part> partsSorted = new Array<>();
        ship.getRootPart().addParts(partsSorted);
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

            cells.add(new InteriorCell(game, body, x, y, false, false, false, false));
            prev = p;
        }
    
        // Update the connections on each side
        for(int i = 0; i < cells.size; i++){
            InteriorCell cell = cells.get(i);
            boolean up = false;
            boolean down = false;
            boolean left = false;
            boolean right = false;

            // Find neighboring cells
            for(int j = 0; j < cells.size; j++){
                InteriorCell neighbor = cells.get(j);

                if(neighbor == cell) continue;
                if(up && down && left && right) break;

                if(!up && neighbor.x == cell.x && neighbor.y == cell.y + 1){
                    up = true;
                }
                if(!down && neighbor.x == cell.x && neighbor.y == cell.y - 1){
                    down = true;
                }
                if(!left && neighbor.x == cell.x - 1 && neighbor.y == cell.y){
                    left = true;
                }
                if(!right && neighbor.x == cell.x + 1 && neighbor.y == cell.y){
                    right = true;
                }
            }

            cell.updateConnections(up, down, left, right);
        }
    }
    
    public PhysWorld getWorld(){
        return world;
    }

    public void draw(Batch batch){
        for(InteriorCell c : cells){
            c.draw(batch);
        }
    }

    // @Override
    // public void start(){}
    
	// @Override
	// public void update() {
    //     world.update();
    // }

	// @Override
	// public void render() {}
}
