package apps.tp1.dla;

import processing.core.PApplet;
import processing.core.PVector;
import java.util.ArrayList;
import java.util.List;


public class DLA {

    // parâmetros ajustáveis
    public int   BASE_RADIUS     = 3;
    public float STEP_SIZE       = 1.8f;
    public float CENTER_BIAS     = 0.0035f;
    public float STICKINESS      = 1.0f;
    public int   WALKERS_TARGET  = 200;
    public float SPAWN_MARGIN_PX = 36f;
    public float KILL_MARGIN_PX  = 180f;

    private final PApplet p;

    private final List<PVector> clusterPos = new ArrayList<>();
    private final List<Integer> clusterCol = new ArrayList<>();
    private final List<Walker> walkers = new ArrayList<>();

    private PVector center;
    private float frontierRadMax = 0;
    private float frontierRadMin = Float.MAX_VALUE;
    private float frontierLineY  = Float.MAX_VALUE;
    private float frontierSquareLinf = Float.MAX_VALUE;

    private int shape = 0; // 0:POINT, 1:CIRCLE, 2:LINE, 3:SQUARE
    private boolean paused = false;
    private long totalSticks = 0, sticksThisFrame = 0;

    public DLA(PApplet p) { this.p = p; }

    // ciclo principal

    public void setup() {
        p.colorMode(PApplet.HSB, 360, 100, 100, 100);
        p.getSurface().setTitle("Parte B — DLA (com switch)");
        reset();
    }

    public void draw(float dt) {
        p.background(0, 0, 0);
        sticksThisFrame = 0;

        // cluster
        p.noStroke();
        for (int i = 0; i < clusterPos.size(); i++) {
            p.fill(clusterCol.get(i));
            PVector q = clusterPos.get(i);
            p.circle(q.x, q.y, BASE_RADIUS * 2f);
        }

        if (!paused) simulate();

        // walkers
        p.noStroke();
        p.fill(200, 40, 100, 70);
        for (Walker w : walkers) p.circle(w.pos.x, w.pos.y, BASE_RADIUS * 2f);

        drawHUD();
    }

    public void keyPressed(char k) {
        switch (k) {
            case 'p', 'P' -> paused = !paused;
            case 'r', 'R' -> reset();
            case 's', 'S' -> p.saveFrame("dla-####.png");
            case '+', '=' -> STICKINESS = Math.min(1f, STICKINESS + 0.1f);
            case '-', '_' -> STICKINESS = Math.max(0f, STICKINESS - 0.1f);
            case 'c', 'C' -> { shape = (shape + 1) % 4; reset(); }
            case '1' -> { shape = 0; reset(); }
            case '2' -> { shape = 1; reset(); }
            case '3' -> { shape = 2; reset(); }
            case '4' -> { shape = 3; reset(); }
        }
    }

    // simulação

    public void reset() {
        clusterPos.clear();
        clusterCol.clear();
        walkers.clear();
        totalSticks = 0;
        sticksThisFrame = 0;

        center = new PVector(p.width / 2f, p.height / 2f);
        frontierRadMax = 0;
        frontierRadMin = Float.MAX_VALUE;
        frontierLineY  = Float.MAX_VALUE;
        frontierSquareLinf = Float.MAX_VALUE;

        switch (shape) {
            case 0 -> { // Ponto
                addParticle(center.copy(), p.color(0, 0, 100));
            }
            case 1 -> { // Círculo
                float r = Math.min(p.width, p.height) * 0.35f;
                for (int i = 0; i < 200; i++) {
                    float a = PApplet.map(i, 0, 200, 0, PApplet.TWO_PI);
                    PVector q = new PVector(center.x + PApplet.cos(a)*r, center.y + PApplet.sin(a)*r);
                    addParticle(q, p.color(0, 0, 100));
                }
            }
            case 2 -> { // Linha
                float y = center.y + Math.min(p.width, p.height) * 0.20f;
                for (float x = BASE_RADIUS; x <= p.width - BASE_RADIUS; x += BASE_RADIUS * 2.2f)
                    addParticle(new PVector(x, y), p.color(0, 0, 100));
            }
            case 3 -> { // Quadrado
                float s = Math.min(p.width, p.height) * 0.22f;
                for (float x = center.x - s; x <= center.x + s; x += BASE_RADIUS * 2.2f) {
                    addParticle(new PVector(x, center.y - s), p.color(0, 0, 100));
                    addParticle(new PVector(x, center.y + s), p.color(0, 0, 100));
                }
                for (float y = center.y - s; y <= center.y + s; y += BASE_RADIUS * 2.2f) {
                    addParticle(new PVector(center.x - s, y), p.color(0, 0, 100));
                    addParticle(new PVector(center.x + s, y), p.color(0, 0, 100));
                }
            }
        }

        recomputeFrontier();
        while (walkers.size() < WALKERS_TARGET) walkers.add(makeWalker());
    }

    private void simulate() {
        for (int i = walkers.size() - 1; i >= 0; i--) {
            Walker w = walkers.get(i);

            if (outOfBounds(w.pos)) {
                walkers.set(i, makeWalker());
                continue;
            }

            w.step(center);
            int idx = firstHitIndex(w.pos);
            if (idx >= 0 && p.random(1) <= STICKINESS) {
                int c = colorByDistance(w.pos);
                addParticle(w.pos.copy(), c);
                updateFrontierAfterAdd(w.pos);
                walkers.set(i, makeWalker());
                totalSticks++;
                sticksThisFrame++;
            }
        }
    }

    //  lógica de spawn / fronteiras

    private Walker makeWalker() {
        switch (shape) {
            case 0 -> { // Ponto: cresce para fora
                float r = frontierRadMax + SPAWN_MARGIN_PX;
                return new Walker(p, ringPoint(r), BASE_RADIUS, STICKINESS, CENTER_BIAS, STEP_SIZE);
            }
            case 1 -> { // Circulo: cresce para dentro
                float r = Math.max(BASE_RADIUS * 2f, frontierRadMin - SPAWN_MARGIN_PX);
                return new Walker(p, ringPoint(r), BASE_RADIUS, STICKINESS, CENTER_BIAS, STEP_SIZE);
            }
            case 2 -> { // Linha: cresce para cima
                float x = p.random(BASE_RADIUS, p.width - BASE_RADIUS);
                float y = frontierLineY - SPAWN_MARGIN_PX;
                return new Walker(p, new PVector(x, y), BASE_RADIUS, STICKINESS, CENTER_BIAS, STEP_SIZE);
            }
            case 3 -> { // Quadrado: cresce para dentro
                float s = Math.max(BASE_RADIUS * 4f, frontierSquareLinf - SPAWN_MARGIN_PX);
                int side = (int) p.random(4);
                float x = center.x, y = center.y;
                switch (side) {
                    case 0 -> { y = center.y - s; x = p.random(center.x - s, center.x + s); }
                    case 1 -> { x = center.x + s; y = p.random(center.y - s, center.y + s); }
                    case 2 -> { y = center.y + s; x = p.random(center.x - s, center.x + s); }
                    case 3 -> { x = center.x - s; y = p.random(center.y - s, center.y + s); }
                }
                return new Walker(p, new PVector(x, y), BASE_RADIUS, STICKINESS, CENTER_BIAS, STEP_SIZE);
            }
        }
        return new Walker(p, center.copy(), BASE_RADIUS, STICKINESS, CENTER_BIAS, STEP_SIZE);
    }

    private boolean outOfBounds(PVector pos) {
        switch (shape) {
            case 0 -> { // Ponto
                float r = PVector.dist(pos, center);
                return r > frontierRadMax + SPAWN_MARGIN_PX + KILL_MARGIN_PX;
            }
            case 1 -> { // Círculo
                float r = PVector.dist(pos, center);
                return r < Math.max(0, frontierRadMin - SPAWN_MARGIN_PX - KILL_MARGIN_PX);
            }
            case 2 -> { // Linha
                float yKill = frontierLineY - SPAWN_MARGIN_PX - KILL_MARGIN_PX;
                return pos.y < yKill || pos.y < -KILL_MARGIN_PX || pos.y > p.height + KILL_MARGIN_PX;
            }
            case 3 -> { // Quadrado
                float sKill = Math.max(0, frontierSquareLinf - SPAWN_MARGIN_PX - KILL_MARGIN_PX);
                float linf = Math.max(Math.abs(pos.x - center.x), Math.abs(pos.y - center.y));
                return linf < sKill;
            }
        }
        return false;
    }

    private void recomputeFrontier() {
        switch (shape) {
            case 0 -> { // Ponto
                frontierRadMax = (float) clusterPos.stream()
                        .mapToDouble(q -> PVector.dist(q, center))
                        .max().orElse(0);
            }
            case 1 -> { // Círculo
                frontierRadMin = (float) clusterPos.stream()
                        .mapToDouble(q -> PVector.dist(q, center))
                        .min().orElse(Float.MAX_VALUE);
            }
            case 2 -> { // Linha
                frontierLineY = (float) clusterPos.stream()
                        .mapToDouble(q -> q.y)
                        .min().orElse(Float.MAX_VALUE);
            }
            case 3 -> { // Quadrado
                frontierSquareLinf = (float) clusterPos.stream()
                        .mapToDouble(q -> Math.max(Math.abs(q.x - center.x), Math.abs(q.y - center.y)))
                        .min().orElse(Float.MAX_VALUE);
            }
        }
    }


    private void updateFrontierAfterAdd(PVector added) {
        switch (shape) {
            case 0 -> frontierRadMax = Math.max(frontierRadMax, PVector.dist(added, center));
            case 1 -> frontierRadMin = Math.min(frontierRadMin, PVector.dist(added, center));
            case 2 -> { if (added.y < frontierLineY) frontierLineY = added.y; }
            case 3 -> {
                float linf = Math.max(Math.abs(added.x - center.x), Math.abs(added.y - center.y));
                if (linf < frontierSquareLinf) frontierSquareLinf = linf;
            }
        }
    }

    //  utilidades

    private int firstHitIndex(PVector probe) {
        float r2 = (BASE_RADIUS * 2.1f) * (BASE_RADIUS * 2.1f);
        for (int i = 0; i < clusterPos.size(); i++) {
            PVector q = clusterPos.get(i);
            float dx = probe.x - q.x, dy = probe.y - q.y;
            if (dx * dx + dy * dy <= r2) return i;
        }
        return -1;
    }

    private void addParticle(PVector pos, int col) {
        clusterPos.add(pos);
        clusterCol.add(col);
    }

    private int colorByDistance(PVector pos) {
        float d = PVector.dist(pos, center);
        float ref = Math.max(1f, Math.min(p.width, p.height) * 0.5f);
        float h = PApplet.map(d, 0, ref, 0, 300) % 360f;
        return p.color(h, 90, 100);
    }

    private PVector ringPoint(float r) {
        float a = p.random(PApplet.TWO_PI);
        return new PVector(center.x + PApplet.cos(a) * r, center.y + PApplet.sin(a) * r);
    }

    private void drawHUD() {
        p.fill(255);
        p.textSize(14);
        String name = switch (shape) {
            case 0 -> "POINT";
            case 1 -> "CIRCLE";
            case 2 -> "LINE";
            case 3 -> "SQUARE";
            default -> "?";
        };
        p.text("Init: " + name, 20, 30);
    }

}
