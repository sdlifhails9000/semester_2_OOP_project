//TODO:
//Add Attack range when constructing DynamicSprite and rewrite updateMovement to be resued in the updateAttack accordingly 

package com.gdx.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two

abstract class DynamicEntity extends Entity {

    //Variables for moveTarget
    protected Vector2 targetPosition;     //Every subclass will define its own target in Update() method
    protected float speed;
    protected Vector2 velocity;

    // Creates the Sprite(Parent Class)
    // TODO Pass the animations as a list and the other stuff as a list
    DynamicEntity(Animation<TextureRegion> dead,
                  Animation<TextureRegion> idle,
                  float startX, float startY,
                  float maxHealth,
                  float speed,
                  float spriteWidth,
                  float spriteHeight,
                  boolean isAlly) {

        super(
            dead, idle,
            startX, startY,
            maxHealth, spriteWidth,
            spriteHeight, isAlly
        );
        
        this.speed = speed;

        velocity = new Vector2();
        targetPosition = new Vector2(startX, startY);
    }

    //To be used in CHASE states (They calculate their input and get a position)
    void moveTowards(Vector2 Position, float delta){
        float angle;
        float destinationX = Position.x - currentXY.x;
        float destinationY = Position.y - currentXY.y;

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destinationY, destinationX);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        
        //-----NOTE----
        //you can add rotation or not your choice just uncomment the player.setRotation() lines to see it in play

        if (angle > 90 && angle < 270) { 
            //this.setRotation(angle + 180);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant 
            this.setFlip(true, false);       //Flip the x axis
        }
        else {
            //this.setRotation(angle);         //Same reason as above
            this.setFlip(false, false);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
        }

        velocity.x = destinationX;
        velocity.y = destinationY;
        
        //Prevents division by zero
        if(!velocity.isZero()){
            velocity.nor().scl(speed);
        }
    }

    boolean isCollidingWithEntity() {
        for (Entity i : entityList) {
            if (i == this) {
                continue;
            }

            if (i.currentXY.dst(this.currentXY) >= 20) {
                continue;
            }

            //Do not check collision between DEAD entities
            if (this.isDead || i.isDead){   
                continue;
            }

            Rectangle enemyHitBox = i.getCollisionBox();
            if (this.collisionBox.overlaps(enemyHitBox)) {
                System.out.println("Colliding");
                return true;
            }
        }

        return false;
    }

    void updateBoxes() {
        this.collisionBox.setCenter(currentXY);
        this.hitBox.setCenter(currentXY);
    }

    // ################### GETTERS SETTERS ###################

    void setTargetPosition(float x, float y) {
        targetPosition.x = x;
        targetPosition.y = y;
    }

    Vector2 getTargetPosition() {
        return targetPosition;
    }
}

// DynamicSprite is a subclass of Sprite with movement functonality