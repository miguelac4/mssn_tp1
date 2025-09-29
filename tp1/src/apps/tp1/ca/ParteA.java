package apps.tp1.ca;

import processing.core.PApplet;
import setup.IProcessingApp;

import java.util.HashSet;
import java.util.Set;

public class ParteA implements IProcessingApp {

    private boolean isRunning = true;

    private CellularAutomata ca; // Tabuleiro
    private int rows, cols, cw, ch;  // dimensões da grelha

    // Listas para as regras (nao permitem duplicados)
    private Set<Integer> survive = new HashSet<>();
    private Set<Integer> born = new HashSet<>();

    // ________________Regras JogoDaVida_________________
    private void setRule_23_3() {
        survive.clear(); born.clear();
        survive.add(2); survive.add(3); // 2/3 vizinhos -> mantêm a célula viva
        born.add(3);                    //   3 vizinhos -> fazem nascer
    }

    private void setRule_23_36() { // OPCIONAL
        survive.clear(); born.clear();
        survive.add(2); survive.add(3); // 2/3 vizinhos -> mantêm a célula viva
        born.add(3); born.add(6);       // 3/6 vizinhos -> fazem nascer
    }
    // __________________________________________________

    // Função Motora do Jogo da Vida
    private void step(){
        // Buffer para evitar alterar células enquanto ainda estás a contar vizinhos
        int[][] next = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // Transformar coordenadas para objeto Cell
                Cell cell = ca.pixel2Cell(c * cw, r * ch);

                int alive = 0; // Contar vizinhos vivos
                // Somamos todos os estados vizinho e eliminamos o proprio (que nao é vizinho)
                for (Cell n : cell.getNeighbors()) {
                    alive += n.getState();
                }

                alive -= cell.getState();

                // Verifica o estado da celula e muda consoante a regra
                if (cell.getState() == 1) { // célula viva
                    next[r][c] = survive.contains(alive) ? 1 : 0;
                } else { // célula morta
                    next[r][c] = born.contains(alive) ? 1 : 0;
                }
            }
        }

        // Aplicar novo estado da grelha
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                ca.pixel2Cell(c * cw, r * ch).setState(next[r][c]);
            }
        }
    }


    @Override
    public void setup(PApplet p) {
        /*
        * nStates = 2            , estado podera ser viva/morta
        * radiusNeigh = 1        , raio de vizinhança
        * */
        ca = new CellularAutomata(p, 50, 50, 2, 1);
        p.frameRate(5);

        // Definir linhas e colunas da grelha
        cw = ca.getCellWidth();
        ch = ca.getCellHeight();
        rows = p.height / ch;
        cols = p.width / cw;

        int[] colors = ca.getStateColors();
        colors[0] = p.color(0);     // Cor Morta
        colors[1] = p.color(255, 0, 0); // Cor Viva

        /* Descomentar regra a usar */
        setRule_23_3();
        //setRule_23_36();

        ca.initRandom(); // Iniciar celulas randomly

        System.out.println("Espaço (Para o Jogo) | S (Avança step by step)");
    }

    @Override
    public void draw(PApplet p, float dt) {
        p.background(200);
        if (isRunning){
            step();
        }
        ca.display(p);
    }

    @Override
    public void keyPressed(PApplet p) {
        switch (p.key) {
            case ' ': isRunning = !isRunning; break;
            case 's': step(); break;
        }
    }

    @Override
    public void mousePressed(PApplet p) {

    }
}
