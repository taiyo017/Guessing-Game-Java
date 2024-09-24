import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class EnhancedGuessingGame extends JFrame {
    private static final int MAX_ATTEMPTS = 4;
    private static final int TIME_LIMIT_EASY = 60;
    private static final int TIME_LIMIT_MEDIUM = 45;
    private static final int TIME_LIMIT_HARD = 30;
    private static final int TIME_WARNING_THRESHOLD = 10;

    private int secretNumber;
    private int attemptsLeft;
    private int score;
    private Timer timer; // Timer instance
    private int timeLeft;
    private int highScore;
    private String difficulty = "Medium (Ancient Path)";

    private JLabel attemptsLabel;
    private JLabel hintLabel;
    private JLabel previousGuessLabel;
    private JTextField guessField;
    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JButton guessButton;
    private JProgressBar timeProgressBar;
    private JLabel highScoreLabel;
    private JLabel storyLabel;

    private JPanel welcomePanel;
    private JPanel gamePanel;
    private JPanel countdownPanel;
    private JPanel feedbackPanel;
    private JLabel feedbackLabel;
    private JButton retryButton;
    private JButton quitButton;
    private JButton leaderboardButton;

    private float fontSizeMultiplier = 1.0f;

    // Color Palette (Temple Theme)
    private Color backgroundColor = new Color(251, 248, 240); // Light Sand
    private Color accentColor = new Color(139, 69, 19);  // Saddle Brown
    private Color buttonColor = new Color(205, 133, 63); // Peru
    private Color retryButtonColor = new Color(160, 82, 45); // Sienna
    private Color guessButtonColor = new Color(218, 165, 32); // Goldenrod

    private boolean isDarkMode = false;
    private JToggleButton themeToggle;

    private ArrayList<Integer> leaderboardScores = new ArrayList<>();

    private JLabel scoreResultLabel;

    public EnhancedGuessingGame() {
        setTitle("The Treasure of the Forgotten Temple");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());

        createThemeToggle();
        highScore = 0;

        createWelcomePanel();
        createGamePanel();
        createCountdownPanel();
        createFeedbackPanel();

        add(welcomePanel, "Welcome");
        add(countdownPanel, "Countdown");
        add(gamePanel, "Game");
        add(feedbackPanel, "Feedback");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.8);
        int height = (int) (screenSize.height * 0.8);
        setSize(width, height);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateFontSizes();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createThemeToggle() {
        themeToggle = new JToggleButton("🌙");
        themeToggle.addActionListener(e -> toggleTheme());
        themeToggle.setFocusable(false);
        themeToggle.setPreferredSize(new Dimension(40, 40));
        themeToggle.setFont(getScaledFont(Font.PLAIN, 16));
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        Color bgColor = isDarkMode ? new Color(45, 45, 45) : backgroundColor;
        Color fgColor = isDarkMode ? Color.LIGHT_GRAY : Color.BLACK;

        SwingUtilities.invokeLater(() -> {
            setBackground(bgColor);
            setForeground(fgColor);
            updateComponentColors(this, bgColor, fgColor);
        });
    }

    private void updateComponentColors(Container container, Color bgColor, Color fgColor) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(bgColor);
            }
            if (comp instanceof JLabel || comp instanceof JButton) {
                comp.setForeground(fgColor);
            }
            if (comp instanceof Container) {
                updateComponentColors((Container) comp, bgColor, fgColor);
            }
        }
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(backgroundColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(getScaledFont(Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setOpaque(false);
        button.setContentAreaFilled(false);

        return button;
    }

    private void createWelcomePanel() {
        welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(backgroundColor);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("The Treasure of the Forgotten Temple");
        welcomeLabel.setFont(getScaledFont(Font.BOLD, 36));
        welcomeLabel.setForeground(accentColor);

        storyLabel = new JLabel("<html><center>Deep within the jungle lies a forgotten temple, its treasures guarded by ancient riddles. Brave adventurer, can you decipher the secret number and claim the riches within?</center></html>");
        storyLabel.setFont(getScaledFont(Font.PLAIN, 18));
        storyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        storyLabel.setForeground(accentColor);

        JButton playButton = createStyledButton("Enter the Temple", buttonColor);
        playButton.addActionListener(e -> startCountdown());

        highScoreLabel = new JLabel("Highest Score: " + highScore);
        highScoreLabel.setFont(getScaledFont(Font.BOLD, 24));
        highScoreLabel.setForeground(accentColor);

        String[] difficulties = {"Easy (Novice Explorer)", "Medium (Ancient Path)", "Hard (Guardian's Trial)"};
        JComboBox<String> difficultySelector = new JComboBox<>(difficulties);
        difficultySelector.setSelectedItem("Medium (Ancient Path)");
        difficultySelector.setFont(getScaledFont(Font.PLAIN, 18));
        difficultySelector.addActionListener(e -> difficulty = (String) difficultySelector.getSelectedItem());

        JButton tutorialButton = createStyledButton("Read the Ancient Scroll", new Color(139, 69, 19));
        tutorialButton.addActionListener(e -> showTutorial());

        leaderboardButton = createStyledButton("Hall of Explorers", new Color(184, 134, 11));
        leaderboardButton.addActionListener(e -> showLeaderboard());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridy = 0;
        welcomePanel.add(themeToggle, gbc);

        gbc.gridy = 1;
        welcomePanel.add(welcomeLabel, gbc);
        gbc.gridy = 2;
        welcomePanel.add(storyLabel, gbc);
        gbc.gridy = 3;
        welcomePanel.add(highScoreLabel, gbc);
        gbc.gridy = 4;
        welcomePanel.add(difficultySelector, gbc);
        gbc.gridy = 5;
        welcomePanel.add(playButton, gbc);
        gbc.gridy = 6;
        welcomePanel.add(tutorialButton, gbc);
        gbc.gridy = 7;
        welcomePanel.add(leaderboardButton, gbc);
    }

    private void createGamePanel() {
        gamePanel = new JPanel(new GridBagLayout());
        gamePanel.setBackground(backgroundColor);
        gamePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createComponents();
        layoutComponents();
    }

    private void createCountdownPanel() {
        countdownPanel = new JPanel(new GridBagLayout());
        countdownPanel.setBackground(backgroundColor);
        countdownPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel countdownLabel = new JLabel("3", SwingConstants.CENTER);
        countdownLabel.setFont(getScaledFont(Font.BOLD, 72));
        countdownLabel.setForeground(accentColor);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        countdownPanel.add(countdownLabel, gbc);
    }

    private void createFeedbackPanel() {
        feedbackPanel = new JPanel(new GridBagLayout());
        feedbackPanel.setBackground(backgroundColor);
        feedbackPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        feedbackLabel = new JLabel("");
        feedbackLabel.setFont(getScaledFont(Font.BOLD, 28));
        feedbackLabel.setForeground(accentColor);
        feedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);

        scoreResultLabel = new JLabel("");
        scoreResultLabel.setFont(getScaledFont(Font.BOLD, 24));
        scoreResultLabel.setForeground(accentColor);
        scoreResultLabel.setHorizontalAlignment(SwingConstants.CENTER);

        retryButton = createStyledButton("Attempt Another Riddle", retryButtonColor);
        retryButton.addActionListener(e -> startCountdown());

        quitButton = createStyledButton("Leave the Temple", buttonColor);
        quitButton.addActionListener(e -> showWelcomePanel());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridy = 0;
        feedbackPanel.add(new JLabel(" "), gbc);

        gbc.gridy = 1;
        feedbackPanel.add(feedbackLabel, gbc);
        gbc.gridy = 2;
        feedbackPanel.add(scoreResultLabel, gbc);
        gbc.gridy = 3;
        feedbackPanel.add(retryButton, gbc);
        gbc.gridy = 4;
        feedbackPanel.add(quitButton, gbc);
    }

    private void createComponents() {
        attemptsLabel = new JLabel("Torches remaining: " + MAX_ATTEMPTS);
        attemptsLabel.setFont(getScaledFont(Font.BOLD, 24));
        attemptsLabel.setForeground(accentColor);

        hintLabel = new JLabel("Temple whispers: -");
        hintLabel.setFont(getScaledFont(Font.PLAIN, 20));
        hintLabel.setForeground(accentColor);

        previousGuessLabel = new JLabel("Last offering: -");
        previousGuessLabel.setFont(getScaledFont(Font.PLAIN, 20));
        previousGuessLabel.setForeground(accentColor);

        guessField = new JTextField(5);
        guessField.setFont(getScaledFont(Font.PLAIN, 28));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setBorder(BorderFactory.createLineBorder(accentColor, 2));
        guessField.addActionListener(e -> makeGuess());

        guessButton = createStyledButton("Offer", guessButtonColor);
        guessButton.setPreferredSize(new Dimension(120, 45));
        guessButton.addActionListener(e -> makeGuess());

        scoreLabel = new JLabel("Treasure value: 0");
        scoreLabel.setFont(getScaledFont(Font.BOLD, 24));
        scoreLabel.setForeground(accentColor);

        timerLabel = new JLabel("Time remaining: 30s");
        timerLabel.setFont(getScaledFont(Font.BOLD, 24));
        timerLabel.setForeground(accentColor);

        timeProgressBar = new JProgressBar(0, TIME_LIMIT_MEDIUM);
        timeProgressBar.setValue(TIME_LIMIT_MEDIUM);
        timeProgressBar.setForeground(buttonColor);
        timeProgressBar.setStringPainted(true);
        timeProgressBar.setFont(getScaledFont(Font.PLAIN, 16));
    }

    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gamePanel.add(attemptsLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gamePanel.add(timerLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gamePanel.add(timeProgressBar, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gamePanel.add(hintLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gamePanel.add(previousGuessLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gamePanel.add(guessField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gamePanel.add(guessButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gamePanel.add(scoreLabel, gbc);
    }

    private Font getScaledFont(int style, int baseSize) {
        return new Font("Georgia", style, (int) (baseSize * fontSizeMultiplier));
    }

    private void updateFontSizes() {
        fontSizeMultiplier = Math.min((float) getWidth() / 800, (float) getHeight() / 600);
        updateComponentFontSizes(this);
        revalidate();
        repaint();
    }

    private void updateComponentFontSizes(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel || comp instanceof JButton || comp instanceof JTextField || comp instanceof JProgressBar) {
                Font font = comp.getFont();
                comp.setFont(font.deriveFont(font.getStyle(), font.getSize2D() * fontSizeMultiplier));
            }
            if (comp instanceof Container) {
                updateComponentFontSizes((Container) comp);
            }
        }
    }

    private void showWelcomePanel() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "Welcome");
    }

    private void startCountdown() {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "Countdown");

        SwingUtilities.invokeLater(() -> {
            JLabel countdownLabel = (JLabel) countdownPanel.getComponent(0);
            for (int i = 3; i >= 0; i--) {
                final int count = i;
                try {
                    countdownLabel.setText(count > 0 ? String.valueOf(count) : "Begin!"); // Update directly
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            startGame();
        });
    }

    private void startGame() {
        attemptsLeft = MAX_ATTEMPTS;
        score = 0;
        timeLeft = switch (difficulty) {
            case "Easy (Novice Explorer)" -> TIME_LIMIT_EASY;
            case "Medium (Ancient Path)" -> TIME_LIMIT_MEDIUM;
            default -> TIME_LIMIT_HARD;
        };
        secretNumber = new Random().nextInt(50) + 1;

        attemptsLabel.setText("Torches remaining: " + attemptsLeft);
        hintLabel.setText("Temple whispers: -");
        previousGuessLabel.setText("Last offering: -");
        scoreLabel.setText("Treasure value: " + score);
        timerLabel.setText("Time remaining: " + timeLeft + "s");
        timerLabel.setForeground(accentColor);
        timeProgressBar.setMaximum(timeLeft);
        timeProgressBar.setValue(timeLeft);

        // Stop the existing timer if it's running
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        // Create a new Timer instance and start it
        timer = new Timer(1000, new TimerListener());
        timer.start();

        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), "Game");
    }

    private void makeGuess() {
        try {
            int guess = Integer.parseInt(guessField.getText());

            if (guess < 1 || guess > 50) {
                JOptionPane.showMessageDialog(this, "The temple guardian warns: 'Your offering must be between 1 and 50.'", "Invalid Offering", JOptionPane.WARNING_MESSAGE);
                return;
            }

            guessField.setText("");
            attemptsLeft--;
            attemptsLabel.setText("Torches remaining: " + attemptsLeft);
            previousGuessLabel.setText("Last offering: " + guess);

            if (guess == secretNumber) {
                score += timeLeft * 10;
                timer.stop();
                highScore = Math.max(highScore, score);
                highScoreLabel.setText("Highest Score: " + highScore);
                leaderboardScores.add(score);
                Collections.sort(leaderboardScores, Collections.reverseOrder());
                feedbackLabel.setText("<html><center>The ancient door creaks open, revealing the treasure chamber!</center></html>");
                scoreResultLabel.setText("<html><center>You have triumphed! Treasure value: " + score + "</center></html>");
                CardLayout cl = (CardLayout) getContentPane().getLayout();
                cl.show(getContentPane(), "Feedback");
            } else {
                String hint = guess < secretNumber ? "The temple whispers: 'The sacred number is greater.'" : "The temple whispers: 'The sacred number is lesser.'";
                hintLabel.setText("Temple whispers: " + hint);

                if (attemptsLeft == 0) {
                    timer.stop();
                    feedbackLabel.setText("<html><center>The temple guardian speaks: 'Your quest has failed. The sacred number was " + secretNumber + ".'</center></html>");
                    scoreResultLabel.setText("<html><center>Treasure value: " + score + "</center></html>");
                    CardLayout cl = (CardLayout) getContentPane().getLayout();
                    cl.show(getContentPane(), "Feedback");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "The temple guardian growls: 'Offer a number, seeker!'", "Invalid Offering", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class TimerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            timeLeft--;
            timerLabel.setText("Time remaining: " + timeLeft + "s");
            timeProgressBar.setValue(timeLeft);

            if (timeLeft <= TIME_WARNING_THRESHOLD) {
                timerLabel.setForeground(Color.RED);
            }

            if (timeLeft <= 0) {
                timer.stop();
                feedbackLabel.setText("<html><center>Time has run out! The temple doors slam shut. The sacred number was: " + secretNumber + "</center></html>");
                scoreResultLabel.setText("<html><center>Treasure value: " + score + "</center></html>");
                CardLayout cl = (CardLayout) getContentPane().getLayout();
                cl.show(getContentPane(), "Feedback");
            }
        }
    }

    private void showLeaderboard() {
        StringBuilder leaderboardText = new StringBuilder("<html><center><h2>Hall of Legendary Explorers</h2><br>");
        for (int i = 0; i < Math.min(10, leaderboardScores.size()); i++) {
            leaderboardText.append((i + 1)).append(". ").append(leaderboardScores.get(i)).append("<br>");
        }
        leaderboardText.append("</center></html>");
        JOptionPane.showMessageDialog(this, leaderboardText.toString(), "Hall of Legendary Explorers", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showTutorial() {
        JOptionPane.showMessageDialog(this,
                "<html><center><h2>The Ancient Scroll</h2><br>" +
                "Welcome, brave seeker of fortune!<br><br>" +
                "You stand before the Forgotten Temple, a place of mystery and untold riches. To claim its treasure, you must solve the temple's sacred riddle.<br><br>" +
                "Your task: Guess the secret number hidden within the temple walls. You have limited torches (attempts) and time. The quicker you solve the riddle, the greater your reward!<br><br>" +
                "Listen to the temple's whispers for guidance, but beware - the guardian's patience is not endless.<br><br>" +
                "May the ancient spirits guide your path to glory!</center></html>",
                "The Ancient Scroll",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnhancedGuessingGame::new);
    }
}