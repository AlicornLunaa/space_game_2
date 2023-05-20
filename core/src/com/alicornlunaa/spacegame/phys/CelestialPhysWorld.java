package com.alicornlunaa.spacegame.phys;

import com.alicornlunaa.selene_engine.core.BaseEntity;
import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.badlogic.gdx.Gdx;

public class CelestialPhysWorld extends PhysWorld {

    // Variables
    private final App game;
    private final Celestial parent;

    // Constructor
    public CelestialPhysWorld(final App game, final Celestial parent, float physScale) {
        super(physScale);
        this.game = game;
        this.parent = parent;
    }

    // Functions
    public Celestial getParent(){ return parent; }

    @Override
    public void onEntityUpdate(IEntity eRaw) {
        // Check keplerian approximation transfer
        BaseEntity e = (BaseEntity)eRaw;

        if(!(e instanceof Celestial))
            game.universe.checkTransfer(e);
        
        // Custom gravity update
        Celestial parent = game.universe.getParentCelestial(e);
        if(parent != null){
            e.getBody().applyForceToCenter(parent.applyPhysics(Gdx.graphics.getDeltaTime(), e), true);
        }
    }

}
