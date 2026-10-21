package br.mackenzie;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {
    protected Sprite sprite;
    protected Rectangle hitbox;

    public GameObject(Sprite sprite) {
        this.sprite = sprite;
        this.hitbox = new Rectangle();
        this.hitbox.setSize(sprite.getWidth(), sprite.getHeight());
        this.hitbox.setPosition(sprite.getX(), sprite.getY());
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public abstract void update(float delta);

    public Rectangle getHitbox() {
        hitbox.setPosition(sprite.getX(), sprite.getY());
        return hitbox;
    }

    public Sprite getSprite() {
        return sprite;
    }
}
