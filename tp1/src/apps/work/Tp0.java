package apps.work;
import processing.core.PApplet;
import setup.IProcessingApp;

public class Tp0 implements IProcessingApp {
    @Override
    public void setup(PApplet p) {
        p.textSize(16);
    }

    @Override
    public void draw(PApplet p, float dt) {
        p.background(200);
        p.fill(0);
        p.text("X: " + p.mouseX + "  Y: " + p.mouseY, 10, 30);

        // círculos concêntricos
        p.fill(0);
        p.ellipse(p.width/2, p.height/2, 400, 400);

        p.fill(255);
        p.ellipse(p.width/2, p.height/2, 370, 370);

        p.fill(0);
        p.ellipse(p.width/2, p.height/2, 290, 290);

        // linhas com estilo temporário
        p.pushStyle();
        p.stroke(255);
        p.strokeWeight(40);

        int lm_x1 = 145;
        int lm_y1 = 65;
        int lm_x2 = 250;
        int lm_y2 = 330;

        // linha esquerda
        p.line(lm_x1, lm_y1, lm_x2, lm_y2);
        // espelhada no eixo Y
        p.line(p.width - lm_x1, lm_y1, p.width - lm_x2, lm_y2);

        int pm_x1 = 145;
        int pm_y1 = 340;
        int pm_x2 = 80;
        int pm_y2 = 100;

        // outras duas linhas fixas
        p.line(pm_x1, pm_y1, pm_x2, pm_y2);
        p.line(p.width - pm_x1, pm_y1, p.width - pm_x2, pm_y2);


        p.popStyle();
        p.pushStyle();

        p.fill(255);

        p.strokeWeight(10);
        p.line(160, 200, 250, 200);

        p.popStyle();
    }

    @Override
    public void keyPressed(PApplet parent) {

    }

    @Override
    public void mousePressed(PApplet parent){

}
}
