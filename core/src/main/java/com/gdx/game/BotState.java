// Try and understand BotChaseState, i dare you
// Ask me about BotChaseState, and i will cry
package com.gdx.game;
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

        else{
            // calculate speed and turn towards the target
            e.moveTowards(e.getTargetPosition(), delta);
        }

        // Change position based on velocity 

        // Change x
        e.currentXY.x += e.velocity.x * delta;

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
    Vector2 backScale = new Vector2(0,0);
    Vector2 maxBackScale = new Vector2(0,0);

    public void enter(Bot e){
        e.currentAnimation = e.runAnimation;
    }

    public void update(Bot e, float delta){
        e.setAttackTarget(e.getAttackTarget());
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
        if (e != null && e.getAttackTarget().isDead) {
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
            System.out.println("Collision detected!");
            
            if(e.isCollidingWithBoundry()){
                e.collisionCounter++;
            }
            // Revert position
            if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()){
                System.out.println("Current Position grid:"+Math.ceil(e.currentXY.x/Bot.tileSize/Bot.scale)+","+Math.ceil(e.currentXY.y/Bot.tileSize/Bot.scale));

                backScale = e.velocity.scl(5f);
                if(backScale.x>maxBackScale.x){
                    maxBackScale.x = backScale.x;
                }
                if(backScale.y>maxBackScale.y){
                    maxBackScale.y = backScale.y;
                }


                e.currentXY.x -= backScale.x * delta;
                e.targetPosition.x -= backScale.x * delta;
                e.velocity.x = 0;
                e.updateBoxes();

                // Change y
                e.currentXY.y += backScale.y * delta;
                e.targetPosition.y += backScale.y * delta;
                e.velocity.y = 0;
                e.updateBoxes();
                
                System.out.println("positions reverted");
                System.out.println("New Position grid:"+Math.ceil(e.currentXY.x/Bot.tileSize/Bot.scale)+","+Math.ceil(e.currentXY.y/Bot.tileSize/Bot.scale));

            }

            if(e.collisionCounter>3){
                System.out.println(1);
                // Calculate BFS from CURRENT position to target position
                int gx = (int) Math.ceil(e.attackTarget.getCurrentPosition().x / Bot.tileSize / Bot.scale);
                int gy = (int) Math.ceil(e.attackTarget.getCurrentPosition().y / Bot.tileSize / Bot.scale);
                
                int sx = (int) Math.ceil(e.currentXY.x / Bot.tileSize / Bot.scale);
                int sy = (int) Math.ceil(e.currentXY.y / Bot.tileSize / Bot.scale);
                
                if(maxBackScale.y>0){
                    sy = (int) Math.floor(e.currentXY.y / Bot.tileSize / Bot.scale);
                }
                if(maxBackScale.x>0){
                    sx = (int) Math.ceil(e.currentXY.x / Bot.tileSize / Bot.scale);
                }

                e.BFSpath = e.bfs(sx, sy, gx, gy, Bot.blocked,e.gridSpanWidth, e.gridSpanHeight);
                pathIndex = 0;
                
                if(e.BFSpath != null) {
                    System.out.println("BFS path found with " + e.BFSpath.size() + " nodes");
                } else {
                    System.out.println("No BFS path found!");
                }
                e.collisionCounter++;
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
        
        System.out.println("Following BFS path - index: " + pathIndex + " of " + e.BFSpath.size());
        
        // If we've reached the end of the path, clear it and return
        if(pathIndex >= e.BFSpath.size()){
            e.BFSpath = null;
            pathIndex = 0;
            return;
        }
        
        // Get the current target node from the path
        Node targetNode = e.BFSpath.get(pathIndex);
        Vector2 pathTarget = new Vector2(
            targetNode.x * Bot.tileSize * Bot.scale,
            targetNode.y * Bot.tileSize * Bot.scale
        );
        
        System.out.println("Path target: " + pathTarget.x + "," + pathTarget.y);
        System.out.println("Current position: " + e.currentXY.x + "," + e.currentXY.y);
        
        // Move towards the path target
        e.moveTowards(pathTarget, delta);
        
        // Update targetPosition for collision handling
        e.targetPosition.set(pathTarget);
        
        // Check if we've reached the current path node (within tolerance)
        if(e.currentXY.epsilonEquals(pathTarget, 2.0f)){
            pathIndex++;
            System.out.println("Reached node " + (pathIndex - 1) + ", moving to next...");
        }
        
        // Update sprite position
        e.setCenter(e.currentXY.x, e.currentXY.y);  // TODO: Check if this line is needed
        if (e.isCollidingWithEntity() || e.isCollidingWithBoundry()){
                backScale = e.velocity.scl(5f);

                e.currentXY.x -= backScale.x * delta;
                e.targetPosition.x -= backScale.x * delta;
                e.velocity.x = 0;
                e.updateBoxes();

                // Change y
                e.currentXY.y += backScale.y * delta;
                e.targetPosition.y += backScale.y * delta;
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
