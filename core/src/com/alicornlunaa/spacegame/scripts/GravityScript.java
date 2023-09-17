package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.phys.CelestialPhysWorld;
import com.badlogic.gdx.Gdx;

public class GravityScript implements IScriptComponent {

    private App game;
    private IEntity entity;

    public GravityScript(App game, IEntity entity){
        this.game = game;
        this.entity = entity;
    }

    @Override
    public void update() {
        BodyComponent bc = entity.getComponent(BodyComponent.class);

        if(bc == null) return;
        if(!(bc.world instanceof CelestialPhysWorld)) return;
        
        Celestial parent = game.gameScene.universe.getParentCelestial(entity);

        if(parent != null && parent != entity){
            bc.body.applyForceToCenter(parent.applyPhysics(Gdx.graphics.getDeltaTime(), entity), true);
        }
    }

    @Override
    public void render() {}
    
}
