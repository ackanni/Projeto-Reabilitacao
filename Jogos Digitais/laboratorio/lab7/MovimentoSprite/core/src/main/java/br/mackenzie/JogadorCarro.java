package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

public class JogadorCarro extends GameObject {
    private Texture texturaParada;
    private Texture texturaMovimento;
    private Viewport viewport;
    
    public float velLat = 300f; 
    public float aceleracaoIoT = 0f; 

    public JogadorCarro(Sprite sprite, Texture parada, Texture movimento, Viewport viewport) {
        super(sprite);
        this.texturaParada = parada;
        this.texturaMovimento = movimento;
        this.viewport = viewport;
        sprite.setSize(50, 100); 
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            sprite.translateX(velLat * delta);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            sprite.translateX(-velLat * delta);
        }

        if (aceleracaoIoT > 0) {
            sprite.setTexture(texturaMovimento);
        } else {
            sprite.setTexture(texturaParada);
        }

        //posição do carro vertical e limite da tela
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        
        sprite.setX(MathUtils.clamp(sprite.getX(), 0, worldWidth - sprite.getWidth()));
        sprite.setY(50); 

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
