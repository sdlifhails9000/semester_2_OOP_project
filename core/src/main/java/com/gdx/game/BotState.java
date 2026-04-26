package com.gdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;


class BotIdleState implements State<Bot> {
    @Override
    public void enter(Bot e) {
        System.out.println("Entering idle state");
        e.currentAnimation = e.idleAnimation;       // starting animation as idle 
    }

    @Override
    public void update(Bot e, float delta) {
        // If the entity itself dies
        System.out.println(1);
        if (e.isDead){
            e.setState(e.BotDeadState);
            return;
        }

        // if the target position and current position don't match, start moving to that target position
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.BotMoveState);
            return;
        }

        if(e.getAttackTarget() != null){        // in case of finding attack target
            e.setState(e.BotAttackState);
            return;
        }
    }

    @Override
    public void exit (Bot e){            // exists the state
        e.animationTimer = 0;
    }
}


class BotMoveState implements State<Bot> {
    @Override
    public void enter(Bot e) {       // set current starting animation to idle
        e.currentAnimation = e.runAnimation;
    }

    @Override
    public void update(Bot e, float delta) {
        System.out.println("Target Position is: " + e.getTargetPosition());

        // If the entity itself dies
        if (e.isDead){
            e.setState(e.BotDeadState);
            return;
        }

        // In case of an attackTarget being found Bot attacks
        if(e.getAttackTarget() != null){
            e.setState(e.BotAttackState);
            return;
        }

        // If the current position and target position are equal, to a certain degree, stop walking and go into the idle state
        if (e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.BotIdleState);
            return;
        }

        // calculate speed and turn towards the target
        e.moveTowards(e.getTargetPosition(), delta);

        // Change position based on velocity 

        // Change x
        e.currentXY.x += e.velocity.x * delta;
        e.updateBoxes();

        // Handle collision;
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            e.currentXY.x -= e.velocity.x * delta;
            e.velocity.x = 0;
            e.updateBoxes();
        }

        // Change y
        e.currentXY.y += e.velocity.y * delta;
        e.updateBoxes();

        // Handle collision
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            e.currentXY.y -= e.velocity.y * delta;
            e.velocity.y = 0;
            e.updateBoxes();
        }

        if (e.velocity.isZero()) {
            e.setState(e.BotIdleState);          // for no movement set to idle state
        }

        // Update collision and hitboxes and update the sprite position
        e.setCenter(e.currentXY.x, e.currentXY.y);
    }

    @Override
    public void exit(Bot e) {        // exists the state
        e.velocity.setZero();
        e.targetPosition.set(e.currentXY);
        e.animationTimer = 0;
    }
}



class BotAttackState implements State<Bot> {
    public void enter(Bot e){
        e.currentAnimation = e.attackAnimation;         // set current starting animation to idle
    }

    @Override
    public void update(Bot e, float delta){
        // If the entity itself dies
        if (e.isDead){
            e.setState(e.BotDeadState);
            e.attackTarget = null;
            return;
        }

        // Movement of the Bot
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.BotMoveState);
            return;
        }

        //If the target is dead
        if (e.getAttackTarget().isDead) {
            e.setState(e.BotIdleState);
            e.attackTarget = null;
            return;
        }

        //If our hero is far from the enemy
        if (!e.isCloseToEnemy()){
            e.setState(e.BotChaseState);
            return;
        }

        // Check if we have passed the interval of attack and reset the timer
        if (e.attackTimer >= e.attackSpeed) {
            e.attackTarget.takeDamage(e.attackStrength);
            System.out.printf("Victim's Current Health... %f\n", e.attackTarget.currentHealth);
            e.attackTimer = 0;
        }

        // Calculate the angle and flip accordingly
        Vector2 displacement =  e.attackTarget.getCurrentPosition().cpy();
        displacement.sub(e.getCurrentPosition());

        float angle = MathUtils.atan2Deg360(displacement.y, displacement.x);

        if (angle > 90f && angle < 270f) {
            //this.setRotation(angle + 180);
            e.setFlip(true, false);
        } else {
            //this.setRotation(angle);
            e.setFlip(false, false);
        }

        e.attackTimer += delta;
    }

    @Override
    public void exit(Bot e) {
        e.animationTimer = 0;
    }
}

class BotChaseState implements State<Bot> {

    public void enter(Bot e){
        e.currentAnimation = e.runAnimation;
    }

    public void update(Bot e, float delta){
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.BotDeadState);
            e.attackTarget = null;
            return;
        }

        // MOVE NIGGA MOVE
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.BotMoveState);
            e.attackTarget = null;
            return;
        }

        //If the target is dead
        if (e.getAttackTarget().isDead) {
            e.setState(e.BotIdleState);
            e.attackTarget = null;
            return;
        }

        // if Bot is close to an enemy then go into attack state
        if (e.isCloseToEnemy()){
            e.setState(e.BotAttackState);
            return;
        }

        // calculate speed and turn towards the target
        e.moveTowards(e.attackTarget.getCurrentPosition(), delta);

        // Change position based on velocity
        // Change x
        e.currentXY.x += e.velocity.x * delta;
        e.targetPosition.x += e.velocity.x * delta;
        e.updateBoxes();

        // Handle collision
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            e.currentXY.x -= e.velocity.x * delta;
            e.targetPosition.x -= e.velocity.x * delta;
            e.velocity.x = 0;
            e.updateBoxes();
        }

        // Change y
        e.currentXY.y += e.velocity.y * delta;
        e.targetPosition.y += e.velocity.y * delta;
        e.updateBoxes();

        // Handle collision
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            e.currentXY.y -= e.velocity.y * delta;
            e.targetPosition.y -= e.velocity.y * delta;
            e.velocity.y = 0;
            e.updateBoxes();
        }

        // in case of no movement
        if (e.velocity.isZero()) {
            e.setState(e.BotIdleState);
        }

        // Update collision and hitboxes and update the sprite position
        e.setCenter(e.currentXY.x, e.currentXY.y);
    }

    public void exit(Bot e){
        e.velocity.setZero();
        e.animationTimer = 0;
    }
}


class BotDeadState implements State<Bot> {
    private float timer = 0f;
    private static final float MAX_RESPAWN_TIME = 5f; // In seconds btw
    private boolean isDying;
    private boolean isRespawing;

    public void enter (Bot e){
        e.currentAnimation = e.deadAnimation;
        e.currentAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        isDying = true;
        isRespawing = false;
    }

    public void update (Bot e, float delta) {
        if (isDying) {
            if (e.currentAnimation.isAnimationFinished(e.animationTimer)) {
                isDying = false;
            }
            return;
        }
        else if (isRespawing) {
            if (e.currentAnimation.isAnimationFinished(e.animationTimer)) {
                e.setState(e.BotIdleState);
            }
            return;
        }
        else if (timer < MAX_RESPAWN_TIME) {
            timer += delta;
            return;
        }

        if (Bot.BotList.size() > 5) {
            return;
        }

        e.currentHealth = e.maxHealth;
        e.isDead = false;

        float respawnPositionX = 20f;
        float respawnPositionY = 20f;

        e.setCurrentPosition(respawnPositionX, respawnPositionY);
        e.updateBoxes();

        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            float respawnOffsetX = MathUtils.random(-100, 100);
            float respawnOffsetY = MathUtils.random(-100, 100);

            respawnPositionX = MathUtils.clamp(respawnPositionX + respawnOffsetX,
                e.getCollisionBox().getWidth()/2, 200f - e.getCollisionBox().getWidth()/2);
            respawnPositionY = MathUtils.clamp(respawnPositionY + respawnOffsetY,
                e.getCollisionBox().getHeight()/2, 200f - e.getCollisionBox().getHeight()/2);

            e.setCurrentPosition(respawnPositionX, respawnPositionY);
            e.updateBoxes();
            
            if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
                return;
            }
        }

        e.setTargetPosition(respawnPositionX, respawnPositionY);
        e.setCenter(respawnPositionX, respawnPositionY);

        timer = 0f;
        isRespawing = true;
        e.currentAnimation.setPlayMode(Animation.PlayMode.REVERSED);
        e.animationTimer = 0f;
    }

    public void exit (Bot e) {
        e.animationTimer = 0;
    }
}