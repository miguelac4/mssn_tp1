package apps.face;

import processing.core.PApplet;
import processing.core.PVector;
import setup.IProcessingApp;

import java.util.ArrayList;
import java.util.List;

public class FaceApp2 implements IProcessingApp {

    private List<Face> faces;
    private int NumFaces = 5;


    @Override
    public void setup(PApplet parent) {
        faces = new ArrayList<Face>();

        for (int i = 0; i < NumFaces; i++) {
            float r = parent.random(20f, 60f);
            float x = parent.random(r, parent.width-r);
            float y = parent.random(r, parent.height)-r;

            Face f = new Face(new PVector(x, y), r);
            faces.add(f);
        }
    }

    @Override
    public void draw(PApplet parent, float dt) {
        for (Face f : faces) {
            f.display(parent);
        }
        for (Face f : faces) {
            f.moveToTarget();
        }
    }

    @Override
    public void keyPressed(PApplet parent) {

    }

    @Override
    public void mousePressed(PApplet parent) {
        for (Face f : faces) {
            f.setTarget(new PVector(parent.mouseX, parent.mouseY));
        }
    }
}
