package com.alicornlunaa.spacegame.util.asset_loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

@SuppressWarnings("all")
public class ShaderAssetLoader extends SynchronousAssetLoader<ShaderProgram, ShaderAssetLoader.ShaderParameters> {

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

    public static class ShaderParameters extends AssetLoaderParameters<ShaderProgram> {
    }

}
