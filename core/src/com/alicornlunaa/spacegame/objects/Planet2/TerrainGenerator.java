package com.alicornlunaa.spacegame.objects.Planet2;

import java.util.HashMap;

import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Vector3;

/**
 * Generates the terrain on a planet
 */
public class TerrainGenerator {

    // Variables
    private HashMap<Vector3, Color> tiles = new HashMap<>();
    private OpenSimplexNoise noise;
    private BiomeClassifier classifier;
    private int width;
    private int height;
    private long seed;

    private Pixmap biomeMap;
    private float biomeAmplitude = 0.5f;
    private float biomeFrequency = 40.f;

    // Functions
    private void generateBiomeMap(){
        // Its width*width here because the biome on the 2d world is 1 dimensional.
        // the two dimensions here are only used for the space-view of the planet
        biomeMap = new Pixmap(width, width, Format.RGBA8888);

        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                float val = (float)(noise.eval(x / biomeFrequency, y / biomeFrequency) + 1.f) / 2.f * biomeAmplitude;

                biomeMap.setColor(val, val, val, 1.f);
                biomeMap.drawPixel(x, y);
            }
        }
    }
    
    private void classifyBiomes(){
        // Convert map colors to biomes
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                float val = new Color(biomeMap.getPixel(x, y)).r;
                Biome biome = classifier.getBiome(val);
                biomeMap.setColor(biome.getColor());
                biomeMap.drawPixel(x, y);
            }
        }
    }

    public TerrainGenerator(int width, int height, long seed){
        this.width = width;
        this.height = height;
        this.seed = seed;
        noise = new OpenSimplexNoise(seed);
        classifier = new BiomeClassifier(seed);

        generateBiomeMap();
        classifyBiomes();
    }

    public Pixmap getBiomeMap(){ return biomeMap; }
    
}
