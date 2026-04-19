package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

interface State<T extends Entity> {
    void enter(T e);
    void update(T e, float delta);
    void exit(T e);
}
//---------REAL HERO PLAYER STATES-----------
class HeroIdleState implements State<HeroPlayer> {
    @Override
    public void enter(HeroPlayer e) {
        e.currentAnimation = e.idleAnimation;
    }

    @Override
    public void update(HeroPlayer e, float delta) {
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.heroDeadState);
            return;
        }

        // if the target position and current position don't match, start moving to that target position
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.heroMoveState);
            return;
        }

        if(e.getAttackTarget() != null){
            e.setState(e.heroAttackState);
            return;
        }
    }

    @Override
    public void exit (HeroPlayer e){
        e.animationTimer = 0;
    }
}

class HeroMoveState implements State<HeroPlayer> {
    @Override
    public void enter(HeroPlayer e) {
        e.currentAnimation = e.runAnimation;
    }

    @Override
    public void update(HeroPlayer e, float delta) {
        System.out.println("Target Position is: " + e.getTargetPosition());

        //If the entity itself dies
        if (e.isDead){
            e.setState(e.heroDeadState);
            return;
        }

        //In case of rightClickEvent an attackTarget is generated
        if(e.getAttackTarget() != null){
            e.setState(e.heroAttackState);
            return;
        }

        // If the current position and target position are equal, to a certain degree, stop walking and go into the
        if (e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.heroIdleState);
            return;
        }

        // calculate speed and turn towards the target
        // Takes leftClick as input
        e.moveTowards(e.getTargetPosition(), delta);

        // Change position based on velocity

        // Change x
        e.currentXY.x += e.velocity.x * delta;
        e.updateBoxes();

        // Handle collision
        if (e.isCollidingWithEntity()) {
            e.currentXY.x -= e.velocity.x * delta;
            e.velocity.x = 0;
            e.updateBoxes();
        }

        // Change y
        e.currentXY.y += e.velocity.y * delta;
        e.updateBoxes();

        // Handle collision
        if (e.isCollidingWithEntity()) {
            e.currentXY.y -= e.velocity.y * delta;
            e.velocity.y = 0;
            e.updateBoxes();
        }

        if (e.velocity.isZero()) {
            e.setState(e.heroIdleState);
        }

        // Update collision and hitboxes and update the sprite position
        e.setCenter(e.currentXY.x, e.currentXY.y);
    }

    @Override
    public void exit(HeroPlayer e) {
        e.velocity.setZero();
        e.targetPosition.set(e.currentXY);
        e.animationTimer = 0;
    }
}

class HeroAttackState implements State<HeroPlayer> {
    public void enter(HeroPlayer e){
        e.currentAnimation = e.attackAnimation;
    }

    @Override
    public void update(HeroPlayer e, float delta){
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.heroDeadState);
            e.attackTarget = null;
            return;
        }

        //In case hero leftClicks a targetPosition is set and currentXY will obv not match it
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.heroMoveState);
            e.attackTarget = null;
            return;
        }

        //If the target is dead
        if (e.getAttackTarget().isDead) {
            e.setState(e.heroIdleState);
            e.attackTarget = null;
            return;
        }

        //If our hero is far from the enemy
        if (!e.isCloseToEnemy()){
            e.setState(e.heroChaseState);
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
    public void exit(HeroPlayer e) {
        e.animationTimer = 0;
    }
}

class HeroChaseState implements State<HeroPlayer> {

    public void enter(HeroPlayer e){
        e.currentAnimation = e.runAnimation;
    }

    public void update(HeroPlayer e, float delta){
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.heroDeadState);
            e.attackTarget = null;
            return;
        }

        //In case hero leftClicks a targetPosition is set and currentXY will obv not match it
        if (!e.currentXY.epsilonEquals(e.targetPosition, 0.5f)) {
            e.setState(e.heroMoveState);
            e.attackTarget = null;
            return;
        }

        //If the target is dead
        if (e.getAttackTarget().isDead) {
            e.setState(e.heroIdleState);
            e.attackTarget = null;
            return;
        }

        if (e.isCloseToEnemy()){
            e.setState(e.heroAttackState);
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
        if (e.isCollidingWithEntity()) {
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
        if (e.isCollidingWithEntity()) {
            e.currentXY.y -= e.velocity.y * delta;
            e.targetPosition.y -= e.velocity.y * delta;
            e.velocity.y = 0;
            e.updateBoxes();
        }

        if (e.velocity.isZero()) {
            e.setState(e.heroIdleState);
        }

        // Update collision and hitboxes and update the sprite position
        e.setCenter(e.currentXY.x, e.currentXY.y);
    }

    public void exit(HeroPlayer e){
        e.velocity.setZero();
        e.animationTimer = 0;
    }
}

class HeroDeadState implements State<HeroPlayer> {
    private float timer = 0f;
    private static final float MAX_RESPAWN_TIME = 5f; // In seconds btw
    private boolean isDying;
    private boolean isRespawing;

    public void enter (HeroPlayer e){
        e.currentAnimation = e.deadAnimation;
        e.currentAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        isDying = true;
        isRespawing = false;
    }

    public void update (HeroPlayer e, float delta) {
        if (isDying) {
            if (e.currentAnimation.isAnimationFinished(e.animationTimer)) {
                isDying = false;
            }
            return;
        }
        else if (isRespawing) {
            if (e.currentAnimation.isAnimationFinished(e.animationTimer)) {
                e.setState(e.heroIdleState);
            }
            return;
        }
        else if (timer < MAX_RESPAWN_TIME) {
            timer += delta;
            return;
        }

        if (Goblin.goblinList.size() > 5) {
            return;
        }

        e.currentHealth = e.maxHealth;
        e.isDead = false;

        float respawnPositionX = 20f;
        float respawnPositionY = 20f;

        e.setCurrentPosition(respawnPositionX, respawnPositionY);
        e.updateBoxes();

        if (e.isCollidingWithEntity()) {
            float respawnOffsetX = MathUtils.random(-100, 100);
            float respawnOffsetY = MathUtils.random(-100, 100);

            respawnPositionX = MathUtils.clamp(respawnPositionX + respawnOffsetX,
                e.getCollisionBox().getWidth()/2, 200f - e.getCollisionBox().getWidth()/2);
            respawnPositionY = MathUtils.clamp(respawnPositionY + respawnOffsetY,
                e.getCollisionBox().getHeight()/2, 200f - e.getCollisionBox().getHeight()/2);

            e.setCurrentPosition(respawnPositionX, respawnPositionY);
            e.updateBoxes();

            if (e.isCollidingWithEntity()) {
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

    public void exit (HeroPlayer e) {
        e.animationTimer = 0;
    }
}
