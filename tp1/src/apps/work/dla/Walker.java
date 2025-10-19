package apps.work.dla;

import processing.core.PApplet;
import processing.core.PVector;

public class Walker {

    enum State { WANDER, STOPPED }

    final PApplet p;
    PVector pos;
    final float size;
    final float stickiness;
    final float centerBias;
    final float stepSize;

    private State state = State.WANDER;

    Walker(PApplet p, PVector startPos, float size, float stickiness, float centerBias, float stepSize) {
        this.p = p;
        this.pos = startPos.copy();
        this.size = size;
        this.stickiness = PApplet.constrain(stickiness, 0f, 1f);
        this.centerBias = centerBias;
        this.stepSize = stepSize;
    }

    public boolean isStopped() { return state == State.STOPPED; }

    /** Um passo de passeio aleatório com viés opcional ao centro. */
    public void step(PVector center) {
        if (state == State.STOPPED) return;

        float ang = p.random(PApplet.TWO_PI);
        PVector jitter = new PVector(PApplet.cos(ang), PApplet.sin(ang)).mult(stepSize);

        if (centerBias > 0f) {
            PVector bias = PVector.sub(center, pos);
            if (bias.magSq() > 0) {
                bias.normalize().mult(stepSize * centerBias);
                jitter.add(bias);
            }
        }
        pos.add(jitter);
    }

    public void display() {
        // o desenho da cor do walker é feito na classe DLA (para aplicar o esquema selecionado)
        p.noStroke();
        p.circle(pos.x, pos.y, size * 2f);
    }
}


