//NOTES
    //Make arraylists of all types so for playertypes whose movement attack, etc has same logic make an array list for that
    //For goblins (Whose movement logic is obv different from player (they wont use LeftClick()) make a seperate array list and then update where necessary
    //REASON why i removed updateAllMovement() was that each subclass will have its unique way of feeding value to whatever updateMovement it has defined
    //So i thought to make that updateAllMovement an abstract method THOUGH if its uesless later on when we actually make other child classes.
    //Then just make Update() in DynamicSprite a concrete method in whatever way you see fit.

package com.gdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;       //Gets all viewport types (FixViewport, StrectViewport, ExtendViewport, etc)
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
//Tiled Map Loaders
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;


// This contain default values for specific hero types

public class MainGame extends ApplicationAdapter {
    public static AssetManager manager = new AssetManager();

    //-----MAP WORK DECLARATION START----
    //ALERT: google why we use tmxmaploader and tiledmap together

    //Declare Maploader using TmxMapLoader
    TmxMapLoader mapLoader;

    //Declare Map var using TiledMap
    TiledMap map;

    //Declare mapRendered by OrthogonalTiledMapRenderer

    OrthogonalTiledMapRenderer mapRenderer;

    // ----MAP WORK DECLARATION END----

    //Initializing gameWorld sizing and camera sizing
    final static float worldWidth = 800f;                // -> Playable Region (Scaled to 1 tile = 1 world units)
    final static float worldHeight = 108f;               //Equivalent to a pixel in tiled map

    static float mapWidth = 200f;
    static float mapHeight = 27f;       //Map number of tiles i.e 50x50 tiles
    static float tileSize = 16f;        //Each tile size in pixels (16x16)

    static float scale = (worldWidth/mapWidth)/tileSize;       // Current scale is 1 tile = 4 gameUnits

    float aspectRatio = 0.5625f;        //Use to fix scaling (comment out viewport portion and see orthocamera libgdx way)

    private float cameraWidth = 80f;                 // -> Visible Region
    private float cameraHeight = 80f;

    //Player move speed and camera move speed
    float cameraSpeed = 40f;             // -> Camera Move Speed (Based off above world size)

    Vector3 clickCoords;

    // Declare Spritbatch and textureAtlas for Images
    SpriteBatch batch;
    Sprite background, healthBarSprite;

    //DynamicSprites (self defined)
    HeroPlayer player;
    HeroPlayer testEnemy;

    //DynamicEntities (Bots)
    Bot g1, g2, g3, g4, g5, g6;


    //A variable to track elapsed time during animation
    // float stateTime; //Handled in Entity.java

    //Initialize 3D vector (since we have to unproject camera and it takes vector3 not vector 2)

    //Declaring a viewport (to handle resizing) (You can use anykind fit extend etc just change name)
    Viewport viewport;      //Viewport is an abstract class so cannot be instantiated and should be downcasted to child (extend, fit, stretchViewport, etc)

    // Declare camera
    OrthographicCamera camera;

    // TODO: REMOVE
    // DEBUGGING HITBOXES
    ShapeRenderer shapeRenderer;  // DEBUG tool

    // Pathfinding
    static boolean[][] blocked;

    @Override
    public void create() {
        // Initialize SpriteBatch
        batch = new SpriteBatch();


        // -------------- Queue all the assets in `manager` --------------

        manager.load("atlas\\practiceAtlas.atlas", TextureAtlas.class);

        //Loads HeroPreset which is made in Loader.java
        for (HeroPreset preset : HeroPreset.values()) {
            manager.load(preset.assetPath, TextureAtlas.class);
        }

        //Loads GoblinPreset which is made in Loader.java
        for (GoblinPreset preset: GoblinPreset.values()){
            manager.load(preset.assetPath, TextureAtlas.class);
        }

        manager.setLoader(TiledMap.class, new TmxMapLoader());
        manager.load("practiceMap/projectmap.tmx", TiledMap.class);

        manager.finishLoading(); // We could add a loading screen here if there are enough assets that it becomes slow

        // -------------- Make all the necessary thingies --------------

        Loader.load(manager);
        TextureAtlas atlas = manager.get("atlas\\practiceAtlas.atlas");

        // Initialize health bar from atlas

        healthBarSprite = atlas.createSprite("healthBar");

        clickCoords = new Vector3();

        //Initialize the DYNAMIC SPRITES
        player = new HeroPlayer(HeroPreset.HERO_HEAVY, 400, 50);
        testEnemy = new HeroPlayer(HeroPreset.ENEMY_HERO_HEAVY, 20, 20);

        // //Initialize the goblins
        // g1 = new Bot(GoblinPreset.GOBLIN, 10,20);
        // // g2 = new Goblin(Preset.GOBLIN, 20,20);
        // // g3 = new Goblin(Preset.GOBLIN, 20,10);
          g4 = new Bot(GoblinPreset.ENEMY_GOBLIN, 300,30);
        // // g5 = new Goblin(Preset.ENEMY_GOBLIN, 180,180);
        // // g6 = new Goblin(Preset.ENEMY_GOBLIN, 180,190);

        // Initialize Camera
        float height = Gdx.graphics.getHeight();    //For aspect ratio calculation
        float width = Gdx.graphics.getWidth();

        camera = new OrthographicCamera(cameraWidth, cameraHeight * (height / width));        //Visible region (multiplied height by aspect ratio)
        camera.position.set(player.getCurrentPosition(), 0);         //Takes vector3 so pass in the whole method made in DynamicSprite class
        camera.update();

        //Initialize Viewport (for scaling and resizing)
        viewport = new ExtendViewport(cameraWidth, cameraHeight, camera);  //Same sizing as camera as we want to scale camera coords if resized (for extend no need for aspect ratio google reason)

        //----MAPWORK INITIALIZATION----

        //Initialize the tiledMap
        map = manager.get("practiceMap/projectmap.tmx");

        //Initialize the mapRenderer (this is the main working unit here which scales the tiledMap with our current game units)
        mapRenderer = new OrthogonalTiledMapRenderer(map, scale);       //We can pass our own spritebatch for optimization but will have to change some internal methods so jst let it use its own spritebatch for map

        // TO DO: REMOVE
        shapeRenderer = new ShapeRenderer();  // DEBUG tool

        // Pathfinding
        blocked = new boolean[(int)worldWidth][(int)worldHeight];
        blocked = getGrid();
        Bot.blocked = blocked;

        // store all the map collisions
        ArrayList<Rectangle> boundaryCollisions = getMapCollisions();
        DynamicEntity.boundaryCollisions = boundaryCollisions;


    }

    @Override
    public void render() {
        //delta exists in render because its the amount of time between first and second frame
        float delta = Gdx.graphics.getDeltaTime();  //StateTime is being handled in Update of entity

        clickEvent(delta);    // Check left click movement

        for (Entity e : Entity.entityList) {
            e.Update(delta);
        }

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
        batch.dispose();
        manager.dispose();
        shapeRenderer.dispose();   // TO DO: Remove DEBUG tool
        mapRenderer.dispose();
    }


    //METHODS USED IN CREATE, RENDER, ETC OVER HERE

    // Main draw function, draws everything that has breath, praise the Lord
    private void draw(){
        ScreenUtils.clear(Color.BLACK);

        //Draw map and set camera view (OUTSIDE of batch.begin because it has its own spriteBatch initialized (google for more info))
        mapRenderer.setView(camera);    //This makes our tiled map use our camera we made
        mapRenderer.render();           //Render the map obv

        // Draw the batch
        batch.begin();

        //Draw the sprites
        healthBarSprite.draw(batch);    //Draw HealthBar

        //Draw all the entities by for loop
        for (Entity e : Entity.entityList){
            e.draw(batch);
        }

        batch.end();

        // TO DO: REMOVE START {

        //DEBUG FOR COLLISION
        shapeRenderer.setProjectionMatrix(camera.combined);
        ArrayList<Rectangle> listOfHitbox = new ArrayList<Rectangle>();

        for (Entity e : Entity.entityList) {
            listOfHitbox.add(e.getCollisionBox());
        }

        for (Rectangle rect : getMapCollisions()) {
            listOfHitbox.add(rect);
        }
        
        // Grid is sized to match map tiles (200x27 grid from getGrid)
        float cellSize = worldWidth / mapWidth;  // = 800/200 = 4 world units per tile
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int x = 0; x < blocked.length; x++) {
            for (int y = 0; y < blocked[0].length; y++) {

                if (blocked[x][y]) {
                    shapeRenderer.setColor(1, 0, 0, 0.5f); // red = blocked
                    shapeRenderer.rect(
                    x * cellSize,
                    y * cellSize,
                    cellSize,
                    cellSize
                );
                }

                // Render at map tile position (already in world units
            }
        }
        shapeRenderer.end();

        // shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // for(Rectangle rect : listOfHitbox) {
        //     shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        // }
        // shapeRenderer.end();


        // } REMOVE END
    }


    // Implement later for handling keyboard events
    // private void keyEvent(float delta) {

    // }

    // Mouse Click Event
    private void clickEvent(float delta){
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            // Vector is basically a class with 3 data members, includes methods for magnitude,normalization(unit vector)
            clickCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0 );
            camera.unproject(clickCoords);  // Converts screen coords to World coords

            //NOTE: Handling an edge case, because camera is only place which needs Vector3
            Vector2 clickCoords2D = new Vector2(clickCoords.x, clickCoords.y);     //We do this because clickCoords is only place where we need a 3d Vector. Everywhere else a 2d

            //We use for loop for playerEntities because bots i.e goblins etc will not get leftClick. Their movement call is seperate
            clickCoords2D.x = MathUtils.clamp(clickCoords2D.x, player.getWidth() / 2, worldWidth - player.getWidth() / 2);        //Doing correction because target is centered
            clickCoords2D.y = MathUtils.clamp(clickCoords2D.y, player.getHeight() / 2,worldHeight - player.getHeight() / 2);      //Binding it to world width and height dimensions
            player.setTargetPosition(clickCoords2D.x, clickCoords2D.y);     //Using the small fix for edge case above
        }
        else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            clickCoords = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0 );
            camera.unproject(clickCoords);  // Converts screen coords to World coords

            Vector2 clickCoords2D = new Vector2(clickCoords.x, clickCoords.y);
            clickCoords2D.x = MathUtils.clamp(clickCoords2D.x, player.getWidth() / 2, worldWidth - player.getWidth() / 2);
            clickCoords2D.y = MathUtils.clamp(clickCoords2D.y, player.getHeight() / 2,worldHeight - player.getHeight() / 2);

            for (Entity e : Entity.entityList) {
                //Do not set yourself as a target
                if (e == player) {
                    continue;
                }

                //Do not set ally as targets
                if(e.isAlly == player.isAlly){
                    continue;
                }

                if (e.getCollisionBox().contains(clickCoords2D)) {
                    player.setAttackTarget(e);
                    break;
                }
            }
        }
    }

    boolean detachedState = false;

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

        if (Gdx.input.isKeyJustPressed(Keys.Q)) {
            detachedState = !detachedState;
        }

        if(!moving && !detachedState){
            // Set camera position to lizard, ALERT change to hero
            //Camera movement
            Vector3 playerPosition3D = new Vector3(player.getCurrentPosition(), 0);

            camera.position.lerp(playerPosition3D, 0.1f);            //THIS IS WHERE heroPos IS BEING USED (this brings smoothness for camera following)

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
        healthBarSprite.setCenter(player.getCurrentPosition().x, player.getCurrentPosition().y + 3);        //Setting it just above our hero sprite
        float healthBarWidth = 5 * player.currentHealth / player.maxHealth;
        float healthBarHeight = 5;

        healthBarSprite.setSize(healthBarWidth, healthBarHeight);

    }

    // this method return an arraylist with all static collision boxes (environment)
    public ArrayList<Rectangle> getMapCollisions() {
        ArrayList<Rectangle> mapCollisions = new ArrayList<Rectangle>();

        for (MapLayer mapLayer : map.getLayers()){
            if(mapLayer instanceof TiledMapTileLayer){   // gives us only tiles layers not object or image
                TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer; // downcast 

                for (int x = 0; x < layer.getWidth(); x++){    // loop through horizontal tiles
                    for(int y = 0; y < layer.getHeight(); y++){    // loop through vertical tiles
                        TiledMapTileLayer.Cell cell = layer.getCell(x, y);

                        if (cell == null) continue;     // skip cell if its empty
                        TiledMapTile tile = cell.getTile();         // gets the actual tile in the map

                        if (tile == null) continue;     // skip if tile is empty
                        MapObjects objects = tile.getObjects();     // return collision data of tile

                        
                        for (MapObject obj : objects){          // loop through 
                            if (obj instanceof RectangleMapObject){     // check if collision is rectangular collision
                                Rectangle rect = ((RectangleMapObject) obj).getRectangle(); // gives us the collision shape of tile 
                                Rectangle worldRect = new Rectangle(        // Convert from tile coordinates to world coordinates
                                    (rect.x + x * layer.getTileWidth()) * scale,
                                    (y * layer.getTileHeight() + rect.y) * scale,
                                    rect.width * scale,
                                    rect.height * scale
                                );
                            mapCollisions.add(worldRect);       // store it in our array list of collisions
                            }
                        }
                    }
                }
            }    
        }
        return mapCollisions;
    }

    
    // this method return an arraylist with all static collision boxes (environment)
    public boolean[][] getGrid() {
        boolean[][] blocked;

        // Find a base layer for dimensions - use the same layer that getMapCollisions uses
        TiledMapTileLayer baseLayer = null;
        TiledMapTileLayer gridLayer = null;

        for (MapLayer l : map.getLayers()) {
            if (l instanceof TiledMapTileLayer) {
                TiledMapTileLayer layer = (TiledMapTileLayer) l;
                if (baseLayer == null) {
                    baseLayer = layer;
                }
                // Use the layer with most tiles for the grid
                if (gridLayer == null || layer.getWidth() * layer.getHeight() > gridLayer.getWidth() * gridLayer.getHeight()) {
                    gridLayer = layer;
                }
            }
        }
        if (gridLayer == null) return null;

        // Use actual layer dimensions instead of mapWidth+3
        int width = gridLayer.getWidth();
        int height = gridLayer.getHeight();

        blocked = new boolean[width][height];

        // Use the same iteration as getMapCollisions - check ALL layers
        for (MapLayer mapLayer : map.getLayers()){
            if(mapLayer instanceof TiledMapTileLayer){   // gives us only tiles layers not object or image
                TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer; // downcast 

                for (int x = 0; x < layer.getWidth(); x++){    // loop through horizontal tiles
                    for(int y = 0; y < layer.getHeight(); y++){    // loop through vertical tiles

                        TiledMapTileLayer.Cell cell = layer.getCell(x, y);

                        if (cell == null) continue;     // skip cell if its empty
                        TiledMapTile tile = cell.getTile();         // gets the actual tile in the map

                        if (tile == null) continue;     // skip if tile is empty
                        
                        // Use SAME logic as getMapCollisions - only check for RectangleMapObject
                        MapObjects objects = tile.getObjects();
                        for (MapObject obj : objects) {
                            if (obj instanceof RectangleMapObject) {
                                // Only mark as blocked if within grid bounds
                                if (x < width && y < height) {
                                    blocked[x][y] = true;
                                }
                                break; // Found a collision, no need to check more objects
                            }
                        }
                    }
                }
            }
        }
        
        // Debug output
        System.out.println("Blocked grids");
        for(int k=0;k<blocked.length;k++)
            for(int j=0;j<blocked[0].length;j++){
                if(blocked[k][j]){
                    System.out.println(k+","+j);
                }
            }

        return blocked;
    }
}

