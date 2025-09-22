package apps.face;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Face {
    private PVector pos;
    private float radius;
    private PVector target;

    public Face(PVector pos, float radius) {
        this.pos = pos;
        this.radius = radius;
    }

    public void setTarget(PVector target) {
        this.target = target;
    }

    public void move(PVector d) {
        pos.add(d);
    }

    public void moveToTarget() {
        if (target != null) {
            float alpha = 0.01f;
            PVector d = PVector.sub(target, pos);
            pos.add(d.mult(alpha));
        }
    }

    public void display(PApplet p) {
        
        p.pushMatrix();

        p.translate(pos.x, pos.y);
        //face
        p.circle(0, 0, 2f * radius);
        //nose
        p.circle(0, 0, radius / 20f);
        //boca
        p.arc(0, 0, 1.2f * radius, 0.6f * radius, p.radians(20), p.radians(160), PConstants.CHORD);
        //eyes
        p.translate(0.3f * radius, -0.4f * radius);
        p.circle(0, 0, radius / 5f);
        p.translate(-2 * 0.3f * radius, 0);
        p.circle(0, 0, radius / 5f);

        p.popMatrix();
    }
}
