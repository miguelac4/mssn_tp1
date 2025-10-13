package apps.tp1.ca;

import processing.core.PApplet;
import setup.IProcessingApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ParteA implements IProcessingApp {

    private boolean isRunning = false;

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
                    if (n.getState() > 0) alive++;
                }

                if (cell.getState() > 0) alive--; // retirar a própria

                int cself = cell.getState();

                // Se viva
                if (cself > 0) {
                    next[r][c] = survive.contains(alive) ? cself : 0; // mantem a cor
                } else {
                    // se morta e nasce
                    // Nasce com a cor (estado) consoante as regras do ex. opcional 2
                    next[r][c] = born.contains(alive) ? dominantState(cell) : 0;
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

    // Exercicio Facultativo 2 (Regra da vizinhança de cores)
    private int dominantState(Cell cell) {
        int nStates = ca.getStateColors().length; // num de estados (cores) possiveis
        int[] counts = new int[nStates];

        // Contar e add ao array dos estados existentes (vivos)
        for (Cell n : cell.getNeighbors()) {
            int s = n.getState();
            if (s > 0) counts[s]++;
        }

        // Encontrar cor dominante na vizinhança 8
        int max = 0;
        for (int s = 1; s < nStates; s++) max = Math.max(max, counts[s]);
        if (max == 0) return 1;

        // Junta todas as cores dominantes
        ArrayList<Integer> top = new ArrayList<>();
        for (int s = 1; s < nStates; s++) if (counts[s] == max) top.add(s);

        // caso haja mais que uma, decide de forma random
        int idx = (int) (Math.random() * top.size());
        return top.get(idx);
    }

    // Cria celulas de forma random e atribui estados maiores que 1 (cores) aleatorias
    private void initRandomColoredStart(double pAlive) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean alive = Math.random() < pAlive;
                int state = 0;
                if (alive) {
                    state = 2 + (int)(Math.random() * (8)); // 2..9 se nStates=10
                }
                ca.pixel2Cell(c * cw, r * ch).setState(state);
            }
        }
    }

    // Função para completar a funcionalidade facultativa 2.3
    private void stampCenteredInit(String[] pat, int state) {
        // limpar a grelha
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                ca.pixel2Cell(c * cw, r * ch).setState(0);

        // centrar
        int h = pat.length;
        int w = pat[0].length();
        int r0 = rows/2 - h/2;
        int c0 = cols/2 - w/2;

        // meter os padroes na grelha
        for (int dr = 0; dr < h; dr++) {
            String line = pat[dr];
            for (int dc = 0; dc < w; dc++) {
                char ch = line.charAt(dc);
                if (ch == 'O' || ch == 'X' || ch == '1') {
                    int r = r0 + dr, c = c0 + dc;
                    ca.pixel2Cell(c * this.cw, r * this.ch).setState(state);
                }
            }
        }
    }

    // ---------- Padrões ----------
    private static final String[] PULSAR = new String[] {
            "..OOO...OOO..",
            ".............",
            "O....O.O....O",
            "O....O.O....O",
            "O....O.O....O",
            "..OOO...OOO..",
            ".............",
            "..OOO...OOO..",
            "O....O.O....O",
            "O....O.O....O",
            "O....O.O....O",
            ".............",
            "..OOO...OOO.."
    }; // period 3, 13x13

    private static final String[] PENTA_DECATHLON = new String[] {
            "..O..",
            "OO.OO",
            "..O..",
            "..O..",
            "..O..",
            "..O..",
            "..O..",
            "..O..",
            "OO.OO",
            "..O.."
    }; // period 15, 10x5 (orientação vertical)




    @Override
    public void setup(PApplet p) {
        /*
        * nStates = 2            , estado podera ser viva/morta
        * radiusNeigh = 1        , raio de vizinhança
        * */
        ca = new CellularAutomata(p, 50, 50, 10, 1);
        p.frameRate(5);

        // Definir linhas e colunas da grelha
        cw = ca.getCellWidth();
        ch = ca.getCellHeight();
        rows = p.height / ch;
        cols = p.width / cw;

        int[] colors = ca.getStateColors();
        colors[0] = p.color(0, 0, 0);     // Cor Morta
        // Atribuir cores aos restantes estados
        for (int s = 1; s < colors.length; s++) {
            colors[s] = p.color(p.random(255), p.random(255), p.random(255));
        }

        /* Descomentar regra a usar */
        setRule_23_3();
        //setRule_23_36();

        initRandomColoredStart(0.15); // Iniciar numero de celulas e cores randomly

        System.out.println(
                "COMANDOS:\n" +
                "Espaço (Começa / Pára o Jogo) \n" +
                "S      (Avança step by step) \n" +
                "R      (Random) \n" +
                "1      (Padrão: Pulsar) \n" +
                "2      (Padrão: Penta-decathlon)");
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
            case 'r': initRandomColoredStart(0.15); break;
            case '1':
                isRunning = false;
                setRule_23_3();
                stampCenteredInit(PULSAR, 1);
                break;

            case '2':
                isRunning = false;
                setRule_23_3();
                stampCenteredInit(PENTA_DECATHLON, 1);
                break;
        }
    }

    @Override
    public void mousePressed(PApplet p) {

    }
}
