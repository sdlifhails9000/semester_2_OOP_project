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
    public static ArrayList<Entity> entityList = new ArrayList<>();

    //Animations
    protected Animation<TextureRegion> idleAnimation;
    protected Animation<TextureRegion> currentAnimation;

    protected float animationTimer = 0;

    // Health info
    protected final float maxHealth;
    protected float currentHealth;

    protected float spriteWidth;
    protected float spriteHeight;

    protected boolean isAlly;
    protected boolean isDead = false;       //Initially character is alive

    protected Vector2 currentXY;     //Starting points (game World Coords not screen coords)  //IN CHILD CLASS NOW

    protected Rectangle collisionBox;
    protected Rectangle hitBox;

    Entity(Animation<TextureRegion> idleAnimation,
           float startX, float startY,
           float maxHealth,
           float spriteWidth,
           float spriteHeight,
           boolean isAlly) {

        super(idleAnimation.getKeyFrame(0));     //Get first idle frame

        this.idleAnimation = idleAnimation;
        this.currentAnimation = idleAnimation;  //Initially your character is idle

        this.currentXY = new Vector2(startX, startY);

        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;     //Forgot to initialize this
        this.isAlly = isAlly;

        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;

        this.setSize(spriteWidth, spriteHeight);        //Set size here
        this.setOriginCenter();
        this.setCenter(startX, startY);

        //Please DONT REMOVE THIS MFER
        entityList.add(this);
        System.out.println("Added:" + this);
        createBoxes();
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

    // ################### METHODS (override where required) ###################

    public void Update(float delta){
        animationTimer += delta;
        this.setRegion(currentAnimation.getKeyFrame(animationTimer));
    }

    //Abstract method
    public abstract void setState(State state);

    // ################### GETTERS SETTER ###################

    public Rectangle getCollisionBox(){
        return this.collisionBox;
    }
    public Rectangle getHitBox(){
        return this.hitBox;
    }

    public Vector2 getCurrentPosition() {
        return currentXY;
    }

    public void setCurrentPosition(float newX, float newY) {
        currentXY.x = newX;
        currentXY.y = newY;
    }
}

