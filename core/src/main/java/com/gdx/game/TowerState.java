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
            //Kill the weapon entirely alongwith it (towerWeapon is the reference of the TOWER'S weapon)
            e.towerWeapon.setState(e.towerWeapon.weaponDeadState);
            e.towerWeapon.isDead = true;

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

            //Set the arrow's attack target
            e.arrow.setAttackTarget(potentialTarget);
            return;
        }

        // turn towards the target
        float angle = getAngle(e, potentialTarget);

        //-----NOTE----
        //you can add rotation or not your choice just uncomment the player.setRotation() lines to see it in play

        if (angle > 0 && angle < 180) {
            e.setRotation(angle - 90);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant
            e.setFlip(false, false);       //Flip the x axis
        }
        else {
            e.setRotation(angle + 90);         //Same reason as above
            e.setFlip(false, true);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
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

        //If you want homing projectiles just set it to flying state instead of idle state
        if (e.arrow.currentState == e.arrow.projectileIdleState) {
            e.arrow.setTargetPosition(e.getAttackTarget().getCurrentPosition().x, e.getAttackTarget().getCurrentPosition().y);
        }

        float angle = getAngle(e);


        if (angle > 0 && angle < 180) {
            e.setRotation(angle - 90);        //We have to do a 90 degree CORRECTION offset because initially our bow is facing upward
            e.setFlip(false, false);       //Flip the x axis
        }
        else {
            e.setRotation(angle + 90);         //Same reason as above here arrow is facing directly downward now
            e.setFlip(false, true);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
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
        e.arrow.attackTarget = null;
    }
}

class WeaponDeadState implements State<Weapon> {
    @Override
    public void enter(Weapon e) {
       e.currentAnimation = e.deadAnimation;

       //Kill the projectile completely
       e.arrow.isDead = true;
       e.arrow.attackTarget = null;
    }

    @Override
    public void update (Weapon e, float delta) {
        //IT DIES FINITO KHALAS HASTA LA VISTA
    }

    @Override
    public void exit(Weapon e){
        e.animationTimer = 0;
    }
}

class ProjectileIdleState implements State<Projectile> {
    @Override
    public void enter(Projectile e) {
        e.currentAnimation = e.idleAnimation;
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
        e.isDead = true;
    }
}

class ProjectileFlyingState implements State<Projectile> {
    @Override
    public void enter(Projectile e) {
        e.currentAnimation = e.flyingAnimation;
    }

    @Override
    public void update(Projectile e, float delta) {
        //Only calculate these if your in range of the weapon
        if (e.getAttackTarget() != null){
            boolean isArrowColliding = e.getHitBox().overlaps(e.getAttackTarget().getHitBox());

            if (isArrowColliding) {
            e.getAttackTarget().takeDamage(e.attackStrength);
            e.setState(e.projectileImpactState);
            return;
        }
        }

        

        float angle = getAngle(e, e.targetPosition);

        e.setRotation(angle);

        if (e.targetPosition.epsilonEquals(e.currentXY, 0.5f)) {
            e.setState(e.projectileImpactState);
            return;
        }

        e.moveTowards(e.targetPosition, delta);

        e.currentXY.mulAdd(e.velocity, delta);
        e.updateBoxes();
        e.setCenter(e.currentXY.x, e.currentXY.y);
    }

    @Override
    public void exit(Projectile e) {
        e.animationTimer = 0;
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

        //Kill the arrow for now (revived in exit of ProjectileIdleState)
        e.isDead = true;
    }

    @Override
    public void update(Projectile e, float delta) {
        if (e.impactAnimation.isAnimationFinished(e.animationTimer)) {
            e.setCurrentPosition(e.parent.currentXY.x, e.parent.currentXY.y);
            e.updateBoxes();
            e.setCenter(e.parent.getCurrentPosition().x, e.parent.getCurrentPosition().y);      //Sets it back to the bow aka the parent
            e.setState(e.projectileIdleState);
        }
    }

    @Override
    public void exit(Projectile e) {
        e.animationTimer = 0;
    }
}
