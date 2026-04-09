package com.gdx.game;

// TODO Add projectiles for towers
// TODO Add dynamic hitbox whilst keeping collisionbox untouched

// make the tower an Entity and make it's projectiles DynamicEntities that harm enemies

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Sprite;

abstract class Entity extends Sprite {

    State currentState;

    public static ArrayList<Entity> entityList = new ArrayList<>();

    // Health info
    protected float maxHealth;
    protected float currentHealth;

    // Attack info
    protected float attackStrength;
    protected float attackRange;
    protected float attackSpeed;

    protected float spriteWidth;
    protected float spriteHeight;

    protected boolean isAlly;
    protected boolean isDead = false;       //Initially character is alive

    protected Vector2 currentXY;     //Starting points (game World Coords not screen coords)  //IN CHILD CLASS NOW

    protected float animationTimer = 0f; // Used for retrieving a certain from from the current animation. THAT'S IT
    protected float attackTimer = 0f; // Used to keep track of how long it has been since the last attack

    Entity attackTarget;     //Stores entity to attack
    protected static ArrayList<Entity> heroList, goblinList, towerList;

    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> attackAnimation;
    Animation<TextureRegion> deadAnimation;

    Animation<TextureRegion> currentAnimation;

    protected Rectangle collisionBox;
    protected Rectangle hitBox; 
    
    Entity(Animation<TextureRegion> attack,
           Animation<TextureRegion> dead,
           Animation<TextureRegion> idle,
           float startX, float startY,
           float maxHealth,
           float attackRange,
           float attackSpeed,
           float attackStrength,
           float spriteWidth,
           float spriteHeight,
           boolean isAlly) {

        super(idle.getKeyFrame(0));     //Get first idle frame

        entityList.add(this);

        this.attackAnimation = attack;
        this.idleAnimation = idle;
        this.deadAnimation = dead;

        this.currentXY = new Vector2(startX, startY);

        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;     //Forgot to initialize this
        this.attackRange = attackRange;
        this.attackSpeed = attackSpeed;
        this.attackStrength = attackStrength;
        this.isAlly = isAlly;
        
        this.setSize(spriteWidth, spriteHeight);        //Set size here
        this.setOriginCenter();
        this.setCenter(startX, startY);

        currentAnimation = idleAnimation;
    
        createBoxes();
    }
    
    // Is used to determine the state, don't touchy wouchy this or else things will break. TF2 coconut.jpeg moment
    protected boolean isCloseToEnemy() {
        if (attackTarget == null) {
            return false;
        }

        Rectangle enemyBounds = attackTarget.getHitBox();
        Rectangle playerBounds = this.getHitBox();
        
        boolean isClose = playerBounds.overlaps(enemyBounds);

        Vector2 enemyPos = attackTarget.getPosition();

        // This condition is so that the hits of a light class register on a heavy class while chasing it
        boolean isInRange = enemyPos.dst(this.getPosition()) <= attackRange;

        return isClose || isInRange;
    }

    // Collision Detection ahead    
    // The Method is called in updateMovement()
    public boolean checkEntityCollision(){
        for (Entity i : entityList) {
            if (i == this) {
                continue;
            }

            if (i.currentXY.dst(this.currentXY) >= 20) {
                continue;
            }

            //Do not check collision between DEAD entities
            if (this.isDead || i.isDead){   
                continue;
            }

            Rectangle enemyHitBox = i.getCollisionBox();
            if (this.collisionBox.overlaps(enemyHitBox)) {
                System.out.println("Colliding");
                return true;
            }
        }

        return false;
    }

    protected void takeDamage(float damage) {
        if (isDead) {
            return;
        }

        currentHealth -= damage;

        if (currentHealth <= 0f) {
            isDead = true;
        }
    }

    


    // COLLISION WORK AHEAD
    // HITBOX GENERATION AND RELATED METHODS

    public void createBoxes() {
        TextureRegion frame = this.idleAnimation.getKeyFrame(0);     //SWITCH TO STATETIME IF YOU WANT DYNAMIC HITBOX
        TextureData textureData = frame.getTexture().getTextureData(); // raw texture data for pixel access
        Pixmap pixmap;

        if (!textureData.isPrepared()) {
            textureData.prepare(); // must prepare before creating a Pixmap
        }
        pixmap = textureData.consumePixmap(); // access pixel values

        // Bounds of the frame in the atlas
        int startX = frame.getRegionX();
        int startY = frame.getRegionY();
        int width = frame.getRegionWidth();
        int height = frame.getRegionHeight();

        int minX = width, minY = height;
        int maxX = 0, maxY = 0;

        for (int y = 0; y < height; y++) {  // Pixamp starts from the top left corner
            for (int x = 0; x < width; x++) {
                int pixel = pixmap.getPixel(startX + x, startY + y);    // Start from the frame region in the atlas
                int alpha = pixel >>> 24;   // Alpha shows the transparency

                // ARGB >>> (8*3) = 000A triple right shift is an unsigned shift. It does not care about the sign and does not preserve it
                // Alpha greater than 0 means any pixels that aren't transparent
                if (alpha > 0) {    // 0-255
                    minX = Math.min(minX, x);   // Left Boundary
                    minY = Math.min(minY, y);   // Bottom Boundary
                    maxX = Math.max(maxX, x);   // Right Boundary
                    maxY = Math.max(maxY, y);   // Top Boundary
                }
            }
        }

        pixmap.dispose();

        float scaleX = this.getWidth() / (float) width;    // Width of the sprite accounting for scale
        float scaleY = this.getHeight() / (float) height;  // Height of the sprite accounting for scale

        // Untransformed values for the hitbox
        float worldX = this.getX() + minX * scaleX;    // Scale accordingly at the outer most pixel
        float worldY = this.getY() + (height - maxY - 1) * scaleY; // Invert(Top left to bottom left) then scale
        float worldWidth = (maxX - minX + 1) * scaleX;
        float worldHeight = (maxY - minY + 1) * scaleY;

        this.collisionBox = new Rectangle(worldX, worldY, worldWidth, worldHeight);
        this.hitBox = new Rectangle(worldX - 1/2, worldY - 1/2, worldWidth + 1, worldHeight + 1);   //Generating a hitbox which is bigger than collision box
    }

    public void Update(float delta) {
        setRegion(currentAnimation.getKeyFrame(animationTimer));
        animationTimer += delta;
        currentState.update(this, delta);

        System.out.println("Position: " + currentXY.x + currentXY.y + "  State is: " + currentState.getClass().getName());
    }

    // ################### GETTERS SETTER ###################

    public Rectangle getCollisionBox(){
        return this.collisionBox;    
    }
    public Rectangle getHitBox(){
        return this.hitBox;
    }

    public Vector2 getPosition() {
        return currentXY;
    }
    
    public void setPosition(float newX, float newY) {
        currentXY.x = newX;
        currentXY.y = newY;
    }

    public void setState(State state) {
        currentState.exit(this);
        currentState = state;
        currentState.enter(this);
    }    
}

