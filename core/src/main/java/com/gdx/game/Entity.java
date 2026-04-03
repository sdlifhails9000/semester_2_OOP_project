package com.gdx.game;

// TODO Add projectiles for towers
// make the tower an Entity and make it's projectiles DynamicEntities that harm enemies

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Sprite;

abstract class Entity extends Sprite {
    enum State {
        IDLE,
        MOVING,
        ATTACK,
        DEAD,
    }

    protected float maxHealth;
    protected float currentHealth;
    protected float attackStrength;
    protected float attackRange;
    protected float attackSpeed;
    protected float spriteWidth;
    protected float spriteHeight;

    protected boolean isAlly;

    protected Vector2 currentXY;     //Starting points (game World Coords not screen coords)  //IN CHILD CLASS NOW

    protected float stateTime = 0; // Time since last attack

    Entity attackTarget;     //Stores entity to attack

    State state;

    Animation<TextureRegion> idleAnimation;
    Animation<TextureRegion> attackAnimation;
    Animation<TextureRegion> deadAnimation;

    Animation<TextureRegion> currentAnimation;

    protected Rectangle collisionBox;
    protected Rectangle hitBox; 
    
    Entity(Animation<TextureRegion> attack,
           Animation<TextureRegion> dead,
           Animation<TextureRegion> idle,
           float stateTime, float startX, float startY,
           float maxHealth,
           float attackRange,
           float attackSpeed,
           float attackStrength,
           float spriteWidth,
           float spriteHeight,
           boolean isAlly) {

        super(idle.getKeyFrame(stateTime));

        this.attackAnimation = attack;
        this.idleAnimation = idle;
        this.deadAnimation = dead;

        this.currentXY = new Vector2(startX, startY);

        this.stateTime = stateTime;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;     //Forgot to initialize this
        this.attackRange = attackRange;
        this.attackSpeed = attackSpeed;
        this.attackStrength = attackStrength;
        this.isAlly = isAlly;
        
        this.setSize(spriteWidth, spriteHeight);        //Set size here

        state = State.IDLE;
        currentAnimation = idleAnimation;
    
        updateBoxes();
    }
    

    // Get Postion
    public Vector2 getPosition() {
        return new Vector2(currentXY);
    }

    // Only set when you click right click. Only set it if the position is near an enemy
    protected void setAttackInfo(Entity entity){
        attackTarget = entity;
    }

    public Entity getAttackInfo() {
        return attackTarget;
    }

    // Is used to determine the state, don't touchy wouchy this or else things will break. TF2 coconut.jpeg moment
    protected boolean isCloseToEnemy() {
        if (attackTarget == null) {
            return false;
        }

        Rectangle enemyBounds = attackTarget.getHitBox();
        Rectangle playerBounds = this.getHitBox();
        
        boolean isClose = playerBounds.overlaps(enemyBounds);

        return isClose;
    }

    protected void takeDamage(float damage) {
        currentHealth -= damage;

        if (currentHealth <= 0f) {
            // Set state to dead
        }
    }

    protected void updateAttack(float delta) {                 //Override this for tower entity (If you dont want a rotating tower entity)
        // Check if we have passed the interval of attack and reset the timer
        if (stateTime >= attackSpeed) {
            attackTarget.takeDamage(attackStrength);
            System.out.printf("Attacker's Current Health... %f\n", attackTarget.currentHealth);
            stateTime = 0;
        }

        // Calculate the angle and flip accordingly

        Vector2 displacement = new Vector2();
        attackTarget.getBoundingRectangle().getCenter(displacement);
        displacement.sub(currentXY);

        float angle = MathUtils.atan2Deg360(displacement.y, displacement.x);

        if (angle > 90f && angle < 270f) {
            //this.setRotation(angle + 180);
            setFlip(true, false);
        } else {
            //this.setRotation(angle);
            setFlip(false, false);
        }

        stateTime += delta;
    }


    // COLLISION WORK AHEAD
    // HITBOX GENERATION AND RELATED METHODS
    static Pixmap pixmap;
    static Texture pixmapTexture;

    public void updateBoxes() {
        Animation<TextureRegion> animation = this.idleAnimation;
        TextureRegion frame = animation.getKeyFrame(stateTime);
        Texture texture = frame.getTexture(); // the atlas texture containing the frame

        if (pixmap == null || pixmapTexture != texture) {   // If pixmap is not initialized or the texture has changed, create a new pixmap, was casuing crash
            if (pixmap != null) {
                pixmap.dispose();   // If pixmap has changed dispose the previous one
            }
            TextureData textureData = texture.getTextureData(); // raw texture data for pixel access
            if (!textureData.isPrepared()) {
                textureData.prepare(); // must prepare before creating a Pixmap
            }
            pixmap = textureData.consumePixmap(); // access pixel values
            pixmapTexture = texture;
        }

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

    public Rectangle getCollisionBox(){
        return this.collisionBox;
    }

    public Rectangle getHitBox(){
            return this.hitBox;
    }


    abstract public void Update(float stateTime, float delta);
}
