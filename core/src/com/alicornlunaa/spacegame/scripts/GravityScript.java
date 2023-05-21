package com.alicornlunaa.spacegame.scripts;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.IScriptComponent;
import com.alicornlunaa.selene_engine.core.BaseEntity;
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
        if(!entity.hasComponent(BodyComponent.class)) return;
        if(!(entity.getComponent(BodyComponent.class).world instanceof CelestialPhysWorld)) return;

        if(!(entity instanceof Celestial))
            game.universe.checkTransfer((BaseEntity)entity);
        
        Celestial parent = game.universe.getParentCelestial((BaseEntity)entity);
        if(parent != null){
            ((BaseEntity)entity).getBody().applyForceToCenter(parent.applyPhysics(Gdx.graphics.getDeltaTime(), (BaseEntity)entity), true);
        }
    }

    @Override
    public void render() {}
    
}
