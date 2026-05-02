//NOTES
    //Make arraylists of all types so for playertypes whose movement attack, etc has same logic make an array list for that
    //For goblins (Whose movement logic is obv different from player (they wont use LeftClick()) make a seperate array list and then update where necessary
    //REASON why i removed updateAllMovement() was that each subclass will have its unique way of feeding value to whatever updateMovement it has defined
    //So i thought to make that updateAllMovement an abstract method THOUGH if its uesless later on when we actually make other child classes.
    //Then just make Update() in DynamicSprite a concrete method in whatever way you see fit.

package com.gdx.game;

import java.util.ArrayList;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
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
import jdk.javadoc.internal.doclets.formats.html.Table;


// This contain default values for specific hero types

public class GameScreen implements Screen {
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
    Vector3 hoverCoords;

    // Declare Spritbatch and textureAtlas for Images
    SpriteBatch batch;

    //DynamicSprites (self defined)
    HeroPlayer player;
    HeroPlayer enemy;

    //DynamicEntities (Bots)
    Bot g1, g2, g3, g4, g5, g6;

    Tower mainTower; // for testing

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
    ShapeRenderer shapeRendererGreen;

    // Pathfinding
    static boolean[][] blocked;

    MainGame game;

    public GameScreen(MainGame entry, HeroPreset preset) {
        // Initialize SpriteBatch
        batch = new SpriteBatch();

        game = entry;

        // -------------- Make all the necessary thingies --------------

        Loader.load(game.manager);

        clickCoords = new Vector3();
        hoverCoords = new Vector3();

        //Initialize the DYNAMIC SPRITES
        mainTower = new Tower(TowerPreset.ENEMY_MAIN, 400, 54);

        //DRAW THE TOWER FIRST SO THAT WHEN TOWER DIES PLAYER CANNOT HIDE UNDER ITS RUBBLE
        player = new HeroPlayer(preset, 300, 50);
        enemy = new HeroPlayer(HeroPreset.ENEMY_HERO_HEAVY, 20, 20);

        // //Initialize the goblins
        // g1 = new Bot(GoblinPreset.GOBLIN, 10,20);
        // // g2 = new Goblin(Preset.GOBLIN, 20,20);
        //g3 = new Bot(GoblinPreset.GOBLIN, 300,30);
        g4 = new Bot(GoblinPreset.ENEMY_GOBLIN, 300,40);
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
        map = game.manager.get("practiceMap/projectmap.tmx");

        //Initialize the mapRenderer (this is the main working unit here which scales the tiledMap with our current game units)
        mapRenderer = new OrthogonalTiledMapRenderer(map, scale);       //We can pass our own spritebatch for optimization but will have to change some internal methods so jst let it use its own spritebatch for map

        // TO DO: REMOVE
        shapeRenderer = new ShapeRenderer();  // DEBUG tool
        shapeRendererGreen = new ShapeRenderer();
        // Pathfinding
        blocked = new boolean[(int)worldWidth][(int)worldHeight];
        Bot.blocked = blocked;

        // store all the map collisions
        ArrayList<Rectangle> boundaryCollisions = getMapCollisions();
        DynamicEntity.boundaryCollisions = boundaryCollisions;


    }

    @Override
    public void render(float delta) {
        if (mainTower.isDead) {
            game.setScreen(new EndGameScreen(game, true));
        }

        clickEvent();    // Check left click movement
        hoverEvent();    //Check if

        for (Entity e : Entity.entityList) {
            e.Update(delta);
        }

        cameraRoam(delta);  // Camera Free Roam

        camera.update();    //Update camera

        viewport.apply();   //Checks every frame if resized occured
        batch.setProjectionMatrix(camera.combined);
        draw();

    }

    //METHODS USED IN CREATE, RENDER, ETC OVER HERE

    // Main draw function, draws everything that has breath, praise the Lord
    private void draw(){
        ScreenUtils.clear(Color.BLACK);

        //Draw map and set camera view (OUTSIDE of batch.begin because it has its own spriteBatch initialized (google for more info))
        mapRenderer.setView(camera);    //This makes our tiled map use our camera we made
        mapRenderer.render();           //Render the map obv

        //To get goblinHealth bar to be viewed by right Click (See getGoblinHealthBarSprite())
        Sprite showGobHealthBarSprite = getGoblinHealthBarSprite();         //This is made so that when we right click on a goblin only then we can see its healthbar
        Sprite hoverOverHealthBarSprite = getHovSprite();

        // Draw the batch
        batch.begin();
        //Draw all the entities by for loop
        for (Entity e : Entity.entityList){
            e.draw(batch);
        }

        //Draw the player health bar
        player.HealthBarSprite.draw(batch);

        if (showGobHealthBarSprite != null){
            showGobHealthBarSprite.draw(batch);
        }

        if (hoverOverHealthBarSprite != null){
            hoverOverHealthBarSprite.draw(batch);
        }


        //Draw the enemy healthbar
        enemy.HealthBarSprite.draw(batch);

        for (Tower t : Tower.towerList) {
            t.HealthBarSprite.draw(batch);
        }

        batch.end();

        // TO DO: REMOVE START {

        //DEBUG FOR COLLISION
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRendererGreen.setProjectionMatrix(camera.combined);
        ArrayList<Rectangle> listOfHitbox = new ArrayList<Rectangle>();

        // for (Entity e : Entity.entityList) {
        //     listOfHitbox.add(e.getCollisionBox());
        // }

        // for (Rectangle rect : getMapCollisions()) {
        //     listOfHitbox.add(rect);
        // }
        for (Rectangle rect: Bot.rectArray){
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
        shapeRendererGreen.begin(ShapeRenderer.ShapeType.Line);
        for(Node i : BotChaseState.nodeList){
            int x = i.x;
            int y = i.y; 
                    shapeRendererGreen.setColor(0, 1, 0, 0.5f); // red = blocked
                    shapeRendererGreen.rect(
                    x * cellSize,
                    y * cellSize,
                    cellSize,
                    cellSize
                );

                // Render at map tile position (already in world units
            }

        shapeRendererGreen.end();
        listOfHitbox.add(mainTower.getCollisionBox());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(Rectangle rect : listOfHitbox) {
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        shapeRenderer.end();


        // } REMOVE END
    }

    @Override
    public void show() {

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
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();   // TO DO: Remove DEBUG tool
        mapRenderer.dispose();
    }


    // Implement later for handling keyboard events
    // private void keyEvent(float delta) {

    // }

    // Mouse Click Event
    private void clickEvent(){
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

    //Made to be passed in render (utilized by getHovSprite())
    public void hoverEvent(){
        if (hoverCoords != null){
            hoverCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(hoverCoords);
        }
    }

    //Made to be utilized in draw to get the effective sprite
    public Sprite getHovSprite(){
        //Convert to 2d coords
        Vector2 hoverCoords2D = new Vector2(hoverCoords.x, hoverCoords.y);
        for (Bot b : Bot.BotList){
            if (b.getCollisionBox().contains(hoverCoords2D)){     //If we hover on a character get its healthbarsprite
                return b.HealthBarSprite;
            }
        }
        return null;        //Otherwise give null
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



    public Sprite getGoblinHealthBarSprite(){
        if (player.getAttackTarget() instanceof Bot){
            Bot target = (Bot)player.getAttackTarget();
            return target.HealthBarSprite;
        }
        return null;
    }
}

