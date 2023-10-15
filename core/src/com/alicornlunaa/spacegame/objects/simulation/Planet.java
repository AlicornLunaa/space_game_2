package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.components.ShaderComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.components.PlanetSprite;
import com.badlogic.gdx.graphics.Color;

public class Planet extends Celestial {
    // Constructor
    public Planet(PhysicsSystem phys, PhysWorld world, float x, float y, float terraRadius, float atmosRadius, float atmosDensity) {
        super(phys, world, terraRadius, x, y);

        addComponent(new ShaderComponent(App.instance.manager, "shaders/atmosphere"));
        addComponent(new ShaderComponent(App.instance.manager, "shaders/planet"));
        addComponent(new ShaderComponent(App.instance.manager, "shaders/cartesian_atmosphere"));

        PlanetComponent planetComponent = addComponent(new PlanetComponent(this));
        // planetComponent.chunkHeight = (int)Math.floor(terraRadius / Tile.TILE_SIZE / Constants.CHUNK_SIZE);
        planetComponent.chunkHeight = 10;
        planetComponent.chunkWidth = (int)(2.0 * Math.PI * planetComponent.chunkHeight);
        planetComponent.terrainRadius = terraRadius;
        planetComponent.atmosphereRadius = atmosRadius;
        planetComponent.atmosphereDensity = atmosDensity;
        planetComponent.start();

        addComponent(new PlanetSprite(this, terraRadius, atmosRadius, Color.WHITE));
    }
}
