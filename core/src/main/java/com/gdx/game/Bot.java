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

        gridSpanHeight = (int) Math.ceil(preset.getSpriteHeight()  / tileSize / scale); // I GOT THIS THROUGH TRIAL AND ERROR
        gridSpanWidth = (int) Math.ceil(preset.getSpriteWidth()  / tileSize / scale);


    }
    List<Node> s;
    int length;
    Vector2 moveTo;
    public void tstgetbsflist(){
        System.out.println((int) Math.ceil(40/tileSize/scale));
        s = bfs((int) Math.ceil(this.currentXY.x/tileSize/scale),(int) Math.ceil(this.currentXY.y/tileSize/scale) , (int) Math.ceil(currentXY.x/tileSize/scale + 10), (int) Math.ceil(currentXY.y/tileSize/scale + 3) , gridSpanWidth, gridSpanHeight, blocked);
        for(Node node :s)
            System.out.println(node.x*tileSize*scale+","+node.y*tileSize*scale);
        if(s!=null) // No path found
            length = s.size();
        moveTo = currentXY;
    }

    int i = 0;
    public void test(){
        System.out.println("Current Position: " + currentXY.x + ","+currentXY.y);
        if(i==length){
            return;
        }
        Node node = s.get(i);
        System.out.println("Distance to MoveTo: " + currentXY.dst(moveTo));
        if(this.currentXY.epsilonEquals(this.targetPosition, 0.5f)){
            moveTo = new Vector2(node.x *tileSize * scale,node.y * tileSize * scale);
            targetPosition.set(moveTo);
            this.setState(this.BotMoveState);
            System.out.println("Set State to Move");
            System.out.println("Target Position: " + targetPosition.x + ","+targetPosition.y);
            i++;

        }

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

        // Handle the scenario when no entities were found
        if (nearestEnemyDistance == Float.MAX_VALUE) {
            return null;
        }

        //return nearestEntity;
        return null; // testing
    }
    
    public void setState(State state){
        this.currentState.exit(this);
        this.currentState = state;
        this.currentState.enter(this);
    }

    @Override
    public void Update(float delta) {
        setAttackTarget(getAttackTarget());
        test();
        currentState.update(this, delta);
        super.Update(delta);
    }


    public List<Node> bfs(
        int sx, int sy,
        int gx, int gy,
        int gridSpanWidth,
        int gridSpanHeight,
        boolean[][] blocked){

        Queue<Node> queue = new LinkedList<>();
        boolean[][] visited = new boolean[blocked.length][blocked[0].length];

        Node start = new Node(sx, sy);
        queue.add(start);
        visited[sx][sy] = true;

        int[][] dirs = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},   // 4-way
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}  // diagonals
        };

        while (!queue.isEmpty()) {

            Node current = queue.poll();

            // GOAL REACHED
            if (current.x == gx && current.y == gy) {
                return reconstructPath(current);
            }

            // EXPAND NEIGHBORS
            for (int[] d : dirs) {

                int nx = current.x + d[0];
                int ny = current.y + d[1];

                // bounds check
                if (nx < 0 || ny < 0 ||
                    nx >= blocked.length ||
                    ny >= blocked[0].length)
                    continue;

                // already visited
                if (visited[nx][ny])
                    continue;

                // 🧠 SIZE-AWARE CHECK (IMPORTANT PART)
                if (!canStand(nx, ny, gridSpanWidth, gridSpanHeight, blocked))
                    continue;

                Node next = new Node(nx, ny);
                next.parent = current;

                visited[nx][ny] = true;
                queue.add(next);
            }
        }

        return null; // no path found
    }

    boolean canStand(
        int x, int y,
        int w, int h,
        boolean[][] blocked){

        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy < h; dy++) {

                int nx = x + dx;
                int ny = y + dy;

                // out of bounds
                if (nx < 0 || ny < 0 ||
                    nx >= blocked.length ||
                    ny >= blocked[0].length)
                    return false;

                // collision check
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

