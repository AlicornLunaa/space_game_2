package com.alicornlunaa.spacegame.objects.Planet;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class Biome {

    private static Array<Biome> biomes = new Array<>();

    private String name;
    private Color color;
    private float minTemp = 0.f;
    private float minHumidity = 0.f;
    private float minHeight = 0.f;
    private Array<Float> frequencies = new Array<>();
    private Array<Float> amplitudes = new Array<>();

    public Biome(String name, Color color, float minTemp, float minHumidity, float minHeight){
        this.name = name;
        this.color = color;
        this.minTemp = minTemp;
        this.minHumidity = minHumidity;
        this.minHeight = minHeight;
    }

    /**
     * Creates new simplex noise layer for the biome's terrain generation
     * @param frequency
     * @param amplitude
     */
    public Biome newLayer(float frequency, float amplitude){
        frequencies.add(frequency);
        amplitudes.add(amplitude);
        return this;
    }

    public String getName(){ return name; }
    public Color getColor(){ return color; }
    public float getMinTemp(){ return minTemp; }
    public float getMinHumidity(){ return minHumidity; }
    public float getMinHeight(){ return minHeight; }
    public float getBiomeDifference(float minTemp, float minHumidity, float minHeight){ return (minHeight - this.minHeight) + (minHumidity - this.minHumidity) + (minTemp - this.minTemp); }
    public boolean validate(float temp, float humidity, float height){ return (temp > minTemp) && (humidity > minHumidity) && (height > minHeight); }

    public void generate(long seed){

    }

    public static Biome register(String name, Color color, float minTemp, float minHumidity, float minHeight, float frequency, float ampltiude){
        Biome b = new Biome(name, color, minTemp, minHumidity, minHeight);
        b.newLayer(frequency, ampltiude);
        biomes.add(b);
        return b;
    }
    
    public static Array<Biome> getBiomes(){
        return biomes;
    }

    public static Biome getBiome(float temp, float humidity, float height){
        Array<Biome> possibleBiomes = new Array<>();

        for(Biome b : biomes){
            if(b.validate(temp, humidity, height)){
                possibleBiomes.add(b);
            }
        }

        Biome closestMatch = null;
        float matchDiff = Float.MAX_VALUE;
        for(int i = 0; i < possibleBiomes.size; i++){
            Biome b = possibleBiomes.get(i);
            float diff = b.getBiomeDifference(temp, humidity, height);

            if(diff < matchDiff){
                closestMatch = b;
                matchDiff = diff;
            }
        }

        return closestMatch;
    }

}
