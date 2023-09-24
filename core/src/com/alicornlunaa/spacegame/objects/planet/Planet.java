package com.alicornlunaa.spacegame.objects.planet;

import com.alicornlunaa.selene_engine.components.ShaderComponent;
import com.alicornlunaa.selene_engine.components.SingleColorTextureComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.selene_engine.systems.PhysicsSystem;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.CelestialComponent;
import com.alicornlunaa.spacegame.components.CustomSpriteComponent;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.objects.blocks.Tile;
import com.alicornlunaa.spacegame.objects.simulation.Celestial;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;

public class Planet extends Celestial {
    // Variables
    private SingleColorTextureComponent texture = addComponent(new SingleColorTextureComponent());
    private ShaderComponent atmosShader = addComponent(new ShaderComponent(App.instance.manager, "shaders/atmosphere"));
    private ShaderComponent terraShader = addComponent(new ShaderComponent(App.instance.manager, "shaders/planet"));

    // Constructor
    public Planet(PhysicsSystem phys, PhysWorld world, float x, float y, float terraRadius, float atmosRadius, float atmosDensity) {
        super(phys, world, terraRadius, x, y);

        CelestialComponent celestialComponent = getComponent(CelestialComponent.class);
        PlanetComponent planetComponent = addComponent(new PlanetComponent(this));
        planetComponent.chunkHeight = (int)Math.floor(celestialComponent.radius / Tile.TILE_SIZE / Constants.CHUNK_SIZE);
        planetComponent.chunkWidth = (int)(2.0 * Math.PI * planetComponent.chunkHeight);
        planetComponent.atmosphereRadius = atmosRadius;
        planetComponent.atmosphereDensity = atmosDensity;
        planetComponent.start();

        addComponent(new CustomSpriteComponent() {
            private TransformComponent transform = getComponent(TransformComponent.class);
            private CelestialComponent celestialComponent = getComponent(CelestialComponent.class);
            private PlanetComponent planetComponent = getComponent(PlanetComponent.class);
            private ShaderComponent atmosShader = Planet.this.atmosShader;
            private ShaderComponent terraShader = Planet.this.terraShader;

            @Override
            public void render(Batch batch) {
                // Save rendering state
                Matrix4 proj = batch.getProjectionMatrix().cpy();
                Matrix4 invProj = proj.cpy().inv();
                
                // Shade the planet in
                batch.setShader(terraShader.program);
                terraShader.program.setUniformMatrix("u_invCamTrans", invProj);
                terraShader.program.setUniformf("u_planetWorldPos", transform.position);
                terraShader.program.setUniformf("u_starDirection", planetComponent.starDirection);
                terraShader.program.setUniformf("u_planetRadius", celestialComponent.radius);
                batch.draw(
                    texture.texture,
                    celestialComponent.radius * -1,
                    celestialComponent.radius * -1,
                    celestialComponent.radius * 2,
                    celestialComponent.radius * 2
                );
                // worldBlocks.draw(batch, batch.getColor().a);

                batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 1280, 720));
                batch.setTransformMatrix(new Matrix4());

                batch.setShader(atmosShader.program);
                atmosShader.program.setUniformMatrix("u_invCamTrans", invProj);
                atmosShader.program.setUniformf("u_starDirection", planetComponent.starDirection);
                atmosShader.program.setUniformf("u_planetWorldPos", transform.position);
                atmosShader.program.setUniformf("u_planetRadius", celestialComponent.radius);
                atmosShader.program.setUniformf("u_atmosRadius", planetComponent.atmosphereRadius);
                atmosShader.program.setUniformf("u_atmosColor", planetComponent.getAtmosphereColor());
                batch.draw(texture.texture, 0, 0, 1280, 720);

                batch.setShader(null);
            }
        });
    }
}
