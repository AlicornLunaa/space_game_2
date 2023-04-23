package com.alicornlunaa.spacegame.engine.phys;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.engine.core.BaseEntity;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.badlogic.gdx.Gdx;

public class CelestialPhysWorld extends PhysWorld {

    // Variables
    private final App game;

    // Constructor
    public CelestialPhysWorld(final App game, float physScale) {
        super(physScale);
        this.game = game;
    }

    // Functions
    @Override
    public void onEntityUpdate(BaseEntity e) {
        // Check keplerian approximation transfer
        if(!(e instanceof Celestial))
            game.universe.checkTransfer(e);
        
        // Custom gravity update
        Celestial parent = game.universe.getParentCelestial(e);
        if(parent != null){
            e.getBody().applyForceToCenter(parent.applyPhysics(Gdx.graphics.getDeltaTime(), e), true);
        }
    }

}
