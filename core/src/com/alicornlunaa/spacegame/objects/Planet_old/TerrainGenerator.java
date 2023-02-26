package com.alicornlunaa.spacegame.objects.Planet_old;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Planet_old.Tile.TileType;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.physics.box2d.Body;

public class TerrainGenerator {

    // Variables
    private final App game;
    private final OpenSimplexNoise noise;
    
    private int planetWidth = 10;  // Width in chunks
    private int planetHeight = 10; // Height in chunks
    private float radius;

    // Constructor
    public TerrainGenerator(final App game, long seed, int w, int h, float radius){
        this.game = game;
        noise = new OpenSimplexNoise(seed);

        planetWidth = w;
        planetHeight = h;
        this.radius = radius;
    }

    // Functions
    public int getWidth(){ return planetWidth; }
    public int getHeight(){ return planetHeight; }

    /**
     * Generates a tile for the supplied position
     * @param x Block coordinates world-space
     * @param y Block coordinates world-space
     * @return Tile generated with these settings
     */
    public Tile createTile(final Body chunkBody, int x, int y, int chunkX, int chunkY){
        TileType type = TileType.STONE;

        if(chunkX < 0 || chunkX > planetWidth - 1) return null;
        if(chunkY < 0 || chunkY > planetHeight - 1) return null;
        if(y > radius) return null;
        if(noise.eval(x / 10.0f, y / 10.0f) < -0.5f) return null;

        if(x < 1) type = TileType.DIRT;

        return new Tile(game, chunkBody, x, y, chunkX, chunkY, type);
    }
    
}
