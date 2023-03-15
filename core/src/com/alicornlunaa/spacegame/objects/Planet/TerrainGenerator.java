package com.alicornlunaa.spacegame.objects.Planet;

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
    private int width;
    private int height;
    private long seed;

    private Pixmap biomeMap;
    private float tempFrequency = 40;
    private float tempAmplitude = 1;
    private float humidityFrequency = 80;
    private float humidityAmplitude = 1;
    private float heightFrequency = 120;
    private float heightAmplitude = 1;

    // Functions
    private void generateBiomeMaps(){
        // Its width*width here because the biome on the 2d world is 1 dimensional.
        // the two dimensions here are only used for the space-view of the planet
        biomeMap = new Pixmap(width, width, Format.RGBA8888);

        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                float temp = (float)(noise.eval(x / tempFrequency, y / tempFrequency) + 1.f) / 2.f * tempAmplitude;
                float humidity = (float)(noise.eval(x / humidityFrequency, y / humidityFrequency) + 1.f) / 2.f * humidityAmplitude;
                float height = (float)(noise.eval(x / heightFrequency, y / heightFrequency) + 1.f) / 2.f * heightAmplitude;

                biomeMap.setColor(temp, humidity, height, 1.f);
                biomeMap.drawPixel(x, y);
            }
        }
    }
    
    private void classifyBiomes(){
        // Convert map colors to biomes
        for(int y = 0; y < width; y++){
            for(int x = 0; x < width; x++){
                Color val = new Color(biomeMap.getPixel(x, y));
                float temp = val.r;
                float humidity = val.g;
                float height = val.b;
                
                Biome biome = Biome.getBiome(temp, humidity, height);
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

        generateBiomeMaps();
        classifyBiomes();
    }

    public Pixmap getBiomeMap(){ return biomeMap; }
    
}
