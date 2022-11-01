package com.alicornlunaa.spacegame.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.parts.*;
import com.alicornlunaa.spacegame.parts.ShipPart.Attachment;
import com.alicornlunaa.spacegame.scenes.EditorScene;

/*
 * The ShipEditor class is a stage used to render a window used to create
 * and edit 2d ships.
 */
public class ShipEditorPanel extends Stage {
    
    // Variables
    private final App game;

    private World world;
    public Body ghostBody; // Required for making a part w/o attaching it
    public Ship rootShip; // The ship being built
    public ShipPart ghostPart;

    private Attachment selectedAttachment;
    private int targetAttachmentId;

    // Constructor
    public ShipEditorPanel(final App game){
        super(new ScreenViewport());
        this.game = game;

        world = new World(new Vector2(0, 0), true);
        rootShip = new Ship(
            game.manager,
            game.partManager,
            world,
            0,
            0,
            0
        );
        this.addActor(rootShip);
        
        BodyDef def = new BodyDef();
		def.type = BodyType.KinematicBody;
		ghostBody = world.createBody(def);
        
        this.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                ShipEditorUIPanel ui = ((EditorScene)game.getScreen()).uiPanel;

                if(!ui.selectedPart.equals("")){
                    // Part is ghosted, spawn one and reset it
                    ghostPart.setPosition(0, 0);
                    selectedAttachment.getParent().attachPart(
                        ghostPart,
                        targetAttachmentId,
                        selectedAttachment.getThisId()
                    );

                    ghostPart = null;
                    ui.selectedPart = "";
                }
            }
        });
    }

    // Functions
    @Override
    public void act(float delta){
        super.act(delta);

        ShipEditorUIPanel ui = ((EditorScene)game.getScreen()).uiPanel;

        // Set the cursor object to the mouse if an object was selected
        if(!ui.selectedPart.equals("")){
            Vector2 pos = this.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            Attachment closest = rootShip.getClosestAttachment(new Vector2(pos), 16);

            if(closest != null && ghostPart != null){
                Vector2 attachmentPos = closest.getGlobalPos().add(rootShip.getX(), rootShip.getY());

                Attachment closest2 = ghostPart.getClosestAttachment(attachmentPos);
                Vector2 attachmentPos2 = closest2.getPos();

                pos.set(attachmentPos.x + attachmentPos2.x, attachmentPos.y + attachmentPos2.y);
                targetAttachmentId = closest2.getThisId();
                selectedAttachment = closest;
            }

            ghostPart.setPosition(pos.x, pos.y);
        }
    }

    @Override
    public void draw(){
        super.draw();

        ShipEditorUIPanel ui = ((EditorScene)game.getScreen()).uiPanel;

        if(!ui.selectedPart.equals("")){
            // Render the selected part
            getBatch().begin();
            ghostPart.draw(getBatch(), 255);
            getBatch().end();

            // Make it snap to other parts
            Vector2 pos = this.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            Attachment closest = rootShip.getClosestAttachment(new Vector2(pos), 16);

            if(closest != null && ghostPart != null){
                Vector2 attachmentPos = closest.getGlobalPos().add(rootShip.getX(), rootShip.getY());

                Attachment closest2 = ghostPart.getClosestAttachment(attachmentPos);
                Vector2 attachmentPos2 = closest2.getGlobalPos();

                game.shapeRenderer.begin(ShapeType.Filled);
                game.shapeRenderer.setColor(Color.YELLOW);
                game.shapeRenderer.circle(attachmentPos.x, attachmentPos.y, 4);
                game.shapeRenderer.setColor(Color.MAGENTA);
                game.shapeRenderer.circle(attachmentPos2.x, attachmentPos2.y, 4);
                game.shapeRenderer.end();
            }
        }
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}
