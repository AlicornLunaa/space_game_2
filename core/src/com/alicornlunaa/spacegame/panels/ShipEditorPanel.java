package com.alicornlunaa.spacegame.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Ship;
import com.alicornlunaa.spacegame.parts.*;
import com.alicornlunaa.spacegame.parts.ShipPart.Attachment;
import com.alicornlunaa.spacegame.scenes.EditorScene;
import com.alicornlunaa.spacegame.util.ControlSchema;

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
    public Vector2 camOffset = new Vector2();

    private Attachment selectedAttachment;
    private Vector2 attachmentPoint;
    private int targetAttachmentId;

    private static final float BREAK_DISTANCE = 24;

    // Constructor
    public ShipEditorPanel(final App game){
        super(new FillViewport(360, 360));
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
        rootShip.drawPoints(true);
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

        this.addListener(new InputListener(){
            Vector2 prevDrag = new Vector2();

            @Override
            public boolean keyDown(InputEvent event, int keycode){
                ShipEditorUIPanel ui = ((EditorScene)game.getScreen()).uiPanel;

                if(!ui.selectedPart.equals("")){
                    if(keycode == ControlSchema.EDITOR_ROTATE){
                        ghostPart.rotateBy(45);
                        return true;
                    }

                    if(keycode == ControlSchema.EDITOR_FLIP_X){
                        ghostPart.flipX();
                        return true;
                    }

                    if(keycode == ControlSchema.EDITOR_FLIP_Y){
                        ghostPart.flipY();
                        return true;
                    }
                }

                return false;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                ShipEditorPanel editor = ((EditorScene)game.getScreen()).editorPanel;
                OrthographicCamera cam = ((OrthographicCamera)editor.getCamera());

                cam.zoom = Math.min(Math.max(cam.zoom + (amountY / 50), 0.1f), 2.f);

                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                prevDrag.set(x, y);
                return button == Buttons.RIGHT;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                ShipEditorPanel editor = ((EditorScene)game.getScreen()).editorPanel;
                OrthographicCamera cam = ((OrthographicCamera)editor.getCamera());
                
                Vector2 vel = new Vector2(x, y).sub(prevDrag).scl(-0.25f * (cam.zoom * 2));
                prevDrag.set(x, y);
                camOffset.add(vel);
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

            if(shipClosestToCursor != null && shipClosestToCursor.getChild() == null && !shipClosestToCursor.getInUse()){
                // Find attachment point on the ghostpart closest to the other attachment and snap them
                Vector2 attachmentPoint1 = shipClosestToCursor.getGlobalPos().add(rootShip.getX(), rootShip.getY());
                
                Attachment ghostClosestToAttachment = ghostPart.getClosestAttachment(attachmentPoint1);
                ShipPart p = ghostClosestToAttachment.getParent();
                Vector2 attachmentPoint2 = new Vector2(ghostClosestToAttachment.getPos());
                attachmentPoint2.scl(p.getFlipX() ? -1 : 1, p.getFlipY() ? -1 : 1);
                attachmentPoint2.rotateDeg(ghostPart.getRotation());

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

        // Center ship
        rootShip.setPosition(this.getWidth() / 2.0f, this.getHeight() / 2.0f);
    }

    @Override
    public void draw(){
        super.draw();

        ShipEditorUIPanel ui = ((EditorScene)game.getScreen()).uiPanel;

        if(!ui.selectedPart.equals("")){
            // Render the selected part
            Batch batch = getBatch();
            Color originalCol = new Color(batch.getColor());
            batch.begin();
            batch.setColor(selectedAttachment == null ? new Color(1, 1, 1, 0.45f) : new Color(0.5f, 1f, 0.5f, 0.95f));
            ghostPart.draw(batch, batch.getColor().a);
            batch.end();

            // Make it snap to other parts
            if(selectedAttachment != null){
                game.shapeRenderer.begin(ShapeType.Filled);
                game.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                game.shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
                game.shapeRenderer.setColor(new Color(1, 1, 0, batch.getColor().a));
                game.shapeRenderer.circle(
                    attachmentPoint.x - selectedAttachment.getPos().x,
                    attachmentPoint.y - selectedAttachment.getPos().y,
                    4
                );
                game.shapeRenderer.end();
            }
            
            batch.setColor(originalCol);
        }
    }

    @Override
    public void dispose(){
        super.dispose();
    }

}
