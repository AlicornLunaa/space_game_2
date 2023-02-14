package com.alicornlunaa.spacegame.objects.Planet2;

import java.util.Random;

import com.badlogic.gdx.utils.Array;

/**
 * Creates a scale to classify biomes from -1 to 1
 * Biome List:
 *  - Desert
 *  - Ocean
 *  - Forest
 *  - Plains
 */
public class BiomeClassifier {

    private Random random;
    private Array<Float> biomePercentages = new Array<>();
    private Array<Biome> biomeList = new Array<>();

    public BiomeClassifier(long seed){
        random = new Random(seed);

        Array<Biome> biomes = Biome.getBiomes();
        biomeList.addAll(biomes);

        float allocatedPercentage = 1.f;
        for(int i = 0; i < biomeList.size - 1; i++){
            float biomePercentage = allocatedPercentage * random.nextFloat();
            allocatedPercentage -= biomePercentage;
            biomePercentages.add(biomePercentage);
        }
        biomePercentages.add(allocatedPercentage);
        biomePercentages.sort();
    }

    public Biome getBiome(float v){
        for(int i = 0; i < biomeList.size - 1; i++){
            float percent = biomePercentages.get(i);
            
            if(v <= percent){
                return biomeList.get(i);
            }
        }

        return biomeList.peek();
    }
    
}
