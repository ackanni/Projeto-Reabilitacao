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
    SpriteBatch spriteBatch;
    FitViewport viewport;

    // Fundo
    Texture fundo;
    float fundoOffsetY = 0f;

    // Elementos do Jogo
    JogadorCarro player;
    Array<NpcCar> npcs;
    Texture playerParado;
    Texture playerMovimento;
    Texture npcTexture;

    // IoT e Rampa de Aceleração (Software Ramping)
    ArduinoReaderThread arduinoReader;
    float forwardSpeed = 0f;
    float maxSpeed = 800f;
    float accelerationRate = 250f; // Ganho de velocidade isométrico
    float decelerationRate = 150f; // Atrito ao soltar o pedal
    float brakeRate = 500f;        // Força da frenagem

    // Regras da Fase
    float spawnTimer = 0f;
    float distanciaPercorrida = 0f;
    float distanciaAlvo = 10000f; 
    float tempoRestante = 90f;    

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(480, 800); // Proporção retrato/estrada

        // Carrega as texturas (Ajuste os nomes para os seus arquivos de imagem)
        fundo = new Texture("tela_fundo.jpg"); 
        playerParado = new Texture("carro_parado.png"); 
        playerMovimento = new Texture("carro_movimento.png");
        npcTexture = new Texture("carro_npc.png");

        // Instancia Jogador
        Sprite playerSprite = new Sprite(playerParado);
        player = new JogadorCarro(playerSprite, playerParado, playerMovimento, viewport);
        
        npcs = new Array<>();

        // Inicia a comunicação Serial com o ESP32
        arduinoReader = new ArduinoReaderThread("COM3"); // Ajuste para a porta correta do bluetooth/usb
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

        // Leitura contínua do ESP32 para acelerar/frear
        String serialData = arduinoReader.getLastData();
        
        if (serialData.equals("1")) { // Botão Acelerador pressionado
            forwardSpeed += accelerationRate * delta;
            if (forwardSpeed > maxSpeed) forwardSpeed = maxSpeed;
        } else if (serialData.equals("2")) { // Botão Freio pressionado
            forwardSpeed -= brakeRate * delta;
            if (forwardSpeed < 0) forwardSpeed = 0;
        } else { // Pés soltos
            forwardSpeed -= decelerationRate * delta;
            if (forwardSpeed < 0) forwardSpeed = 0;
        }
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();

        // 1. Atualiza teclado e limites do carro
        player.update(delta);

        // 2. Faz o fundo rolar para baixo baseado na velocidade
        fundoOffsetY -= forwardSpeed * delta;

        // 3. Atualiza HUD/Metas da reabilitação
        if (tempoRestante > 0 && distanciaPercorrida < distanciaAlvo) {
            tempoRestante -= delta;
            distanciaPercorrida += forwardSpeed * delta;
        }

        // 4. Spawner Dinâmico de Inimigos (Mais rápido = mais inimigos)
        spawnTimer += delta;
        float tempoSpawn = MathUtils.clamp(1.5f - (forwardSpeed / maxSpeed), 0.5f, 1.5f);
        if (spawnTimer > tempoSpawn && forwardSpeed > 50) {
            spawnTimer = 0f;
            createNpc();
        }

        // 5. Atualiza e checa colisão dos Inimigos
        for (int i = npcs.size - 1; i >= 0; i--) {
            NpcCar npc = npcs.get(i);
            npc.setSpeed(forwardSpeed); // NPC desce na velocidade que o jogador sobe
            npc.update(delta);

            // Punição: Se bater, o carro freia bruscamente
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
        float posX = MathUtils.random(0, viewport.getWorldWidth() - npcSprite.getWidth());
        npcSprite.setPosition(posX, viewport.getWorldHeight()); // Nasce fora da tela no topo
        
        NpcCar npc = new NpcCar(npcSprite, forwardSpeed);
        npcs.add(npc);
    }

    private void draw() {
        ScreenUtils.clear(Color.DARK_GRAY);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();

        // Loop infinito do fundo (Paralaxe Vertical)
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float y1 = fundoOffsetY % worldHeight;
        if (y1 > 0) y1 -= worldHeight;

        spriteBatch.draw(fundo, 0, y1, worldWidth, worldHeight);
        spriteBatch.draw(fundo, 0, y1 + worldHeight, worldWidth, worldHeight);

        // Desenha Objetos (Polimorfismo aplicado)
        player.draw(spriteBatch);
        for (NpcCar npc : npcs) {
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
        arduinoReader.stopReading();
        try {
            arduinoReader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        spriteBatch.dispose();
        fundo.dispose();
        playerParado.dispose();
        playerMovimento.dispose();
        npcTexture.dispose();
    }
}