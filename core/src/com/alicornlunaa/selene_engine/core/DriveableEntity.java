package com.alicornlunaa.selene_engine.core;

import com.alicornlunaa.selene_engine.components.BodyComponent;
import com.alicornlunaa.selene_engine.components.CameraComponent;
import com.alicornlunaa.selene_engine.components.TransformComponent;
import com.alicornlunaa.selene_engine.phys.PhysWorld;
import com.alicornlunaa.spacegame.App;
import com.alicornlunaa.spacegame.objects.Player;
import com.badlogic.gdx.utils.Null;

public abstract class DriveableEntity extends BaseEntity {

    // Variables
    @SuppressWarnings("unused")
    protected final App game;
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
        CameraComponent driverCamera = driver.getComponent(CameraComponent.class);
        CameraComponent shipCamera = this.getComponent(CameraComponent.class);

        if(bodyComponent != null){
            bodyComponent.body.setActive(false);
            bodyComponent.body.setLinearVelocity(0, 0);
        }

        if(driverCamera != null && shipCamera != null){
            driverCamera.active = false;
            shipCamera.active = true;
        }
    }

    public void stopDriving(){
        driver.setVehicle(null);

        TransformComponent driverTransform = driver.getComponent(TransformComponent.class);
        BodyComponent bodyComponent = driver.getComponent(BodyComponent.class);
        CameraComponent driverCamera = driver.getComponent(CameraComponent.class);
        CameraComponent shipCamera = this.getComponent(CameraComponent.class);

        if(bodyComponent != null){
            bodyComponent.body.setActive(true);
            bodyComponent.body.setLinearVelocity(getComponent(BodyComponent.class).body.getLinearVelocity());
            driverTransform.position.add(64, 0);
        }

        if(driverCamera != null && shipCamera != null){
            driverCamera.active = true;
            shipCamera.active = false;
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
