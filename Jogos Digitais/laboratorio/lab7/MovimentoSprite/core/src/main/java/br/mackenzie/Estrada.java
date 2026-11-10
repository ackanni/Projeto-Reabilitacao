package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Estrada {
    private static final float SOBREPOSICAO = 1f;

    private static class Bloco {
        Texture textura;
        float y;
        float altura;
    }

    private final Viewport viewport;
    private final Array<Bloco> blocos = new Array<>();
    private Texture texturaAtual;

    private float distanciaRolada = 0f;

    public Estrada(Texture texturaInicial, Viewport viewport) {
        this.viewport = viewport;
        reiniciar(texturaInicial);
    }

    public void update(float velocidade, float delta) {
        if (velocidade <= 0f) return;

        float dy = velocidade * delta;
        distanciaRolada += dy;

        for (Bloco b : blocos) {
            b.y -= dy;
        }

        while (blocos.size > 0 && blocos.first().y + blocos.first().altura <= 0f) {
            blocos.removeIndex(0);
        }

        garantirCobertura();
    }

    public void draw(SpriteBatch batch) {
        float largura = viewport.getWorldWidth();
        for (Bloco b : blocos) {
            batch.draw(b.textura, 0f, b.y, largura, b.altura + SOBREPOSICAO);
        }
    }

    public void trocarTextura(Texture novaTextura) {
        if (novaTextura == texturaAtual) return;
        configurarTextura(novaTextura);
        texturaAtual = novaTextura;
    }

    public void reiniciar(Texture textura) {
        configurarTextura(textura);
        texturaAtual = textura;
        blocos.clear();
        distanciaRolada = 0f;
        garantirCobertura();
    }

    public float imagemParaMundoX(float xPixelNaImagem) {
        return xPixelNaImagem * viewport.getWorldWidth() / texturaAtual.getWidth();
    }

    public float getDistanciaRolada() {
        return distanciaRolada;
    }

    private void garantirCobertura() {
        float topo = blocos.size == 0 ? 0f : blocos.peek().y + blocos.peek().altura;
        float alturaTela = viewport.getWorldHeight();

        while (topo < alturaTela) {
            Bloco b = new Bloco();
            b.textura = texturaAtual;
            b.altura = alturaEscalada(texturaAtual);
            b.y = topo;
            blocos.add(b);
            topo += b.altura;
        }
    }

    private float alturaEscalada(Texture t) {
        return viewport.getWorldWidth() * t.getHeight() / (float) t.getWidth();
    }

    private void configurarTextura(Texture t) {
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        t.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    }
}