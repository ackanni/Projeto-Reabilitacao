package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class UIManager {
    private static final float MARGEM = 20f;
    private static final float PADDING_HUD = 14f;
    private static final float RAIO_VIDA = 9f;

    private final FitViewport viewport;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 mouse = new Vector2();

    private final BitmapFont fonteHud;
    private final BitmapFont fonteTitulo;
    private final BitmapFont fonteOpcao;
    private final BitmapFont fonteDica;
    private final BitmapFont fonteContagem;

    private static final float ESCALA_CONTAGEM = 7f;
    private String textoCentral = null;
    private float animacaoCentral = 0f;
    private final Color corCentral = new Color(Color.WHITE);

    private boolean menuVisivel = false;
    private String tituloMenu = "";
    private String subtituloMenu = null;
    private String[] opcoes = new String[0];
    private int selecionada = 0;
    private int ultimoMouseX = -1, ultimoMouseY = -1;

    private float px, py, painelW, painelH;
    private float yTitulo, ySub, yDica, hOpcao;
    private Rectangle[] areasOpcoes = new Rectangle[0];

    public UIManager(float larguraMundo, float alturaMundo) {
        viewport = new FitViewport(larguraMundo, alturaMundo);
        fonteHud = criarFonte(1.6f);
        fonteTitulo = criarFonte(3.2f);
        fonteOpcao = criarFonte(2.1f);
        fonteDica = criarFonte(1.1f);
        fonteContagem = criarFonte(ESCALA_CONTAGEM);
    }

    public void mostrarTextoCentral(String texto, float animacao, Color cor) {
        textoCentral = texto;
        animacaoCentral = MathUtils.clamp(animacao, 0f, 1f);
        corCentral.set(cor);
    }

    public void esconderTextoCentral() {
        textoCentral = null;
    }

    public void resize(int largura, int altura) {
        viewport.update(largura, altura, true);
    }

    public void mostrarMenu(String titulo, String subtitulo, String... opcoesMenu) {
        tituloMenu = titulo;
        subtituloMenu = subtitulo;
        opcoes = opcoesMenu;
        selecionada = 0;
        menuVisivel = true;
        ultimoMouseX = Gdx.input.getX();
        ultimoMouseY = Gdx.input.getY();
        calcularLayoutMenu();
    }

    public void esconderMenu() {
        menuVisivel = false;
    }

    public String processarInputMenu() {
        if (!menuVisivel || opcoes.length == 0) return null;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) moverSelecao(-1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) moverSelecao(1);

        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);
        int sobMouse = opcaoNaPosicao(mouse.x, mouse.y);

        boolean mouseMexeu = Gdx.input.getX() != ultimoMouseX || Gdx.input.getY() != ultimoMouseY;
        ultimoMouseX = Gdx.input.getX();
        ultimoMouseY = Gdx.input.getY();
        if (mouseMexeu && sobMouse >= 0) selecionada = sobMouse;

        if (Gdx.input.justTouched() && sobMouse >= 0) {
            selecionada = sobMouse;
            return opcoes[selecionada];
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            return opcoes[selecionada];
        }
        return null;
    }

    private void moverSelecao(int direcao) {
        selecionada = (selecionada + direcao + opcoes.length) % opcoes.length;
    }

    private int opcaoNaPosicao(float x, float y) {
        for (int i = 0; i < areasOpcoes.length; i++) {
            if (areasOpcoes[i].contains(x, y)) return i;
        }
        return -1;
    }

    private void calcularLayoutMenu() {
        float W = viewport.getWorldWidth();
        float H = viewport.getWorldHeight();

        float padding = 32f;
        float hTitulo = fonteTitulo.getLineHeight();
        float hSub = subtituloMenu == null ? 0f : fonteHud.getLineHeight() + 10f;
        hOpcao = fonteOpcao.getLineHeight() + 14f;
        float hDica = fonteDica.getLineHeight();

        painelW = 640f;
        painelH = padding + hTitulo + 10f + hSub + 20f
                + hOpcao * opcoes.length + 24f + hDica + padding;
        px = (W - painelW) / 2f;
        py = (H - painelH) / 2f;

        yTitulo = py + painelH - padding;
        ySub = yTitulo - hTitulo - 10f;
        float yOpcoes = ySub - hSub - 20f;
        yDica = py + padding + hDica;

        float slotX = px + 90f;
        float slotW = painelW - 180f;
        areasOpcoes = new Rectangle[opcoes.length];
        for (int i = 0; i < opcoes.length; i++) {
            float topo = yOpcoes - i * hOpcao;
            areasOpcoes[i] = new Rectangle(slotX, topo - hOpcao, slotW, hOpcao);
        }
    }

    public void draw(SpriteBatch batch, int fase, int totalFases, float tempoRestante,
                     float distanciaMetros, float metaMetros, int vidas, int maxVidas) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        desenharHud(batch, fase, totalFases, tempoRestante, distanciaMetros, metaMetros, vidas, maxVidas);
        if (textoCentral != null) {
            desenharTextoCentral(batch);
        }
        if (menuVisivel) {
            desenharMenu(batch);
        }
    }

    private void desenharHud(SpriteBatch batch, int fase, int totalFases, float tempo,
                             float distM, float metaM, int vidas, int maxVidas) {
        String[] linhas = {
            "FASE " + fase + "/" + totalFases,
            "TEMPO " + formatarTempo(tempo),
            MathUtils.floor(distM) + " / " + MathUtils.floor(metaM) + " m",
            "VIDAS"
        };

        float largura = 0f;
        for (String l : linhas) {
            layout.setText(fonteHud, l);
            largura = Math.max(largura, layout.width);
        }
        largura = Math.max(largura, 180f);

        float alturaLinha = fonteHud.getLineHeight();
        float alturaBarra = 10f;
        float painelHudW = largura + PADDING_HUD * 2f;
        float painelHudH = alturaLinha * linhas.length + alturaBarra + PADDING_HUD * 2f + 6f;
        float painelX = viewport.getWorldWidth() - MARGEM - painelHudW;
        float painelY = viewport.getWorldHeight() - MARGEM - painelHudH;

        float progresso = metaM <= 0f ? 0f : MathUtils.clamp(distM / metaM, 0f, 1f);
        float barraX = painelX + PADDING_HUD;
        float barraY = painelY + PADDING_HUD;

        float topoLinhaVidas = painelY + painelHudH - PADDING_HUD - alturaLinha * 3f;
        float yCentroVidas = topoLinhaVidas - fonteHud.getCapHeight() / 2f;

        ativarTransparencia();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(painelX, painelY, painelHudW, painelHudH);
        shapes.setColor(1f, 1f, 1f, 0.2f);
        shapes.rect(barraX, barraY, largura, alturaBarra);
        shapes.setColor(0.3f, 0.85f, 0.4f, 1f);
        shapes.rect(barraX, barraY, largura * progresso, alturaBarra);
        for (int i = 0; i < maxVidas; i++) {
            float cx = painelX + painelHudW - PADDING_HUD - RAIO_VIDA - i * (RAIO_VIDA * 2f + 8f);
            boolean temVida = (maxVidas - 1 - i) < vidas;
            if (temVida) shapes.setColor(0.95f, 0.25f, 0.25f, 1f);
            else shapes.setColor(0.4f, 0.4f, 0.4f, 1f);
            shapes.circle(cx, yCentroVidas, RAIO_VIDA, 20);
        }
        shapes.end();

        batch.begin();
        float y = painelY + painelHudH - PADDING_HUD;
        for (int i = 0; i < 3; i++) {
            boolean tempoAcabando = (i == 1 && tempo <= 10f);
            fonteHud.setColor(tempoAcabando ? Color.RED : Color.WHITE);
            fonteHud.draw(batch, linhas[i], painelX + PADDING_HUD, y, largura, Align.right, false);
            y -= alturaLinha;
        }
        fonteHud.setColor(Color.WHITE);
        fonteHud.draw(batch, linhas[3], painelX + PADDING_HUD, y);
        batch.end();
    }

    private void desenharTextoCentral(SpriteBatch batch) {
        float t = animacaoCentral;
        float escala = ESCALA_CONTAGEM * (1.6f - 0.6f * t);
        float alpha = t < 0.75f ? 1f : (1f - t) / 0.25f;

        fonteContagem.getData().setScale(escala);
        float W = viewport.getWorldWidth();
        float yTopo = viewport.getWorldHeight() / 2f + fonteContagem.getCapHeight() / 2f;

        batch.begin();
        fonteContagem.setColor(0f, 0f, 0f, 0.6f * alpha);
        fonteContagem.draw(batch, textoCentral, 4f, yTopo - 4f, W, Align.center, false);
        fonteContagem.setColor(corCentral.r, corCentral.g, corCentral.b, alpha);
        fonteContagem.draw(batch, textoCentral, 0f, yTopo, W, Align.center, false);
        batch.end();
    }

    private void desenharMenu(SpriteBatch batch) {
        float W = viewport.getWorldWidth();
        float H = viewport.getWorldHeight();

        ativarTransparencia();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.6f);
        shapes.rect(0f, 0f, W, H);
        shapes.setColor(0.08f, 0.10f, 0.14f, 0.94f);
        shapes.rect(px, py, painelW, painelH);
        if (areasOpcoes.length > 0) {
            Rectangle r = areasOpcoes[selecionada];
            shapes.setColor(1f, 0.84f, 0f, 0.18f);
            shapes.rect(r.x, r.y, r.width, r.height);
        }
        shapes.end();

        batch.begin();
        fonteTitulo.setColor(Color.WHITE);
        fonteTitulo.draw(batch, tituloMenu, px, yTitulo, painelW, Align.center, false);

        if (subtituloMenu != null) {
            fonteHud.setColor(Color.LIGHT_GRAY);
            fonteHud.draw(batch, subtituloMenu, px, ySub, painelW, Align.center, false);
        }

        for (int i = 0; i < opcoes.length; i++) {
            Rectangle r = areasOpcoes[i];
            float yTexto = r.y + r.height - (r.height - fonteOpcao.getCapHeight()) / 2f;
            boolean sel = (i == selecionada);
            fonteOpcao.setColor(sel ? Color.GOLD : Color.LIGHT_GRAY);
            String texto = sel ? "> " + opcoes[i] + " <" : opcoes[i];
            fonteOpcao.draw(batch, texto, r.x, yTexto, r.width, Align.center, false);
        }

        fonteDica.setColor(Color.GRAY);
        fonteDica.draw(batch, "CLIQUE  ou  SETAS + ENTER", px, yDica, painelW, Align.center, false);
        batch.end();
    }

    public static String formatarTempo(float segundos) {
        int s = Math.max(0, MathUtils.ceil(segundos));
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    private void ativarTransparencia() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private BitmapFont criarFonte(float escala) {
        BitmapFont f = new BitmapFont();
        f.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        f.getData().setScale(escala);
        return f;
    }

    public void dispose() {
        shapes.dispose();
        fonteHud.dispose();
        fonteTitulo.dispose();
        fonteOpcao.dispose();
        fonteDica.dispose();
        fonteContagem.dispose();
    }
}