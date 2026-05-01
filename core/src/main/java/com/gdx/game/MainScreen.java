package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.audio.Sound;


public class MainScreen implements Screen {
    private final MainGame game;
    private Stage stage;
    private Skin skin;
    private Table mainTable;
    private Table pickTable;

    //Sound
    private Sound clickSound;
    private Music menuMusic;

    // Background color
    private static final float BG_R = 0.07f, BG_G = 0.07f, BG_B = 0.10f;

    public MainScreen(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        createSkin();

        // Root tables for different "states"
        mainTable = createMainTable();
        pickTable = createPickTable();

        stage.addActor(mainTable);
        stage.addActor(pickTable);

        showMainState();

        //For sound effects (Loaded from MainGame)
        clickSound = game.manager.get("Kwality_Sounds/Game_Click.mp3", Sound.class);
        menuMusic = game.manager.get("Kwality_Sounds/Menu_Music.mp3", Music.class);
        menuMusic.setLooping(true);
        menuMusic.play();
        menuMusic.setVolume(0.5f);

    }

    private void createSkin() {
        skin = new Skin();

        // 1. Fonts
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(4.0f);
        skin.add("title", titleFont);

        BitmapFont btnFont = new BitmapFont();
        btnFont.getData().setScale(2.2f);
        skin.add("default", btnFont);

        BitmapFont hintFont = new BitmapFont();
        hintFont.getData().setScale(1.3f);
        skin.add("hint", hintFont);

        // 2. Generate Textures for Buttons (Programmatic "Skin")
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

        // Idle state
        pixmap.setColor(0.15f, 0.15f, 0.22f, 1f);
        pixmap.fill();
        skin.add("btn-bg", new Texture(pixmap));

        // Hover state
        pixmap.setColor(0.25f, 0.20f, 0.45f, 1f);
        pixmap.fill();
        skin.add("btn-hov", new Texture(pixmap));
        pixmap.dispose();

        // 3. Define Button Style
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.up = skin.newDrawable("btn-bg");
        tbs.over = skin.newDrawable("btn-hov");
        tbs.font = skin.getFont("default");
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        // 4. Label Styles
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.getFont("title"), new Color(0.90f, 0.75f, 1.00f, 1f));
        skin.add("title", titleStyle);

        Label.LabelStyle subStyle = new Label.LabelStyle(skin.getFont("default"), new Color(0.65f, 0.60f, 0.80f, 1f));
        skin.add("sub", subStyle);

        Label.LabelStyle hintStyle = new Label.LabelStyle(skin.getFont("hint"), new Color(0.65f, 0.60f, 0.80f, 0.75f));
        skin.add("hint", hintStyle);
    }

    private Table createMainTable() {
        Table t = new Table();
        t.setFillParent(true);

        Label title = new Label("ARENA LEGENDS", skin, "title");
        TextButton startBtn = new TextButton("START GAME", skin);

        startBtn.addListener(new ClickListener() {      //This is an argument btw (ALL OF IT)
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                showPickState();
            }
        });

        t.add(title).padBottom(80f).row();
        t.add(startBtn).size(280f, 54f);
        return t;
    }

    private Table createPickTable() {
        Table t = new Table();
        t.setFillParent(true);

        Label title = new Label("CHOOSE YOUR HERO", skin, "title"); // Reusing title style
        title.setFontScale(2.5f);
        Label sub = new Label("pick a class to begin", skin, "sub");

        // Hero Selection Row
        Table btnRow = new Table();

        // Light Hero Column
        VerticalGroup lightCol = new VerticalGroup();
        TextButton lightBtn = new TextButton("LIGHT HERO", skin);
        lightBtn.getLabelCell().pad(5f, 5f, 5f, 5f);
        Label lightHint = new Label("Fast | Low HP | High DPS", skin, "hint");
        lightCol.addActor(lightBtn);
        lightCol.addActor(lightHint);

        lightBtn.addListener(new ClickListener() {      //This is an argument btw (ALL OF IT)
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new GameScreen(game, HeroPreset.HERO_LIGHT));
            }
        });

        // Heavy Hero Column
        VerticalGroup heavyCol = new VerticalGroup();
        TextButton heavyBtn = new TextButton("HEAVY HERO", skin);
        heavyBtn.getLabelCell().pad(5f, 5f, 5f, 5f);
        Label heavyHint = new Label("Slow | High HP | Tank", skin, "hint");
        heavyCol.addActor(heavyBtn);
        heavyCol.addActor(heavyHint);

        heavyBtn.addListener(new ClickListener() {      //This is an argument btw (ALL OF IT)
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickSound.play();
                game.setScreen(new GameScreen(game, HeroPreset.HERO_HEAVY));
            }
        });

        btnRow.add(lightCol).width(280f).height(100f).padRight(30f);
        btnRow.add(heavyCol).width(280f).height(100f);

        t.add(title).padBottom(10f).row();
        t.add(sub).padBottom(60f).row();
        t.add(btnRow);
        return t;
    }

    private void showMainState() {
        mainTable.setVisible(true);
        pickTable.setVisible(false);
    }

    private void showPickState() {
        mainTable.setVisible(false);
        pickTable.setVisible(true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_R, BG_G, BG_B, 1.0f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { dispose(); }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        menuMusic.stop();
        //ClickSound dispose is handled by manager which is disposed when game ends
    }
}
