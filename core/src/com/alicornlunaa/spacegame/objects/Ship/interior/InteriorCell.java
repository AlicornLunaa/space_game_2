package com.alicornlunaa.spacegame.objects.Ship.interior;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** The interior is built of cells, cell size is in the part data in the JSON.
 * Allow the player to build interior walls and cosmetics inside
 */
public class InteriorCell {

    // Variables
    protected int x = 0;
    protected int y = 0;
    private TextureRegion texture;

    // Constructor
    public InteriorCell(final App game, int x, int y, boolean up, boolean down, boolean left, boolean right){
        this.x = x;
        this.y = y;
        
        int connections = 0;
        connections += (up ? 1 : 0);
        connections += (down ? 1 : 0);
        connections += (left ? 1 : 0);
        connections += (right ? 1 : 0);

        if(connections == 0){
            texture = new TextureRegion(game.atlas.findRegion("interior/cell"));
        } else if(connections == 1){
            if(left){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_one_horizontal"));
                texture.flip(false, false);
            } else if(right){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_one_horizontal"));
                texture.flip(true, false);
            } else if(up){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_one_vertical"));
                texture.flip(false, true);
            } else if(down){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_one_vertical"));
                texture.flip(false, false);
            }
        } else if(connections == 2){
            // Either corner or wall
            if((up || down) && (left || right)){
                // Corner piece
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_corner"));

                if(left && down){
                    texture.flip(false, true);
                } else if(right && down){
                    texture.flip(true, true);
                } else if(right && up){
                    texture.flip(true, false);
                }
            } else {
                // Through piece
                if(left || right){
                    texture = new TextureRegion(game.atlas.findRegion("interior/cell_through_horizontal"));
                } else {
                    texture = new TextureRegion(game.atlas.findRegion("interior/cell_through_vertical"));
                }
            }
        } else if(connections == 3){
            // Wall piece
            if(up){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_wall_horizontal"));
                texture.flip(false, true);
            } else if(down){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_wall_horizontal"));
                texture.flip(false, false);
            } else if(left){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_wall_vertical"));
                texture.flip(false, false);
            } else if(right){
                texture = new TextureRegion(game.atlas.findRegion("interior/cell_wall_vertical"));
                texture.flip(true, false);
            }
        } else if(connections == 4){
            texture = new TextureRegion(game.atlas.findRegion("interior/cell_all"));
        }
    }

    // Functions
    public void draw(Batch batch){
        batch.draw(
            texture,
            x * texture.getRegionWidth() - texture.getRegionWidth() / 2,
            y * texture.getRegionHeight() - texture.getRegionHeight() / 2
        );
    }
    
}
