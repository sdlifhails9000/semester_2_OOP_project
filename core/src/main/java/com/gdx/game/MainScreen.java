package com.gdx.game;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainScreen implements Screen {
    private final MainGame entry;
    private Stage stage;
    private Skin style;

    public MainScreen(MainGame entry) {
        this.entry = entry;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());

        Table root = new Table();
        Button button = new Button();
        root.add(button).align(Align.center).row();
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

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

    }
}
