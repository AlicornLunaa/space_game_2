package com.alicornlunaa.spacegame.objects.Planet2;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Blocks.Tile;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Holds all the tiles
 */
public class WorldBody extends Group {

    private Tile[][] tiles;
    private Body body;

    public WorldBody(final App game, World world, int width, int height){
        this.setTransform(false);

        PolygonShape shape = new PolygonShape();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.StaticBody;
        body = world.createBody(bodyDef);

        tiles = new Tile[width][height];

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                shape.setAsBox(Tile.TILE_SIZE / Constants.PLANET_PPM, Tile.TILE_SIZE / Constants.PLANET_PPM, new Vector2(x, y).scl(Tile.TILE_SIZE / Constants.PLANET_PPM), 0.f);
                body.createFixture(shape, 0.f);

                tiles[x][y] = new Tile(game, x, y, "stone");
                this.addActor(tiles[x][y]);
            }
        }

        shape.dispose();
    }
    
}
