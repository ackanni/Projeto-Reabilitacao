package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    SpriteBatch spriteBatch;
    FitViewport viewport;

    // Fundo
    Texture fundo;
    float fundoOffsetX = 0f;
    float velocidadeParalaxe = 1f; // fundo mais lento que personagem

    // Animações
    Animation<TextureRegion> animacaoCorrida;
    Animation<TextureRegion> animacaoPulo;
    float stateTimeCorrida;
    float stateTimePulo;
    TextureRegion frameAtual;

    // Personagem
    Rectangle personagemRetangulo;
    float posX, posY;
    boolean olhandoDireita = true;

    // Física do pulo
    boolean noChao = true;
    float velY = 0f;
    float gravidade = -15f;
    float forcaPulo = 7f;

    // Tamanho do personagem
    float personagemWidth = 1f;
    float personagemHeight = 1f;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);

        // Carrega fundo
        fundo = new Texture("tela_fundo.jpg");

        // Frames de corrida (Run1 até Run8)
        TextureRegion[] framesCorrida = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            Texture t = new Texture("Run" + (i + 1) + ".png");
            framesCorrida[i] = new TextureRegion(t);
        }
        animacaoCorrida = new Animation<>(0.1f, framesCorrida);

        // Frames de pulo (Jump1 até Jump12)
        TextureRegion[] framesPulo = new TextureRegion[12];
        for (int i = 0; i < 12; i++) {
            Texture t = new Texture("Jump" + (i + 1) + ".png");
            framesPulo[i] = new TextureRegion(t);
        }
        animacaoPulo = new Animation<>(0.1f, framesPulo);

        // Estado inicial
        frameAtual = framesCorrida[0];
        stateTimeCorrida = 0f;
        stateTimePulo = 0f;

        // Posição inicial
        posX = 0;
        posY = 0;
        personagemRetangulo = new Rectangle(posX, posY, personagemWidth, personagemHeight);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        float velocidade = 1f;
        float delta = Gdx.graphics.getDeltaTime();

        // Movimento lateral
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            posX += velocidade * delta;
            olhandoDireita = true;

            fundoOffsetX -= velocidade * delta * velocidadeParalaxe;

            if (noChao) {
                stateTimeCorrida += delta;
                frameAtual = animacaoCorrida.getKeyFrame(stateTimeCorrida, true);
            }
        } else if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
            posX -= velocidade * delta;
            olhandoDireita = false;

            fundoOffsetX += velocidade * delta * velocidadeParalaxe;

            if (noChao) {
                stateTimeCorrida += delta;
                frameAtual = animacaoCorrida.getKeyFrame(stateTimeCorrida, true);
            }
        } else {
            if (noChao) {
                frameAtual = animacaoCorrida.getKeyFrames()[0];
            }
        }

        // Pulo
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP) && noChao){
            velY = forcaPulo;
            noChao = false;
            stateTimePulo = 0f;
        }
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();

        // Gravidade
        if (!noChao) {
            velY += gravidade * delta;
            posY += velY * delta;

            stateTimePulo += delta;
            frameAtual = animacaoPulo.getKeyFrame(stateTimePulo, false);

            if (posY <= 0) {
                posY = 0;
                velY = 0;
                noChao = true;
            }
        }

        // Limite horizontal da tela
        float worldWidth = viewport.getWorldWidth();
        posX = MathUtils.clamp(posX, 0, worldWidth - personagemWidth);

        personagemRetangulo.set(posX, posY, personagemWidth, personagemHeight);
    }

    private void draw() {
        ScreenUtils.clear(Color.WHITE);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        // Fundo (preenchendo toda a tela)
        float fundoWidth = viewport.getWorldWidth();
        float fundoHeight = viewport.getWorldHeight();

        float x1 = fundoOffsetX % fundoWidth;
        if (x1 > 0) x1 -= fundoWidth;

        spriteBatch.draw(fundo, x1, 0, fundoWidth, fundoHeight);
        spriteBatch.draw(fundo, x1 + fundoWidth, 0, fundoWidth, fundoHeight);

        // Personagem
        if (olhandoDireita) {
            spriteBatch.draw(frameAtual, posX, posY, personagemWidth, personagemHeight);
        } else {
            spriteBatch.draw(frameAtual, posX + personagemWidth, posY, -personagemWidth, personagemHeight);
        }

        spriteBatch.end();
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void dispose() {
        spriteBatch.dispose();
        fundo.dispose();
        for (TextureRegion t : animacaoCorrida.getKeyFrames()) t.getTexture().dispose();
        for (TextureRegion t : animacaoPulo.getKeyFrames()) t.getTexture().dispose();
    }
}
