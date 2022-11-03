package com.alicornlunaa.spacegame.util;

import java.util.HashMap;

import com.alicornlunaa.spacegame.App;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;

/*
 * Load all the files for the game here
 */
public class Assets extends AssetManager {

    // Variables
    public HashMap<String, ParticleEffectPool> effects = new HashMap<>();

    // Constructor
    public Assets(){
        super();

        // Load the texture atlases
        load("textures_packed/textures.atlas", TextureAtlas.class);
        load("particles_packed/particles.atlas", TextureAtlas.class);
    }

    // Functions
    private void addEffect(App game, String name){
        ParticleEffect effect = new ParticleEffect();
        effect.load(Gdx.files.internal("effects/rcs"), game.particleAtlas);
        effects.put(name, new ParticleEffectPool(effect, 1, 32));
    }

    public void initEffects(App game){
        // Load effects
        addEffect(game, "effects/rcs");
    }

    public PooledEffect getEffect(String name){
        return effects.get(name).obtain();
    }

}
