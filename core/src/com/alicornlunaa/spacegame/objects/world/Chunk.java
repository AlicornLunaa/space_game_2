package com.alicornlunaa.spacegame.objects.world;

import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Null;

public class Chunk extends Group {
    // Variables
    private Tile[][] tiles = new Tile[Constants.CHUNK_SIZE][Constants.CHUNK_SIZE];
    private boolean active = false;
    private int chunkX;
    private int chunkY;

    public boolean chunkUpdate = false;

    // Private functions
    private void generateTiles(TerrainGenerator generator){
        for(int y = 0; y < Constants.CHUNK_SIZE; y++){
            for(int x = 0; x < Constants.CHUNK_SIZE; x++){
                final @Null Tile tile = generator.getTile(chunkX, chunkY, x, y);

                if(tile != null){
                    tile.setBounds(
                        x * Tile.TILE_SIZE + chunkX * Constants.CHUNK_SIZE * Tile.TILE_SIZE,
                        y * Tile.TILE_SIZE + chunkY * Constants.CHUNK_SIZE * Tile.TILE_SIZE,
                        Tile.TILE_SIZE,
                        Tile.TILE_SIZE
                    );
                    tiles[x][y] = tile;
                    
                    addActor(tile);
                    tile.addListener(new ClickListener(){
                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                            if(!Gdx.input.isTouched(0)) return;
                            removeActor(tile);
                            tiles[tile.getTileX()][tile.getTileY()] = null;
                            chunkUpdate = true;
                        }
                    });
                }
            }
        }
    }

    // Constructor
    public Chunk(TerrainGenerator generator, PhysWorld world, int chunkX, int chunkY){
        // Slight performance save
        this.setTransform(false);
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        generateTiles(generator);
    }

    // Functions
    public boolean isLoaded(){
        return active;
    }

    public void setLoaded(boolean b){
        active = b;
    }

    public int getChunkX(){
        return chunkX;
    }

    public int getChunkY(){
        return chunkY;
    }

    public Tile getTile(int x, int y){
        if(x < 0 || x >= tiles.length) return null;
        if(y < 0 || y >= tiles[x].length) return null;
        return tiles[x][y];
    }
    
    @Override
    public void draw(Batch batch, float a){
        Matrix4 trans = batch.getTransformMatrix().cpy();
        batch.setTransformMatrix(trans.cpy().translate(chunkX * Constants.CHUNK_SIZE * Tile.TILE_SIZE, chunkY * Constants.CHUNK_SIZE * Tile.TILE_SIZE, 0));
        super.draw(batch, a);
        batch.setTransformMatrix(trans);
    }
}
