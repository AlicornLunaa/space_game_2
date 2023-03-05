package com.alicornlunaa.spacegame.objects.Planet2;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Blocks.Tile;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Holds all the tiles
 */
public class WorldBody extends Group {

    private Tile[][] tiles;

    public WorldBody(final App game, int width, int height){
        tiles = new Tile[width][height];

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                tiles[x][y] = new Tile(game, x, y, "stone");
                this.addActor(tiles[x][y]);
            }
        }
    }
    
}
