package com.project.practice;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;

// ALERT: Main character is lizard temporarily
// ALERT: Change Coordinate system to pure vectors??

// Sprite is a subclass of texture with movement functonality
class Sprite extends Texture{
    // ALERT: SET TO BACKGROUND DIMENSIONS
    private static float worldHeight = 3000;
    private static float worldWidth = 3000;

    public float currentX= 0, currentY=0; // Always world Coordinates
    private float movingToX, movingToY;
    private float speed = 500;
    private boolean isMoving = false;

    private float width = this.getWidth();
    private float height = this.getHeight();

    // Creates the Texture(Parent Class)
    Sprite(String path) {
        super(path);
    }


    // Move Functionality
    // Set the position of the sprite
    public void setPosition(float x, float y) {
        this.currentX = x;
        this.currentY = y;
    }

    // Set the destination for the sprite to move towards
    public void setDestination(Vector3 coords) {
        // Recieves World Coordinates
        isMoving = true;
        this.movingToX = coords.x-(width/2);    // Subtracts sprite width to centre
        this.movingToY = coords.y-(height/2);
    }

    public void move(float delta,OrthographicCamera camera) {
        // Check if supposed to move
        if(!isMoving)
            return;

        movingToX = MathUtils.clamp(movingToX, 0, worldWidth-width);
        movingToY = MathUtils.clamp(movingToY, 0, worldHeight-height);

        Vector2 destVector = new Vector2(movingToX - currentX, movingToY - currentY);   // Calculate vector
        


        if (destVector.len() > 5) {
            destVector.nor().scl(delta*speed);    // Normalize than multiply(scale) by speed and delta time
            // Change current coordiantes
            currentX += destVector.x;
            currentY += destVector.y;
        }
        else
        {
            isMoving = false;
        }
    }

}

public class Main extends ApplicationAdapter {

    private float worldHeight = 3000;
    private float worldWidth = 3000;

    // Declare Spritbatch and texture for Images
    SpriteBatch batch;
    Texture background;
    Sprite lizard;

    // Declare camera
    OrthographicCamera camera;

    // Declare arrraylist of textures for disposing
    ArrayList<Texture> textures = new ArrayList<>();
    @Override
    public void create() {

    // Initialize SpriteBatch and Texture
    batch = new SpriteBatch();
    background = new Texture("bg.png");
    lizard = new Sprite("Lizard.png");

    // Add textures to the list for disposing
    textures.add(background);
    textures.add(lizard);

    // Initialize Camera
    camera = new OrthographicCamera(1680, 768);
    camera.position.set(1680/2f, 768/2f, 0);
    camera.update();
        
    }

    @Override
    public void render() {
    
    // ALERT: Google this, its important
    float delta = Gdx.graphics.getDeltaTime();
    
    leftClick();    // Check left click movement
    cameraRoam(delta);  // Camera Free Roam
    
    lizard.move(delta,camera);

    draw();

    }

    @Override
    public void dispose() {
        // ALERT: Use TexturePacker
        batch.dispose();
        // Dispose all textures in the list
        for (Texture texture : textures) {
            texture.dispose();
        
        }
    }

    // Mouse Click Event
    private void leftClick(){
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            // Vector is basically a class with 3 data members, includes methods for magnitude,normalization(unit vector)
            Vector3 clickCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(clickCoords);  // Converts screen coords to World coords
            lizard.setDestination(clickCoords); // Set destinaion 
        }
    }
    // Camera Roam
    private void cameraRoam(float delta){
        float cameraSpeed = 500;

        float dx = 0;
        float dy  = 0;
        // Check inputs
        boolean up = Gdx.input.isKeyPressed(Keys.W);
        boolean down =  Gdx.input.isKeyPressed(Keys.S);
        boolean right =  Gdx.input.isKeyPressed(Keys.D);
        boolean left =  Gdx.input.isKeyPressed(Keys.A);
        
        // Check if any movement
        boolean moving = up || down || right || left ;

        if(!moving){
            // Set camera position to lizard, ALERT change to hero
            camera.position.x = MathUtils.clamp(lizard.currentX,camera.viewportWidth/2 -10,worldWidth - camera.viewportWidth/2 +10);
            camera.position.y = MathUtils.clamp(lizard.currentY, camera.viewportHeight/2 -10, worldHeight- camera.viewportHeight/2 +10);
        }

        // Movement
        if(right){
            dx = cameraSpeed*delta;
        }
        if(left){
            dx = -cameraSpeed*delta;
        }
        if(up){
            dy = cameraSpeed*delta;
        }
        if(down){
            dy = -cameraSpeed*delta;
        }

        //Translating camera
        camera.translate(dx,dy,0);

        //Bounding camera with world edges
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        //Restricts camera borders to the map
        camera.position.x = MathUtils.clamp(camera.position.x, halfWidth -10, worldWidth - halfWidth +10);
        camera.position.y = MathUtils.clamp(camera.position.y, halfHeight -10, worldHeight - halfHeight +10);
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
    }

    // Main draw function, draws everything that has breath, praise the Lord
    private void draw(){
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw Sprites
        batch.begin();
        batch.draw(background, 0, 0);    // Draw background
        batch.draw(lizard, lizard.currentX, lizard.currentY);    // Draw lizard
        batch.end();
    }

}