package com.alicornlunaa.spacegame.objects.Planet2;

import com.badlogic.gdx.utils.Array;

public class Biomes {

    private static class Biome {
        private String name;

        private Biome(String name){
            this.name = name;
        }
    }

    private static Array<Biome> biomes = new Array<>();

    public static Array<Biome> getBiomes(){
        return biomes;
    }

    public static void register(String name){
        biomes.add(new Biome(name));
    }

    public static void init(){
        register("Desert");
    }
    
}
