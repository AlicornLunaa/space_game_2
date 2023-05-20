package com.alicornlunaa.spacegame.objects.ship.interior;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.ship.Ship;
import com.alicornlunaa.spacegame.objects.ship.parts.Part;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;

/** Drawable class to render the inside of a ship
 * Will also store all user-created objects
 */
public class Interior {

    // Variables
    private final App game;
    private final Ship ship;
    private Array<InteriorCell> cells = new Array<>();
    private PhysWorld internalWorld;
    private Body internalBody;

    // Constructor
    public Interior(final App game, final Ship ship){
        // Construct interior cells based on the ship
        this.game = game;
        this.ship = ship;
        internalWorld = game.simulation.addWorld(Constants.SHIP_PPM);

        BodyDef def = new BodyDef();
		def.type = BodyType.StaticBody;
		internalBody = internalWorld.getBox2DWorld().createBody(def);
    }
    
    // Functions
    public void assemble(){
        for(InteriorCell cell : cells){
            cell.collider.detachCollider();
        }

        cells.clear();

        Array<Part> partsSorted = new Array<>(ship.getParts());
        partsSorted.sort();

        // Expand cells into each direction, adding based on cell count in each direction
        // Keep count for each direction, add each time
        int x = 0;
        int y = 0;
        Part prev = partsSorted.get(0);

        for(Part p : partsSorted){
            if(p.getInteriorSize() == 0) continue;
            int xDirection = (int)((p.getX() - prev.getX()) / Math.abs(p.getX() - prev.getX()));
            int yDirection = (int)((p.getY() - prev.getY()) / Math.abs(p.getY() - prev.getY()));
            
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

            cells.add(new InteriorCell(game, internalBody, x, y, false, false, false, false));
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
    
    public PhysWorld getWorld(){ return internalWorld; }

    public void draw(Batch batch){
        for(InteriorCell c : cells){
            c.draw(batch);
        }
    }
    
}
