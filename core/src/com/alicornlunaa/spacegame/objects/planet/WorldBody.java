package com.alicornlunaa.spacegame.objects.planet;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.planet.terrain.Chunk;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Group;

/**
 * Holds world chunks and manages which is loaded or not
 */
public class WorldBody extends Group {

    // Variables
    private Chunk[][] chunks;

    // Constructor
    public WorldBody(final App game, PhysWorld world, int width, int height){
        this.setTransform(false);
        chunks = new Chunk[width][height];

        // Generate everything as a test
        for(int y = 0; y < height; y++) for(int x = 0; x < width; x++){
            chunks[x][y] = new Chunk(game, world, x, y);
            this.addActor(chunks[x][y]);
        }
    }

    // Functions
    @Override
    public void draw(Batch batch, float a){
        Matrix4 trans = batch.getTransformMatrix().cpy();
        super.draw(batch, a);
        batch.setTransformMatrix(trans);
    }
    
}
