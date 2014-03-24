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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Main extends javax.swing.JFrame {

    public static final double DPI = 300.0;
    public static final Path cwd = new File("").getAbsoluteFile().toPath();
    private String matchsource;
    private String pitsource;
    private String rulesource;

    private final TeamDisplay[] teamDisplays;
    private final JFormattedTextField[] textFields;

    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        teamDisplays = new TeamDisplay[]{
            teamDisplay1, teamDisplay2, teamDisplay3,
            teamDisplay4, teamDisplay5, teamDisplay6
        };
        textFields = new JFormattedTextField[]{
            jFormattedTextField1, jFormattedTextField2, jFormattedTextField3,
            jFormattedTextField4, jFormattedTextField5, jFormattedTextField6
        };
        for (int i = 0; i < textFields.length; i++) {
            bind(textFields[i], teamDisplays[i]);
        }

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

        jScrollPane1.getHorizontalScrollBar().setUnitIncrement(40);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(40);

        final Preferences pref = Preferences.userRoot().node("scouting-summary");
        setMatchSource(pref.get("match_source", "data/qualifications.tsv"));
        setPitSource(pref.get("pit_source", "data/pitscouting.tsv"));
        setRuleSource(pref.get("rule_source", "data/rules.xml"));

        addOpener(matchItem, "Choose Match Data File", new StringFunc() {
            @Override
            public void doIt(String s) {
                pref.put("match_source", s);
                setMatchSource(s);
                reload();
            }
        });

        addOpener(scoutingItem, "Choose Pit Scouting Data File", new StringFunc() {
            @Override
            public void doIt(String s) {
                pref.put("pit_source", s);
                setPitSource(s);
                reload();
            }
        });

        addOpener(rulesItem, "Choose Graph Rules", new StringFunc() {
            @Override
            public void doIt(String s) {
                pref.put("rule_source", s);
                setRuleSource(s);
                reload();
            }
        });

        reload();
    }

    private static void addOpener(JMenuItem i, final String title, final StringFunc f) {
        i.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getPath(title, f);
            }
        });
    }

    private void setMatchSource(String s) {
        matchsource = s;
    }

    private void setPitSource(String s) {
        pitsource = s;
    }

    private void setRuleSource(String s) {
        rulesource = s;
    }

    private void reload() {
        for (TeamDisplay r : teamDisplays) {
            r.setTeam(null);
        }
        records.clear();

        System.out.println(matchsource);
        System.out.println(pitsource);
        System.out.println(rulesource);

        TSVDoc matches = new TSVDoc(matchsource);
        TSVDoc pitscout = new TSVDoc(pitsource);

        loadData(matches, pitscout);

        GraphRules[] rules = loadRules(rulesource);
        for (TeamDisplay r : teamDisplays) {
            r.setRules(rules);
        }

        for (int i = 0; i < textFields.length; i++) {
            forceUpdate(textFields[i], teamDisplays[i]);
        }
    }

    private static Iterable<Element> getChildren(final Element e) {
        final NodeList l = e.getChildNodes();
        return new Iterable<Element>() {
            @Override
            public Iterator<Element> iterator() {
                return new Iterator<Element>() {
                    int i = 0;

                    @Override
                    public boolean hasNext() {
                        advance();
                        return i < l.getLength();
                    }

                    @Override
                    public Element next() {
                        Element n = (Element) l.item(i);
                        i++;
                        advance();
                        return (Element) n;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    private void advance() {
                        while (i < l.getLength() && !(l.item(i) instanceof Element)) {
                            i++;
                        }
                    }
                };
            }
        };
    }

    private static void maltypeError(Element e) {
        System.err.format("Maltyped child: |%s|%n", e.getTagName());
    }

    private static void loadBlock(ArrayList<Element> a, Element e) {
        for (Element sub : getChildren(e)) {
            if (sub.getTagName().equals("block")) {
                a.add(sub);
            } else {
                maltypeError(sub);
            }
        }
    }

    private static int intAttr(Element e, String attr, int def) {
        String scale = e.getAttribute(attr);
        if (scale.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(scale);
        } catch (NumberFormatException ex) {
            return def;
        }
    }

    private static String strAttr(Element e, String attr, String def) {
        if (e.hasAttribute(attr)) {
            String s = e.getAttribute(attr);
            if (s.isEmpty()) {
                return def;
            }
            return s;
        }
        return def;
    }

    private GraphRules[] loadRules(String path) {
        Document document;
        try {
            File file = new File(path);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(file);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace(System.err);
            return new GraphRules[0];
        }
        Element root = document.getDocumentElement();
        root.normalize();

        ArrayList<Element> plot = new ArrayList<>();
        ArrayList<Element> success = new ArrayList<>();
        ArrayList<Element> failure = new ArrayList<>();

        ArrayList<GraphRules> graphs = new ArrayList<>();

        ArrayList<SII> kp = new ArrayList<>();
        ArrayList<SII> ks = new ArrayList<>();
        ArrayList<SII> kf = new ArrayList<>();

        for (Element graph : getChildren(root)) {
            if (!graph.getTagName().equals("graph")) {
                continue;
            }

            int plotmax = 1;
            for (Element e : getChildren(graph)) {
                switch (e.getTagName()) {
                    case "plot":
                        if (e.hasAttribute("max")) {
                            try {
                                plotmax = Integer.parseInt(e.getAttribute("max"));
                            } catch (NumberFormatException ex) {
                                ex.printStackTrace(System.err);
                            }
                        }
                        loadBlock(plot, e);
                        break;
                    case "success":
                        loadBlock(success, e);
                        break;
                    case "failure":
                        loadBlock(failure, e);
                        break;
                    default:
                        maltypeError(e);
                        break;
                }
            }

            String name = graph.getAttribute("name");
            if (name.isEmpty()) {
                name = "???";
            }

            for (Element e : success) {
                ks.add(new SII(strAttr(e, "key", "???"), 0, intAttr(e, "scale", 1)));
            }
            for (Element e : failure) {
                kf.add(new SII(strAttr(e, "key", "???"), 0, intAttr(e, "scale", 1)));
            }
            for (Element e : plot) {
                String k = strAttr(e, "color", "BLACK");
                int ch;
                switch (k) {
                    case "BLACK":
                        ch = Chart.FULL;
                        break;
                    case "GRAY":
                        ch = Chart.MEDIUM;
                        break;
                    case "WHITE":
                        ch = Chart.CLEAR;
                        break;
                    default:
                        ch = Chart.FULL;
                        break;

                }
                kp.add(new SII(strAttr(e, "key", "???"), ch, intAttr(e, "scale", 1)));
            }
            graphs.add(new GraphRules(name,
                    ks.toArray(new SII[ks.size()]),
                    kf.toArray(new SII[kf.size()]),
                    plotmax,
                    kp.toArray(new SII[kp.size()])));
            plot.clear();
            success.clear();
            failure.clear();
            ks.clear();
            kf.clear();
            kp.clear();
        }

        return graphs.toArray(new GraphRules[graphs.size()]);
    }

    private void forceUpdate(JFormattedTextField j, TeamDisplay d) {
        Object e = j.getValue();
        if (e != null) {
            long l = (Long) j.getValue();
            d.setTeam(records.get((int) l));
        } else {
            d.setTeam(null);
        }
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
                        d.setTeam(r);
                    }
                }
            }
        });
        j.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        j.addFocusListener(new SelectOnFocus(j));
    }

    private static class SelectOnFocus implements FocusListener {

        private final JFormattedTextField field;

        public SelectOnFocus(JFormattedTextField p) {
            this.field = p;
        }

        @Override
        public void focusGained(FocusEvent e) {
            field.setText(field.getText());
            field.selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {
        }
    }

    private static interface StringFunc {

        public void doIt(String s);
    }

    private static void getPath(String title, StringFunc func) {
        final StringFunc fff = func;
        final JFileChooser s = new JFileChooser(".");
        final JFrame r = new JFrame(title);
        r.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        r.add(s);
        r.pack();
        r.setVisible(true);
        s.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Path target = s.getSelectedFile().getAbsoluteFile().toPath();
                if (target == null) {
                    return;
                }
                String p = cwd.relativize(target).toString();
                fff.doIt(p);
                r.dispose();
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
            try {
                ms.put(Integer.parseInt(kl[0]), v);
            } catch (NumberFormatException e) {
                e.printStackTrace(System.err);
            }
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
        if (s.length <= cut) {
            return new String[0];
        }
        String[] out = new String[s.length - cut];
        for (int i = cut; i < s.length; i++) {
            out[i - cut] = s[i];
        }
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
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        rulesItem = new javax.swing.JMenuItem();
        scoutingItem = new javax.swing.JMenuItem();
        matchItem = new javax.swing.JMenuItem();

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
        saveItem.setText("Save To Image");
        jMenu1.add(saveItem);
        jMenu1.add(jSeparator1);

        rulesItem.setText("Set Graph Rules");
        jMenu1.add(rulesItem);

        scoutingItem.setText("Set Pit Scouting Source");
        jMenu1.add(scoutingItem);

        matchItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        matchItem.setText("Set Match Data Source");
        jMenu1.add(matchItem);

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
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JMenuItem matchItem;
    private javax.swing.JMenuItem rulesItem;
    private javax.swing.JMenuItem saveItem;
    private javax.swing.JMenuItem scoutingItem;
    private scouting.summary.TeamDisplay teamDisplay1;
    private scouting.summary.TeamDisplay teamDisplay2;
    private scouting.summary.TeamDisplay teamDisplay3;
    private scouting.summary.TeamDisplay teamDisplay4;
    private scouting.summary.TeamDisplay teamDisplay5;
    private scouting.summary.TeamDisplay teamDisplay6;
    // End of variables declaration//GEN-END:variables
}
