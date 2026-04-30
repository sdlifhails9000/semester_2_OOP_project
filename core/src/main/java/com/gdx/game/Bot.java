package com.gdx.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

class Bot extends DynamicEntity{
    static float tileSize = MainGame.tileSize;
    public static ArrayList<Bot> BotList = new ArrayList<>();
    static float scale = MainGame.scale;
    int collisionCounter = 0;

    //Entities Declaration
    Entity attackTarget;

    // Bot States
    State BotIdleState = new BotIdleState();
    State BotMoveState = new BotMoveState();
    State BotChaseState = new BotChaseState();
    State BotAttackState = new BotAttackState();
    State BotDeadState = new BotDeadState();

    //State declaration (for setState)
    State currentState;

    // Path finding
    int gridSpanHeight;
    int gridSpanWidth;
    public static boolean[][] blocked;
    List<Node> BFSpath;

    
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

        gridSpanWidth = 1;
        gridSpanHeight = 2;

        System.out.println("Sprite: " + preset.getSpriteWidth() + "x" + preset.getSpriteHeight());
System.out.println("Scale: " + scale);
System.out.println("TileSize: " + tileSize);
System.out.println("GridSpan: " + gridSpanWidth + "x" + gridSpanHeight);


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
            System.out.println("Checking entity:" + entity);
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
    
    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }

    @Override
    public void Update(float delta) {
        setAttackTarget(getAttackTarget());
        super.Update(delta);
        currentState.update(this, delta);
    }


    public List<Node> bfs(int sx, int sy, int gx, int gy, boolean[][] blocked, int width, int height){
    
    Queue<Node> queue = new LinkedList<>();
        boolean[][] visited = new boolean[blocked.length][blocked[0].length];

        Node start = new Node(sx, sy);
        queue.add(start);
        visited[sx][sy] = true;

        int[][] dirs = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        while (!queue.isEmpty()) {

            Node current = queue.poll();

            if (current.x == gx && current.y == gy) {
                return reconstructPath(current);
            }

    for (int[] d : dirs) {
        int nx = current.x + d[0];
        int ny = current.y + d[1];

        if (nx < 0 || ny < 0 || nx >= blocked.length || ny >= blocked[0].length)
            continue;

        if (visited[nx][ny])
            continue;


    // Check the actual target position
    int spriteTopY = ny - (height - 1);
    if (!canStand(nx, spriteTopY, width, height, blocked))
        continue;

    Node next = new Node(nx, ny);
    next.parent = current;
    visited[nx][ny] = true;
    queue.add(next);
}
        }

        System.out.println("Didnt find a path");
        return null;
    }

   boolean canStand(
    int x, int y,
    int w, int h,
    boolean[][] blocked){

    System.out.println("canStand check at (" + x + "," + y + ") size " + w + "x" + h);
    
    for (int dx = 0; dx < w; dx++) {
        for (int dy = 0; dy < h; dy++) {

            int nx = x + dx;
            int ny = y + dy;

            System.out.println("  Checking (" + nx + "," + ny + "): blocked=" + 
                (nx < 0 || ny < 0 || nx >= blocked.length || ny >= blocked[0].length ? "OOB" : blocked[nx][ny]));

            if (nx < 0 || ny < 0 ||
                nx >= blocked.length ||
                ny >= blocked[0].length)
                return false;

            if (blocked[nx][ny])
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
        System.out.println("Path");
        for(Node i :path){
            System.out.println(i.x+","+i.y);
        }
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

