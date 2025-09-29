package apps.work.ca;

import processing.core.PApplet;
import setup.IProcessingApp;

import java.util.HashSet;
import java.util.Set;

public class TestCA implements IProcessingApp {

    private CellularAutomata ca;
    private boolean running = true;

    // regras (sobrevive/nasce)
    private Set<Integer> survive = new HashSet<>();
    private Set<Integer> born = new HashSet<>();

    private int rows, cols, cw, ch;

    @Override
    public void setup(PApplet p) {
        // 2 estados (0 morto, 1 vivo), vizinhança Moore raio 1
        ca = new CellularAutomata(p, 80, 80, 2, 1);

        // derivar dimensões sem getters adicionais
        cw = ca.getCellWidth();
        ch = ca.getCellHeight();
        rows = p.height / ch;
        cols = p.width / cw;

        // cores (podemos alterar o array devolvido)
        int[] colors = ca.getStateColors();
        colors[0] = p.color(30);          // morto
        colors[1] = p.color(30, 220, 90); // vivo

        setRule_23_3();
        ca.initRandom(); // usa o que já existe
    }

    private void setRule_23_3() { // clássico
        survive.clear(); born.clear();
        survive.add(2); survive.add(3);
        born.add(3);
    }

    private void setRule_23_36() { // variante opcional
        survive.clear(); born.clear();
        survive.add(2); survive.add(3);
        born.add(3); born.add(6);
    }

    private void step() {
        int[][] next = new int[rows][cols];

        // calcular próximo estado
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = ca.pixel2Cell(c * cw, r * ch);
                int alive = 0;
                for (Cell n : cell.getNeighbors()) alive += n.getState();
                alive -= cell.getState(); // retirar a própria

                next[r][c] = (cell.getState() == 1)
                        ? (survive.contains(alive) ? 1 : 0)
                        : (born.contains(alive) ? 1 : 0);
            }
        }
        // aplicar (usando novamente pixel2Cell)
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                ca.pixel2Cell(c * cw, r * ch).setState(next[r][c]);
    }

    @Override
    public void draw(PApplet p, float dt) {
        p.background(18);
        if (running) step();
        ca.display(p);

        p.fill(255);
        p.text("SPACE: play/pause | N: step | R: random | C: clear | 1: 23/3 | 2: 23/36",
                8, p.height - 8);
    }

    @Override
    public void keyPressed(PApplet p) {
        switch (p.key) {
            case ' ': running = !running; break;
            case 'n': case 'N': step(); break;
            case 'r': case 'R': ca.initRandom(); break; // random simples já existente
            case 'c': case 'C':
                // limpar: põe tudo a 0 percorrendo com pixel2Cell
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++)
                        ca.pixel2Cell(c * cw, r * ch).setState(0);
                break;
            case '1': setRule_23_3(); break;
            case '2': setRule_23_36(); break;
        }
    }

    @Override
    public void mousePressed(PApplet p) {
        // alternar célula clicada
        Cell cell = ca.pixel2Cell(p.mouseX, p.mouseY);
        cell.setState(cell.getState() == 1 ? 0 : 1);
    }
}
