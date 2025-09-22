package apps.face;

import processing.core.PApplet;
import processing.core.PVector;
import setup.IProcessingApp;

public class FaceApp implements IProcessingApp {

    private Face f1, f2, f3;

    @Override
    public void setup(PApplet parent) {
        f1 = new Face(new PVector(400,300), 30f);
        f2 = new Face(new PVector(100,500), 50f);
        f3 = new Face(new PVector(500,100), 20f);
    }

    @Override
    public void draw(PApplet parent, float dt) {
        f1.display(parent);
        f2.display(parent);
        f3.display(parent);
    }

    @Override
    public void keyPressed(PApplet parent) {

    }

    @Override
    public void mousePressed(PApplet parent) {
        PVector pos = new PVector(parent.mouseX, parent.mouseY);
        Face f = new Face(pos, 30f);
        f.display(parent);
    }
}
