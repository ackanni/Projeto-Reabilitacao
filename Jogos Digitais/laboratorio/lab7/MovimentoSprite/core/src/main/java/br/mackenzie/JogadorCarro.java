package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

public class JogadorCarro extends GameObject {
    private final Texture texturaFreio;      
    private final Texture texturaNormal;
    private final Viewport viewport;

    public float velLateral = 300f;

    private boolean freando = false;

    public JogadorCarro(Sprite sprite, Texture freio, Texture normal, Viewport viewport) {
        super(sprite);
        this.texturaFreio = freio;
        this.texturaNormal = normal;
        this.viewport = viewport;
        sprite.setSize(90, 100);
    }

    public void setFreando(boolean freando) {
        this.freando = freando;
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            sprite.translateX(velLateral * delta);
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            sprite.translateX(-velLateral * delta);
        }

        sprite.setTexture(freando ? texturaFreio : texturaNormal);

        float worldWidth = viewport.getWorldWidth();
        sprite.setX(MathUtils.clamp(sprite.getX(), 0, worldWidth - sprite.getWidth()));
        sprite.setY(50);

        float hitboxWidth = sprite.getWidth() * 0.45f;
        float hitboxHeight = sprite.getHeight() * 0.75f;
        hitbox.set(
            sprite.getX() + (sprite.getWidth() - hitboxWidth) / 2,
            sprite.getY() + (sprite.getHeight() - hitboxHeight) / 2,
            hitboxWidth,
            hitboxHeight
        );
    }
}