// Try and understand BotChaseState, i dare you
// Ask me about BotChaseState, and i will cry
package com.gdx.game;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;


class BotIdleState implements State<Bot> {
    @Override
    public void enter(Bot e) {
        e.currentAnimation = e.idleAnimation;       // starting animation as idle 
    }

    @Override
    public void update(Bot e, float delta) {
        // If the entity itself dies
        if (e.isDead){
            e.setState(e.BotDeadState);
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
        //If the target is dead
        if (e.getAttackTarget() != null){
            if (e.getAttackTarget().isDead) {
                e.setState(e.BotIdleState);
                e.attackTarget = null;
                return;
            }
        }
        

        //If our hero is far from the enemy
        if (!e.isCloseToEnemy()){
            e.setState(e.BotChaseState);
            return;
        }

        // Check if we have passed the interval of attack and reset the timer
        if (e.attackTimer >= e.attackSpeed) {
            e.attackTarget.takeDamage(e.attackStrength);
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
    Vector2 lastValidPostion = new Vector2();
    public static ArrayList<Node> nodeList = new ArrayList<>();

    public void enter(Bot e){
        e.currentAnimation = e.runAnimation;
        e.BFSpath = null;
    }

    public void update(Bot e, float delta){

        
        lastValidPostion.set(e.currentXY);

        e.setAttackTarget(e.getAttackTarget()); // Move to closest always
        //If the entity itself dies
        if (e.isDead){
            e.setState(e.BotDeadState);
            e.attackTarget = null;
            return;
        }

        // MOVE NIGGA MOVE

        // Use BFS path if available, otherwise use normal movement
        if(e.BFSpath != null){
            moveOnPath(e, delta);
        }


        // If the target is dead
        Entity attackTarget = e.getAttackTarget();
        if (attackTarget != null && e.getAttackTarget().isDead) {
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
        if(e.BFSpath == null){
            if(attackTarget != null)
                e.moveTowards(e.attackTarget.getCurrentPosition(), delta);
        }

        // Change position based on velocity - ALWAYS update currentXY regardless of BFS path
        // Change x
        e.currentXY.x += e.velocity.x * delta;
        e.targetPosition.x += e.velocity.x * delta;
        e.updateBoxes();

        // Change y
        e.currentXY.y += e.velocity.y * delta;
        e.targetPosition.y += e.velocity.y * delta;
        e.updateBoxes();



        // Handle collision - revert movement when colliding
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()){
            // Calculate the direction we were trying to move (from last valid to current)
            Vector2 moveDir = e.currentXY.cpy().sub(lastValidPostion).nor();
            
            e.collisionCounter++;

            // Revert position to last valid state
            e.currentXY.set(lastValidPostion);
            e.targetPosition.x -= e.velocity.x * delta;
            e.targetPosition.y -= e.velocity.y * delta;
            e.updateBoxes();

            e.velocity.setZero();
            

            
            if(e.collisionCounter>30){
                if(attackTarget != null){
                    // Calculate BFS from CURRENT position to target position
                    int gx = (int) (e.attackTarget.getCurrentPosition().x / Bot.tileSize / Bot.scale);
                    int gy = (int) (e.attackTarget.getCurrentPosition().y / Bot.tileSize / Bot.scale);
                    
                    int sx = (int) (e.currentXY.x / Bot.tileSize / Bot.scale);
                    int sy = (int) (e.currentXY.y / Bot.tileSize / Bot.scale);
                    
                    // Adjust starting grid position based on direction of collision
                    // If we hit something while moving right (moveDir.x > 0)
                    if(moveDir.x > 0){
                        sx--;
                    }
                    else if(moveDir.x < 0){
                        sx++;
                    }

                    // If we hit something while moving up (moveDir.y > 0)
                    if(moveDir.y > 0){
                        sy--;
                    }
                    else if(moveDir.y < 0){
                        sy++;
                    }
                    e.BFSpath = e.bfs(sx, sy, gx, gy, Bot.blocked,e.gridSpanWidth, e.gridSpanHeight);
                    if(e.BFSpath != null)
                        for(Node i : e.BFSpath){
                            nodeList.add(i);
                        }
                    pathIndex = 0;
                    
                    if(e.BFSpath != null) {
                    } else {
                    }
                    e.collisionCounter++;

                }
            }
    }
    else
        e.setCenter(e.currentXY.x, e.currentXY.y);
        // Update collision and hitboxes and update the sprite position - ALWAYS update
    }

    public void exit(Bot e){
        e.velocity.setZero();
        e.animationTimer = 0;
    }
    
    // Fixed moveOnPath - uses e.BFSpath directly instead of uninitialized local variables
    private int pathIndex = 0;
    
    public void moveOnPath(Bot e, float delta){
        // Safety check: if path is null or empty, fall back to normal movement
        if(e.BFSpath == null || e.BFSpath.isEmpty()){
            e.BFSpath = null;
            pathIndex = 0;
            e.moveTowards(e.attackTarget.getCurrentPosition(), delta);
            return;
        }
        
        
        // If we've reached the end of the path, clear it and return
        if(pathIndex >= e.BFSpath.size()){
            e.BFSpath = null;
            pathIndex = 0;
            return;
        }
        
        // Get the current target node from the path
        Node targetNode = e.BFSpath.get(pathIndex);

       float unit = Bot.tileSize * Bot.scale;
    
        // Centre of grid
        Vector2 pathTarget = new Vector2(
            targetNode.x * unit + (unit / 2f),
            targetNode.y * unit + (unit / 2f)
        );
        
        // Move towards the path target
        e.moveTowards(pathTarget, delta);
        
        // Update targetPosition for collision handling
        e.targetPosition.set(pathTarget);
        
        // Check if we've reached the current path node (within tolerance)
        if(e.currentXY.epsilonEquals(pathTarget, 2.0f)){
            pathIndex++;
        }
        
        // Update sprite position
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()){
                e.currentXY.x = lastValidPostion.x;
                e.targetPosition.x -= e.velocity.x * delta;
                e.velocity.x = 0;
                e.updateBoxes();

                // Change y
                e.currentXY.y = lastValidPostion.y;
                e.targetPosition.y -= e.velocity.y * delta;
                e.velocity.y = 0;
                e.updateBoxes();
            }

    }
}


class BotDeadState implements State<Bot> {
    private float timer = 0f;
    private static final float MAX_RESPAWN_TIME = 5f; // In seconds btw
    private boolean isDying;
    private boolean isRespawing;
    private boolean blocked;

    public void enter (Bot e){
        e.currentAnimation = e.deadAnimation;
        e.currentAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        isDying = true;
        isRespawing = false;
        blocked = false;        //When in deadState your entity is not blocked
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

        blocked = false;
        //Every entity has their own specific spawn points
        float respawnPositionX = e.startX;
        float respawnPositionY = e.startY;

        e.setCurrentPosition(respawnPositionX, respawnPositionY);
        e.updateBoxes();

        //In case someone is standing on their spawn point
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()) {
            blocked = true;     //In case someone is standing on his respawn point
            // Revert immediately
            e.setCurrentPosition(-9999, -9999); // Some random position while we wait
            e.updateBoxes();
        }
        

        if (!blocked){
            e.setTargetPosition(respawnPositionX, respawnPositionY);
            e.setCenter(respawnPositionX, respawnPositionY);
            e.currentHealth = e.maxHealth;
            e.isDead = false;
            timer = 0f;
            isRespawing = true;     //This flag is what respawns the entity
            e.currentAnimation.setPlayMode(Animation.PlayMode.REVERSED);
            e.animationTimer = 0f;
        }
    }

    @Override
    public void exit (Bot e) {
        e.animationTimer = 0;
    }
}
