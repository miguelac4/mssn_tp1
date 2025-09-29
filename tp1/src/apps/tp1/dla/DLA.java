package apps.tp1.dla;

import processing.core.PApplet;
import processing.core.PVector;
import setup.IProcessingApp;

import java.util.ArrayList;
import java.util.List;

public class DLA implements IProcessingApp {

    private List<Walker> walkers;
    private int NUM_WALKERS = 200;
    private int NUM_STEPS_PER_FRAME = 100;

    @Override
    public void setup(PApplet p) {
        walkers = new ArrayList<Walker>();

        Walker w = new Walker(p, new PVector(p.width/2, p.height/2));
        walkers.add(w);


        for (int i = 0; i < NUM_WALKERS; i++) {
            w = new Walker(p);
            walkers.add(w);
        }

    }

    @Override
    public void draw(PApplet p, float dt) {
        p.background(190);

        for (int i = 0; i < NUM_STEPS_PER_FRAME; i++) {
            for (Walker w : walkers) {
                if (w.getState() == Walker.State.WANDER) {
                    w.wander(p);
                    w.updateState(p, walkers);
                }
            }
        }

        for (Walker w : walkers) {
                w.display(p);
        }
        System.out.println("Stopped = " + Walker.num_stopped + "Wander = " + Walker.num_wonders);
    }

    @Override
    public void keyPressed(PApplet p) {

    }

    @Override
    public void mousePressed(PApplet p) {

    }
}
