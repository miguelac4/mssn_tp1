package apps.work.dla;

import processing.core.PApplet;
import setup.IProcessingApp;

/**
 * ParteB — Runner “de aluno” para testar a implementação do DLA,
 * no mesmo espírito da ParteA (GoL).
 *
 * Esta classe delega no motor DLA (que já implementa IProcessingApp),
 * mas concentra aqui o “entry point” de avaliação/experimentação.
 */
public class ParteB implements IProcessingApp {

    private DLA dla;
    private boolean showHelp = true;

    @Override
    public void setup(PApplet p) {
        // cria e inicializa o motor DLA
        dla = new DLA();
        dla.setup(p);

        // frame rate “suave” (ajusta se quiseres ver mais devagar)
        p.frameRate(60);

        // Mensagens de ajuda
        p.println("[ParteB] Controlo DLA:");
        p.println("  1/2/3/4  -> Seed Point/Line/Circle/Square");
        p.println("  +/-      -> Steps por frame");
        p.println("  W/Q      -> Nº de walkers a vaguear (constante)");
        p.println("  P        -> Pausa/retoma");
        p.println("  R        -> Reset");
        p.println("  H        -> Mostrar/ocultar ajuda no ecrã");
    }

    @Override
    public void draw(PApplet p, float dt) {
        // desenhar simulação (delegado)
        dla.draw(p, dt);

        // overlay opcional com ajuda
        if (showHelp) {
            p.fill(0, 150);
            p.rect(8, p.height - 80, 460, 70);
            p.fill(255);
            p.text("DLA — Controlo: [1]Point [2]Line [3]Circle [4]Square  (+/-)steps  (W/Q)wanderers  (R)reset  (P)pause  (H)help",
                    16, p.height - 58);
            p.text("Nota: nº de walkers em movimento mantém-se constante; cores = ordem de colagem (gradiente HSB).",
                    16, p.height - 38);
        }
    }

    @Override
    public void keyPressed(PApplet p) {
        // teclas próprias
        if (p.key == 'h' || p.key == 'H') {
            showHelp = !showHelp;
            return;
        }
        // resto das teclas delega no DLA (1–4, +/-, W/Q, P, R)
        dla.keyPressed(p);
    }

    @Override
    public void mousePressed(PApplet p) {
        // (opcional) poderias delegar em dla.mousePressed(p) se adicionares funcionalidade lá
        dla.mousePressed(p);
    }
}
