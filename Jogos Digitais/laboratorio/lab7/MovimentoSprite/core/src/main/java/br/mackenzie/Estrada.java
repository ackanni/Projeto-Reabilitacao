package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Estrada {
    private Texture texturaFundo;
    private float fundoOffsetY = 0f;
    private float velocidadeParalaxe = 1f; 
    private Viewport viewport;

    public Estrada(Texture texturaFundo, Viewport viewport) {
        this.texturaFundo = texturaFundo;
        this.viewport = viewport;
    }

    public void update(float velocidadeIoT, float delta) {
        fundoOffsetY -= velocidadeIoT * delta * velocidadeParalaxe;
    }

    public void draw(SpriteBatch batch) {
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        float y1 = fundoOffsetY % worldHeight;
        if (y1 > 0) {
            y1 -= worldHeight;
        }

        batch.draw(texturaFundo, 0, y1, worldWidth, worldHeight);
        batch.draw(texturaFundo, 0, y1 + worldHeight, worldWidth, worldHeight);
    }
}