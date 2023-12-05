package com.alicornlunaa.spacegame.objects.world;

import com.alicornlunaa.selene_engine.components_old.ActorComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.space_game.App;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.components.tiles.StaticTileComponent;
import com.alicornlunaa.spacegame.components.tiles.TileComponent;
import com.alicornlunaa.spacegame.objects.ItemEntity;
import com.alicornlunaa.spacegame.objects.blocks.BaseTile;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Null;

public class Chunk extends Group {
    // Variables
    private Registry registry;
    private int chunkX;
    private int chunkY;
    private BaseTile[][] tiles = new BaseTile[Constants.CHUNK_SIZE][Constants.CHUNK_SIZE]; // Tiles within this chunk
    public boolean chunkLoaded = false; // Whether or not the chunk is loaded
    public boolean chunkUpdate = false; // Whether or not to update the chunk

    private ClickListener defaultListener = new ClickListener(){
        @Override
        public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
            // Get variables
            BaseTile tile = ((BaseTile)((ActorComponent)event.getListenerActor()).getEntity());
            StaticTileComponent staticTileComponent = tile.getComponent(StaticTileComponent.class);

            // Inputs
            if(Gdx.input.isButtonPressed(Buttons.LEFT)){
                PlanetComponent pc = App.instance.gameScene.planetViewPanel.getPlanet().getComponent(PlanetComponent.class);
                pc.chunkManager.setTile(null, staticTileComponent.x, staticTileComponent.y);

                ItemEntity newItem = new ItemEntity(
                    pc.physWorld,
                    staticTileComponent.x * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.f,
                    staticTileComponent.y * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.f,
                    tile.getComponent(TileComponent.class).tileID + "_square",
                    1,
                    64
                );
                registry.addEntity(newItem);
                
                chunkUpdate = true;
            } else if(Gdx.input.isButtonPressed(Buttons.MIDDLE)){
                BaseTile newTile = BaseTile.convertToDynamic(registry, App.instance.gameScene.planetViewPanel.getPlanet().getComponent(PlanetComponent.class), tile);
                
                newTile.setEventListener(new ClickListener(){
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                        // Get variables
                        BaseTile newBaseTile = ((BaseTile)((ActorComponent)event.getListenerActor()).getEntity());
            
                        // Inputs
                        if(Gdx.input.isButtonPressed(Buttons.LEFT)){
                            BaseTile.convertToStatic(registry, App.instance.gameScene.planetViewPanel.getPlanet().getComponent(PlanetComponent.class), newBaseTile);
                            chunkUpdate = true;
                        }
                    }
                });

                chunkUpdate = true;
            }
        }
    };

    // Private functions
    private void generateTiles(TerrainGenerator generator){
        for(int y = 0; y < Constants.CHUNK_SIZE; y++){
            for(int x = 0; x < Constants.CHUNK_SIZE; x++){
                @Null BaseTile tile = generator.getTile(x + chunkX * Constants.CHUNK_SIZE, y + chunkY * Constants.CHUNK_SIZE);
                setTile(tile, x, y);

                if(tile != null)
                    tile.setEventListener(defaultListener);
            }
        }
    }

    // Constructor
    public Chunk(Registry registry, int chunkX, int chunkY){
        // Slight performance save
        this.setTransform(false);
        this.registry = registry;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }

    public Chunk(Registry registry, TerrainGenerator generator, int chunkX, int chunkY){
        this(registry, chunkX, chunkY);
        generateTiles(generator);
    }

    // Functions
    protected int getChunkX(){
        return chunkX;
    }

    protected int getChunkY(){
        return chunkY;
    }

    protected BaseTile getTile(int x, int y){
        if(x < 0 || x >= tiles.length) return null;
        if(y < 0 || y >= tiles[x].length) return null;
        return tiles[x][y];
    }

    protected boolean setTile(@Null BaseTile tile, int x, int y){
        if(x < 0 || x >= tiles.length) return false;
        if(y < 0 || y >= tiles[x].length) return false;

        BaseTile currentTile = getTile(x, y);

        if(currentTile != null){
            // Remove it from the current actor
            currentTile.getComponent(ActorComponent.class).remove();
        }

        if(tile != null){
            // Add to the current registry if its not null
            addActor(tile.getComponent(ActorComponent.class));
        }

        tiles[x][y] = tile;
        return true;
    }
}
