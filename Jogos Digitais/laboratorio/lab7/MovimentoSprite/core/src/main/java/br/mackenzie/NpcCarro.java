package br.mackenzie;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class NpcCarro extends GameObject {
    public float velInicial = 150f;
    public float velJogador = 0f; 

    public NpcCarro(Sprite sprite){
        super(sprite);
        sprite.setSize(50, 100);
    }

    @Override 
    public void update(float delta) {
        float velFinal = velInicial + velJogador; 
        sprite.translateY(-velFinal * delta); 

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
