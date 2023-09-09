package com.alicornlunaa.spacegame.objects.ship.interior;

import org.json.JSONArray;

import com.alicornlunaa.selene_engine.phys.PhysicsCollider;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/** The interior is built of cells, cell size is in the part data in the JSON.
 * Allow the player to build interior walls and cosmetics inside
 */
public class InteriorCell {
    // Variables
    private final App game;
    private final Body body;

    protected int x = 0;
    protected int y = 0;
    protected PhysicsCollider collider;
    private TextureRegion texture;

    // Private functions
    private void createShapes(boolean up, boolean down, boolean left, boolean right){
        int connections = 0;
        connections += (up ? 1 : 0);
        connections += (down ? 1 : 0);
        connections += (left ? 1 : 0);
        connections += (right ? 1 : 0);

        if(connections == 0){
            texture = new TextureRegion(game.atlas.findRegion("interior/cell"));
            collider = new PhysicsCollider(new JSONArray(Gdx.files.internal("colliders/interior/cell_through.json").readString()));
        } else if(connections == 1){
            collider = new PhysicsCollider(new JSONArray(Gdx.files.internal("colliders/interior/cell_one.json").readString()));

            if(left){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_one_horizontal"));
                texture.flip(true, false);
                collider.setRotation(180);
            } else if(right){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_one_horizontal"));
                texture.flip(false, false);
                collider.setRotation(0);
            } else if(up){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_one_vertical"));
                texture.flip(false, true);
                collider.setRotation(90);
            } else if(down){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_one_vertical"));
                texture.flip(false, false);
                collider.setRotation(270);
            }
        } else if(connections == 2){
            // Either corner or wall
            if((up || down) && (left || right)){
                // Corner piece
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_corner"));
                collider = new PhysicsCollider(new JSONArray(Gdx.files.internal("colliders/interior/cell_corner.json").readString()));

                if(right && down){
                    texture.flip(false, false);
                    collider.setRotation(0);
                } else if(left && down){
                    texture.flip(true, false);
                    collider.setRotation(270);
                } else if(left && up){
                    texture.flip(true, true);
                    collider.setRotation(180);
                } else if(right && up){
                    texture.flip(false, true);
                    collider.setRotation(90);
                }
            } else {
                // Through piece
                collider = new PhysicsCollider(new JSONArray(Gdx.files.internal("colliders/interior/cell_through.json").readString()));

                if(left || right){
                    texture = new TextureRegion(game.atlas.findRegion("interior/cell_through_horizontal"));
                    collider.setRotation(0);
                } else {
                    texture = new TextureRegion(game.atlas.findRegion("interior/cell_through_vertical"));
                    collider.setRotation(90);
                }
            }
        } else if(connections == 3){
            // Wall piece
            collider = new PhysicsCollider(new JSONArray(Gdx.files.internal("colliders/interior/cell_wall.json").readString()));

            if(!up){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_wall_horizontal"));
                texture.flip(false, false);
                collider.setRotation(0);
            } else if(!down){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_wall_horizontal"));
                texture.flip(false, true);
                collider.setRotation(180);
            } else if(!left){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_wall_vertical"));
                texture.flip(false, false);
                collider.setRotation(90);
            } else if(!right){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_wall_vertical"));
                texture.flip(true, false);
                collider.setRotation(270);
            }
        } else if(connections == 4){
            texture = new TextureRegion(game.atlas.findRegion("interior/cell_all"));
            collider = new PhysicsCollider();
        }
    
        collider.setScale(1 / Constants.SHIP_PPM);
        collider.setPosition(new Vector2(x * texture.getRegionWidth(), y * texture.getRegionHeight()).scl(1 / Constants.SHIP_PPM));
        collider.attachCollider(body);
    }
    
    // Constructor
    public InteriorCell(final App game, final Body body, int x, int y, boolean up, boolean down, boolean left, boolean right){
        this.game = game;
        this.body = body;
        this.x = x;
        this.y = y;

        createShapes(up, down, left, right);
    }

    // Functions
    public void updateConnections(boolean up, boolean down, boolean left, boolean right){
        collider.detachCollider();
        createShapes(up, down, left, right);
    }

    public void draw(Batch batch){
        batch.draw(
            texture,
            x * texture.getRegionWidth() - texture.getRegionWidth() / 2,
            y * texture.getRegionHeight() - texture.getRegionHeight() / 2
        );
    }
}
