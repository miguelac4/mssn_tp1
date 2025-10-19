package apps.tp1.dla;

import processing.core.PApplet;
import setup.IProcessingApp;

public class ParteB implements IProcessingApp {
    private DLA dla;

    @Override
    public void setup(PApplet p) {
        dla = new DLA(p);

        // presets simples
        dla.BASE_RADIUS     = 3;
        dla.STEP_SIZE       = 1.8f;
        dla.CENTER_BIAS     = 0.0035f;
        dla.STICKINESS      = 1.0f;
        dla.WALKERS_TARGET  = 200;
        dla.SPAWN_MARGIN_PX = 12f * dla.BASE_RADIUS;
        dla.KILL_MARGIN_PX  = 60f * dla.BASE_RADIUS;

        dla.setup();
    }

    @Override public void draw(PApplet p, float dt) { dla.draw(dt); }

    @Override
    public void keyPressed(PApplet p) {
        // usa switch por estilo uniforme; delega tudo para o DLA
        switch (p.key) {
            case '1','2','3','4','c','C','p','P','r','R','s','S','+','=' ,'-','_' -> dla.keyPressed(p.key);
            default -> dla.keyPressed(p.key); // restantes teclas também seguem para o DLA
        }
    }

    @Override public void mousePressed(PApplet p) { /* sem uso aqui */ }
}

