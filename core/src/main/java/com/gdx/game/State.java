package com.gdx.game;

interface State {
    void enter(Entity e);
    void update(Entity e, float delta);

    default void exit(Entity e) {
        e.animationTimer = 0;
    }
}

class DynamicIdleState implements State {
    @Override
    public void enter(Entity e) {
        DynamicEntity _e = (DynamicEntity)e;
        _e.currentAnimation = _e.idleAnimation;
    }

    @Override
    public void update(Entity e, float delta) {
        DynamicEntity _e = (DynamicEntity)e;

        // if the target position and current position don't match, start moving to that target position
        if (!_e.currentXY.epsilonEquals(_e.targetPosition, 0.5f)) {
            _e.setState(_e.moveState);
        }
    }
}

class DynamicMoveState implements State {
    @Override
    public void enter(Entity e) {
        DynamicEntity _e = (DynamicEntity)e;
        _e.currentAnimation = _e.runAnimation;
    }

    @Override
    public void update(Entity e, float delta) {
        DynamicEntity _e = (DynamicEntity)e;

        // If the current position and target position are equal, to a certain degree, stop walking and go into the 
        if (_e.currentXY.epsilonEquals(_e.targetPosition, 0.5f)) {
            _e.setState(_e.idleState);
            return;
        } else if (_e.currentXY.x == _e.targetPosition.x) {
            _e.setState(_e.idleState);
            return;
        } else if (_e.currentXY.y == _e.targetPosition.y) {
            _e.setState(_e.idleState);
            return;
        }

        // calculate speed and turn towards the target
        _e.moveTowardsTarget(delta);

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
            _e.setState(_e.idleState);
        }

        // Update collision and hitboxes and update the sprite position
        _e.setPosition(_e.currentXY.x - _e.getWidth()/2, _e.currentXY.y - _e.getHeight()/2);
    }

    @Override
    public void exit(Entity e) {
        DynamicEntity _e = (DynamicEntity)e;
        _e.velocity.setZero();
        _e.targetPosition.set(_e.currentXY);
        _e.animationTimer = 0;
    }
}
