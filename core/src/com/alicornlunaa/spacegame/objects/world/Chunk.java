package com.alicornlunaa.spacegame.objects.world;

import com.alicornlunaa.selene_engine.components.ActorComponent;
import com.alicornlunaa.selene_engine.ecs.Registry;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.components.PlanetComponent;
import com.alicornlunaa.spacegame.components.tiles.StaticTileComponent;
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

    // Private functions
    private void generateTiles(TerrainGenerator generator){
        for(int y = 0; y < Constants.CHUNK_SIZE; y++){
            for(int x = 0; x < Constants.CHUNK_SIZE; x++){
                final @Null BaseTile tile = generator.getTile(x + chunkX * Constants.CHUNK_SIZE, y + chunkY * Constants.CHUNK_SIZE);

                if(tile != null){
                    final StaticTileComponent staticTileComponent = tile.getComponent(StaticTileComponent.class);
                    final ActorComponent actorComponent = tile.getComponent(ActorComponent.class);
                    setTile(tile, x, y);

                    actorComponent.addListener(new ClickListener(){
                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor){
                            if(Gdx.input.isButtonPressed(Buttons.LEFT)){
                                PlanetComponent pc = App.instance.gameScene.planetViewPanel.getPlanet().getComponent(PlanetComponent.class);
                                pc.chunkManager.setTile(null, staticTileComponent.x, staticTileComponent.y);

                                ItemEntity newItem = new ItemEntity(
                                    pc.physWorld,
                                    staticTileComponent.x * Constants.TILE_SIZE,
                                    staticTileComponent.y * Constants.TILE_SIZE,
                                    "stone_square",
                                    1,
                                    64
                                );
                                App.instance.gameScene.registry.addEntity(newItem);
                                
                                chunkUpdate = true;
                            } else if(Gdx.input.isButtonPressed(Buttons.MIDDLE)){
                                BaseTile.convertToDynamic(registry, App.instance.gameScene.planetViewPanel.getPlanet().getComponent(PlanetComponent.class), tile); //! TODO: Garbage code, fix it later
                                chunkUpdate = true;
                            }
                        }
                    });
                }
            }
        }
    }

    // Constructor
    public Chunk(Registry registry, TerrainGenerator generator, int chunkX, int chunkY){
        // Slight performance save
        this.setTransform(false);
        this.registry = registry;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
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
