package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    private Estrada pista;
    private JogadorCarro player;
    private Array<NpcCarro> npcs;

    private Texture fundo;
    private Texture playerParado;
    private Texture playerMovimento;
    private Texture npcTexture;

    // IoT e Rampa de Aceleração (Software Ramping)
    private ArduinoReaderThread arduinoReader;
    private float forwardSpeed = 0f;
    private final float MAX_SPEED = 800f;
    private final float ACCELERATION_RATE = 250f; 
    private final float DECELERATION_RATE = 150f; 
    private final float BRAKE_RATE = 500f;        

    // Regras da Fase e HUD
    private float spawnTimer = 0f;
    private float distanciaPercorrida = 0f;
    private final float DISTANCIA_ALVO = 10000f; 
    private float tempoRestante = 90f;    

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(480, 800); // Proporção retrato/estrada

        fundo = new Texture("tela_fundo.jpg"); 
        playerParado = new Texture("carro_parado.png"); 
        playerMovimento = new Texture("carro_movimento.png");
        npcTexture = new Texture("carro_npc.png");

        pista = new Estrada(fundo, viewport);

        Sprite playerSprite = new Sprite(playerParado);
        player = new JogadorCarro(playerSprite, playerParado, playerMovimento, viewport);
        player.getSprite().setX((viewport.getWorldWidth() - playerSprite.getWidth()) / 2f);
        
        npcs = new Array<>();

        //inicia o arduino
        arduinoReader = new ArduinoReaderThread("COM3"); 
        arduinoReader.start();
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
        float delta = Gdx.graphics.getDeltaTime();
        String serialData = arduinoReader.getLastData();
        
        //le o esp32 
        if (serialData.equals("1")) { 
            forwardSpeed += ACCELERATION_RATE * delta;
            if (forwardSpeed > MAX_SPEED) forwardSpeed = MAX_SPEED;
            
        } else if (serialData.equals("2")) {
            forwardSpeed -= BRAKE_RATE * delta;
            if (forwardSpeed < 0) forwardSpeed = 0;
            
        } else {
            forwardSpeed -= DECELERATION_RATE * delta;
            if (forwardSpeed < 0) forwardSpeed = 0;
        }
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();

        //movimento do jogador
        player.aceleracaoIoT = forwardSpeed; 
        player.update(delta);

        //move a estrada
        pista.update(forwardSpeed, delta);

       
        if (tempoRestante > 0 && distanciaPercorrida < DISTANCIA_ALVO) {
            tempoRestante -= delta;
            distanciaPercorrida += forwardSpeed * delta;
        }

        //spawn dos npcs
        spawnTimer += delta;
        float tempoSpawn = MathUtils.clamp(1.5f - (forwardSpeed / MAX_SPEED), 0.5f, 1.5f);
        if (spawnTimer > tempoSpawn && forwardSpeed > 50) {
            spawnTimer = 0f;
            createNpc();
        }


        //verificação de colisão
        for (int i = npcs.size - 1; i >= 0; i--) {
            NpcCarro npc = npcs.get(i);
            
            npc.setSpeed(forwardSpeed); 
            npc.update(delta);

            if (player.getHitbox().overlaps(npc.getHitbox())) {
                forwardSpeed = 0f; 
                npcs.removeIndex(i); 
            } 
            // Limpeza de memória: Saiu da tela por baixo
            else if (npc.getSprite().getY() + npc.getSprite().getHeight() < 0) {
                npcs.removeIndex(i); 
            }
        }
    }

    private void createNpc() {
        Sprite npcSprite = new Sprite(npcTexture);
        
        // Sorteia a posição X aleatória dentro dos limites da pista
        float posX = MathUtils.random(0, viewport.getWorldWidth() - npcSprite.getWidth());
        npcSprite.setPosition(posX, viewport.getWorldHeight()); // Nasce fora da tela no topo
        
        // Cria passando a velocidade atual
        NpcCarro npc = new NpcCarro(npcSprite, forwardSpeed);
        npcs.add(npc);
    }

    private void draw() {
        ScreenUtils.clear(Color.DARK_GRAY);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        // A ordem de desenho importa: o que é desenhado primeiro fica no fundo
        pista.draw(spriteBatch);
        player.draw(spriteBatch);
        for (NpcCarro npc : npcs) {
            npc.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        // Encerramento seguro da thread do ESP32
        arduinoReader.stopReading();
        try {
            arduinoReader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Limpeza de memória da placa de vídeo
        spriteBatch.dispose();
        fundo.dispose();
        playerParado.dispose();
        playerMovimento.dispose();
        npcTexture.dispose();
    }
}