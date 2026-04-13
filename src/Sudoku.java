import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.Border;

public class Sudoku {
    class Tile extends JButton {
        int r;
        int c;
        boolean fixed;

        Tile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int boardWidth = 620;
    int boardHeight = 760;

    String[] solution = {
        "387491625",
        "241568379",
        "569327418",
        "758619234",
        "123784596",
        "496253187",
        "934176852",
        "675832941",
        "812945763"
    };

    String[][] puzzleSets = {
        {
            "3-74916-5",
            "2-15683-9",
            "5-9327418",
            "-5861-234",
            "--3784596",
            "496253187",
            "934176852",
            "675832941",
            "812945763"
        },
        {
            "--74916-5",
            "2---6-3-9",
            "-----7-1-",
            "-586----4",
            "--3----9-",
            "--62--187",
            "9-4-7---2",
            "67-83----",
            "81--45---"
        },
        {
            "---491-5-",
            "2---6-3-9",
            "-----7-1-",
            "-5-6----4",
            "--3----9-",
            "---2--187",
            "9-4-7---2",
            "67-8----9",
            "81--45---"
        }
    };

    String[] difficultyNames = {"Easy", "Medium", "Hard"};
    String currentDifficulty = "Medium";
    String[] currentPuzzle;

    JFrame frame = new JFrame("Sudoku");
    JLabel titleLabel = new JLabel("Sudoku");
    JLabel timerLabel = new JLabel("Time: 00:00");
    JLabel errorsLabel = new JLabel("Errors: 0");

    JPanel topPanel = new JPanel(new BorderLayout());
    JPanel statusPanel = new JPanel(new GridLayout(1, 3, 10, 0));
    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
    JPanel boardPanel = new JPanel();
    JPanel buttonsPanel = new JPanel();

    JComboBox<String> difficultyCombo;
    JButton darkModeButton;
    JButton hintButton;
    JButton restartButton;

    JButton numSelected = null;
    Tile[][] tiles = new Tile[9][9];
    int errors = 0;
    int secondsElapsed = 0;
    boolean darkMode = false;

    Timer gameTimer;

    Color boardColor = Color.WHITE;
    Color tileColor = Color.WHITE;
    Color fixedTileColor = Color.LIGHT_GRAY;
    Color selectedColor = new Color(180, 205, 230);
    Color backgroundColor = Color.WHITE;
    Color foregroundColor = Color.BLACK;
    Color panelColor = Color.WHITE;
    Color buttonColor = new Color(245, 245, 245);
    Color buttonTextColor = Color.BLACK;
    Color controlButtonColor = Color.WHITE;
    Color tileBorderColor = Color.BLACK;

    Sudoku() {
        frame.setSize(boardWidth, boardHeight);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        errorsLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        errorsLabel.setHorizontalAlignment(JLabel.CENTER);

        statusPanel.add(titleLabel);
        statusPanel.add(timerLabel);
        statusPanel.add(errorsLabel);

        Font controlFont = new Font("Arial", Font.PLAIN, 16);
        JLabel difficultyLabel = new JLabel("Difficulty:");
        difficultyLabel.setFont(controlFont);

        difficultyCombo = new JComboBox<>(difficultyNames);
        difficultyCombo.setSelectedItem(currentDifficulty);
        difficultyCombo.setFont(controlFont);
        difficultyCombo.setPreferredSize(new Dimension(120, 34));
        difficultyCombo.setFocusable(false);
        difficultyCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newDifficulty = (String) difficultyCombo.getSelectedItem();
                if (!currentDifficulty.equals(newDifficulty)) {
                    currentDifficulty = newDifficulty;
                    loadPuzzle(currentDifficulty);
                }
            }
        });

        hintButton = new JButton("Hint");
        hintButton.setFont(controlFont);
        hintButton.setFocusable(false);
        hintButton.setPreferredSize(new Dimension(110, 36));
        hintButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                useHint();
            }
        });

        darkModeButton = new JButton("Dark Mode");
        darkModeButton.setFont(controlFont);
        darkModeButton.setFocusable(false);
        darkModeButton.setPreferredSize(new Dimension(110, 36));
        darkModeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                darkMode = !darkMode;
                darkModeButton.setText(darkMode ? "Light Mode" : "Dark Mode");
                applyTheme();
            }
        });

        restartButton = new JButton("New Game");
        restartButton.setFont(controlFont);
        restartButton.setFocusable(false);
        restartButton.setPreferredSize(new Dimension(130, 36));
        restartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadPuzzle(currentDifficulty);
            }
        });

        controlPanel.add(difficultyLabel);
        controlPanel.add(difficultyCombo);
        controlPanel.add(hintButton);
        controlPanel.add(darkModeButton);
        controlPanel.add(restartButton);

        topPanel.add(statusPanel, BorderLayout.NORTH);
        topPanel.add(controlPanel, BorderLayout.SOUTH);
        frame.add(topPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(9, 9));
        frame.add(boardPanel, BorderLayout.CENTER);

        buttonsPanel.setLayout(new GridLayout(1, 9, 4, 4));
        setupButtons();
        frame.add(buttonsPanel, BorderLayout.SOUTH);

        initTimer();
        loadPuzzle(currentDifficulty);
        applyTheme();

        frame.setVisible(true);
    }

    void initTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                secondsElapsed += 1;
                timerLabel.setText("Time: " + formatTime(secondsElapsed));
            }
        });
    }

    String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return String.format("%02d:%02d", minutes, remainder);
    }

    int difficultyIndex(String difficulty) {
        for (int i = 0; i < difficultyNames.length; i++) {
            if (difficultyNames[i].equals(difficulty)) {
                return i;
            }
        }
        return 1;
    }

    void loadPuzzle(String difficulty) {
        int index = difficultyIndex(difficulty);
        currentPuzzle = puzzleSets[index];
        errors = 0;
        secondsElapsed = 0;
        errorsLabel.setText("Errors: 0");
        timerLabel.setText("Time: 00:00");
        numSelected = null;
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
        gameTimer.start();

        boardPanel.removeAll();
        setupTiles();
        boardPanel.revalidate();
        boardPanel.repaint();
        applyTheme();
    }

    void setupTiles() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = new Tile(r, c);
                tiles[r][c] = tile;
                char tileChar = currentPuzzle[r].charAt(c);

                if (tileChar != '-') {
                    tile.fixed = true;
                    tile.setText(String.valueOf(tileChar));
                    tile.setFont(new Font("Arial", Font.BOLD, 20));
                    tile.setBackground(fixedTileColor);
                } else {
                    tile.fixed = false;
                    tile.setText("");
                    tile.setFont(new Font("Arial", Font.PLAIN, 20));
                    tile.setBackground(tileColor);
                }

                tile.setFocusPainted(false);
                tile.setOpaque(true);
                tile.setForeground(foregroundColor);
                tile.setBorder(createTileBorder(r, c));
                tile.setFocusable(false);

                tile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Tile tile = (Tile) e.getSource();
                        if (tile.fixed) {
                            return;
                        }
                        if (numSelected == null) {
                            return;
                        }
                        if (!tile.getText().equals("")) {
                            return;
                        }
                        String numSelectedText = numSelected.getText();
                        String tileSolution = String.valueOf(solution[tile.r].charAt(tile.c));
                        if (tileSolution.equals(numSelectedText)) {
                            tile.setText(numSelectedText);
                            tile.setForeground(foregroundColor);
                        } else {
                            errors += 1;
                            errorsLabel.setText("Errors: " + errors);
                            playSound(true);
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }
    }

    Border createTileBorder(int r, int c) {
        if ((r == 2 || r == 5) && (c == 2 || c == 5)) {
            return BorderFactory.createMatteBorder(1, 1, 5, 5, tileBorderColor);
        } else if (r == 2 || r == 5) {
            return BorderFactory.createMatteBorder(1, 1, 5, 1, tileBorderColor);
        } else if (c == 2 || c == 5) {
            return BorderFactory.createMatteBorder(1, 1, 1, 5, tileBorderColor);
        }
        return BorderFactory.createLineBorder(tileBorderColor);
    }

    void setupButtons() {
        for (int i = 1; i < 10; i++) {
            JButton button = new JButton(String.valueOf(i));
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.setFocusable(false);
            button.setBackground(buttonColor);
            button.setForeground(buttonTextColor);
            button.setOpaque(true);
            button.setBorder(BorderFactory.createLineBorder(tileBorderColor));
            buttonsPanel.add(button);

            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JButton button = (JButton) e.getSource();
                    if (numSelected != null) {
                        numSelected.setBackground(buttonColor);
                        numSelected.setForeground(buttonTextColor);
                    }
                    numSelected = button;
                    numSelected.setBackground(selectedColor);
                    numSelected.setForeground(Color.WHITE);
                }
            });
        }
    }

    void useHint() {
        List<Tile> emptyTiles = new ArrayList<>();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = tiles[r][c];
                if (!tile.fixed && tile.getText().equals("")) {
                    emptyTiles.add(tile);
                }
            }
        }

        if (emptyTiles.isEmpty()) {
            return;
        }

        Tile hintTile = emptyTiles.get(new Random().nextInt(emptyTiles.size()));
        String correctValue = String.valueOf(solution[hintTile.r].charAt(hintTile.c));
        hintTile.setText(correctValue);
        hintTile.setFont(new Font("Arial", Font.PLAIN, 20));
        hintTile.setForeground(foregroundColor);
        playSound(false);
    }

    void playSound(boolean error) {
        Toolkit.getDefaultToolkit().beep();
    }

    void applyTheme() {
        if (darkMode) {
            backgroundColor = new Color(24, 28, 34);
            panelColor = new Color(28, 33, 40);
            tileColor = new Color(42, 48, 57);
            fixedTileColor = new Color(55, 63, 75);
            foregroundColor = new Color(235, 235, 235);
            buttonColor = new Color(44, 50, 61);
            buttonTextColor = new Color(230, 230, 230);
            controlButtonColor = new Color(36, 42, 54);
            tileBorderColor = new Color(86, 96, 112);
            selectedColor = new Color(72, 121, 185);
        } else {
            backgroundColor = Color.WHITE;
            panelColor = new Color(245, 245, 245);
            tileColor = Color.WHITE;
            fixedTileColor = new Color(230, 230, 230);
            foregroundColor = Color.BLACK;
            buttonColor = new Color(245, 245, 245);
            buttonTextColor = Color.BLACK;
            controlButtonColor = new Color(240, 240, 240);
            tileBorderColor = Color.BLACK;
            selectedColor = new Color(180, 205, 230);
        }

        frame.getContentPane().setBackground(backgroundColor);
        topPanel.setBackground(panelColor);
        statusPanel.setBackground(panelColor);
        controlPanel.setBackground(panelColor);
        boardPanel.setBackground(panelColor);
        buttonsPanel.setBackground(panelColor);

        titleLabel.setForeground(foregroundColor);
        timerLabel.setForeground(foregroundColor);
        errorsLabel.setForeground(foregroundColor);

        for (Component comp : controlPanel.getComponents()) {
            if (comp instanceof JButton || comp instanceof JLabel || comp instanceof JComboBox) {
                comp.setForeground(foregroundColor);
                comp.setBackground(controlButtonColor);
                if (comp instanceof JButton) {
                    ((JButton) comp).setOpaque(true);
                    ((JButton) comp).setBorderPainted(true);
                    ((JButton) comp).setBorder(BorderFactory.createLineBorder(tileBorderColor));
                }
            }
            if (comp instanceof JComboBox) {
                ((JComboBox<?>) comp).setBackground(controlButtonColor);
                ((JComboBox<?>) comp).setForeground(foregroundColor);
            }
        }

        for (Component comp : buttonsPanel.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button == numSelected) {
                    button.setBackground(selectedColor);
                    button.setForeground(Color.WHITE);
                } else {
                    button.setBackground(buttonColor);
                    button.setForeground(buttonTextColor);
                }
                button.setOpaque(true);
                button.setBorder(BorderFactory.createLineBorder(tileBorderColor));
            }
        }

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = tiles[r][c];
                if (tile == null) {
                    continue;
                }
                if (tile.fixed) {
                    tile.setBackground(fixedTileColor);
                } else if (tile == numSelected) {
                    tile.setBackground(selectedColor);
                } else {
                    tile.setBackground(tileColor);
                }
                tile.setForeground(foregroundColor);
            }
        }

        if (numSelected != null) {
            numSelected.setBackground(selectedColor);
            numSelected.setForeground(foregroundColor);
        }
    }
}

