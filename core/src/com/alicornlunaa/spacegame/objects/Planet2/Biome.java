package com.alicornlunaa.spacegame.objects.Planet2;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

public class Biome {

    private static Array<Biome> biomes = new Array<>();

    private String name;
    private Color color;
    private Array<Float> frequencies = new Array<>();
    private Array<Float> amplitudes = new Array<>();

    public Biome(String name, Color color){
        this.name = name;
        this.color = color;
    }

    /**
     * Creates new simplex noise layer for the biome's terrain generation
     * @param frequency
     * @param amplitude
     */
    public void newLayer(float frequency, float amplitude){
        frequencies.add(frequency);
        amplitudes.add(amplitude);
    }

    public String getName(){
        return name;
    }
    
    public Color getColor(){
        return color;
    }

    public void generate(long seed){

    }

    public static Biome register(String name, Color color, float frequency, float ampltiude){
        Biome b = new Biome(name, color);
        b.newLayer(frequency, ampltiude);
        biomes.add(b);
        return b;
    }
    
    public static Array<Biome> getBiomes(){
        return biomes;
    }

}
