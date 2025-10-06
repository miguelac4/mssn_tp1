package apps.tp1.dla;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.List;

public class Walker {

    public enum State { STOPPED, WANDER }

    // ----------------- parâmetros recomendados -----------------
    public static int   BASE_RADIUS  = 3;       // raio (px) para colisão/desenho
    public static float STICKNESS    = 1.0f;    // prob. de colagem ao tocar (0..1)
    public static float CENTER_BIAS  = 0.0025f; // viés muito leve para o centro
    public static float HUE_SPAN     = 300f;    // amplitude do gradiente (HSB)
    // ------------------------------------------------------------

    public static int num_wanders = 0;
    public static int num_stopped = 0;

    private PVector pos;       // posição contínua (px)
    private State state;
    private int col;           // cor já resolvida
    private int stickTick = -1;
    private float size = BASE_RADIUS;

    public Walker(PApplet p) {
        this(p, randomRingPosition(p, 0.9f), true);
    }

    public Walker(PApplet p, PVector pos, boolean startWander) {
        this.pos = pos.copy();
        setState(p, startWander ? State.WANDER : State.STOPPED, -1);
    }

    // seeds
    public static Walker seedAt(PApplet p, PVector pos) { return new Walker(p, pos, false); }
    public static Walker seedAtCenter(PApplet p) { return seedAt(p, new PVector(p.width/2f, p.height/2f)); }

    public State getState() { return state; }
    public PVector getPos() { return pos; }
    public int getStickTick() { return stickTick; }

    /* ----------------- dinâmica ----------------- */
    public void setState(PApplet p, State s, int tickWhenStuck){
        if (this.state == s) return;
        if (this.state == State.WANDER) num_wanders--;
        if (this.state == State.STOPPED) num_stopped--;

        this.state = s;
        if (state == State.STOPPED) {
            stickTick = tickWhenStuck;
            num_stopped++;
            col = colorFromTick(p, stickTick);
        } else {
            stickTick = -1;
            num_wanders++;
            col = p.color(255);
        }
    }

    /** Passo aleatório 2D com ligeiro viés para o centro */
    public void wander(PApplet p) {
        if (state != State.WANDER) return;
        pos.add(PVector.random2D()); // passo ~1px
        pos.lerp(new PVector(p.width/2f, p.height/2f), CENTER_BIAS);
        pos.x = PApplet.constrain(pos.x, 0, p.width-1);
        pos.y = PApplet.constrain(pos.y, 0, p.height-1);
    }

    /** Cola se tocar num STOPPED, com probabilidade STICKNESS; devolve true se houve contacto */
    public boolean tryStickIfTouching(PApplet p, List<Walker> walkers, int globalTick) {
        if (state == State.STOPPED) return false;

        final float collideDist = 2.1f * BASE_RADIUS; // ligeiramente > 2R
        for (Walker w : walkers) {
            if (w == this || w.state != State.STOPPED) continue;
            if (PVector.dist(pos, w.pos) <= collideDist) {
                if (p.random(1f) < STICKNESS) {
                    setState(p, State.STOPPED, globalTick);
                } else {
                    // “ricochete” leve se falhar colagem por probabilidade
                    PVector away = PVector.sub(pos, w.pos).setMag(1.2f);
                    pos.add(away);
                }
                return true;
            }
        }
        return false;
    }

    public void display(PApplet p) {
        p.noStroke();
        p.fill(col);
        p.circle(pos.x, pos.y, 2 * size);
    }

    /* ----------------- helpers ----------------- */

    /** posição aleatória numa coroa de raio rFrac * min(width,height)/2 */
    static PVector randomRingPosition(PApplet p, float rFrac) {
        float cx = p.width/2f, cy = p.height/2f;
        float r = rFrac * PApplet.min(cx, cy);
        float a = p.random(PApplet.TWO_PI);
        return new PVector(cx + r*PApplet.cos(a), cy + r*PApplet.sin(a));
    }

    /** cor por ordem temporal da colagem (gradiente HSB) */
    static int colorFromTick(PApplet p, int tick){
        if (tick < 0) return p.color(255);
        p.colorMode(PApplet.HSB, 360, 100, 100);
        float hue = (tick % (int)HUE_SPAN);
        int c = p.color(hue, 90, 95);
        p.colorMode(PApplet.RGB, 255);
        return c;
    }
}
