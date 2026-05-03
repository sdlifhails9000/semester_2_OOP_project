package com.gdx.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class Bot extends DynamicEntity{
    static int gridSize = 2;
    static ShapeRenderer shapeRenderer = new ShapeRenderer(); // DELETE
    public static ArrayList<Rectangle> rectArray = new ArrayList<>(); // DELETE
    static float tileSize = GameScreen.tileSize/gridSize;
    public static ArrayList<Bot> BotList = new ArrayList<>();
    static float scale = GameScreen.scale;
    int collisionCounter = 0;
    Rectangle tempCollisionRectangle = new Rectangle(0,0,collisionBox.width,collisionBox.height);
    Rectangle greaterTempCollisioRectangle = new Rectangle(0,0,collisionBox.width*2f,collisionBox.height*2f);

    //Entities Declaration
    Entity attackTarget;

    //Healthbar Sprite declaration
    Sprite HealthBarSprite;
    private TextureRegion fullHealthRegion;

    // Bot States
    State BotIdleState = new BotIdleState();
    State BotChaseState = new BotChaseState();
    State BotAttackState = new BotAttackState();
    State BotDeadState = new BotDeadState();

    //State declaration (for setState)
    State currentState;

    // Path finding
    int gridSpanHeight;
    int gridSpanWidth;
    List<Node> BFSpath;
    Vector2 BFSlastNode = null;


    //Animation Declaration (Idle and dead is handled in Entity.java)   (Current animation is in entity.java because idle and dead is handled there)
    protected Animation<TextureRegion> runAnimation;
    protected Animation<TextureRegion> attackAnimation;
    protected Animation<TextureRegion> deadAnimation;

    //Stats declaration
    protected float attackRange;
    protected float attackSpeed;
    protected float attackStrength;
    protected float attackTimer;


    Bot (BotPreset preset, int startX, int startY){
        super(
            Loader.idle(preset),
            startX, startY,

            preset.getMaxHealth(),
            preset.getSpeed(),
            preset.getSpriteWidth(),
            preset.getSpriteHeight(),
            preset.getIsAlly()
        );



        BotList.add(this);

        this.attackAnimation = Loader.attack(preset);
        this.runAnimation = Loader.run(preset);
        this.deadAnimation = Loader.dead(preset);

        this.attackRange = preset.getAttackRange();
        this.attackSpeed = preset.getAttackSpeed();
        this.attackStrength = preset.getAttackStrength();
        this.currentState = BotIdleState;

        //Loads healthBarSprite and sets it above the hero with offset
        this.fullHealthRegion = Loader.healthBar(preset);
        this.HealthBarSprite = new Sprite(fullHealthRegion);
        //heroHealthBarSprite.setScale(0.15f);

        gridSpanWidth = 1;
        gridSpanHeight = 2;
        gridSpanWidth = preset.getGridSpanWidth();
        gridSpanHeight = preset.getGridSpanHeight();
    }

    //Setters and Getters
    public void setAttackTarget(Entity e){
        this.attackTarget = e;
    }

    // Is used to determine the state, sybau shaheer :wilting_rose:
    protected boolean isCloseToEnemy() {
        if (attackTarget == null) {
            return false;
        }

        Rectangle enemyBounds = attackTarget.getHitBox();
        Rectangle playerBounds = this.getHitBox();

        boolean isClose = playerBounds.overlaps(enemyBounds);

        Vector2 enemyPos = attackTarget.getCurrentPosition();

        // This condition is so that the hits of a light class register on a heavy class while chasing it
        boolean isInRange = enemyPos.dst(this.getCurrentPosition()) <= attackRange;

        return isClose || isInRange;
    }

    public Entity getAttackTarget() {
        Entity nearestEntity = null;
        float nearestEnemyDistance = Float.MAX_VALUE;

        // This finds the nearest enemy to this bot
        for (Entity entity : Entity.entityList) {
            // This skips allies. They aren't enemies, entities which are dead and itself
            if (this.isAlly == entity.isAlly || entity.isDead || this == entity) {
                continue;
            }

            // Calculate distance
            Vector2 entityPos = entity.getCurrentPosition();
            float distance = entityPos.dst(getCurrentPosition());

            // Check if distance is smaller than the current nearestEnemyDistance
            if (nearestEnemyDistance > distance) {
                nearestEnemyDistance = distance;
                nearestEntity = entity;
            }
        }

        return nearestEntity;
    }

    public void updateHealthBar() {
    float healthPercent = currentHealth / maxHealth;

    int fullWidth = fullHealthRegion.getRegionWidth();
    int height = fullHealthRegion.getRegionHeight();

    int visibleWidth = (int)(fullWidth * healthPercent);

    // Clamp so it doesn’t go negative and cry
    visibleWidth = Math.max(0, visibleWidth);

    HealthBarSprite.setRegion(
        fullHealthRegion.getRegionX(),
        fullHealthRegion.getRegionY(),
        visibleWidth,
        height
    );

    HealthBarSprite.setSize(visibleWidth * 0.15f, height * 0.15f);

    HealthBarSprite.setCenter(
        getCurrentPosition().x,
        getCurrentPosition().y + spriteHeight / 2f + 1f
    );
}

    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }

    @Override
    public void Update(float delta) {
        this.setAttackTarget(getAttackTarget()); // Move to closest always
        super.Update(delta);
        currentState.update(this, delta);

        updateHealthBar();
    }


    public List<Node> bfs(int sx, int sy, int gx, int gy){     
    
        int[][] dirs = {    // Directions to move to, removed diagonals
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1,1}, {1,-1}, {-1,1}, {-1,-1}
            };

            Queue<Node> queue = new LinkedList<>(); // FIFO mode
            boolean[][] visited = new boolean[(int)GameScreen.worldWidth *gridSize][(int)GameScreen.worldHeight*gridSize];

            Node start = new Node(sx, sy);
            queue.add(start);
            visited[sx][sy] = true;

            while (!queue.isEmpty()) {

                Node current = queue.poll();

                if (current.x == gx && current.y == gy) {
                    BFSlastNode = new Vector2(current.x,current.y);
                    return reconstructPath(current);
                }

                for (int[] d : dirs) {
                    // Calculate neighbour
                    int nx = current.x + d[0];
                    int ny = current.y + d[1];
                    
                    // Check Validity
                    if (nx < 0 || ny < 0 || nx >= visited.length || ny >= visited[0].length) // Out of bounds
                        continue;

                    if (visited[nx][ny])
                        continue;

                    if (Math.abs(d[0]) == 1 && Math.abs(d[1]) == 1){    // Seperate check for diagonals because at the current grid size they become glitchy
                        if (!canStand(nx, ny,true)) // true just increases the sprite size a bit
                            continue;
                    }
                    else{
                        if (!canStand(nx, ny,false))
                            continue;
                    }
                    Node next = new Node(nx, ny);
                    next.parent = current;
                    visited[nx][ny] = true;
                    queue.add(next);    // Add all neighbours to queue if they are valid


                }
            }
            return null;    // No path
        }

boolean canStand(int x, int y,boolean diagonal) {

    Rectangle usedCollisionBox;
    float half = (Bot.tileSize * scale) / 2f;

    Vector2 worldCoords = new Vector2(
        x * Bot.tileSize * scale + half,
        y * Bot.tileSize * scale + half
    );
    if(diagonal){
        usedCollisionBox = greaterTempCollisioRectangle;
    }
    else{
        usedCollisionBox = tempCollisionRectangle;
    }
    usedCollisionBox.setCenter(worldCoords);

        for (Rectangle rect : boundaryCollisions) {
            float dx = usedCollisionBox.x - rect.x;        // change in x axis
            float dy = usedCollisionBox.y - rect.y;        // change in y axis
            float distance = (float) Math.sqrt((dx*dx) + (dy*dy));      // distance to centre

            if (distance >= 100) { // If far away skip
                continue;
            }

            if (usedCollisionBox.overlaps(rect)) {  // IF collission 
                return false;
            }
        }

        for (Entity i : entityList) {   // Loop through entities
            if (i == this) {
                continue;
            }
            if(this.attackTarget == i){
                continue;
            }
            // Same stuff here
            float dx = usedCollisionBox.x - i.currentXY.x;        // change in x axis
            float dy = usedCollisionBox.y - i.currentXY.y;        // change in y axis
            float distance = (float) Math.sqrt((dx*dx) + (dy*dy));      // distance to centre

            if (distance > 100) {
                continue;
            }

            //Do not check collision between DEAD entities <- Do not redeem the gift card ahh
            if (i.isDead){
                continue;
            }

            Rectangle enemyHitBox = i.getCollisionBox();
            if (usedCollisionBox.overlaps(enemyHitBox)) {
                return false;
            }
        }    
    return true;
}


    public List<Node> reconstructPath(Node end) {

        List<Node> path = new ArrayList<>();
        Node current = end;

        while (current != null) {
            path.add(current);
            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }

}

class Node {
    int x, y;
    Node parent;

    Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
}