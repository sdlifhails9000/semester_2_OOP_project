package com.gdx.game;

//import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
//import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Animation;           //Animation imports are these two
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.viewport.*;       //Gets all viewport types (FixViewport, StrectViewport, ExtendViewport, etc)

//Tiled Map Loaders
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

// ALERT: Main character is lizard temporarily
// ALERT: Change Coordinate system to pure vectors??

// Sprite is a subclass of texture with movement functonality
// class Sprite extends Texture{
//     // ALERT: SET TO BACKGROUND DIMENSIONS
//     private static float worldHeight = 3000;
//     private static float worldWidth = 3000;

//     public float currentX= 0, currentY=0; // Always world Coordinates
//     private float movingToX, movingToY;
//     private float speed = 500;
//     private boolean isMoving = false;

//     private float width = this.getWidth();
//     private float height = this.getHeight();

//     // Creates the Texture(Parent Class)
//     Sprite(String path) {
//         super(path);
//     }


//     // Move Functionality
//     // Set the position of the sprite
//     public void setPosition(float x, float y) {
//         this.currentX = x;
//         this.currentY = y;
//     }

//     // Set the destination for the sprite to move towards
//     public void setDestination(Vector3 coords) {
//         // Recieves World Coordinates
//         isMoving = true;
//         this.movingToX = coords.x-(width/2);    // Subtracts sprite width to centre
//         this.movingToY = coords.y-(height/2);
//     }

//     public void move(float delta,OrthographicCamera camera) {
//         // Check if supposed to move
//         if(!isMoving)
//             return;

//         movingToX = MathUtils.clamp(movingToX, 0, worldWidth-width);
//         movingToY = MathUtils.clamp(movingToY, 0, worldHeight-height);

//         Vector2 destVector = new Vector2(movingToX - currentX, movingToY - currentY);   // Calculate vector
        


//         if (destVector.len() > 5) {
//             destVector.nor().scl(delta*speed);    // Normalize than multiply(scale) by speed and delta time
//             // Change current coordiantes
//             currentX += destVector.x;
//             currentY += destVector.y;
//         }
//         else
//         {
//             isMoving = false;
//         }
//     }

// }

public class MainGame extends ApplicationAdapter {
    //-----MAP WORK DECLARATION START----
    //ALERT: google why we use tmxmaploader and tiledmap together

    //Declare Maploader using TmxMapLoader
    TmxMapLoader mapLoader;

    //Declare Map var using TiledMpa
    TiledMap map;

    //Declare mapRendered by OrthogonalTiledMapRenderer
    OrthogonalTiledMapRenderer mapRenderer;

    //-----MAP WORK DECLARATION END----

    //Initializing gameWorld sizing and camera sizing
    float worldWidth = 200;                // -> Playable Region (Scaled to 1 tile = 1 world units)
    float worldHeight = 200;               //Equivalent to a pixel in tiled map

    float mapWidth = 50;
    float mapHeight = 50;       //Map number of tiles i.e 50x50 tiles
    float tileSize = 16;        //Each tile size in pixels (16x16)

    float scale = (worldWidth / mapWidth) / tileSize;       //WORKS FOR SQUARE DIMENSION ONLY       Current scale is 1 tile = 4 gameUnits

    float aspectRatio = 0.5625f;        //Use to fix scaling (comment out viewport portion and see orthocamera libgdx way)

    private float cameraWidth = 50;                 // -> Visible Region
    private float cameraHeight = 50;

    //Player move speed and camera move speed
    float speed = 20f;                 // -> Player Move Speed (based off above world size)
    float cameraSpeed = 30;             // -> Camera Move Speed (Based off above world size)

    //Use them to define starting position for camera and sprite
    int startX = 10;
    int startY = 10;

    //Health bar percent (testing to show dmg taken and if health bar works as intended)
    float healthPercent = 0.2f;   //(Keep value between 0 and 1)

    //For angle calculation to rotate sprite    (used in updateMovement())
    float angle;

    //Boolean setup to check if moving or not
    boolean spriteMovement = false;
    boolean movingRight;

    // Declare Spritbatch and textureAtlas for Images
    SpriteBatch batch;
    Sprite player, background, healthBarSprite;

    TextureAtlas atlas;
    TextureAtlas heroAtlas;

    //Declaring animations (of type TextureRegions)
    Animation<TextureRegion> heroRunAnimation;
    Animation<TextureRegion> heroIdleAnimation;
    Animation<TextureRegion> currentAnimation;

    //A variable to track elapsed time during animation
    float stateTime;

    //Initialize vectors 2d
    Vector2 currentXY;
    Vector2 targetXY;
    Vector2 destVector;

    //Initialize 3D vector (since we have to unproject camera and it takes vector3 not vector 2)
    Vector3 clickCoords;
    Vector3 heroPos;

    //Declaring a viewport (to handle resizing) (You can use anykind fit extend etc just change name)
    Viewport viewport;      //Viewport is an abstract class so cannot be instantiated and should be downcasted to child (extend, fit, stretchViewport, etc)

    // Declare camera
    OrthographicCamera camera;

    @Override
    public void create() {
        // Initialize SpriteBatch
        batch = new SpriteBatch();

        //Initialize TextureAtlas
        atlas = new TextureAtlas(Gdx.files.internal("atlas\\practiceAtlas.atlas"));
        heroAtlas = new TextureAtlas(Gdx.files.internal("HeroAtlas\\Hero.atlas"));

        //Initialize bg and sprite and health bar from atlas
        
        //background = atlas.createSprite("Background");        //No need since we made our tiled map
        healthBarSprite = atlas.createSprite("healthBar");

        //Initialize the animation
        heroRunAnimation = new Animation<TextureRegion>(0.033f, heroAtlas.findRegions("Run"), PlayMode.LOOP);   //heroAtlas.findRegion gives a textureRegion from a big Texture (the big Hero.png)
        heroIdleAnimation = new Animation<TextureRegion>(0.1f, heroAtlas.findRegions("Idle"), PlayMode.LOOP); 
        currentAnimation = heroIdleAnimation;       //In beginning run the idle animation until action performed

        //Initialize stateTime
        stateTime = 0f;

        //Initialize the player sprite who will handle animations aswell
        player = new Sprite(currentAnimation.getKeyFrame(stateTime));       //Initializes sprite with very first frame of currentAnimation (stateTime = 0)

        
        //Set background and lizard size+position
        //background.setSize(worldWidth, worldHeight);      NO NEED ANYMORE
        //background.setPosition(0,0);

        player.setSize(6,8);        //Game world units
        player.setOriginCenter();
        player.setCenter(startX, startY);            //Centre of world     (setPosition draws from bottom left setCentre draws from centre)

        //Initialize starting vector coords (sprite coords here its lizard)
        currentXY = new Vector2(startX, startY);     //Starting points (game World Coords not screen coords)

        //Initialize the 3d version of currentXY (for camera lerping in cameraRoam() method)
        heroPos = new Vector3(currentXY.x, currentXY.y, 0);


        // Initialize Camera
        float height = Gdx.graphics.getHeight();    //For aspect ration calculation
        float width = Gdx.graphics.getWidth();

        camera = new OrthographicCamera(cameraWidth, cameraHeight * (height / width));        //Visible region (multiplied height by aspect ratio)
        camera.position.set(startX, startY, 0);         //Same as lizard
        camera.update();

        //Initialize Viewport (for scaling and resizing)
        viewport = new ExtendViewport(cameraWidth, cameraHeight, camera);  //Same sizing as camera as we want to scale camera coords if resized (for extend no need for aspect ratio google reason)
        
        //----MAPWORK INITIALIZATION----
        mapLoader  = new TmxMapLoader();

        //Initialize the tiledMap
        map = mapLoader.load("practiceMap\\MainMap.tmx");

        

        //Initialize the mapRenderer (this is the main working unit here which scales the tiledMap with our current game units)
        mapRenderer = new OrthogonalTiledMapRenderer(map, scale);       //We can pass our own spritebatch for optimization but will have to change some internal methods so jst let it use its own spritebatch for map
    }

    @Override
    public void render() {
        //delta exists in render because its the amount of time between first and second frame
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;     //Update stateTime in render
        
        leftClick();    // Check left click movement
        updateMovement(delta);
        cameraRoam(delta);  // Camera Free Roam
        updateHealthBar();  //Updates position and size of health bar

        camera.update();    //Update camera
        
        

        viewport.apply();   //Checks every frame if resized occured
        batch.setProjectionMatrix(camera.combined); 
        draw();

    }
    

    @Override
    public void resize(int width, int height){
        viewport.update(width, height, true);       //True to centre camera when resized (not resizing camera here as viewport was made to handle it)

        //USE THIS IF YOU WANT FIXED RESIZING SCALE (Abit cursed)

        // float aspect = (float) width / (float) height;
        // camera.viewportWidth = cameraWidth * aspect;
        // camera.viewportHeight = cameraHeight;
    }

     @Override
    public void dispose() {
        // ALERT: Use TexturePacker
        // Dispose all textures in the list
        batch.dispose();
        atlas.dispose();
        heroAtlas.dispose();

        //Clear our map resources
        map.dispose();
        mapRenderer.dispose();      //THIS DISPOSE ISNT NECESSARY BECAUSE OUR MAP IS HANDLED IN batch ABOVE but its a good practice
    }


    //METHODS USED IN CREATE, RENDER, ETC OVER HERE 

    // Main draw function, draws everything that has breath, praise the Lord
    private void draw(){
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Draw map and set camera view (OUTSIDE of batch.begin because it has its own spriteBatch initialized (google for more info))
        mapRenderer.setView(camera);    //This makes our tiled map use our camera we made
        mapRenderer.render();           //Render the map obv

        // Draw the batch
        batch.begin();

        //Draw the sprites
        //background.draw(batch);    //No need for this as now as you can see above we render in our own map
        player.draw(batch);    // Draw player
        healthBarSprite.draw(batch);    //Draw HealthBar

        batch.end();
    }
    
    // Mouse Click Event
    private void leftClick(){
        
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            // Vector is basically a class with 3 data members, includes methods for magnitude,normalization(unit vector)
            clickCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0 );
            camera.unproject(clickCoords);  // Converts screen coords to World coords

            float gameX = clickCoords.x;        //clickCoords gives bottom left coords so to make target centre we correct it by adding
            float gameY = clickCoords.y;        //We do not do correction because sprite is drawn using setCentre not setPosition. Also issue before was in destVector (notes over there)

            spriteMovement = true;      //Movement occured
        
        
            targetXY = new Vector2(gameX, gameY); // Set target vector and subtract dimension of width and height of sprite
            targetXY.x = MathUtils.clamp(targetXY.x, player.getWidth() / 2, worldWidth - player.getWidth() / 2);        //Doing correction because target is centered
            targetXY.y = MathUtils.clamp(targetXY.y, player.getHeight() / 2,worldHeight - player.getHeight() / 2);      //Binding it to world width and height dimensions
        }
    }
    private void updateMovement(float delta){
        
        if (!spriteMovement){      //Signifies no mouseclick yet
            currentAnimation = heroIdleAnimation;       //Set to idle mode
            player.setRegion(currentAnimation.getKeyFrame(stateTime));
            return;
        }
        else{
            currentAnimation = heroRunAnimation;
            player.setRegion(currentAnimation.getKeyFrame(stateTime));
        }

        destVector = new Vector2();
        destVector.set(targetXY).sub(currentXY);

        //-----ROTATION CALCULATION START-----
        //Sort of skews off at endpoint likeeee just test and see (Works perfectly for bottom edge but skewed for the other 3)

        angle = MathUtils.atan2Deg360(destVector.y, destVector.x);      //atan2Deg360 ensures that whatever y and x is we get angle in range of 0 to 360 not from 180 to -180
        
        //-----NOTE----
        //you can add rotation or not your choice just uncomment the player.setRotation() lines to see it in play

        if (angle > 90 && angle < 270){ 
            //player.setRotation(angle + 180);        //We have to do a 180 degree CORRECTION offset because setFlip offsets the angle by 180 degrees (inverts x axis)            //This range signifies 2 and 3 quadrant 
            player.setFlip(true, false);       //Flip the x axis
        }

        else{
            //player.setRotation(angle);         //Same reason as above
            player.setFlip(false, false);      //If its a click in 1 or 4 quadrant no flip just bring sprite to what it was originally (sprite was originally drawn right facing)
        }

        //-----ROTATION CALCULATION END-----

        if (destVector.len() > 0.5) {         //Previously it was 5 (from saad) which was too big for my gameWorld Coords so adjusted it
            destVector.nor().scl(delta*speed);    // Normalize then multiply(scale) by speed and delta time

            // Change current coordiantes
            currentXY.add(destVector);      //Updates the current vector co-ordiantes
            heroPos.set(currentXY, 0);     //Setting heroPos vector as 3d version of currentXY to be used in cameraRoam()
            player.setCenter(currentXY.x, currentXY.y);     //Update player position
        }
        else{
            spriteMovement = false;
        }
        //No need for updating camera over here as its handled in cameraRoam otherwise it causes conflict between two

    }
    // Camera Roam
    private void cameraRoam(float delta){
        //Camera measurements
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        //Movement
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
            //Camera movement
            camera.position.lerp(heroPos, 0.1f);            //THIS IS WHERE heroPos IS BEING USED (this brings smoothness for camera following)

            //No need for the camera clamp commented below because hero is already clamped 
            //and in free roam camera is clamped (it doesnt break you can test)

            //camera.position.x = MathUtils.clamp(heroPos.x, halfWidth - 1, (worldWidth - halfWidth) + 1);      
            //camera.position.y = MathUtils.clamp(heroPos.y, halfHeight - 1, (worldHeight - halfHeight) + 1);  
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

        //Restricts camera borders to the map
        camera.position.x = MathUtils.clamp(camera.position.x, halfWidth -1, worldWidth - halfWidth +1);
        camera.position.y = MathUtils.clamp(camera.position.y, halfHeight -1, worldHeight - halfHeight +1);
        
    }

    public void updateHealthBar(){
        healthBarSprite.setCenter(currentXY.x, currentXY.y + 3);        //Setting it just above our hero sprite
        float healthBarWidth = 5 * healthPercent;
        float healthBarHeight = 5;

        healthBarSprite.setSize(healthBarWidth, healthBarHeight);


    }

}