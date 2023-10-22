package com.alicornlunaa.spacegame.objects.world;

import java.util.HashMap;

import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.blocks.BaseTile;
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

    private float tempFrequency = 40;
    private float tempAmplitude = 1;
    private float humidityFrequency = 80;
    private float humidityAmplitude = 1;
    private float heightFrequency = 120;
    private float heightAmplitude = 0.3f;
    private float caveFrequency1 = 0.018f;
    private float caveFrequency2 = 0.08f;
    private float caveFrequency3 = 0.021f;
    
    private Pixmap biomeMap;

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

    private float getHeight(int x){
        float guaranteeHeight = (planetComponent.chunkHeight * Constants.CHUNK_SIZE) * (1.f - heightAmplitude); // Used to push up height if not being used
        return (float)(noise.eval(x / heightFrequency, 0.f) + 1) / 2.f * planetComponent.chunkHeight * Constants.CHUNK_SIZE * heightAmplitude + guaranteeHeight;
    }

    private boolean isCaveZone(int x, int y){
        boolean caveZone = ((noise.eval(x * caveFrequency1, y * caveFrequency1) + 1) / 2.0) < 0.6;
        boolean isCave = ((noise.eval(x * caveFrequency2, y * caveFrequency2) + 1) / 2.0) < 0.25;

        float modulatedFrequency = (y > (int)getHeight(x)-9) ? caveFrequency3 / 4 : caveFrequency3;
        float spindleVal = (float)noise.eval(x * modulatedFrequency, y * modulatedFrequency);
        boolean spindleCave = (spindleVal < 0.1 && spindleVal > -0.1);

        return (caveZone && isCave) || spindleCave;
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

    public @Null BaseTile getTile(int x, int y){
        // Starting values
        String type = "stone";
        float height = getHeight(x);

        // Heightmapping
        if(y > height)
            return null;

        // Surface generation
        if(y > (int)(height - 7))
            type = "dirt";

        if(y == (int)height)
            type = "grass";

        // Cave generation
        if(isCaveZone(x, y))
            return null;

        // Final output
        BaseTile tile = new BaseTile(type, x, y);
        return tile;
    }
}
