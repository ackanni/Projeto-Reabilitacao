package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    private enum EstadoJogo {
        CONTAGEM_REGRESSIVA, JOGANDO, PAUSADO, FASE_CONCLUIDA, GAME_OVER, JOGO_CONCLUIDO
    }
    private EstadoJogo estado = EstadoJogo.CONTAGEM_REGRESSIVA;

    private static final String[] FUNDOS_FASES = { "california.jpg", "fase2.jpg", "fase3.jpg" };
    private static final float[] META_METROS   = { 3000f, 4000f, 5000f };
    private static final int TOTAL_FASES = FUNDOS_FASES.length;

    private static final float TEMPO_INICIAL = 150f;
    private static final float[] BONUS_TEMPO = { 0f, 120f, 150f };
    private static final float PIXELS_POR_METRO = 10f;

    private static final float DISTANCIA_ENTRE_ONDAS = 340f;
    private static final float DISTANCIA_PRIMEIRA_ONDA = 600f;

    private static final int MAX_VIDAS = 3;
    private static final float TEMPO_INVENCIVEL = 1.5f;

    private static final float DURACAO_CONTAGEM = 3f;
    private static final float DURACAO_JA = 0.8f;

    private static final float LARGURA_MUNDO = 1400f;
    private static final float ALTURA_MUNDO = 800f;

    private static final String OP_CONTINUAR = "CONTINUAR";
    private static final String OP_REINICIAR = "REINICIAR FASE";
    private static final String OP_PROXIMA = "PROXIMA FASE";
    private static final String OP_JOGAR_NOVAMENTE = "JOGAR NOVAMENTE";

    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private UIManager ui;

    private Estrada pista;
    private JogadorCarro player;
    private Array<NpcCarro> npcs;

    private Texture[] texturasFases;
    private Texture playerFreio;
    private Texture playerNormal;
    private Texture npcTexture;

    private Esp32ReaderThread leitorEsp32;
    private float forwardSpeed = 0f;
    private final float MAX_SPEED = 450f;
    private final float ACCELERATION_RATE = 200f;
    private final float DECELERATION_RATE = 150f;
    private final float BRAKE_RATE = 300f;

    private int faseAtual = 0;
    private float tempoInicioFase;
    private float tempoRestante;
    private float distanciaPixels = 0f;
    private float distanciaProximaOnda = 0f;
    private int vidas = MAX_VIDAS;
    private float invencivelRestante = 0f;
    private float contagemRestante = 0f;
    private float jaRestante = 0f;

    private Music soundtrack;


    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(LARGURA_MUNDO, ALTURA_MUNDO);
        ui = new UIManager(LARGURA_MUNDO, ALTURA_MUNDO);

        carregarFundos();
        playerFreio = new Texture("YellowCarFreio.png");
        playerNormal = new Texture("YellowCar.png");
        npcTexture = new Texture("YellowCarFreio.png");

        soundtrack = Gdx.audio.newMusic(Gdx.files.internal("musica_fundo.mp3"));
        soundtrack.setVolume(0.2f); 
        soundtrack.setLooping(true); 
        soundtrack.play();

        pista = new Estrada(texturasFases[0], viewport);

        Sprite playerSprite = new Sprite(playerNormal);
        player = new JogadorCarro(playerSprite, playerFreio, playerNormal, viewport);
        npcs = new Array<>();

        leitorEsp32 = new Esp32ReaderThread("COM3");
        leitorEsp32.start();

        faseAtual = 0;
        tempoInicioFase = TEMPO_INICIAL;
        prepararFase();
    }

    private void carregarFundos() {
        texturasFases = new Texture[TOTAL_FASES];
        for (int i = 0; i < TOTAL_FASES; i++) {
            FileHandle arquivo = Gdx.files.internal(FUNDOS_FASES[i]);
            if (arquivo.exists()) {
                texturasFases[i] = new Texture(arquivo);
            } else {
                Gdx.app.error("Main", "Fundo nao encontrado: " + FUNDOS_FASES[i] + " (usando o da fase 1)");
                texturasFases[i] = texturasFases[0];
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        ui.resize(width, height);
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f);
        boolean apertouEsc = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);

        if (estado == EstadoJogo.JOGANDO) {
            if (apertouEsc) {
                pausar();
            } else {
                if (jaRestante > 0f) jaRestante = Math.max(0f, jaRestante - delta);
                inputJogo(delta);
                logicJogo(delta);
            }
        } else if (estado == EstadoJogo.CONTAGEM_REGRESSIVA) {
            if (apertouEsc) {
                pausar();
            } else {
                atualizarContagem(delta);
            }
        } else if (estado == EstadoJogo.PAUSADO && apertouEsc) {
            continuar();
        } else {
            String escolha = ui.processarInputMenu();
            if (escolha != null) executarOpcao(escolha);
        }

        draw();
    }

    private void inputJogo(float delta) {
        String dado = leitorEsp32.getLastData();
        String sinal = (dado == null) ? "0" : dado.trim();

        player.setFreando(sinal.equals("2"));

        if (sinal.equals("1")) {
            forwardSpeed += ACCELERATION_RATE * delta;
            if (forwardSpeed > MAX_SPEED) forwardSpeed = MAX_SPEED;
        } else if (sinal.equals("2")) {
            forwardSpeed -= BRAKE_RATE * delta;
            if (forwardSpeed < 0) forwardSpeed = 0;
        } else {
            forwardSpeed -= DECELERATION_RATE * delta;
            if (forwardSpeed < 0) forwardSpeed = 0;
        }
    }

    private void executarOpcao(String opcao) {
        switch (opcao) {
            case OP_CONTINUAR:
                continuar();
                break;
            case OP_REINICIAR:
                prepararFase();
                break;
            case OP_PROXIMA:
                faseAtual++;
                tempoInicioFase = tempoRestante + BONUS_TEMPO[faseAtual];
                prepararFase();
                break;
            case OP_JOGAR_NOVAMENTE:
                faseAtual = 0;
                tempoInicioFase = TEMPO_INICIAL;
                prepararFase();
                break;
        }
    }

    private void iniciarContagem() {
        ui.esconderMenu();
        forwardSpeed = 0f;
        congelarJogador();
        contagemRestante = DURACAO_CONTAGEM;
        jaRestante = 0f;
        estado = EstadoJogo.CONTAGEM_REGRESSIVA;
    }

    private void atualizarContagem(float delta) {
        contagemRestante -= delta;
        if (contagemRestante <= 0f) {
            contagemRestante = 0f;
            jaRestante = DURACAO_JA;
            estado = EstadoJogo.JOGANDO;
        }
    }

    private void logicJogo(float delta) {
        player.update(delta);
        pista.update(forwardSpeed, delta);

        tempoRestante = Math.max(0f, tempoRestante - delta);
        distanciaPixels += forwardSpeed * delta;
        if (invencivelRestante > 0f) invencivelRestante -= delta;

        if (distanciaPixels >= META_METROS[faseAtual] * PIXELS_POR_METRO) {
            concluirFase();
            return;
        }
        if (tempoRestante <= 0f) {
            gameOver("FIM DE TEMPO");
            return;
        }

        if (distanciaPixels >= distanciaProximaOnda) {
            createNpc();
            distanciaProximaOnda = distanciaPixels + DISTANCIA_ENTRE_ONDAS;
        }

        for (int i = npcs.size - 1; i >= 0; i--) {
            NpcCarro npc = npcs.get(i);
            npc.setSpeed(forwardSpeed);
            npc.update(delta);

            boolean bateu = invencivelRestante <= 0f
                         && player.getHitbox().overlaps(npc.getHitbox());

            if (bateu) {
                npcs.removeIndex(i);
                perderVida();
                if (estado != EstadoJogo.JOGANDO) return;
            } else if (npc.getSprite().getY() + npc.getSprite().getHeight() < 0) {
                npcs.removeIndex(i);
            }
        }
    }

    private void perderVida() {
        vidas--;
        forwardSpeed = 0f;
        invencivelRestante = TEMPO_INVENCIVEL;
        if (vidas <= 0) {
            vidas = 0;
            gameOver("SEM VIDAS");
        }
    }

    private void createNpc() {
        int quantidadeCarros = MathUtils.random(1, 2);

        Array<Integer> faixas = new Array<>();
        faixas.add(0); faixas.add(1); faixas.add(2); faixas.add(3);
        faixas.shuffle();

        float margemEsquerda = 430f;
        float margemDireita = viewport.getWorldWidth() - margemEsquerda;
        float larguraFaixa = (margemDireita - margemEsquerda) / 4f;

        for (int i = 0; i < quantidadeCarros; i++) {
            Sprite npcSprite = new Sprite(npcTexture);
            npcSprite.setSize(90, 100);

            int faixa = faixas.get(i);
            float centroDaFaixa = margemEsquerda + (larguraFaixa * faixa) + (larguraFaixa / 2f);
            npcSprite.setPosition(centroDaFaixa - npcSprite.getWidth() / 2f, viewport.getWorldHeight());

            npcs.add(new NpcCarro(npcSprite, forwardSpeed));
        }
    }

    private void prepararFase() {
        pista.reiniciar(texturasFases[faseAtual]);
        npcs.clear();
        distanciaPixels = 0f;
        distanciaProximaOnda = DISTANCIA_PRIMEIRA_ONDA;
        tempoRestante = tempoInicioFase;
        vidas = MAX_VIDAS;
        invencivelRestante = 0f;

        Sprite s = player.getSprite();
        s.setX((viewport.getWorldWidth() - s.getWidth()) / 2f);

        iniciarContagem();
    }

    private void pausar() {
        estado = EstadoJogo.PAUSADO;
        jaRestante = 0f;
        ui.mostrarMenu("PAUSADO", "Fase " + (faseAtual + 1) + "/" + TOTAL_FASES,
                       OP_CONTINUAR, OP_REINICIAR);
    }

    private void continuar() {
        iniciarContagem();
    }

    private void concluirFase() {
        forwardSpeed = 0f;
        jaRestante = 0f;
        congelarJogador();

        if (faseAtual >= TOTAL_FASES - 1) {
            estado = EstadoJogo.JOGO_CONCLUIDO;
            ui.mostrarMenu("PARABENS!", "Voce completou as " + TOTAL_FASES + " fases",
                           OP_JOGAR_NOVAMENTE);
        } else {
            estado = EstadoJogo.FASE_CONCLUIDA;
            String sub = "Sobrou " + UIManager.formatarTempo(tempoRestante)
                       + "   +" + (int) BONUS_TEMPO[faseAtual + 1] + "s de bonus";
            ui.mostrarMenu("FASE " + (faseAtual + 1) + " CONCLUIDA", sub, OP_PROXIMA);
        }
    }

    private void gameOver(String motivo) {
        forwardSpeed = 0f;
        jaRestante = 0f;
        invencivelRestante = 0f;
        congelarJogador();
        estado = EstadoJogo.GAME_OVER;

        float faltaM = META_METROS[faseAtual] - distanciaPixels / PIXELS_POR_METRO;
        ui.mostrarMenu(motivo, "Faltaram " + MathUtils.ceil(faltaM) + " m", OP_REINICIAR);
    }

    private void congelarJogador() {
        player.setFreando(true);
        player.update(0f);
    }

    private void draw() {
        ScreenUtils.clear(Color.DARK_GRAY);

        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        pista.draw(spriteBatch);

        boolean piscandoApagado = invencivelRestante > 0f
                               && MathUtils.floor(invencivelRestante * 10f) % 2 == 0;
        if (!piscandoApagado) player.draw(spriteBatch);

        for (NpcCarro npc : npcs) {
            npc.draw(spriteBatch);
        }
        spriteBatch.end();

        atualizarTextoCentral();

        ui.draw(spriteBatch, faseAtual + 1, TOTAL_FASES, tempoRestante,
                distanciaPixels / PIXELS_POR_METRO, META_METROS[faseAtual], vidas, MAX_VIDAS);
    }

    private void atualizarTextoCentral() {
        if (estado == EstadoJogo.CONTAGEM_REGRESSIVA) {
            int numero = Math.max(1, MathUtils.ceil(contagemRestante));
            float animacao = numero - contagemRestante;
            ui.mostrarTextoCentral(String.valueOf(numero), animacao, Color.WHITE);
        } else if (estado == EstadoJogo.JOGANDO && jaRestante > 0f) {
            ui.mostrarTextoCentral("JA!", 1f - jaRestante / DURACAO_JA, Color.GREEN);
        } else {
            ui.esconderTextoCentral();
        }
    }

    @Override
    public void pause() {
        if (estado == EstadoJogo.JOGANDO || estado == EstadoJogo.CONTAGEM_REGRESSIVA) pausar();
    }

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        leitorEsp32.stopReading();
        try {
            leitorEsp32.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        spriteBatch.dispose();
        ui.dispose();
        for (int i = 0; i < texturasFases.length; i++) {
            if (i == 0 || texturasFases[i] != texturasFases[0]) {
                texturasFases[i].dispose();
            }
        }
        playerFreio.dispose();
        playerNormal.dispose();
        npcTexture.dispose();
        soundtrack.dispose();
    }
}