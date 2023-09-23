package com.alicornlunaa.selene_engine.components;

import com.alicornlunaa.selene_engine.ecs.IComponent;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets.Reloadable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderComponent implements IComponent, Reloadable {
    public String shaderName;
    public ShaderProgram program;

    public ShaderComponent(Assets manager, String name){
        this.shaderName = name;
        this.program = manager.get(name, ShaderProgram.class);
    }

    @Override
    public void reload(Assets manager) {
        this.program = manager.get(shaderName, ShaderProgram.class);
    }
}
