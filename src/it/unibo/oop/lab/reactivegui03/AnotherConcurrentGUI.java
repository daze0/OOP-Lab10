package it.unibo.oop.lab.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class AnotherConcurrentGUI {
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;

    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    private final CounterAgent counter = new CounterAgent();
    private final StopperAgent stopper = new StopperAgent(counter);

    public AnotherConcurrentGUI() {
        final JFrame mainFrame = new JFrame();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(this.display);
        panel.add(this.up);
        panel.add(this.down);
        panel.add(this.stop);
        mainFrame.setContentPane(panel);
        mainFrame.setVisible(true);
        /*
         * Listeners
         */
        this.up.addActionListener(e -> this.counter.up());
        this.down.addActionListener(e -> this.counter.down());
        this.stop.addActionListener(e -> this.counter.stopCounting());
        /*
         * Agents in action
         */
        new Thread(this.counter).start();
        new Thread(this.stopper).start();
    }

    private final class StopperAgent extends Thread {
        private static final int COUNT_TIME = 10_000;
        private final CounterAgent agent;

        private StopperAgent(final CounterAgent agent) {
            this.agent = agent;
        }

        public void run() {
            try {
                Thread.sleep(COUNT_TIME);
                SwingUtilities.invokeLater(() -> {
                    AnotherConcurrentGUI.this.up.setEnabled(false);
                    AnotherConcurrentGUI.this.down.setEnabled(false);
                    AnotherConcurrentGUI.this.stop.setEnabled(false);
                });
                this.stopCounter(this.agent);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        private void stopCounter(final AnotherConcurrentGUI.CounterAgent agent) {
            agent.stopCounting();
        }
    }

    private final class CounterAgent extends Thread {
        private volatile boolean stop;
        private volatile int counter;
        private volatile boolean isUp = true;

        public void run() {
            while (!this.stop) {
                try {
                    SwingUtilities.invokeAndWait(() -> display.setText(Integer.toString(counter)));
                    if (this.isUp) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void stopCounting() {
            this.stop = true;
            interrupt();
        }

        public void up() {
            this.isUp = true;
        }

        public void down() {
            this.isUp = false;
        }
    }
}
