//TODO:
//Add Attack range when constructing DynamicSprite and rewrite updateMovement to be resued in the updateAttack accordingly 

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
import com.badlogic.gdx.math.Rectangle;
//import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

//import com.badlogic.gdx.math.Vector3;
// import com.badlogic.gdx.graphics.OrthographicCamera;
// import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
// import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
//import com.badlogic.gdx.graphics.g2d.Sprite;
// import com.badlogic.gdx.utils.viewport.*;       //Gets all viewport types (FixViewport, StrectViewport, ExtendViewport, etc)


//Tiled Map Loaders
// import com.badlogic.gdx.maps.tiled.TiledMap;
// import com.badlogic.gdx.maps.tiled.TmxMapLoader;
// import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

// DynamicSprite is a subclass of Sprite with movement functonality
abstract class DynamicEntity extends Entity {
    // This will be the state that everything DynamicSprite is in

    //Declare Animation Variables
    protected Animation<TextureRegion> runAnimation;

    protected Vector2 moveTargetVector;     //Every subclass will define its own target in Update() method
    protected float speed;

    protected static ArrayList<Entity> enemyList; // All enemies should be present in this list, list should be global,set in main>create()
    // The list could later be made into two seperate team lists


    // Creates the Sprite(Parent Class)
    // TODO Pass the animations as a list and the other stuff as a list
    DynamicEntity(Animation<TextureRegion> attack,
                  Animation<TextureRegion> run,
                  Animation<TextureRegion> dead,
                  Animation<TextureRegion> idle,
                  float stateTime, float startX, float startY,
                  float maxHealth,
                  float attackRange,
                  float attackSpeed,
                  float attackStrength,
                  float speed,
                  float spriteWidth,
                  float spriteHeight) {

        super(
            attack, dead, idle,
            stateTime, startX, startY,
            maxHealth, attackRange, attackSpeed, attackStrength,
            spriteWidth, spriteHeight
        );
        
        this.runAnimation = run;
        this.speed = speed;
    }

    //Concrete methods (Getter and setter)

    // Only used when you click left click
    public void setMove(Vector2 clickCoords){
        this.moveTargetVector = new Vector2(clickCoords);
    }

    public Vector2 getMove(){
        return moveTargetVector;
    }

    // Movement Method
    public void updateMovement(float stateTime , float delta){
        //For angle calculation to rotate sprite
        float angle;
    


        Vector2 destVector = new Vector2();
        
        destVector.set(moveTargetVector).sub(currentXY);        //Calculate the destination vector

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destVector.y, destVector.x);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        
        //-----NOTE----
        //you can add rotation or not your choice just uncomment the player.setRotation() lines to see it in play

        if (angle > 90 && angle < 270) { 
            //player.setRotation(angle + 180);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant 
            this.setFlip(true, false);       //Flip the x axis
        }
        else {
            //player.setRotation(angle);         //Same reason as above
            this.setFlip(false, false);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
        }

        //-----ROTATION CALCULATION END-----

        if (destVector.len() > 0.5) {         //ALERT: Add a check for bounding box aswell so if we click in open area it goes there using 0.5 otherwise by bounding box calculations
            Vector2 oldPosition = new Vector2(currentXY);   // Store old position before updating and checking collision
            destVector.nor().scl(delta * speed);    // Normalize then multiply(scale) by speed and delta time

            currentXY.add(destVector);      //Updates the current vector co-ordiantes
            this.setCenter(currentXY.x, currentXY.y);     //Update player position
            updateHitBox();

            if (checkEnemyCollision()) {
                currentXY.set(oldPosition);
                this.setCenter(oldPosition.x, oldPosition.y);   // Move back to old position
                // updateHitBox(); // Remake hitbox
                moveTargetVector = null;
            }
        } else if (destVector.len() <= 0.5) {
            moveTargetVector = null;
        }
        //No need for updating camera over here as its handled in cameraRoam otherwise it causes conflict between two

    }


    // Collision Detection ahead    
    // The Method is called in updateMovement()
    public boolean checkEnemyCollision(){
        if (this.hitBox == null) {
            updateHitBox();
        }

        for (Entity i : enemyList) {
            if (i == this) {
                continue;
            }

            if (i.currentXY.dst(this.currentXY) >= 20) {
                continue;
            }

            Rectangle enemyHitBox = i.getHitBox();
            if (enemyHitBox != null && this.hitBox.overlaps(enemyHitBox)) {
                System.out.println("Colliding");
                return true;
            }
        }

        return false;
    }

}
//Child class number 1

class HeroPlayer extends DynamicEntity {
    HeroPlayer(HeroPreset preset, float stateTime, int startX, int startY) {
        super(
            HeroLoader.attack(preset),
            HeroLoader.run(preset),
            HeroLoader.dead(preset),
            HeroLoader.idle(preset),
            stateTime, startX, startY,
            
            preset.maxHealth,
            preset.attackRange,
            preset.attackSpeed,
            preset.attackStrength,
            preset.speed,
            preset.spriteWidth,
            preset.spriteHeight
        );
    }

    @Override
    public Vector2 getPosition(){
        return new Vector2(currentXY);
    }

    @Override
    public void Update(float stateTime, float delta) {
        this.setRegion(currentAnimation.getKeyFrame(stateTime));    //Updates current Animation or you get slender man running

        if (state == State.DEAD) {
        }
        else if (moveTargetVector != null)  {
            state = State.MOVING;
            currentAnimation = runAnimation;
        }
        else if (getAttackInfo() != null) {
            state = State.ATTACK;
        }
        else {
            state = State.IDLE;    
            currentAnimation = idleAnimation;
        }
        //Modify the else if above to check for range and reuse updateMovement

        switch(state){
        case MOVING:
            updateMovement(stateTime, delta);
            break;

        case ATTACK:
            if (!isCloseToEnemy()) {
                setMove(getAttackInfo().getPosition());
            } else {
                updateAttack(delta);
                currentAnimation = attackAnimation;
            }
            break;

        default:
            break;
        }   
        updateHitBox();// Currently set here but should be set after some kind of changed boolean check
    }
}