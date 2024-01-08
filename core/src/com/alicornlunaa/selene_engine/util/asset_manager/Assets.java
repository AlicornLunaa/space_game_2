package com.alicornlunaa.selene_engine.util.asset_manager;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.ray3k.stripe.FreeTypeSkinLoader;

public class Assets extends AssetManager {
    // Inner classes
    @SuppressWarnings("all")
    public static class ShaderAssetLoader extends SynchronousAssetLoader<ShaderProgram, ShaderAssetLoader.ShaderParameters> {
        private static final String VERTEX_SHADER_FILE = "/vertex.vert";
        private static final String FRAGMENT_SHADER_FILE = "/fragment.frag";

        public ShaderAssetLoader(FileHandleResolver resolver) {
            super(resolver);
        }

        @Override
        public ShaderProgram load(AssetManager assetManager, String fileName, FileHandle file, ShaderParameters parameter) {
            final ShaderProgram shader = new ShaderProgram(resolve(fileName + VERTEX_SHADER_FILE), resolve(fileName + FRAGMENT_SHADER_FILE));

            if(shader.isCompiled() == false) {
                throw new IllegalStateException(shader.getLog());
            }
            
            return shader;
        }

        @Override
        public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ShaderParameters parameter) {
            return null;
        }

        public static class ShaderParameters extends AssetLoaderParameters<ShaderProgram> {}
    }

    @SuppressWarnings("all")
    public static class ParticleAssetLoader extends SynchronousAssetLoader<ParticleEffectPool, ParticleAssetLoader.ParticleParameters> {
        public ParticleAssetLoader(FileHandleResolver resolver){
            super(resolver);
        }

        @Override
        public ParticleEffectPool load(AssetManager assetManager, String fileName, FileHandle file, ParticleParameters parameter){
            final ParticleEffect particle = new ParticleEffect();
            particle.load(file, assetManager.get("particles_packed/particles.atlas", TextureAtlas.class, true));
            return new ParticleEffectPool(particle, 1, 64);
        }

        @Override
        public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ParticleParameters parameter) {
            Array<AssetDescriptor> dependencies = new Array<>();
            dependencies.add(new AssetDescriptor<>("particles_packed/particles.atlas", TextureAtlas.class));
            return dependencies;
        }

        public static class ParticleParameters extends AssetLoaderParameters<ParticleEffectPool> {}
    }

    @SuppressWarnings("all")
    public static class AsepriteAssetLoader extends SynchronousAssetLoader<AsepriteSheet, AsepriteAssetLoader.AsepriteParameters> {
        public AsepriteAssetLoader(FileHandleResolver resolver){
            super(resolver);
        }

        @Override
        public AsepriteSheet load(AssetManager assetManager, String fileName, FileHandle file, AsepriteParameters parameter){
            return new AsepriteSheet(file);
        }

        @Override
        public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AsepriteParameters parameter) {
            return null;
        }

        public static class AsepriteParameters extends AssetLoaderParameters<AsepriteSheet> {}
    }

    public static interface ILoader {
        void loadAssets(Assets manager);
    }

    public static interface Reloadable {
        void reload(Assets assets);
    }

    // Variables
    private ILoader loader;

    // Constructor
    public Assets(ILoader loader){
        super();

        setLoader(Skin.class, new FreeTypeSkinLoader(getFileHandleResolver()));
        setLoader(ShaderProgram.class, new ShaderAssetLoader(getFileHandleResolver()));
        setLoader(ParticleEffectPool.class, new ParticleAssetLoader(getFileHandleResolver()));
        setLoader(AsepriteSheet.class, new AsepriteAssetLoader(getFileHandleResolver()));

        // Load skin
        load("skins/spacecadet/spacecadet.json", Skin.class);
        load("skins/default/uiskin.json", Skin.class);
        finishLoading();

        // Initialize first queue
        this.loader = loader;
        loader.loadAssets(this);
    }

    // Functions
    public void reload(){
        this.clear();
        load("skins/spacecadet/spacecadet.json", Skin.class);
        load("skins/default/uiskin.json", Skin.class);
        finishLoading();

        // Initialize first queue
        loader.loadAssets(this);
        finishLoading();
    }
}
