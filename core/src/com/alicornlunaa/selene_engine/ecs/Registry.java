package com.alicornlunaa.selene_engine.ecs;


import com.alicornlunaa.selene_engine.core.IEntity;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets;
import com.alicornlunaa.selene_engine.util.asset_manager.Assets.Reloadable;
import com.alicornlunaa.spacegame.util.Constants;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class Registry implements Disposable, Reloadable {
    
    // Variables
    private Array<IEntity> entities = new Array<>();
    private Array<ISystem> systems = new Array<>();
    private float accumulator = 0.0f;

    // Functions
    public int addEntity(IEntity entity){
        entities.add(entity);
        return (entities.size - 1);
    }

    public IEntity getEntity(int entity){
        return entities.get(entity);
    }

    public IEntity removeEntity(int entity){
        return entities.removeIndex(entity);
    }

    public Array<IEntity> getEntities(){ return entities; }

    public <T extends ISystem> T registerSystem(T system){
        systems.add(system);
        return system;
    }

    public void update(float delta){
        accumulator += Math.min(delta, 0.25f);

        while(accumulator >= Constants.TIME_STEP){
            accumulator -= Constants.TIME_STEP;
            
            for(ISystem system : systems){
                system.beforeUpdate();

                for(IEntity entity : entities){
                    if(!system.shouldRunOnEntity(entity)) continue;
                    system.update(entity);
                }

                system.afterUpdate();
            }
        }
    }

    public void render(){
        for(ISystem system : systems){
            system.beforeRender();

            for(IEntity entity : entities){
                if(!system.shouldRunOnEntity(entity)) continue;
                system.render(entity);
            }

            system.afterRender();
        }
    }

    @Override
    public void dispose(){
        for(IEntity entity : entities){
            if(!(entity instanceof Disposable)) continue;
            ((Disposable)entity).dispose();
        }

        for(ISystem system : systems){
            if(!(system instanceof Disposable)) continue;
            ((Disposable)system).dispose();
        }
    }

    @Override
    public void reload(Assets assets) {
        assets.reload();

        for(IEntity entity : entities){
            if(!(entity instanceof Reloadable)) continue;
            ((Reloadable)entity).reload(assets);
        }

        for(ISystem system : systems){
            if(!(system instanceof Reloadable)) continue;
            ((Reloadable)system).reload(assets);
        }
    }

}
