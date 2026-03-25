// TOBE AN ABSTRACT CLASS

package com.gdx.game;

//import java.util.ArrayList;

// import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
// import com.badlogic.gdx.graphics.GL20;
// import com.badlogic.gdx.Gdx;
// import com.badlogic.gdx.Input;
// import com.badlogic.gdx.Input.Keys;
//import com.badlogic.gdx.graphics.Texture;
// import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
// import com.badlogic.gdx.graphics.OrthographicCamera;
// import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
// import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
// import com.badlogic.gdx.utils.viewport.*;       //Gets all viewport types (FixViewport, StrectViewport, ExtendViewport, etc)

//Tiled Map Loaders
// import com.badlogic.gdx.maps.tiled.TiledMap;
// import com.badlogic.gdx.maps.tiled.TmxMapLoader;
// import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

// DynamicSprite is a subclass of Sprite with movement functonality
class DynamicSprite extends Sprite{

    // Data members
    public boolean movement = false;
    private float speed = 20f;  // -> Player Move Speed (based off above world size)
        // Declare Animation variables
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> idleAnimation;

        // Move this to Player Subclass, this is respawn position HARDCODED
    //Use them to define starting position for camera and sprite
    // Make getters?
    public int startX = 10;
    public int startY = 10;

    private Vector2 destVector;
    private Vector2 currentXY = new Vector2(startX, startY);     //Starting points (game World Coords not screen coords)


    // Creates the Sprite(Parent Class)
    DynamicSprite(Animation<TextureRegion> runAnimation, Animation<TextureRegion> idleAnimation , float stateTime) {
        super(idleAnimation.getKeyFrame(stateTime));
        this.idleAnimation = idleAnimation;
        this.runAnimation = runAnimation;
    }


    // Get Postion
    public Vector3 getPosition(){
        return new Vector3(currentXY,0);
    }

    // Movement Method
    public void updateMovement(Vector2 targetVector , float stateTime , float delta){
    
        //For angle calculation to rotate sprite
        float angle;

        if (!movement){      //Signifies no mouseclick yet
            this.setRegion(idleAnimation.getKeyFrame(stateTime));
            return;
         }
        else{
            this.setRegion(runAnimation.getKeyFrame(stateTime));
        }

        destVector = new Vector2();
        destVector.set(targetVector).sub(currentXY);

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destVector.y, destVector.x);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        
        //-----NOTE----
        //you can add rotation or not your choice just uncomment the player.setRotation() lines to see it in play

        if (angle > 90 && angle < 270){ 
            //player.setRotation(angle + 180);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant 
            this.setFlip(true, false);       //Flip the x axis
        }

        else{
            //player.setRotation(angle);         //Same reason as above
            this.setFlip(false, false);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
        }

        //-----ROTATION CALCULATION END-----

        if (destVector.len() > 0.5) {         //Previously it was 5 (from saad) which was too big for my gameWorld Coords so adjusted it
            destVector.nor().scl(delta*speed);    // Normalize then multiply(scale) by speed and delta time

            // Change current coordiantes
            currentXY.add(destVector);      //Updates the current vector co-ordiantes
            this.setCenter(currentXY.x, currentXY.y);     //Update player position
        }
        else{
            movement = false;
        }
        //No need for updating camera over here as its handled in cameraRoam otherwise it causes conflict between two


    }
}