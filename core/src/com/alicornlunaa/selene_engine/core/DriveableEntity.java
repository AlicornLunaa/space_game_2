package com.alicornlunaa.selene_engine.core;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.badlogic.gdx.utils.Null;

public abstract class DriveableEntity extends BaseEntity {

    // Variables
    @SuppressWarnings("unused")
    private final App game;
    private @Null Player driver;

    // Constructor
    public DriveableEntity(final App game){
        super();
        this.game = game;
    }
    
    // Functions
    public Player getDriver(){ return driver; }

    public void drive(Player driver){
        this.driver = driver;
        driver.setVehicle(this);

        BodyComponent bodyComponent = driver.getComponent(BodyComponent.class);
        if(bodyComponent != null){
            bodyComponent.body.setActive(false);
            driver.setVelocity(0, 0);
        }
    }

    public void stopDriving(){
        driver.setPosition(driver.transform.position);
        driver.setVehicle(null);

        TransformComponent driverTransform = driver.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = driver.getComponent(BodyComponent.class);
        if(bodyComponent != null){
            bodyComponent.body.setActive(true);
            driverTransform.velocity.set(getComponent(TransformComponent.class).velocity);
        }
        
        driver = null;
    }
    
    public void afterWorldChange(PhysWorld world){
        // Carry the driver with the vehicle
        if(driver == null) return;

        BodyComponent bodyComponent = driver.getComponent(BodyComponent.class);
        if(bodyComponent != null){
            driver.bodyComponent.setWorld(world);
            bodyComponent.body.setActive(false);
        }
    }
    
}
