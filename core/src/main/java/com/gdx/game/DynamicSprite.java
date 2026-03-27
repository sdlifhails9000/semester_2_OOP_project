// TOBE AN ABSTRACT CLASS  (DONE)
//Made another abstract method named Update()
//This is because player movement updates in a different way while goblins updates in a different way

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
//import com.badlogic.gdx.math.Vector3;
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
abstract class DynamicSprite extends Sprite {
    // This will be the state that everything DynamicSprite is in
    enum State {
        IDLE,
        MOVING,
        ATTACK,
        EMOTE
    }

    // Data members
    protected float speed = 20f;  // -> Player Move Speed (based off above world size)

    // Declare Animation variables
    protected Animation<TextureRegion> runAnimation;
    protected Animation<TextureRegion> idleAnimation;

    // Currently used animation
    protected Animation<TextureRegion> currentAnimation;

    // Move this to Player Subclass, this is respawn position HARDCODED
    //Use them to define starting position for camera and sprite
    // Make getters?
    // public int startX;      // COMMENTED OUT BECAUSE MADE A CHILD CLASS
    // public int startY;

    protected Vector2 destVector;
    protected Vector2 moveTargetVector;     //Every subclass will define its own target in Update() method
    protected Vector2 attackTargetVector;
    protected Vector2 currentXY;     //Starting points (game World Coords not screen coords)  //IN CHILD CLASS NOW

    State state;

    // Creates the Sprite(Parent Class)
    DynamicSprite(Animation<TextureRegion> runAnimation, Animation<TextureRegion> idleAnimation , float stateTime, int startX, int startY, float speed) {
        super(idleAnimation.getKeyFrame(stateTime));
        this.idleAnimation = idleAnimation;
        this.runAnimation = runAnimation;
        // Add attack and death animation later

        this.speed = speed;
        this.currentXY = new Vector2(startX, startY);

        state = State.IDLE;
        currentAnimation = idleAnimation;
    }

    //Concrete methods (Getter and setter)

    // Only used when you click left click
    public void setMoveTarget(Vector2 clickCoords){
        this.moveTargetVector = new Vector2(clickCoords);
    }

    public Vector2 getMoveTarget(){
        return moveTargetVector;
    }

    // Only set when you click right click. Only set it if the position is near an enemy
    public void setAttackTarget(Vector2 clickCoords){
        this.attackTargetVector = new Vector2(clickCoords);
    }

    public Vector2 getAttackTarget(){
        return attackTargetVector;
    }

    // Get Postion
    abstract public Vector2 getPosition();

    //Method which calls updateMovement and passes correct targetVector
    //ALERT: I think this may cause issue later on when other subclasses are made so later make it a concrete which handles HeroPlayer by default and is overriden in the rest
    //abstract public void Update(Vector3 clickCoords, float stateTime, float delta);     //Made to handle updateMovement for different subclasses

    // Movement Method
    public void updateMovement(Vector2 targetVector, float stateTime , float delta){
        //For angle calculation to rotate sprite
        float angle;
    
        setRegion(runAnimation.getKeyFrame(stateTime));

        destVector = new Vector2();
        destVector.set(targetVector).sub(currentXY);        //Calculate the destination vector

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
            // Add clamping later
            this.setCenter(currentXY.x, currentXY.y);     //Update player position
        } else {
            moveTargetVector = null;
        }
        //No need for updating camera over here as its handled in cameraRoam otherwise it causes conflict between two

    }

    public void updateIdleAnimation(float stateTime) {
        setRegion(idleAnimation.getKeyFrame(stateTime));
    }

    public void setState(State state) {
        this.state = state;
    }

    abstract public void Update(State state, float stateTime, float delta);

}
//Child class number 1

class HeroPlayer extends DynamicSprite{
    // Later
    // enum Class {
    //     HEAVY,
    //     LIGHT,
    // }
    // final float HEAVY_SPEED, LIGHT_SPEED

    HeroPlayer(Animation<TextureRegion> runAnimation, Animation<TextureRegion> idleAnimation, float stateTime, int startX, int startY, float speed) {
        super(runAnimation, idleAnimation, stateTime, startX, startY, speed);
    }

    @Override
    public Vector2 getPosition(){
        return new Vector2(currentXY);
    }


    @Override
    public void Update(State state, float stateTime, float delta) {
        switch (state) {
        case MOVING:
            updateMovement(getMoveTarget(), stateTime, delta);
            break;

        case IDLE:
            updateIdleAnimation(stateTime);
            break;

        // case ATTACK:
        //     updateMovement();

        //     /*
        //     if (near an enemy) {
        //         updateAttack;
        //     } else {
        //         updateMovement;
        //     }
        //     */

        default:
            System.out.println("NOT HANDLED YET");
            break;
        }
    }

    // @Override
    // public void Update(Vector3 clickCoords, float stateTime, float delta){

    //     if (clickCoords != null){
    //         targetVector = new Vector2(clickCoords.x, clickCoords.y);
    //     }
    //     this.updateMovement(targetVector, stateTime, delta);

    // }
}