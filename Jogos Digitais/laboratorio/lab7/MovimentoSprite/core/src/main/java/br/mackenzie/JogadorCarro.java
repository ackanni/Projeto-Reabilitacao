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

    public float lateralSpeed = 300f; // Velocidade do volante

    public JogadorCarro(Sprite sprite, Texture parada, Texture movimento, Viewport viewport) {
        super(sprite);
        this.texturaParada = parada;
        this.texturaMovimento = movimento;
        this.viewport = viewport;
        
        // Fixa o carro na parte inferior da tela, centralizado
        this.sprite.setY(50);
        this.sprite.setX((viewport.getWorldWidth() - sprite.getWidth()) / 2f);
    }

    @Override
    public void update(float delta) {
        boolean movendo = false;

        // Controle Lateral (Mãos na dupla tarefa) - Eixo X
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            sprite.translateX(lateralSpeed * delta);
            movendo = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            sprite.translateX(-lateralSpeed * delta);
            movendo = true;
        }

        // Feedback visual
        if (movendo) {
            sprite.setTexture(texturaMovimento);
        } else {
            sprite.setTexture(texturaParada);
        }

        // Limita o carro às bordas horizontais da tela
        float worldWidth = viewport.getWorldWidth();
        sprite.setX(MathUtils.clamp(sprite.getX(), 0, worldWidth - sprite.getWidth()));

        // Atualiza a Hitbox (mantive a proporção original do seu código)
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