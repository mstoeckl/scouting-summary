/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scouting.summary;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFormattedTextField;

public class Main extends javax.swing.JFrame {

    public static final double DPI = 300.0;

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();

        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread save = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveImage();
                    }
                });
                save.setDaemon(false);
                save.start();
            }
        });

        bind(jFormattedTextField1, teamDisplay1);
        bind(jFormattedTextField2, teamDisplay2);
        bind(jFormattedTextField3, teamDisplay3);
        bind(jFormattedTextField4, teamDisplay4);
        bind(jFormattedTextField5, teamDisplay5);
        bind(jFormattedTextField6, teamDisplay6);

        jScrollPane1.getHorizontalScrollBar().setUnitIncrement(40);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(40);

        TSVDoc matches = new TSVDoc("matches.tsv");
        TSVDoc pitscout = new TSVDoc("scouting.tsv");
        loadData(matches, pitscout);
    }

    private void saveImage() {
        double real_x = (double) display.getWidth();
        double real_y = (double) display.getHeight();
        int w = (int) (7.5 * DPI);
        int h = (int) (10.0 * DPI);

        BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) b.getGraphics();
        g.scale(w / real_x, h / real_y);
        display.print(g);
        g.dispose();
        try {
            ImageIO.write(b, "PNG", new File("output.png"));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void bind(final JFormattedTextField j, final TeamDisplay d) {
        j.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("value")) {
                    Object n = evt.getNewValue();
                    Object o = evt.getOldValue();
                    if (n != o && n != null) {
                        TeamRecord r = records.get((int) (long) (Long) n);
                        if (r != null) {
                            d.setTeam(r);
                        }
                    }
                }
            }
        });
        j.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        j.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                j.setText(j.getText());
                j.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
    }

    private final SortedMap<Integer, TeamRecord> records = new TreeMap<>();

    private void loadData(TSVDoc mts, TSVDoc sct) {
        Map<Integer, SortedMap<Integer, int[]>> uuv = new TreeMap<>();
        for (String[] v : mts.data) {
            int[] f = new int[v.length - 1];
            int team = Integer.parseInt(v[0]);
            for (int i = 1; i < v.length; i++) {
                f[i - 1] = Integer.parseInt(v[i]);
            }
            if (!uuv.containsKey(team)) {
                uuv.put(team, new TreeMap<Integer, int[]>());
            }
            SortedMap<Integer, int[]> mp = uuv.get(team);
            mp.put(f[0], f);
        }

        SortedMap<Integer, int[][]> mr = new TreeMap<>();
        Iterator<Map.Entry<Integer, SortedMap<Integer, int[]>>> t = uuv.entrySet().iterator();
        while (t.hasNext()) {
            Map.Entry<Integer, SortedMap<Integer, int[]>> i = t.next();
            SortedMap<Integer, int[]> j = i.getValue();
            int[][] data = new int[j.size()][];
            Iterator<Map.Entry<Integer, int[]>> k = j.entrySet().iterator();
            for (int e = 0; k.hasNext(); e++) {
                data[e] = k.next().getValue();
            }
            mr.put(i.getKey(), data);
        }

        SortedMap<Integer, String[]> ms = new TreeMap<>();
        for (String[] kl : sct.data) {
            String[] v = new String[kl.length - 1];
            System.arraycopy(kl, 1, v, 0, v.length);
            ms.put(Integer.parseInt(kl[0]), v);
        }

        String[] matchheader = clip(mts.header, 1);
        String[] scoutheader = clip(sct.header, 1);

        Set<Integer> teams = new TreeSet<>();
        teams.addAll(mr.keySet());
        teams.addAll(ms.keySet());
        Iterator<Integer> ee = teams.iterator();
        while (ee.hasNext()) {
            int team = ee.next();
            int[][] mss = mr.get(team);
            String[] css = ms.get(team);
            records.put(team, new TeamRecord(team, matchheader, mss == null ? new int[0][] : mss,
                    css == null ? new String[0] : css, scoutheader));
        }
    }

    private static String[] clip(String[] s, int cut) {
        System.out.println(Arrays.toString(s));
        String[] out = new String[s.length - cut];
        for (int i = cut; i < s.length; i++) {
            out[i - cut] = s[i];
        }
        System.out.println(Arrays.toString(out));
        return out;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        jFormattedTextField2 = new javax.swing.JFormattedTextField();
        jFormattedTextField3 = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jFormattedTextField4 = new javax.swing.JFormattedTextField();
        jFormattedTextField5 = new javax.swing.JFormattedTextField();
        jFormattedTextField6 = new javax.swing.JFormattedTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        display = new javax.swing.JPanel();
        teamDisplay1 = new scouting.summary.TeamDisplay();
        teamDisplay2 = new scouting.summary.TeamDisplay();
        teamDisplay3 = new scouting.summary.TeamDisplay();
        teamDisplay4 = new scouting.summary.TeamDisplay();
        teamDisplay5 = new scouting.summary.TeamDisplay();
        teamDisplay6 = new scouting.summary.TeamDisplay();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        saveItem = new javax.swing.JMenuItem();
        openItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Scouting Summaries");
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 40));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));

        jLabel1.setText("Red");
        jPanel1.add(jLabel1);

        jFormattedTextField1.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel1.add(jFormattedTextField1);

        jFormattedTextField2.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel1.add(jFormattedTextField2);

        jFormattedTextField3.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel1.add(jFormattedTextField3);

        jLabel2.setText("Blue");
        jPanel1.add(jLabel2);

        jFormattedTextField4.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel1.add(jFormattedTextField4);

        jFormattedTextField5.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel1.add(jFormattedTextField5);

        jFormattedTextField6.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jPanel1.add(jFormattedTextField6);

        getContentPane().add(jPanel1);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));
        jPanel3.add(filler5);

        display.setLayout(new javax.swing.BoxLayout(display, javax.swing.BoxLayout.Y_AXIS));
        display.add(teamDisplay1);
        display.add(teamDisplay2);
        display.add(teamDisplay3);
        display.add(teamDisplay4);
        display.add(teamDisplay5);
        display.add(teamDisplay6);

        jPanel3.add(display);
        jPanel3.add(filler4);

        jPanel2.add(jPanel3);
        jPanel2.add(filler1);

        jScrollPane1.setViewportView(jPanel2);

        getContentPane().add(jScrollPane1);

        jMenu1.setText("File");

        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveItem.setText("Save");
        jMenu1.add(saveItem);

        openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openItem.setText("Open");
        jMenu1.add(openItem);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel display;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JFormattedTextField jFormattedTextField2;
    private javax.swing.JFormattedTextField jFormattedTextField3;
    private javax.swing.JFormattedTextField jFormattedTextField4;
    private javax.swing.JFormattedTextField jFormattedTextField5;
    private javax.swing.JFormattedTextField jFormattedTextField6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem openItem;
    private javax.swing.JMenuItem saveItem;
    private scouting.summary.TeamDisplay teamDisplay1;
    private scouting.summary.TeamDisplay teamDisplay2;
    private scouting.summary.TeamDisplay teamDisplay3;
    private scouting.summary.TeamDisplay teamDisplay4;
    private scouting.summary.TeamDisplay teamDisplay5;
    private scouting.summary.TeamDisplay teamDisplay6;
    // End of variables declaration//GEN-END:variables
}
