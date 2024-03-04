package com.alicornlunaa.space_game.components.celestial;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.ashley.core.Component;

public class PlanetSprite implements Component {
    // // Variables
    // private SingleColorTextureComponent texture = new SingleColorTextureComponent();
    // private TransformComponent transform;
    // private ShaderComponent atmosShader;
    // private ShaderComponent terraShader;

    // private float planetRadius;
    // private float atmosRadius;
    // private Vector3 starDirection;
    // private Color color;

    // // Constructor
    // public PlanetSprite(IEntity entity, float planetRadius, float atmosRadius, Color color){
    //     transform = entity.getComponent(TransformComponent.class);

    //     ShaderComponent[] shaderComponents = entity.getComponents(ShaderComponent.class);
    //     atmosShader = shaderComponents[0];
    //     terraShader = shaderComponents[1];

    //     this.planetRadius = planetRadius;
    //     this.atmosRadius = atmosRadius;
    //     this.starDirection = new Vector3(1, 0, 0);
    //     this.color = color;
    // }

    // // Functions
    // @Override
    // public void render(Batch batch) {
    //     // Save rendering state
    //     Matrix4 proj = batch.getProjectionMatrix().cpy();
    //     Matrix4 invProj = proj.cpy().inv();
        
    //     // Shade the planet in
    //     batch.setShader(terraShader.program);
    //     terraShader.program.setUniformMatrix("u_invCamTrans", invProj);
    //     terraShader.program.setUniformf("u_planetWorldPos", transform.position);
    //     terraShader.program.setUniformf("u_starDirection", starDirection);
    //     terraShader.program.setUniformf("u_planetRadius", planetRadius);
    //     terraShader.program.setUniformf("u_planetColor", color);
    //     batch.draw(
    //         texture.texture,
    //         planetRadius * -1,
    //         planetRadius * -1,
    //         planetRadius * 2,
    //         planetRadius * 2
    //     );

    //     batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, 1280, 720));
    //     batch.setTransformMatrix(new Matrix4());

    //     batch.setShader(atmosShader.program);
    //     atmosShader.program.setUniformMatrix("u_invCamTrans", invProj);
    //     atmosShader.program.setUniformf("u_starDirection", starDirection);
    //     atmosShader.program.setUniformf("u_planetWorldPos", transform.position);
    //     atmosShader.program.setUniformf("u_planetRadius", planetRadius);
    //     atmosShader.program.setUniformf("u_atmosRadius", atmosRadius);
    //     atmosShader.program.setUniformf("u_atmosColor", color);
    //     batch.draw(texture.texture, 0, 0, 1280, 720);

    //     batch.setShader(null);
    // }
}
