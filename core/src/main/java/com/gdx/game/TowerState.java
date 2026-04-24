package com.gdx.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

class TowerIdleState implements State<Tower> {
    @Override
    public void enter(Tower e) {
        e.currentAnimation = e.idleAnimation;
    }

    @Override
    public void update(Tower e, float delta) {
        if (e.isDead) {
            e.towerWeapon = null;
            e.setState(e.towerDeadState);
        }
    }

    @Override
    public void exit(Tower e) {
        e.animationTimer = 0;
    }
}

class TowerDeadState implements State<Tower> {
    @Override
    public void enter(Tower e) {
        e.currentAnimation = e.deadAnimation;
    }

    @Override
    public void update(Tower e, float delta) {
        // Towers don't respawn, so yea...
    }

    public void exit(Tower e) {
        e.animationTimer = 0;
    }
}

class WeaponIdleState implements State<Weapon> {
    @Override
    public void enter(Weapon e) {
        e.currentAnimation = e.idleAnimation;
    }

    @Override
    public void update(Weapon e, float delta) {
        Entity potentialTarget = null;
        float closestEnemyDistance = Float.MAX_VALUE;

        for (Entity entity : Entity.entityList) {
            if (entity == e || entity.isAlly == e.isAlly || entity.isDead) {
                continue;
            }

            float dist = entity.getCurrentPosition().dst(e.getCurrentPosition());
            if (closestEnemyDistance > dist) {
                closestEnemyDistance = dist;
                potentialTarget = entity;
            }
        }

        if (potentialTarget == null) {
            return;
        }

        if (closestEnemyDistance <= e.attackRange) {
            e.setState(e.weaponAttackState);
            e.setAttackTarget(potentialTarget);
            e.arrow.setAttackTarget(potentialTarget);
            e.arrow.setTargetPosition(potentialTarget.getCurrentPosition().x,
                potentialTarget.getCurrentPosition().y);
            return;
        }

        // turn towards the target
        float angle = getAngle(e, potentialTarget);

        //-----NOTE----
        //you can add rotation or not your choice just uncomment the player.setRotation() lines to see it in play

        e.setRotation(angle);

        if (angle > 90 && angle < 270) {
            e.setRotation(angle);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant
            e.setFlip(true, false);       //Flip the x axis
        }
        else {
            e.setRotation(angle + 180);         //Same reason as above
            e.setFlip(false, false);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
        }
    }

    private static float getAngle(Weapon e, Entity potentialTarget) {
        float angle;
        float destinationX = potentialTarget.getCurrentPosition().x - e.getCurrentPosition().x;
        float destinationY = potentialTarget.getCurrentPosition().y - e.getCurrentPosition().y;

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destinationY, destinationX);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        return angle;
    }

    @Override
    public void exit(Weapon e) {
        e.animationTimer = 0;
    }
}

class WeaponAttackState implements State<Weapon> {
    @Override
    public void enter(Weapon e) {
       e.currentAnimation = e.attackAnimation;
    }

    @Override
    public void update(Weapon e, float delta) {
        Vector2 targetPosition = e.getAttackTarget().getCurrentPosition();

        if (e.getAttackTarget().isDead || e.getCurrentPosition().dst(targetPosition) > e.attackRange) {
            e.setAttackTarget(null);
            e.setState(e.weaponIdleState);
            return;
        }

        float angle = getAngle(e);


        if (angle > 90 && angle < 270) {
            e.setRotation(angle);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant
            e.setFlip(true, false);       //Flip the x axis
        }
        else {
            e.setRotation(angle + 180);         //Same reason as above
            e.setFlip(false, false);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
        }

    }

    private static float getAngle(Weapon e) {
        float angle = 0f;
        float destinationX = e.getAttackTarget().getCurrentPosition().x - e.getCurrentPosition().x;
        float destinationY = e.getAttackTarget().getCurrentPosition().y - e.getCurrentPosition().y;

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destinationY, destinationX);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        return angle;
    }

    @Override
    public void exit(Weapon e) {
        e.animationTimer = 0;
        e.setAttackTarget(null);
    }
}

class ProjectileIdleState implements State<Projectile> {
    @Override
    public void enter(Projectile e) {
    }

    @Override
    public void update(Projectile e, float delta) {
        if (e.getAttackTarget() != null) {
            e.setState(e.projectileFlyingState);
        }
    }

    @Override
    public void exit(Projectile e) {
        e.animationTimer = 0;
    }
}

class ProjectileFlyingState implements State<Projectile> {
    @Override
    public void enter(Projectile e) {
        e.currentAnimation = e.idleAnimation;
    }

    @Override
    public void update(Projectile e, float delta) {
        boolean isArrowColliding = e.getHitBox().overlaps(e.getAttackTarget().getHitBox());

        if (isArrowColliding) {
            e.getAttackTarget().takeDamage(e.attackStrength);
            e.setState(e.projectileImpactState);
            return;
        }

        float angle = getAngle(e, e.targetPosition);

        e.setRotation(angle);

        if (e.targetPosition.epsilonEquals(e.currentXY, 0.5f)) {
            e.setState(e.projectileImpactState);
            return;
        }

        e.moveTowards(e.targetPosition, delta);
        Vector2 currentPosition = e.getCurrentPosition();

        e.currentXY.mulAdd(e.velocity, delta);
        e.updateBoxes();
        e.setCenter(e.currentXY.x, e.currentXY.y);
    }

    @Override
    public void exit(Projectile e) {
        e.animationTimer = 0;
        e.attackTarget = null;
    }

    private static float getAngle(Projectile e, Vector2 potentialTarget) {
        float angle;
        float destinationX = potentialTarget.x - e.getCurrentPosition().x;
        float destinationY = potentialTarget.y - e.getCurrentPosition().y;

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destinationY, destinationX);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        return angle;
    }
}

class ProjectileImpactState implements State<Projectile> {
    @Override
    public void enter(Projectile e) {
        e.currentAnimation = e.impactAnimation;
    }

    @Override
    public void update(Projectile e, float delta) {
        if (e.impactAnimation.isAnimationFinished(e.animationTimer)) {
            e.setCurrentPosition(e.parent.currentXY.x, e.parent.currentXY.y);
            e.setState(e.projectileIdleState);
        }
    }

    @Override
    public void exit(Projectile e) {

    }
}
