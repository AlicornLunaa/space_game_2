package com.alicornlunaa.spacegame.objects.simulation;

import com.alicornlunaa.selene_engine.components_old.ShaderComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.components.PlanetSprite;
import com.badlogic.gdx.graphics.Color;

public class Planet extends Celestial {
    // Constructor
    public Planet(Registry registry, PhysicsSystem phys, PhysWorld world, float x, float y, float terraRadius, float atmosRadius, float atmosDensity) {
        super(phys, world, terraRadius, x, y);

        addComponent(new ShaderComponent(App.instance.manager, "shaders/atmosphere"));
        addComponent(new ShaderComponent(App.instance.manager, "shaders/planet"));
        addComponent(new ShaderComponent(App.instance.manager, "shaders/cartesian_atmosphere"));
        addComponent(new PlanetComponent(registry, this, terraRadius, atmosRadius, atmosDensity, 10));
        addComponent(new PlanetSprite(this, terraRadius, atmosRadius, Color.WHITE));
    }
}
