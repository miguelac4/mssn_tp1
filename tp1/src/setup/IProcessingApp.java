package setup;

import processing.core.PApplet;

public interface IProcessingApp {

    public void setup(PApplet parent);

    public void draw(PApplet parent, float dt);

    public void keyPressed(PApplet parent);

    public void mousePressed(PApplet parent);

}
