package com.alicornlunaa.spacegame.phys;

import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Entity;
import com.alicornlunaa.spacegame.objects.Simulation.Celestial;
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
    public void onEntityUpdate(Entity e) {
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
