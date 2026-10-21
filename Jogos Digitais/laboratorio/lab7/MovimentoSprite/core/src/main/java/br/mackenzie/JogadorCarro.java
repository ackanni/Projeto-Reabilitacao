package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

public class JogadorCarro extends GameObject {
    private Texture texturaParada;
    private Texture texturaMovimento;
    private Viewport viewport;
    private Vector2 touchPos;
    
    public float fundoOffsetX = 0f;
    public float fundoOffsetY = 0f;
    private float velocidadeParalaxe = 0.5f;

    public float speed = 4f; 

    public JogadorCarro(Sprite sprite, Texture parada, Texture movimento, Viewport viewport) {
        super(sprite);
        this.texturaParada = parada;
        this.texturaMovimento = movimento;
        this.viewport = viewport;
        this.touchPos = new Vector2();
    }

    @Override
    public void update(float delta) {
        boolean movendo = false;
        fundoOffsetY -= 1f * delta * velocidadeParalaxe;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            sprite.translateX(speed * delta);
            fundoOffsetX -= speed * delta * velocidadeParalaxe;
            movendo = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            sprite.translateX(-speed * delta);
            fundoOffsetX += speed * delta * velocidadeParalaxe;
            movendo = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            sprite.translateY(speed * delta); 
            fundoOffsetY -= speed * delta * velocidadeParalaxe;
            movendo = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            sprite.translateY(-speed * delta); 
            fundoOffsetY += speed * delta * velocidadeParalaxe;
            movendo = true;
        }
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);
            sprite.setCenterX(touchPos.x);
            movendo = true;
        }

        if (movendo) {
            sprite.setTexture(texturaMovimento);
        } else {
            sprite.setTexture(texturaParada);
        }

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        sprite.setX(MathUtils.clamp(sprite.getX(), 0, worldWidth - sprite.getWidth()));
        sprite.setY(MathUtils.clamp(sprite.getY(), 0, worldHeight - sprite.getHeight()));

        float hitboxWidth = sprite.getWidth() * 0.65f;
        float hitboxHeight = sprite.getHeight() * 0.65f;
        hitbox.set(
            sprite.getX() + (sprite.getWidth() - hitboxWidth) / 2,
            sprite.getY() + (sprite.getHeight() - hitboxHeight) / 2,
            hitboxWidth,
            hitboxHeight
        );
    }
}
