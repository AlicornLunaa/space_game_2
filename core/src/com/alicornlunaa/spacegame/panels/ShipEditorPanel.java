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
    private Vector2 attachmentPoint;
    private int targetAttachmentId;

    private static final float BREAK_DISTANCE = 24;

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
                    if(selectedAttachment != null){
                        ghostPart.setPosition(0, 0);
                        selectedAttachment.getParent().attachPart(
                            ghostPart,
                            targetAttachmentId,
                            selectedAttachment.getThisId()
                        );
                    }

                    ghostPart = null;
                    selectedAttachment = null;
                    ui.selectedPart = "";
                }
            }
        });
    }

    // Functions
    public Vector2 findSnapAttachment(){
        // Finds the closest snap point, once a point is found dont run anymore
        ShipEditorUIPanel ui = ((EditorScene)game.getScreen()).uiPanel;
        Vector2 cursor = this.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

        if(ui.selectedPart.equals("") || ghostPart == null) return null; // Early exit if not part selected

        if(selectedAttachment == null){
            // No point selected, find snap point closest, only once
            Attachment shipClosestToCursor = rootShip.getClosestAttachment(new Vector2(cursor), 16);

            if(shipClosestToCursor != null){
                // Find attachment point on the ghostpart closest to the other attachment and snap them
                Vector2 attachmentPoint1 = shipClosestToCursor.getGlobalPos().add(rootShip.getX(), rootShip.getY());
                
                Attachment ghostClosestToAttachment = ghostPart.getClosestAttachment(attachmentPoint1);
                Vector2 attachmentPoint2 = ghostClosestToAttachment.getPos();
                this.attachmentPoint = attachmentPoint1.sub(attachmentPoint2);

                selectedAttachment = shipClosestToCursor;
                targetAttachmentId = ghostClosestToAttachment.getThisId();

                return this.attachmentPoint;
            }
        } else {
            // Snap point already found, check distance between the two points
            float snapToCursorDist = this.attachmentPoint.dst2(cursor);

            if(snapToCursorDist >= (BREAK_DISTANCE * BREAK_DISTANCE)){
                selectedAttachment = null;
                this.attachmentPoint = null;
            }
        }

        return null;
    }

    @Override
    public void act(float delta){
        super.act(delta);

        // Find the snap point for attachments
        ShipEditorUIPanel ui = ((EditorScene)game.getScreen()).uiPanel;

        if(!ui.selectedPart.equals("")){
            Vector2 cursor = this.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            this.findSnapAttachment();

            if(selectedAttachment != null){
                ghostPart.setPosition(attachmentPoint.x, attachmentPoint.y);
            } else {
                ghostPart.setPosition(cursor.x, cursor.y);
            }
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
            if(selectedAttachment != null){
                game.shapeRenderer.begin(ShapeType.Filled);
                game.shapeRenderer.setColor(Color.YELLOW);
                game.shapeRenderer.circle(attachmentPoint.x, attachmentPoint.y, 4);
                game.shapeRenderer.end();
            }
        }
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}
