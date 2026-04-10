package com.gdx.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

interface State {
    void enter(Entity e);
    void update(Entity e, float delta);
    void exit(Entity e);
}
//---------REAL HERO PLAYER STATES-----------
class HeroIdleState implements State {
    @Override
    public void enter(Entity e) {
        HeroPlayer _e = (HeroPlayer) e;
        _e.currentAnimation = _e.idleAnimation;
    }

    @Override
    public void update(Entity e, float delta) {
        HeroPlayer _e = (HeroPlayer)e;

        //If the entity itself dies
        if (_e.isDead){
            _e.setState(_e.heroDeadState);
            return;
        }

        // if the target position and current position don't match, start moving to that target position
        if (!_e.currentXY.epsilonEquals(_e.targetPosition, 0.5f)) {
            _e.setState(_e.heroMoveState);
            return;
        }

        if(_e.getAttackTarget() != null){
            _e.setState(_e.heroAttackState);
            return;
        }
    }

    @Override
    public void exit (Entity e){
        HeroPlayer _e = (HeroPlayer)e;
        _e.animationTimer = 0;
    }
}

class HeroMoveState implements State {
    @Override
    public void enter(Entity e) {
        HeroPlayer _e = (HeroPlayer)e;
        _e.currentAnimation = _e.runAnimation;
    }

    @Override
    public void update(Entity e, float delta) {
        HeroPlayer _e = (HeroPlayer)e;

        System.out.println("Target Position is: " + _e.getTargetPosition());

        //If the entity itself dies
        if (_e.isDead){
            _e.setState(_e.heroDeadState);
            return;
        }

        //In case of rightClickEvent an attackTarget is generated
        if(_e.getAttackTarget() != null){
            _e.setState(_e.heroAttackState);
            return;
        }

        // If the current position and target position are equal, to a certain degree, stop walking and go into the 
        if (_e.currentXY.epsilonEquals(_e.targetPosition, 0.5f)) {
            _e.setState(_e.heroIdleState);
            return;
        } 

        // calculate speed and turn towards the target
        // Takes leftClick as input
        _e.moveTowards(_e.getTargetPosition(), delta);

        // Change position based on velocity

        // Change x
        _e.currentXY.x += _e.velocity.x * delta;
        _e.updateBoxes();
        
        // Handle collision
        if (_e.isCollidingWithEntity()) {
            _e.currentXY.x -= _e.velocity.x * delta;
            _e.velocity.x = 0;
            _e.updateBoxes();
        }

        // Change y
        _e.currentXY.y += _e.velocity.y * delta;
        _e.updateBoxes();
        
        // Handle collision
        if (_e.isCollidingWithEntity()) {
            _e.currentXY.y -= _e.velocity.y * delta;
            _e.velocity.y = 0;
            _e.updateBoxes();
        }

        if (_e.velocity.isZero()) {
            _e.setState(_e.heroIdleState);
        }

        // Update collision and hitboxes and update the sprite position
        _e.setCenter(_e.currentXY.x, _e.currentXY.y);
    }

    @Override
    public void exit(Entity e) {
        HeroPlayer _e = (HeroPlayer)e;
        _e.velocity.setZero();
        _e.targetPosition.set(_e.currentXY);
        _e.animationTimer = 0;
    }
}

class HeroAttackState implements State{

    public void enter(Entity e){
        HeroPlayer _e = (HeroPlayer) e;
        _e.currentAnimation = _e.attackAnimation;
    }

    @Override
    public void update(Entity e, float delta){
        HeroPlayer _e = (HeroPlayer) e;

        //If the entity itself dies
        if (_e.isDead){
            _e.setState(_e.heroDeadState);
            _e.attackTarget = null;
            return;
        }

        //In case hero leftClicks a targetPosition is set and currentXY will obv not match it
        if (!_e.currentXY.epsilonEquals(_e.targetPosition, 0.5f)) {
            _e.setState(_e.heroMoveState);
            _e.attackTarget = null;
            return;
        }

        //If the target is dead
        if (_e.getAttackTarget().isDead) {
            _e.setState(_e.heroIdleState);
            _e.attackTarget = null;
            return;
        }

        //If our hero is far from the enemy
        if (!_e.isCloseToEnemy()){
            _e.setState(_e.heroChaseState);
            return;
        }

        // Check if we have passed the interval of attack and reset the timer
        if (_e.attackTimer >= _e.attackSpeed) {
            _e.attackTarget.takeDamage(_e.attackStrength);
            System.out.printf("Victim's Current Health... %f\n", _e.attackTarget.currentHealth);
            _e.attackTimer = 0;
        }

        // Calculate the angle and flip accordingly

        Vector2 displacement =  _e.attackTarget.getCurrentPosition().cpy();
        displacement.sub(_e.getCurrentPosition());

        float angle = MathUtils.atan2Deg360(displacement.y, displacement.x);

        if (angle > 90f && angle < 270f) {
            //this.setRotation(angle + 180);
            _e.setFlip(true, false);
        } else {
            //this.setRotation(angle);
            _e.setFlip(false, false);
        }

        _e.attackTimer += delta;
    
    }

    @Override
    public void exit(Entity e) {
        HeroPlayer _e = (HeroPlayer)e;
        _e.animationTimer = 0;
    }
}

class HeroChaseState implements State {

    public void enter(Entity e){
        HeroPlayer _e = (HeroPlayer)e;
        _e.currentAnimation = _e.runAnimation;
    }

    public void update(Entity e, float delta){
        HeroPlayer _e = (HeroPlayer) e;

        //If the entity itself dies
        if (_e.isDead){
            _e.setState(_e.heroDeadState);
            _e.attackTarget = null;
            return;
        }

        //In case hero leftClicks a targetPosition is set and currentXY will obv not match it
        if (!_e.currentXY.epsilonEquals(_e.targetPosition, 0.5f)) {
            _e.setState(_e.heroMoveState);
            _e.attackTarget = null;
            return;
        }

        //If the target is dead
        if (_e.getAttackTarget().isDead) {
            _e.setState(_e.heroIdleState);
            _e.attackTarget = null;
            return;
        }

        if (_e.isCloseToEnemy()){
            _e.setState(_e.heroAttackState);
            return;
        }
        
        // calculate speed and turn towards the target
        _e.moveTowards(_e.attackTarget.getCurrentPosition(), delta);

        // Change position based on velocity

        // Change x
        _e.currentXY.x += _e.velocity.x * delta;
        _e.targetPosition.x += _e.velocity.x * delta;
        _e.updateBoxes();
        
        // Handle collision
        if (_e.isCollidingWithEntity()) {
            _e.currentXY.x -= _e.velocity.x * delta;
            _e.targetPosition.x -= _e.velocity.x * delta;
            _e.velocity.x = 0;
            _e.updateBoxes();
        }

        // Change y
        _e.currentXY.y += _e.velocity.y * delta;
        _e.targetPosition.y += _e.velocity.y * delta;
        _e.updateBoxes();
        
        // Handle collision
        if (_e.isCollidingWithEntity()) {
            _e.currentXY.y -= _e.velocity.y * delta;
            _e.targetPosition.y -= _e.velocity.y * delta;
            _e.velocity.y = 0;
            _e.updateBoxes();
        }

        if (_e.velocity.isZero()) {
            _e.setState(_e.heroIdleState);
        }

        // Update collision and hitboxes and update the sprite position
        _e.setCenter(_e.currentXY.x, _e.currentXY.y);
    }

    public void exit(Entity e){
        HeroPlayer _e = (HeroPlayer)e;
        _e.velocity.setZero();
        _e.animationTimer = 0;
    }
}

class HeroDeadState implements State {
    public void enter (Entity e){
        HeroPlayer _e = (HeroPlayer) e;
        _e.currentAnimation = _e.deadAnimation;
    }

    public void update (Entity e, float delta){
        // TODO: Add functionality aka respawn mechanism checks     
    }

    public void exit (Entity e){
        HeroPlayer _e = (HeroPlayer) e;
        _e.animationTimer = 0;
    }
}