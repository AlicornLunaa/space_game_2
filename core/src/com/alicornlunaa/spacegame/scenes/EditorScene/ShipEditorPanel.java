package com.alicornlunaa.spacegame.scenes.EditorScene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
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
import com.alicornlunaa.spacegame.states.ShipState;
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
    public ShipPart hoveredPart;
    public ShipState ghostState = new ShipState();
    public Vector2 camOffset = new Vector2();

    private Vector2 partOffset = new Vector2();
    private Attachment selectedAttachment;
    private Vector2 attachmentPoint;
    private int targetAttachmentId;

    // Constructor
    public ShipEditorPanel(final App game){
        super(new FillViewport(360, 360));
        this.game = game;

        world = new World(new Vector2(0, 0), true);
        rootShip = new Ship(
            game,
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
                ShipEditorUIPanel ui = game.editorScene.uiPanel;
                ShipEditorPanel editor = game.editorScene.editorPanel;

                if(ghostPart != null){
                    // Part is ghosted, spawn one and reset it
                    if(selectedAttachment != null){
                        ghostPart.setPosition(0, 0);
                        selectedAttachment.getParent().attachPart(
                            ghostPart,
                            targetAttachmentId,
                            selectedAttachment.getThisId()
                        );
                    }

                    partOffset.set(0, 0);
                    ghostPart = null;
                    selectedAttachment = null;
                    ui.selectedPart = "";
                } else {
                    // No part was in the player's hand, check if theyre picking up an already placed object
                    Vector2 cursor = editor.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                    ShipPart part = rootShip.getPartClicked(cursor);

                    if(part != null){
                        // Pick it up by severing the attachment in use because in use means attached to parent and not a child
                        ShipPart parent = rootShip.findParent(part);

                        if(parent != null){
                            // A parent was found, detach the parent and the child
                            for(Attachment a : parent.getAttachments()){
                                if(a.getChild() == part){
                                    // This is the one, detach and break
                                    partOffset.set(part.getX(), part.getY());
                                    parent.detachPart(a.getThisId());
                                    ghostPart = part;
                                    break;
                                }
                            }
                        } else {
                            // No parent found, this is the root item.
                        }
                    }
                }
            }
        });

        this.addListener(new InputListener(){
            Vector2 prevDrag = new Vector2();

            @Override
            public boolean keyDown(InputEvent event, int keycode){
                if(ghostPart != null){
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

                cam.zoom = Math.min(Math.max(cam.zoom + (amountY / 50), 0.05f), 1.5f);

                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
                prevDrag.set(x, y);
                return button == Buttons.RIGHT;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer){
                Vector2 vel = new Vector2(x, y).sub(prevDrag).scl(-0.4f);
                camOffset.add(vel);
            }
        });
    }

    // Functions
    public Vector2 findSnapAttachment(Vector2 cursor){
        // Finds the closest snap point, once a point is found dont run anymore
        if(ghostPart == null) return null; // Early exit if not part selected

        float snapDistance = Math.max(ghostPart.getWidth(), ghostPart.getHeight());

        if(selectedAttachment == null){
            // No point selected, find snap point closest, only once
            Attachment shipClosestToCursor = rootShip.getClosestAttachment(cursor.cpy(), snapDistance);

            if(shipClosestToCursor != null && shipClosestToCursor.getChild() == null && !shipClosestToCursor.getInUse()){
                // Find attachment point on the ghostpart closest to the other attachment and snap them
                ShipPart a = shipClosestToCursor.getParent();
                Vector2 attachmentPoint1 = shipClosestToCursor.getPos().cpy();
                attachmentPoint1.scl(a.getFlipX() ? -1 : 1, a.getFlipY() ? -1 : 1);
                attachmentPoint1.rotateDeg(a.getRotation());
                attachmentPoint1.add(rootShip.getPosition()).add(a.getPosition()); // A1 is now local to the ship
                
                Attachment ghostClosestToAttachment = ghostPart.getClosestAttachment(attachmentPoint1.cpy());
                ShipPart p = ghostClosestToAttachment.getParent();
                Vector2 attachmentPoint2 = ghostClosestToAttachment.getPos().cpy();
                attachmentPoint2.scl(p.getFlipX() ? -1 : 1, p.getFlipY() ? -1 : 1);
                attachmentPoint2.rotateDeg(ghostPart.getRotation());
                attachmentPoint2.add(rootShip.getPosition()); // A2 is now local to the ship

                this.attachmentPoint = rootShip.getPosition().cpy().add(attachmentPoint1.sub(attachmentPoint2));

                selectedAttachment = shipClosestToCursor;
                targetAttachmentId = ghostClosestToAttachment.getThisId();

                return this.attachmentPoint;
            }
        } else {
            // Snap point already found, check distance between the two points
            float snapToCursorDist = this.attachmentPoint.dst2(cursor);
            float breakDistance = (float)Math.pow(snapDistance * 1.1, 2);

            if(snapToCursorDist >= breakDistance){
                selectedAttachment = null;
                this.attachmentPoint = null;
            }
        }

        return null;
    }

    @Override
    public void act(float delta){
        super.act(delta);

        Vector2 cursor = this.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        ShipPart part = rootShip.getPartClicked(cursor);

        // Find the snap point for attachments
        if(ghostPart != null){
            this.findSnapAttachment(cursor);

            if(selectedAttachment != null){
                ghostPart.setPosition(attachmentPoint.x, attachmentPoint.y);
            } else {
                ghostPart.setPosition(cursor.x, cursor.y);
            }
        }

        // Highlight selected parts light green
        if(part != null && part != hoveredPart){
            if(hoveredPart != null)
                hoveredPart.setColor(1, 1, 1, 1);

            part.setColor(0.7f, 1, 0.7f, 1);
            hoveredPart = part;
        } else if(part == null && hoveredPart != null) {
            hoveredPart.setColor(1, 1, 1, 1);
            hoveredPart = null;
        }

        // Center ship
        rootShip.setPosition(this.getWidth() / 2.0f, this.getHeight() / 2.0f);
    }

    @Override
    public void draw(){
        super.draw();

        if(ghostPart != null){
            // Render the selected part
            Vector2 oldPos = new Vector2(ghostPart.getX(), ghostPart.getY());
            Vector2 oldScl = new Vector2(ghostPart.getScaleX(), ghostPart.getScaleY());
            Matrix3 trans = ghostPart.getTransform();
            trans.translate(ghostPart.getOriginX(), ghostPart.getOriginY());
            trans.rotate(ghostPart.getRotation() * -1);
            trans.translate(-partOffset.x, -partOffset.y);
            ghostPart.setPosition(partOffset.x, partOffset.y);
            ghostPart.setScale(1, 1);

            Batch batch = getBatch();
            Color originalCol = new Color(batch.getColor());
            batch.begin();
            batch.setTransformMatrix(new Matrix4().set(trans));
            batch.setColor(selectedAttachment == null ? new Color(1, 1, 1, 0.45f) : new Color(0.5f, 1f, 0.5f, 0.95f));
            ghostPart.draw(batch, batch.getColor().a);
            batch.end();

            ghostPart.setPosition(oldPos.x, oldPos.y);
            ghostPart.setScale(oldScl.x, oldScl.y);

            if(selectedAttachment != null){
                game.shapeRenderer.begin(ShapeType.Filled);
                game.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
                game.shapeRenderer.setTransformMatrix(new Matrix4().translate(oldPos.x, oldPos.y, 0));
                game.shapeRenderer.setColor(new Color(1, 1, 0, batch.getColor().a));
                game.shapeRenderer.circle(
                    -selectedAttachment.getPos().x,
                    -selectedAttachment.getPos().y,
                    2
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