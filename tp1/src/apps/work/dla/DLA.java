package apps.work.dla;

import processing.core.PApplet;
import processing.core.PVector;
import setup.IProcessingApp;

import java.util.ArrayList;
import java.util.List;

/**
 * DLA “Paul Bourke style”
 * - repõe walkers para manter nº de WANDER constante
 * - spawn junto à fronteira do cluster (clusterRadius + margem)
 * - kill radius para reciclar walkers perdidos
 * - stick ao tocar (≈2R) com probabilidade (stickness)
 * - cor por tick (HSB)
 */
public class DLA implements IProcessingApp {

    /* ----------------- parâmetros principais ----------------- */
    private int TARGET_WANDERERS   = 200;   // walkers em movimento (constante)
    private int NUM_STEPS_PER_FRAME= 200;   // mais passos -> mais rápido
    private int SPAWN_MARGIN       = 18;    // px para nascer logo após a fronteira
    private int KILL_MARGIN        = 4 * SPAWN_MARGIN; // px para matar e respawnar
    /* --------------------------------------------------------- */

    private final List<Walker> walkers = new ArrayList<>();

    // seed/inicialização
    private enum Seed { POINT, LINE, CIRCLE, SQUARE }
    private Seed seed = Seed.POINT; // usar POINT para “árvore” clássica

    // estado global
    private boolean paused = false;
    private int globalTick = 0;
    private float clusterRadius = 1; // px, medido ao centro (cresce com o cluster)

    @Override
    public void setup(PApplet p) {
        reset(p);
    }

    private void reset(PApplet p) {
        walkers.clear();
        Walker.num_wanders = 0;
        Walker.num_stopped = 0;
        globalTick = 0;
        paused = false;
        clusterRadius = 1;

        // seeds
        switch (seed) {
            case POINT -> walkers.add(Walker.seedAtCenter(p));
            case LINE -> {
                float y = p.height/2f;
                for (int x = (int)(p.width*0.25f); x <= p.width*0.75f; x += 2*Walker.BASE_RADIUS)
                    walkers.add(Walker.seedAt(p, new PVector(x, y)));
            }
            case CIRCLE -> {
                float cx = p.width/2f, cy = p.height/2f, r = PApplet.min(cx, cy)*0.25f;
                for (float a=0; a<PApplet.TWO_PI; a+= (2*Walker.BASE_RADIUS)/r)
                    walkers.add(Walker.seedAt(p, new PVector(cx + r*PApplet.cos(a), cy + r*PApplet.sin(a))));
            }
            case SQUARE -> {
                float x0 = p.width*0.25f, x1 = p.width*0.75f, y0 = p.height*0.25f, y1 = p.height*0.75f;
                for (float x=x0; x<=x1; x+=2*Walker.BASE_RADIUS) {
                    walkers.add(Walker.seedAt(p, new PVector(x,y0)));
                    walkers.add(Walker.seedAt(p, new PVector(x,y1)));
                }
                for (float y=y0; y<=y1; y+=2*Walker.BASE_RADIUS) {
                    walkers.add(Walker.seedAt(p, new PVector(x0,y)));
                    walkers.add(Walker.seedAt(p, new PVector(x1,y)));
                }
            }
        }

        // encher com wanderers
        while (Walker.num_wanders < TARGET_WANDERERS) walkers.add(spawnWanderer(p));
    }

    @Override
    public void draw(PApplet p, float dt) {
        if (!paused) {
            for (int i = 0; i < NUM_STEPS_PER_FRAME; i++) {
                globalTick++;

                // mover + tentar colar
                for (int idx = walkers.size()-1; idx>=0; idx--) {
                    Walker w = walkers.get(idx);
                    if (w.getState() == Walker.State.WANDER) {
                        w.wander(p);

                        boolean contacted = w.tryStickIfTouching(p, walkers, globalTick);
                        if (contacted && w.getState() == Walker.State.STOPPED) {
                            // atualiza “raio do cluster”
                            PVector c = new PVector(p.width/2f, p.height/2f);
                            clusterRadius = Math.max(clusterRadius, PVector.dist(w.getPos(), c));
                            // repõe para manter nº de walkers constante
                            walkers.add(spawnWanderer(p));
                        } else {
                            // recicla walkers que saem demasiado longe (kill radius)
                            if (tooFarFromCluster(p, w)) {
                                walkers.set(idx, spawnWanderer(p));
                            }
                        }
                    }
                }
            }
        }

        // render
        p.background(190);
        for (Walker w : walkers) w.display(p);

        // HUD
        p.fill(0);
        p.text("stopped=" + Walker.num_stopped +
                "  wander=" + Walker.num_wanders +
                "  tick=" + globalTick +
                "  seed=" + seed +
                "  steps/frame=" + NUM_STEPS_PER_FRAME, 10, 16);
        p.text("[1]Point [2]Line [3]Circle [4]Square  (+/-)steps  (W/Q)wanderers  (R)reset  (P)pause", 10, 32);
    }

    @Override
    public void keyPressed(PApplet p) {
        switch (p.key) {
            case '1' -> { seed = Seed.POINT;  reset(p); }
            case '2' -> { seed = Seed.LINE;   reset(p); }
            case '3' -> { seed = Seed.CIRCLE; reset(p); }
            case '4' -> { seed = Seed.SQUARE; reset(p); }
            case '+' -> NUM_STEPS_PER_FRAME = Math.max(1, NUM_STEPS_PER_FRAME + 50);
            case '-' -> NUM_STEPS_PER_FRAME = Math.max(1, NUM_STEPS_PER_FRAME - 50);
            case 'w' -> TARGET_WANDERERS = Math.max(10, TARGET_WANDERERS + 25);
            case 'q' -> TARGET_WANDERERS = Math.max(10, TARGET_WANDERERS - 25);
            case 'p' -> paused = !paused;
            case 'r' -> reset(p);
        }
        // garantir alvo após mudar w/q
        while (Walker.num_wanders < TARGET_WANDERERS) walkers.add(spawnWanderer(p));
    }

    @Override public void mousePressed(PApplet p) { /* opcional: adicionar seeds onde clicar */ }

    /* ----------------- helpers ----------------- */

    /** nasce numa coroa logo a seguir ao cluster atual */
    private Walker spawnWanderer(PApplet p) {
        float cx = p.width/2f, cy = p.height/2f;
        float r  = Math.max(10, clusterRadius + SPAWN_MARGIN);
        float a  = p.random(PApplet.TWO_PI);
        return new Walker(p, new PVector(cx + r*PApplet.cos(a), cy + r*PApplet.sin(a)), true);
    }

    /** mata/recicla walkers muito afastados (mantém foco na fronteira) */
    private boolean tooFarFromCluster(PApplet p, Walker w) {
        float cx = p.width/2f, cy = p.height/2f;
        float killR = Math.min(Math.min(cx, cy) - 2, clusterRadius + KILL_MARGIN);
        return PVector.dist(w.getPos(), new PVector(cx, cy)) > killR;
    }
}
