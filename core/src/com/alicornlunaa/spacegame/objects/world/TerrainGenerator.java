package com.alicornlunaa.spacegame.objects.world;

import java.util.HashMap;

import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.util.Constants;
import com.alicornlunaa.spacegame.util.OpenSimplexNoise;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Null;

/**
 * Generates the terrain on a planet
 */
@SuppressWarnings("unused")
public class TerrainGenerator {
    // Variables
    private HashMap<Vector3, Color> tiles = new HashMap<>();
    private PlanetComponent planetComponent;
    private OpenSimplexNoise noise;

    private Pixmap biomeMap;
    private float tempFrequency = 40;
    private float tempAmplitude = 1;
    private float humidityFrequency = 80;
    private float humidityAmplitude = 1;
    private float heightFrequency = 120;
    private float heightAmplitude = 1;

    // Private functions
    private void generateBiomeMaps(){
        // Its width*width here because the biome on the 2d world is 1 dimensional.
        // the two dimensions here are only used for the space-view of the planet
        biomeMap = new Pixmap(planetComponent.chunkWidth, planetComponent.chunkWidth, Format.RGBA8888);

        for(int y = 0; y < planetComponent.chunkWidth; y++){
            for(int x = 0; x < planetComponent.chunkWidth; x++){
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
        for(int y = 0; y < planetComponent.chunkWidth; y++){
            for(int x = 0; x < planetComponent.chunkWidth; x++){
                Color val = new Color(biomeMap.getPixel(x, y));
                float temp = val.r;
                float humidity = val.g;
                float height = val.b;
                
                Biome biome = Biome.getBiome(temp, humidity, height);
                if(biome == null){ biome = Biome.getBiomes().get(0); }
                
                biomeMap.setColor(biome.getColor());
                biomeMap.drawPixel(x, y);
            }
        }
    }

    // Constructor
    public TerrainGenerator(PlanetComponent planetComponent){
        this.planetComponent = planetComponent;

        noise = new OpenSimplexNoise(planetComponent.terrainSeed);
        generateBiomeMaps();
        classifyBiomes();
    }

    // Public functions
    public Pixmap getBiomeMap(){ return biomeMap; }

    public @Null Tile getTile(int chunkX, int chunkY, int x, int y){
        int globalX = x + chunkX * Constants.CHUNK_SIZE;
        int globalY = y + chunkY * Constants.CHUNK_SIZE;
        String type = "stone";

        // Heightmapping
        float heightNormalized = (float)globalY / planetComponent.chunkHeight / Constants.CHUNK_SIZE;
        float terrainColumnHeight = (float)(planetComponent.terrainRadius + 10 * noise.eval(globalX * 0.005f, 0));
        if((heightNormalized * planetComponent.atmosphereRadius) > terrainColumnHeight){
            return null;
        }
        
        float estimationHeight = (float)(globalY + 8) / planetComponent.chunkHeight / Constants.CHUNK_SIZE;
        if((estimationHeight * planetComponent.atmosphereRadius) > terrainColumnHeight){
            type = "dirt";
        }

        float caveFrequency = 0.11f;
        float fuckFrequency = 0.7f;
        if(((noise.eval(globalX * caveFrequency, globalY * caveFrequency) + 1) / 2.0) < 0.4 && ((noise.eval(globalX * fuckFrequency, globalY * fuckFrequency) + 1) / 2.0) < 0.95){
            return null;
        }

        Tile tile = new Tile(x, y, type);
        return tile;
    }
}
