package com.oop.platformer.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.oop.platformer.Constants;
import com.oop.platformer.GameClass;
import com.oop.platformer.GameObjects.*;
import com.oop.platformer.Scenes.Hud;
import com.oop.platformer.util.CollisionHandler;
import com.oop.platformer.util.LevelManager;



public class Level1 implements Screen {

    private GameClass gameClass;
    private OrthographicCamera gameCam; //game camera instance to move with the player character
    private Viewport gamePort; //Manages a Camera and determines how world coordinates are mapped to and from the screen.
    private TiledMap map;//reference for the map itself
    private Hud hud;
    private OrthogonalTiledMapRenderer renderer;
    private World world;

    // for rendering debugging
    private Box2DDebugRenderer floorDebugger;

    private Player player;

    private Array<Bullet> bullets;
    private Array<Enemy> enemies;

    public GameClass getGameClass() {
        return gameClass;
    }

    public OrthographicCamera getGameCam() {
        return gameCam;
    }

    public Hud getHud() {
        return hud;
    }
    public OrthogonalTiledMapRenderer getRenderer(){
        return renderer;
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }

    public Array<Enemy> getEnemies() {
        return enemies;
    }


    public Level1(GameClass gameClass) {

        this.gameClass = gameClass;
        //setup camera and window
        gameCam = new OrthographicCamera();
        gamePort = new StretchViewport(GameClass.V_WIDTH / GameClass.PPM, GameClass.V_HEIGHT / GameClass.PPM, gameCam);

        //Load Map
        //loads the level from assets
        TmxMapLoader mapLoader = new TmxMapLoader();
        map = mapLoader.load(Constants.MAP);

        hud = new Hud(gameClass.batch);
        renderer = new OrthogonalTiledMapRenderer(map, 1 / GameClass.PPM);

        gameCam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        //(0, -8) - Gravity on y equals -8
        world = new World(new Vector2(0, -8), true);

        renderFloor();

        addObjectsToTheWorld();
//        levelManager = new LevelManager(gameClass, this, player, enemies, hud, world, bullets, gameCam);
//        levelManager = new LevelManager(this);
        LevelManager.instance.setLevel(this);
        //Adding contact listener to listen for collisions between bodies, with level manager with our game Objects
        world.setContactListener(CollisionHandler.instance);

    }

    private void addObjectsToTheWorld() {
        //Adds player to the world in spritePosition (30,90)
        player = new Player(world, new Vector2(450 / GameClass.PPM, 200 / GameClass.PPM)); //!!!!!!!!!Reset this to 90
//        droneEnemyArrayList.add(new DroneEnemy(world,new Vector2(220 / GameClass.PPM, 150 / GameClass.PPM),this));
        bullets = new Array<Bullet>();
        enemies = new Array<Enemy>();

/*
        Array<Vector2> path = new Array<Vector2>();
        Array<Vector2> path2 = new Array<Vector2>();

        path.add(new Vector2(416 / GameClass.PPM, (336-64) / GameClass.PPM));
        path.add(new Vector2(528 / GameClass.PPM, (336-64) / GameClass.PPM));

        path2.add(new Vector2(900 / GameClass.PPM, 200 / GameClass.PPM));
        path2.add(new Vector2(800 / GameClass.PPM, 200 / GameClass.PPM));


//        path.add(new Vector2(650/GameClass.PPM, 50/GameClass.PPM));
//        droneEnemy = new Enemy(world, new Vector2(250/GameClass.PPM, 200/ GameClass.PPM), this, path);

        enemies.add(new DroneEnemy(world, path.get(0), path));
//        droneEnemy = new DroneEnemy(world, path.get(0), path);
        enemies.add(new TurretEnemy(world, path2.get(0), path2));
//        turretEnemy = new TurretEnemy(world, path2.get(0), path2);

 */
        addEnemeies();
    }

    private void renderFloor() {
        floorDebugger = new Box2DDebugRenderer();

        //defines what the body consists of
        BodyDef floorBodyDef = new BodyDef();

        PolygonShape floorShape = new PolygonShape();

        //to add bodies to the world
        FixtureDef floorFixtureDef = new FixtureDef();

        Body floor;

        //Create Floor Objects which's in the 4th layer

        for (MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            //Shaped as rectangles in the map objects
            Rectangle rect = ((RectangleMapObject) object).getRectangle();


            floorBodyDef.type = BodyDef.BodyType.StaticBody;

            //getX return the start of rect then add half of the width to get the center
            //The same for Y
            floorBodyDef.position.set((rect.getX() + rect.getWidth() / 2) / GameClass.PPM, (rect.getY() + rect.getHeight() / 2) / GameClass.PPM);

            floor = world.createBody(floorBodyDef);

            floorShape.setAsBox(rect.getWidth() / 2 / GameClass.PPM, rect.getHeight() / 2 / GameClass.PPM);
            floorFixtureDef.shape = floorShape;
            floor.createFixture(floorFixtureDef).setUserData("Floor");
        }
    }

    private void addEnemeies()
    {

        for(int i = 0; i<map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class).size; i+=2)
        {
            Array<Vector2> path = new Array<Vector2>();
            float[] durations = new float[2];
            MapObject object = map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class).get(i);

            RectangleMapObject rect = (RectangleMapObject) object;

            path.add(new Vector2(rect.getRectangle().getX() / GameClass.PPM, (rect.getRectangle().getY()) / GameClass.PPM));
            object = map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class).get(i+1);
            rect = (RectangleMapObject) object;
            path.add(new Vector2(rect.getRectangle().getX() / GameClass.PPM, (rect.getRectangle().getY()) / GameClass.PPM));

            durations[0] = durations[1] = 3 / path.get(0).dst2(path.get(1));
            enemies.add(new TurretEnemy(world, path.get(0), path, durations));

        }

        for(int i = 0; i<map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class).size; i+=4)
        {
            Array<Vector2> path = new Array<Vector2>();
            float[] durations = new float[4];

            MapObject object = map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class).get(i);
            RectangleMapObject rect = (RectangleMapObject) object;
            path.add(new Vector2(rect.getRectangle().getX() / GameClass.PPM, (rect.getRectangle().getY()) / GameClass.PPM));

            object = map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class).get(i+1);
            rect = (RectangleMapObject) object;
            path.add(new Vector2(rect.getRectangle().getX() / GameClass.PPM, (rect.getRectangle().getY()) / GameClass.PPM));

            object = map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class).get(i+2);
            rect = (RectangleMapObject) object;
            path.add(new Vector2(rect.getRectangle().getX() / GameClass.PPM, (rect.getRectangle().getY()) / GameClass.PPM));

            object = map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class).get(i+3);
            rect = (RectangleMapObject) object;
            path.add(new Vector2(rect.getRectangle().getX() / GameClass.PPM, (rect.getRectangle().getY()) / GameClass.PPM));

            durations[0] = durations[1] = durations[2] = durations[3] = 2 / path.get(0).dst2(path.get(1));
            enemies.add(new DroneEnemy(world, path.get(0), path, durations));

        }

        Array<Vector2> enemyPath = new Array<Vector2>();
        float[] enemyDurations;
        for(int i = 0; i<6; i++)
        {
            MapObject object = map.getLayers().get(8).getObjects().getByType(RectangleMapObject.class).get(i);
            RectangleMapObject rect = (RectangleMapObject) object;
            enemyPath.add(new Vector2(rect.getRectangle().getX() / GameClass.PPM, (rect.getRectangle().getY()) / GameClass.PPM));
//            enemyDurations[i] = 2;
        }
        enemyDurations = new float[]{0.4f, 0.5f, 0.8f, 1f, 2f, 0.4f};
        enemies.add(new BossEnemy(world, enemyPath.get(0), enemyPath, enemyDurations));

    }

    //update the game state
    private void update(float deltaTime) {
        /*
        set timeStamp and velocity
        to avoid CPU & GPU speed differences
        */
        LevelManager.instance.update(deltaTime);


    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //separate our update logic from render
//        if(delta >= 0.01f) //experimental
        update(delta);


        //Clear the game screen with Black
        Gdx.gl.glClearColor(.1f, .12f, .16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //render our game map
        renderer.render();

        floorDebugger.render(world, gameCam.combined); //remove this line to remove green debugging lines on objects

        gameClass.batch.setProjectionMatrix(gameCam.combined);
        gameClass.batch.begin();

        player.draw(gameClass.batch);

        for (Bullet bullet : bullets) {
            bullet.draw(gameClass.batch);
        }

        for (Enemy enemy : enemies)
            enemy.draw(gameClass.batch);

        gameClass.batch.end();

        //Draw Hud
        gameClass.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
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
        hud.dispose();
    }

}