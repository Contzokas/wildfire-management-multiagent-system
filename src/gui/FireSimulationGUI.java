package gui;

import javax.swing.*;
import javax.swing.UIManager; // ΠΡΟΣΘΗΚΗ: Explicit import για UIManager
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class FireSimulationGUI extends JFrame {
    private static final int GRID_SIZE = 5;
    private static final int CELL_SIZE = 100;
    
    private JPanel[][] gridCells;
    private JLabel[][] cellLabels;
    private JTextArea logArea;
    private JLabel statusLabel;
    private Map<String, String> cellStates;
    private Map<String, String> cellAgents;
    
    // Labels για δυναμική ενημέρωση στατιστικών
    private JLabel fireCountLabel;
    private JLabel trucksLabel;
    private JLabel aircraftLabel;
    private JLabel helicoptersLabel;
    private JLabel crewsLabel;
    
    // ΠΡΟΣΘΗΚΗ: Weather panel fields
    private JLabel windSpeedLabel;
    private JLabel windDirectionLabel;
    private JLabel temperatureLabel;
    private JLabel humidityLabel;
    private JLabel weatherConditionLabel;
    private JPanel weatherPanel;
    
    public FireSimulationGUI() {
        // ΠΡΟΣΘΗΚΗ: Ρύθμιση για καλύτερη υποστήριξη Unicode και ελληνικών
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("user.language", "el");
        System.setProperty("user.country", "GR");
        
        // Ρύθμιση Noto Emoji fonts
        setupNotoEmojiSupport();
        
        cellStates = new HashMap<>();
        cellAgents = new HashMap<>();
        
        // ΔΙΟΡΘΩΣΗ: initializeGrid ΠΡΩΤΑ, μετά initializeGUI
        initializeGrid();
        initializeGUI();
        
        // ΠΡΟΣΘΗΚΗ: Αυτόματη ενημέρωση καιρού και στατιστικών
        SwingUtilities.invokeLater(() -> {
            // Άμεση πρώτη ενημέρωση
            simulateWeatherUpdate();
            updateResourceStats(4, 4, 2, 2, 1, 1, 6, 6, 0);
            
            // ΠΡΟΣΘΗΚΗ: Timer για αυτόματη ενημέρωση καιρού κάθε 30 δευτερόλεπτα
            Timer weatherUpdateTimer = new Timer(30000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    simulateWeatherUpdate();
                    addLog("🔄 Αυτόματη ενημέρωση καιρικών συνθηκών");
                }
            });
            weatherUpdateTimer.start();
            
            // ΠΡΟΣΘΗΚΗ: Timer για ενημέρωση στατιστικών κάθε 10 δευτερόλεπτα
            Timer statsUpdateTimer = new Timer(10000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Μέτρηση ενεργών φωτιών
                    int activeFires = 0;
                    for (String state : cellStates.values()) {
                        if ("FIRE".equals(state)) {
                            activeFires++;
                        }
                    }
                    updateResourceStats(4, 4, 2, 2, 1, 1, 6, 6, activeFires);
                }
            });
            statsUpdateTimer.start();
            
            addLog("⚙️ Αυτόματες ενημερώσεις ενεργοποιήθηκαν");
            addLog("🌤️ Καιρός: Ενημέρωση κάθε 30 δευτερόλεπτα");
            addLog("📊 Στατιστικά: Ενημέρωση κάθε 10 δευτερόλεπτα");
        });
    }

    // ΠΡΟΣΘΗΚΗ: Ρύθμιση Noto Emoji support
    private void setupNotoEmojiSupport() {
        try {
            // Προσπάθεια φόρτωσης Noto Color Emoji
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] availableFonts = ge.getAvailableFontFamilyNames();
            
            boolean hasNoto = false;
            for (String font : availableFonts) {
                if (font.contains("Noto") && font.contains("Emoji")) {
                    hasNoto = true;
                    break;
                }
            }
            
            if (hasNoto) {
                System.out.println("✅ Noto Color Emoji font βρέθηκε!");
            } else {
                System.out.println("⚠️ Noto Color Emoji font δεν βρέθηκε, χρήση fallback");
            }
            
        } catch (Exception e) {
            System.err.println("Σφάλμα ρύθμισης Noto Emoji: " + e.getMessage());
        }
    }

    // ΕΝΗΜΕΡΩΣΗ: Μέθοδος για Noto Emoji font
    private Font getNotoEmojiFont(int size, int style) {
        // Προτεραιότητα fonts για emoji
        String[] emojieFonts = {
            "Noto Color Emoji",      // Google's emoji font
            "Apple Color Emoji",     // macOS
            "Segoe UI Emoji",        // Windows 10/11
            "Twitter Color Emoji",   // Twitter's emoji font
            "EmojiOne Color",        // EmojiOne
            "Symbola",               // Unicode symbols
            "Arial Unicode MS",      // Fallback with good Unicode support
            "Dialog"                 // Java fallback
        };
        
        String testEmojis = "🔥🚒✈️🚁👥💧🌲🌫️";
        
        for (String fontName : emojieFonts) {
            Font font = new Font(fontName, style, size);
            if (font.canDisplayUpTo(testEmojis) == -1) {
                return font;
            }
        }
        
        return new Font("Dialog", style, size);
    }
    
    // ΔΙΟΡΘΩΣΗ: showFireAt με final variables
    public void showFireAt(int x, int y, int intensity) {
        updateCell(x, y, "FIRE");
        
        String key = x + "," + y;
        if (!"FIRE".equals(cellStates.get(key))) {
            addLog("🔥 ΝΕΑ ΦΩΤΙΑ στη θέση (" + x + "," + y + ") - Ένταση: " + intensity);
            updateStatus("🔴 ΕΝΕΡΓΗ ΠΥΡΚΑΓΙΑ - Θέση: (" + x + "," + y + ")");
        }
        
        SwingUtilities.invokeLater(() -> {
            JLabel cellLabel = cellLabels[x-1][y-1];
            JPanel cell = gridCells[x-1][y-1];
            
            // ΔΙΟΡΘΩΣΗ: Ίδιο font με τα δέντρα (24pt)
            cell.setBackground(Color.RED);
            cellLabel.setText("🔥");
            cellLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24)); // Ίδιο font
            cellLabel.setForeground(Color.YELLOW);
            cellLabel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
            
            // Background animation
            animateCell(cell, Color.RED, Color.ORANGE);
        });
        
        // Αυτόματη κατάσβεση μετά από 1 λεπτό (60 δευτερόλεπτα)
        Timer autoExtinguishTimer = new Timer(60000, event -> {
            addLog("🌫️ ΑΥΤΟΜΑΤΗ ΚΑΤΑΣΒΕΣΗ στη θέση (" + x + "," + y + ") μετά από 1 λεπτό");
            showExtinguishedAt(x, y);
        });
        autoExtinguishTimer.setRepeats(false);
        autoExtinguishTimer.start();
    }

    // ΔΙΟΡΘΩΣΗ: showAgentAt με final variables
    public void showAgentAt(int x, int y, String agentType, String agentName) {
        if (x < 1 || x > GRID_SIZE || y < 1 || y > GRID_SIZE) return;
        
        SwingUtilities.invokeLater(() -> {
            JLabel cellLabel = cellLabels[x-1][y-1];
            String currentState = cellStates.get(x + "," + y);
            
            String emoji = "";
            String fallbackText = "";
            
            // FINAL variables για lambda expressions
            final Color primaryColor, secondaryColor;
            
            switch (agentType.toUpperCase()) {
                case "TRUCK":
                case "FIRETRUCK":
                    emoji = "🚒";
                    fallbackText = "TRUCK";
                    primaryColor = new Color(255, 69, 0);    // Πορτοκαλί-κόκκινο
                    secondaryColor = new Color(255, 255, 0); // Κίτρινο
                    break;
                case "AIRCRAFT":
                case "AIRPLANE":
                    emoji = "✈️";
                    fallbackText = "PLANE";
                    primaryColor = new Color(0, 191, 255);   // Deep sky blue
                    secondaryColor = new Color(255, 255, 255); // Άσπρο
                    break;
                case "HELICOPTER":
                    emoji = "🚁";
                    fallbackText = "HELI";
                    primaryColor = new Color(138, 43, 226);  // Blue violet
                    secondaryColor = new Color(255, 255, 255); // Άσπρο
                    break;
                case "CREW":
                case "GROUNDCREW":
                    emoji = "👥";
                    fallbackText = "CREW";
                    primaryColor = new Color(255, 215, 0);   // Χρυσό
                    secondaryColor = new Color(139, 69, 19); // Καφέ
                    break;
                case "WATER":
                    emoji = "💧";
                    fallbackText = "WATER";
                    primaryColor = new Color(0, 191, 255);   // Deep sky blue
                    secondaryColor = new Color(255, 255, 255); // Άσπρο
                    break;
                default:
                    emoji = "🌲";
                    fallbackText = "TREE";
                    primaryColor = new Color(34, 139, 34);   // Forest green
                    secondaryColor = new Color(0, 100, 0);   // Dark green
            }
            
            Font notoFont = getNotoEmojiFont(20, Font.BOLD);
            boolean canDisplayEmoji = notoFont.canDisplayUpTo(emoji) == -1;
            
            String primaryHex = String.format("#%02x%02x%02x", primaryColor.getRed(), primaryColor.getGreen(), primaryColor.getBlue());
            String secondaryHex = String.format("#%02x%02x%02x", secondaryColor.getRed(), secondaryColor.getGreen(), secondaryColor.getBlue());
            
            if ("FIRE".equals(currentState)) {
                // Agent πάνω από φωτιά - ΕΙΔΙΚΗ ΕΜΦΑΝΙΣΗ
                if (canDisplayEmoji) {
                    cellLabel.setText("<html><center>" + 
                        "<span style='font-size:20px; color:#FFFF00; text-shadow: 2px 2px 4px #000000;'>🔥</span><br>" + 
                        "<span style='font-size:16px; color:" + primaryHex + "; text-shadow: 1px 1px 2px #000000;'>" + emoji + "</span>" +
                        "</center></html>");
                } else {
                    cellLabel.setText("<html><center>" + 
                        "<span style='font-size:10px; color:#FFFF00; font-weight:bold; text-shadow: 1px 1px 2px #000000;'>FIRE</span><br>" + 
                        "<span style='font-size:8px; color:" + primaryHex + "; font-weight:bold; text-shadow: 1px 1px 2px #000000;'>" + fallbackText + "</span>" +
                        "</center></html>");
                }
                
                // Pulsing border για προσοχή
                Timer pulseTimer = new Timer(300, new ActionListener() {
                    boolean toggle = false;
                    int count = 0;
                    
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (count >= 10) {
                            ((Timer)e.getSource()).stop();
                            return;
                        }
                        
                        Color borderColor = toggle ? primaryColor : secondaryColor;
                        cellLabel.setBorder(BorderFactory.createLineBorder(borderColor, 4));
                        toggle = !toggle;
                        count++;
                    }
                });
                pulseTimer.start();
                
            } else {
                // Κανονική εμφάνιση agent
                if (canDisplayEmoji) {
                    cellLabel.setText("<html><center>" + 
                        "<span style='font-size:24px; color:" + primaryHex + "; text-shadow: 1px 1px 2px " + secondaryHex + ";'>" + emoji + "</span>" +
                        "</center></html>");
                } else {
                    cellLabel.setText("<html><center>" + 
                        "<span style='font-size:12px; color:" + primaryHex + "; font-weight:bold; text-shadow: 1px 1px 2px " + secondaryHex + ";'>" + fallbackText + "</span>" +
                        "</center></html>");
                }
                
                cellLabel.setBorder(BorderFactory.createLineBorder(secondaryColor, 2));
            }
            
            cellLabel.setFont(notoFont);
            cellAgents.put(x + "," + y, canDisplayEmoji ? emoji : fallbackText);
            
            // Αυτόματη αφαίρεση μετά από λίγο
            Timer timer = new Timer(4000, event -> clearAgentAt(x, y, agentType));
            timer.setRepeats(false);
            timer.start();
        });
    }

    // ΕΝΗΜΕΡΩΣΗ: updateCell με δύο χρώματα
    public void updateCell(int x, int y, String state) {
        if (x < 1 || x > GRID_SIZE || y < 1 || y > GRID_SIZE) return;
        
        JPanel cell = gridCells[x-1][y-1];
        JLabel cellLabel = cellLabels[x-1][y-1];
        String key = x + "," + y;
        
        cellStates.put(key, state);
        
        SwingUtilities.invokeLater(() -> {
            // ΔΙΟΡΘΩΣΗ: Ίδιο font για όλες τις καταστάσεις (24pt Segoe UI Emoji)
            Font standardCellFont = new Font("Segoe UI Emoji", Font.BOLD, 24);
            cellLabel.setBorder(null);
            
            switch (state) {
                case "FIRE":
                    Color fireColor1 = new Color(255, 0, 0);   // Κόκκινο
                    Color fireColor2 = new Color(255, 140, 0); // Πορτοκαλί
                    cell.setBackground(fireColor1);
                    
                    // ΔΙΟΡΘΩΣΗ: Ίδιο font όπως τα δέντρα
                    cellLabel.setText("🔥");
                    cellLabel.setFont(standardCellFont); // Ίδιο font με τα δέντρα
                    cellLabel.setForeground(Color.YELLOW);
                    
                    // Προσθήκη κίτρινου border για ορατότητα
                    cellLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                    animateCell(cell, fireColor1, fireColor2);
                    break;
                    
                case "WATER":
                    cell.setBackground(Color.BLUE);
                    cellLabel.setText("💧");
                    cellLabel.setFont(standardCellFont); // Ίδιο font με τα δέντρα
                    cellLabel.setForeground(Color.WHITE);
                    cellLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
                    
                    Timer waterTimer = new Timer(3000, timerEvent -> {
                        cell.setBackground(Color.GREEN);
                        cellLabel.setText("🌲");
                        cellLabel.setFont(standardCellFont); // Ίδιο font
                        cellLabel.setForeground(new Color(0, 100, 0));
                        cellLabel.setBorder(null);
                        cellStates.put(key, "TREE");
                    });
                    waterTimer.setRepeats(false);
                    waterTimer.start();
                    break;
                    
                case "EXTINGUISHED":
                    cell.setBackground(Color.DARK_GRAY);
                    cellLabel.setText("🌫️");
                    cellLabel.setFont(standardCellFont); // Ίδιο font με τα δέντρα
                    cellLabel.setForeground(Color.LIGHT_GRAY);
                    cellLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
                    
                    Timer extinguishTimer = new Timer(5000, timerEvent -> updateCell(x, y, "TREE"));
                    extinguishTimer.setRepeats(false);
                    extinguishTimer.start();
                    break;
                    
                case "TREE":
                default:
                    cell.setBackground(Color.GREEN);
                    cellLabel.setText("🌲");
                    cellLabel.setFont(standardCellFont); // 24pt Segoe UI Emoji
                    cellLabel.setForeground(new Color(0, 100, 0));
                    cellLabel.setBorder(null);
                    break;
            }
            cell.repaint();
        });
    }
    
    public void showWaterDropAt(int x, int y, String agent) {
        showAgentAt(x, y, agent.contains("aircraft") ? "AIRCRAFT" : "HELICOPTER", agent);
        
        SwingUtilities.invokeLater(() -> {
            JLabel cellLabel = cellLabels[x-1][y-1];
            JPanel cell = gridCells[x-1][y-1];
            
            // Ειδική εμφάνιση για water drop
            cell.setBackground(Color.BLUE);
            cellLabel.setText("💧");
            cellLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24)); // Ίδιο font
            cellLabel.setForeground(Color.WHITE);
            
            // Εφέ animation για το νερό
            animateCell(cell, Color.BLUE, Color.CYAN);
        });
        
        addLog("💧 ΡΙΨΗ ΝΕΡΟΥ από " + agent + " στη θέση (" + x + "," + y + ")");
        
        Timer dropTimer = new Timer(3000, timerEvent -> updateCell(x, y, "WATER"));
        dropTimer.setRepeats(false);
        dropTimer.start();
    }
    
    public void showTruckAt(int x, int y, String truckName) {
        showAgentAt(x, y, "TRUCK", truckName);
        addLog("🚒 " + truckName + " έφτασε στη θέση (" + x + "," + y + ")");
    }
    
    public void showAircraftAt(int x, int y, String aircraftName) {
        showAgentAt(x, y, "AIRCRAFT", aircraftName);
        addLog("✈️ " + aircraftName + " πετάει πάνω από τη θέση (" + x + "," + y + ")");
    }
    
    public void showHelicopterAt(int x, int y, String helicopterName) {
        showAgentAt(x, y, "HELICOPTER", helicopterName);
        addLog("🚁 " + helicopterName + " ιπτάμενος στη θέση (" + x + "," + y + ")");
    }
    
    public void showGroundCrewAt(int x, int y, String crewName) {
        showAgentAt(x, y, "CREW", crewName);
        addLog("👥 " + crewName + " εργάζεται στη θέση (" + x + "," + y + ")");
    }
    
    public void showExtinguishedAt(int x, int y) {
        updateCell(x, y, "EXTINGUISHED");
        addLog("🌫️ ΚΑΤΑΣΒΕΣΗ στη θέση (" + x + "," + y + ")");
        
        SwingUtilities.invokeLater(() -> {
            JLabel cellLabel = cellLabels[x-1][y-1];
            JPanel cell = gridCells[x-1][y-1];
            
            cell.setBackground(Color.DARK_GRAY);
            
            // ΔΙΟΡΘΩΣΗ: Ίδιο font με τα δέντρα
            cellLabel.setText("🌫️");
            cellLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24)); // Ίδιο font
            cellLabel.setForeground(Color.LIGHT_GRAY);
            
            // Προσθήκη εφέ για καλύτερη ορατότητα
            cellLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            
            Timer extinguishTimer = new Timer(5000, timerEvent -> updateCell(x, y, "TREE"));
            extinguishTimer.setRepeats(false);
            extinguishTimer.start();
        });
    }
    
    private void animateCell(JPanel cell, Color color1, Color color2) {
        Timer animationTimer = new Timer(500, new ActionListener() {
            boolean toggle = false;
            int count = 0;
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count >= 10) {
                    ((Timer)e.getSource()).stop();
                    return;
                }
                
                cell.setBackground(toggle ? color1 : color2);
                toggle = !toggle;
                count++;
                cell.repaint();
            }
        });
        animationTimer.start();
    }
    
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
                logArea.append("[" + timestamp + "] " + message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }
    
    // ΔΙΟΡΘΩΣΗ: updateStatus για χρήση Noto font
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(status);
                statusLabel.setFont(getNotoEmojiFont(14, Font.BOLD)); // Noto font
            }
        });
    }
    
    public void showEmergencyDeclared() {
        updateStatus("🚨 ΚΑΤΑΣΤΑΣΗ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ");
        addLog("🚨🚨🚨 ΚΗΡΥΞΗ ΚΑΤΑΣΤΑΣΗΣ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ 🚨🚨🚨");
        
        if (statusLabel != null) {
            statusLabel.setBackground(Color.RED);
            statusLabel.setForeground(Color.WHITE);
            statusLabel.setOpaque(true);
            statusLabel.setFont(getNotoEmojiFont(14, Font.BOLD)); // Noto font
        }
    }
    
    public void showAllExtinguished() {
        updateStatus("🟢 ΠΛΗΡΗΣ ΚΑΤΑΣΒΕΣΗ - Παρακολούθηση για αναζοπυρώσεις");
        addLog("🎉 ΠΛΗΡΗΣ ΚΑΤΑΣΒΕΣΗ ΟΛΩΝ ΤΩΝ ΕΣΤΙΩΝ!");
        
        if (statusLabel != null) {
            statusLabel.setBackground(null);
            statusLabel.setForeground(Color.BLACK);
            statusLabel.setOpaque(false);
            statusLabel.setFont(getNotoEmojiFont(14, Font.BOLD)); // Noto font
        }
    }
    
    private void simulateRandomFire() {
        int x = (int)(Math.random() * GRID_SIZE) + 1;
        int y = (int)(Math.random() * GRID_SIZE) + 1;
        showFireAt(x, y, (int)(Math.random() * 5) + 1);
    }
    
    private void showEmergencyDialog() {
        String[] options = {"Ναι", "Όχι"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Θέλετε να κηρύξετε κατάσταση έκτακτης ανάγκης;",
            "Έκτακτη Ανάγκη",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[1]
        );
        
        if (choice == 0) {
            showEmergencyDeclared();
        }
    }
    
    private void showStatsDialog() {
        StringBuilder stats = new StringBuilder();
        stats.append("📊 ΣΤΑΤΙΣΤΙΚΑ ΠΡΟΣΟΜΟΙΩΣΗΣ\n\n");
        stats.append("🔥 Συνολικές Εστίες: ").append(cellStates.values().stream().mapToInt(s -> s.equals("FIRE") ? 1 : 0).sum()).append("\n");
        stats.append("✅ Σβησμένες Περιοχές: ").append(cellStates.values().stream().mapToInt(s -> s.equals("EXTINGUISHED") ? 1 : 0).sum()).append("\n");
        stats.append("🌲 Υγιή Δέντρα: ").append(cellStates.values().stream().mapToInt(s -> s.equals("TREE") ? 1 : 0).sum()).append("\n");
        
        JOptionPane.showMessageDialog(this, stats.toString(), "Στατιστικά", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ΔΙΟΡΘΩΣΗ: Thread-safe getInstance

    private static FireSimulationGUI instance;
    private static final Object lock = new Object();

    public static FireSimulationGUI getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    System.out.println("🔄 Δημιουργία νέου FireSimulationGUI instance...");
                    try {
                        instance = new FireSimulationGUI();
                        System.out.println("✅ FireSimulationGUI instance δημιουργήθηκε!");
                    } catch (Exception e) {
                        System.err.println("❌ ΣΦΑΛΜΑ δημιουργίας GUI: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return instance;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FireSimulationGUI gui = FireSimulationGUI.getInstance();
            gui.setVisible(true);
            gui.addLog("🚀 Σύστημα προσομοίωσης έτοιμο!");
        });
    }

    // ΕΝΗΜΕΡΩΣΗ: initializeGUI με weather panel
    private void initializeGUI() {
        setTitle("Προσομοίωση Δασικής Πυρκαγιάς - Πολυπρακτορικό Σύστημα");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // ΔΙΟΡΘΩΣΗ: Αρχικό layout - Αριστερά grid, δεξιά panels, κάτω log
        setLayout(new BorderLayout());
        
        // ΑΡΙΣΤΕΡΑ: Grid panel με controls
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(createGridPanel(), BorderLayout.CENTER);
        leftPanel.add(createControlPanel(), BorderLayout.SOUTH);
        
        // ΔΕΞΙΑ: Στοιχεία (καιρός, πόροι, συμβολισμοί)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 0)); // Σταθερό πλάτος
        
        // Δεξί panel με 3 τμήματα από πάνω προς τα κάτω
        rightPanel.add(createWeatherPanel(), BorderLayout.NORTH);     // Καιρός πάνω
        rightPanel.add(createStatsPanel(), BorderLayout.CENTER);      // Πόροι μέση
        rightPanel.add(createLegendPanel(), BorderLayout.SOUTH);      // Συμβολισμοί κάτω
        
        // ΚΑΤΩ: Log panel
        JPanel bottomPanel = createLogPanel();
        bottomPanel.setPreferredSize(new Dimension(0, 120)); // Μικρότερο ύψος
        
        // ΠΑΝΩ: Status bar
        JPanel topPanel = createStatusPanel();
        
        // Προσθήκη στο κύριο frame
        add(leftPanel, BorderLayout.CENTER);        // Αριστερά - Grid
        add(rightPanel, BorderLayout.EAST);         // Δεξιά - Στοιχεία  
        add(bottomPanel, BorderLayout.SOUTH);       // Κάτω - Log
        add(topPanel, BorderLayout.NORTH);          // Πάνω - Status
    }

    // ΠΡΟΣΘΗΚΗ: Δημιουργία Weather Panel
    private JPanel createWeatherPanel() {
        weatherPanel = new JPanel(new GridLayout(5, 1, 2, 2)); // 5 γραμμές, 1 στήλη
        weatherPanel.setBorder(BorderFactory.createTitledBorder("🌤️ Καιρικές Συνθήκες"));
        weatherPanel.setPreferredSize(new Dimension(0, 140)); // Μικρότερο ύψος
        weatherPanel.setBackground(new Color(240, 248, 255));
        
        // Αρχικοποίηση labels
        weatherConditionLabel = new JLabel("☀️ Συνθήκες: Φόρτωση...");
        windSpeedLabel = new JLabel("💨 Άνεμος: -- km/h");
        windDirectionLabel = new JLabel("🧭 Κατεύθυνση: --");
        temperatureLabel = new JLabel("🌡️ Θερμοκρασία: --°C");
        humidityLabel = new JLabel("💧 Υγρασία: --%");
        
        // Ρύθμιση fonts - μικρότερα για καλύτερη εμφάνιση
        Font weatherFont = getGreekSupportFont(11, Font.PLAIN);
        weatherConditionLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        windSpeedLabel.setFont(weatherFont);
        windDirectionLabel.setFont(weatherFont);
        temperatureLabel.setFont(weatherFont);
        humidityLabel.setFont(weatherFont);
        
        // Χρωματική κωδικοποίηση
        weatherConditionLabel.setForeground(new Color(34, 139, 34));
        windSpeedLabel.setForeground(new Color(70, 130, 180));
        windDirectionLabel.setForeground(new Color(72, 61, 139));
        temperatureLabel.setForeground(new Color(255, 69, 0));
        humidityLabel.setForeground(new Color(0, 191, 255));
        
        // Προσθήκη στο panel
        weatherPanel.add(weatherConditionLabel);
        weatherPanel.add(windSpeedLabel);
        weatherPanel.add(windDirectionLabel);
        weatherPanel.add(temperatureLabel);
        weatherPanel.add(humidityLabel);
        
        return weatherPanel;
    }

    // ΠΡΟΣΘΗΚΗ: Μέθοδος ενημέρωσης καιρικών συνθηκών
    public void updateWeatherConditions(double windSpeed, String windDirection, 
                                      double temperature, double humidity, String condition) {
        SwingUtilities.invokeLater(() -> {
            // Ενημέρωση κειμένων
            windSpeedLabel.setText(String.format("💨 Ταχύτητα Ανέμου: %.1f km/h", windSpeed));
            windDirectionLabel.setText("🧭 Κατεύθυνση: " + windDirection);
            temperatureLabel.setText(String.format("🌡️ Θερμοκρασία: %.1f°C", temperature));
            humidityLabel.setText(String.format("💧 Υγρασία: %.0f%%", humidity));
            
            // Δυναμικό emoji και χρώμα ανάλογα με τις συνθήκες
            String weatherEmoji = getWeatherEmoji(condition, temperature, windSpeed, humidity);
            weatherConditionLabel.setText(weatherEmoji + " Συνθήκες: " + condition);
            
            // Χρωματική κωδικοποίηση κινδύνου
            Color dangerColor = calculateFireDangerColor(windSpeed, temperature, humidity);
            weatherPanel.setBackground(dangerColor);
            weatherConditionLabel.setForeground(getDangerTextColor(dangerColor));
            
            // Ενημέρωση χρωμάτων ανάλογα με τις τιμές
            updateWeatherColors(windSpeed, temperature, humidity);
        });
    }

    // ΠΡΟΣΘΗΚΗ: Επιλογή emoji ανάλογα με τις συνθήκες
    private String getWeatherEmoji(String condition, double temperature, double windSpeed, double humidity) {
        if (condition.toLowerCase().contains("καύσωνας") || temperature > 35) {
            return "🔥";
        } else if (condition.toLowerCase().contains("βροχή") || humidity > 85) {
            return "🌧️";
        } else if (condition.toLowerCase().contains("θύελλα") || windSpeed > 25) {
            return "⛈️";
        } else if (condition.toLowerCase().contains("συννεφιά")) {
            return "☁️";
        } else if (condition.toLowerCase().contains("ομίχλη")) {
            return "🌫️";
        } else if (temperature > 25 && humidity < 30) {
            return "☀️"; // Ηλιόλουστο και ξηρό
        } else {
            return "🌤️"; // Μερικώς συννεφιασμένο
        }
    }

    // ΠΡΟΣΘΗΚΗ: Υπολογισμός χρώματος κινδύνου πυρκαγιάς
    private Color calculateFireDangerColor(double windSpeed, double temperature, double humidity) {
        // Υπολογισμός δείκτη κινδύνου (0-100)
        double dangerIndex = 0;
        
        // Θερμοκρασία (0-40 points)
        if (temperature > 35) dangerIndex += 40;
        else if (temperature > 30) dangerIndex += 30;
        else if (temperature > 25) dangerIndex += 20;
        else if (temperature > 20) dangerIndex += 10;
        
        // Άνεμος (0-35 points)
        if (windSpeed > 25) dangerIndex += 35;
        else if (windSpeed > 15) dangerIndex += 25;
        else if (windSpeed > 10) dangerIndex += 15;
        else if (windSpeed > 5) dangerIndex += 10;
        
        // Υγρασία (0-25 points) - αντίστροφα ανάλογη
        if (humidity < 20) dangerIndex += 25;
        else if (humidity < 30) dangerIndex += 20;
        else if (humidity < 40) dangerIndex += 15;
        else if (humidity < 50) dangerIndex += 10;
        
        // Επιστροφή χρώματος ανάλογα με τον κίνδυνο
        if (dangerIndex >= 80) {
            return new Color(220, 20, 60, 50); // Crimson - ΕΞΑΙΡΕΤΙΚΟΣ ΚΙΝΔΥΝΟΣ
        } else if (dangerIndex >= 60) {
            return new Color(255, 69, 0, 50); // Orange red - ΥΨΗΛΟΣ ΚΙΝΔΥΝΟΣ
        } else if (dangerIndex >= 40) {
            return new Color(255, 140, 0, 50); // Dark orange - ΜΕΤΡΙΟΣ ΚΙΝΔΥΝΟΣ
        } else if (dangerIndex >= 20) {
            return new Color(255, 255, 0, 30); // Yellow - ΧΑΜΗΛΟΣ ΚΙΝΔΥΝΟΣ
        } else {
            return new Color(144, 238, 144, 30); // Light green - ΕΛΑΧΙΣΤΟΣ ΚΙΝΔΥΝΟΣ
        }
    }

    // ΠΡΟΣΘΗΚΗ: Χρώμα κειμένου ανάλογα με το background
    private Color getDangerTextColor(Color backgroundColor) {
        // Υπολογισμός luminance για επιλογή κατάλληλου χρώματος κειμένου
        double luminance = (0.299 * backgroundColor.getRed() + 
                           0.587 * backgroundColor.getGreen() + 
                           0.114 * backgroundColor.getBlue()) / 255;
        
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    // ΠΡΟΣΘΗΚΗ: Ενημέρωση χρωμάτων ανάλογα με τις τιμές
    private void updateWeatherColors(double windSpeed, double temperature, double humidity) {
        // Χρώμα ανέμου (πιο κόκκινο = πιο επικίνδυνος)
        if (windSpeed > 20) {
            windSpeedLabel.setForeground(Color.RED);
        } else if (windSpeed > 10) {
            windSpeedLabel.setForeground(new Color(255, 140, 0));
        } else {
            windSpeedLabel.setForeground(new Color(70, 130, 180));
        }
        
        // Χρώμα θερμοκρασίας
        if (temperature > 35) {
            temperatureLabel.setForeground(Color.RED);
        } else if (temperature > 30) {
            temperatureLabel.setForeground(new Color(255, 69, 0));
        } else if (temperature > 25) {
            temperatureLabel.setForeground(new Color(255, 140, 0));
        } else {
            temperatureLabel.setForeground(new Color(0, 100, 0));
        }
        
        // Χρώμα υγρασίας (χαμηλή υγρασία = κίνδυνος)
        if (humidity < 20) {
            humidityLabel.setForeground(Color.RED);
        } else if (humidity < 30) {
            humidityLabel.setForeground(new Color(255, 140, 0));
        } else if (humidity < 50) {
            humidityLabel.setForeground(new Color(255, 215, 0));
        } else {
            humidityLabel.setForeground(new Color(0, 191, 255));
        }
    }

    // ΠΡΟΣΘΗΚΗ: Μέθοδος για προσομοίωση καιρικών συνθηκών (για testing)
    public void simulateWeatherUpdate() {
        // Τυχαίες τιμές για δοκιμή
        double windSpeed = 5 + Math.random() * 25; // 5-30 km/h
        String[] directions = {"Β", "ΒΑ", "Α", "ΝΑ", "Ν", "ΝΔ", "Δ", "ΒΔ"};
        String windDirection = directions[(int)(Math.random() * directions.length)];
        double temperature = 15 + Math.random() * 25; // 15-40°C
        double humidity = 20 + Math.random() * 60; // 20-80%
        
        String[] conditions = {
            "Αίθριος", "Μερικώς Συννεφιασμένος", "Συννεφιασμένος", 
            "Καύσωνας", "Ξηρός", "Υγρός", "Ασταθής"
        };
        String condition = conditions[(int)(Math.random() * conditions.length)];
        
        updateWeatherConditions(windSpeed, windDirection, temperature, humidity, condition);
        addLog("🌤️ Ενημέρωση καιρού: " + condition + ", " + 
               String.format("%.1f°C, %.0f%% υγρασία, άνεμος %.1f km/h %s", 
               temperature, humidity, windSpeed, windDirection));
    }

    // ΕΝΗΜΕΡΩΣΗ: createControlPanel με κουμπί καιρού
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("🎮 Έλεγχος"));
        panel.setPreferredSize(new Dimension(0, 60)); // Μικρότερο ύψος
        
        JButton startFireBtn = new JButton("🔥 Φωτιά");
        JButton emergencyBtn = new JButton("🚨 Έκτακτη");
        JButton clearLogBtn = new JButton("🗑️ Καθαρισμός");
        JButton exitBtn = new JButton("❌ Έξοδος");
        // ΑΦΑΙΡΕΣΗ: weatherBtn

        // Μικρότερα κουμπιά
        Font buttonFont = getGreekSupportFont(10, Font.BOLD);
        Dimension buttonSize = new Dimension(90, 30);
        
        startFireBtn.setFont(buttonFont);
        startFireBtn.setPreferredSize(buttonSize);
        emergencyBtn.setFont(buttonFont);
        emergencyBtn.setPreferredSize(buttonSize);
        clearLogBtn.setFont(buttonFont);
        clearLogBtn.setPreferredSize(buttonSize);
        exitBtn.setFont(buttonFont);
        exitBtn.setPreferredSize(buttonSize);
        
        // Event handlers (χωρίς weather button)
        startFireBtn.addActionListener(event -> simulateRandomFire());
        emergencyBtn.addActionListener(event -> showEmergencyDialog());
        clearLogBtn.addActionListener(event -> logArea.setText(""));
        exitBtn.addActionListener(event -> System.exit(0));
        
        // Χρωματική κωδικοποίηση
        startFireBtn.setBackground(new Color(255, 200, 200));
        emergencyBtn.setBackground(new Color(255, 180, 180));
        clearLogBtn.setBackground(new Color(240, 240, 240));
        exitBtn.setBackground(new Color(220, 220, 220));
        
        panel.add(startFireBtn);
        panel.add(emergencyBtn);
        panel.add(clearLogBtn);
        panel.add(exitBtn);
        
        return panel;
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος initializeGrid
    private void initializeGrid() {
        try {
            System.out.println("🔄 Αρχικοποίηση grid " + GRID_SIZE + "x" + GRID_SIZE + "...");
            
            gridCells = new JPanel[GRID_SIZE][GRID_SIZE];
            cellLabels = new JLabel[GRID_SIZE][GRID_SIZE];
            
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    // Αρχικοποίηση των cells
                    gridCells[i][j] = new JPanel(new BorderLayout());
                    gridCells[i][j].setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                    gridCells[i][j].setBackground(Color.GREEN);
                    gridCells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                    
                    // Αρχικοποίηση των labels
                    cellLabels[i][j] = new JLabel("🌲", SwingConstants.CENTER);
                    cellLabels[i][j].setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
                    cellLabels[i][j].setForeground(new Color(0, 100, 0));
                    
                    gridCells[i][j].add(cellLabels[i][j], BorderLayout.CENTER);
                    
                    // Αρχικοποίηση κατάστασης
                    cellStates.put((i+1) + "," + (j+1), "TREE");
                }
            }
            
            System.out.println("✅ Grid αρχικοποιήθηκε επιτυχώς!");
            
        } catch (Exception e) {
            System.err.println("❌ ΣΦΑΛΜΑ αρχικοποίησης grid: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback αρχικοποίηση
            gridCells = new JPanel[GRID_SIZE][GRID_SIZE];
            cellLabels = new JLabel[GRID_SIZE][GRID_SIZE];
            
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    gridCells[i][j] = new JPanel();
                    gridCells[i][j].setBackground(Color.GREEN);
                    cellLabels[i][j] = new JLabel("T");
                    gridCells[i][j].add(cellLabels[i][j]);
                    cellStates.put((i+1) + "," + (j+1), "TREE");
                }
            }
        }
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος clearAgentAt
    public void clearAgentAt(int x, int y, String agentType) {
        if (x < 1 || x > GRID_SIZE || y < 1 || y > GRID_SIZE) return;
        
        SwingUtilities.invokeLater(() -> {
            String key = x + "," + y;
            cellAgents.remove(key);
            
            // Επαναφορά στην αρχική κατάσταση του cell
            String currentState = cellStates.get(key);
            if (currentState != null) {
                updateCell(x, y, currentState);
            }
        });
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος createGridPanel
    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2));
        gridPanel.setBorder(BorderFactory.createTitledBorder("🌲 Δάσος (5x5) - Πραγματικός Χρόνος"));
        gridPanel.setBackground(Color.DARK_GRAY);
        
        // ΔΙΟΡΘΩΣΗ: Έλεγχος αν το gridCells έχει αρχικοποιηθεί
        if (gridCells == null) {
            System.err.println("⚠️ ΣΦΑΛΜΑ: gridCells δεν έχει αρχικοποιηθεί!");
            initializeGrid(); // Αρχικοποίηση εδώ αν δεν έχει γίνει
        }
        
        // Προσθήκη των cells στο grid
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gridCells[i][j] != null) {
                    gridPanel.add(gridCells[i][j]);
                } else {
                    // Fallback panel αν κάτι πάει στραβά
                    JPanel fallbackPanel = new JPanel();
                    fallbackPanel.setBackground(Color.GREEN);
                    fallbackPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    fallbackPanel.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                    gridPanel.add(fallbackPanel);
                }
            }
        }
        
        return gridPanel;
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος createStatsPanel
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 2, 2)); // 5 γραμμές
        panel.setBorder(BorderFactory.createTitledBorder("📊 Διαθέσιμοι Πόροι"));
        panel.setPreferredSize(new Dimension(0, 140)); // Μικρότερο ύψος
        panel.setBackground(new Color(245, 245, 245));
        
        // Αρχικοποίηση labels
        fireCountLabel = new JLabel("🔥 Ενεργές Εστίες: 0");
        trucksLabel = new JLabel("🚒 Πυροσβεστικά: 4/4");
        aircraftLabel = new JLabel("✈️ Αεροσκάφη: 2/2");
        helicoptersLabel = new JLabel("🚁 Ελικόπτερα: 1/1");
        crewsLabel = new JLabel("👥 Ομάδες: 6/6");
        
        // Styling με μικρότερα fonts
        Font statsFont = getGreekSupportFont(11, Font.BOLD);
        fireCountLabel.setFont(statsFont);
        trucksLabel.setFont(statsFont);
        aircraftLabel.setFont(statsFont);
        helicoptersLabel.setFont(statsFont);
        crewsLabel.setFont(statsFont);
        
        // Χρωματική κωδικοποίηση
        fireCountLabel.setForeground(new Color(34, 139, 34)); // Πράσινο αρχικά
        trucksLabel.setForeground(new Color(255, 140, 0));
        aircraftLabel.setForeground(new Color(0, 191, 255));
        helicoptersLabel.setForeground(new Color(138, 43, 226));
        crewsLabel.setForeground(new Color(255, 215, 0));
        
        panel.add(fireCountLabel);
        panel.add(trucksLabel);
        panel.add(aircraftLabel);
        panel.add(helicoptersLabel);
        panel.add(crewsLabel);
        
        return panel;
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος createLegendPanel
    private JPanel createLegendPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 2, 2)); // 4 σειρές, 2 στήλες
        panel.setBorder(BorderFactory.createTitledBorder("🔑 Συμβολισμοί"));
        panel.setPreferredSize(new Dimension(0, 120)); // Μικρότερο ύψος
        panel.setBackground(new Color(250, 250, 250));
        
        // Προσθήκη συμβόλων σε συμπαγή μορφή
        panel.add(createLegendItem("🌲", "Δέντρο", new Color(34, 139, 34)));
        panel.add(createLegendItem("🔥", "Φωτιά", Color.RED));
        panel.add(createLegendItem("💧", "Νερό", Color.BLUE));
        panel.add(createLegendItem("🚒", "Πυροσβεστικό", new Color(255, 140, 0)));
        panel.add(createLegendItem("✈️", "Αεροσκάφος", Color.CYAN));
        panel.add(createLegendItem("🚁", "Ελικόπτερο", Color.MAGENTA));
        panel.add(createLegendItem("👥", "Ομάδα", new Color(255, 215, 0)));
        panel.add(createLegendItem("🌫️", "Σβησμένο", Color.GRAY));
        
        return panel;
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος createLegendItem
    private JPanel createLegendItem(String symbol, String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        JLabel symbolLabel = new JLabel(symbol);
        symbolLabel.setFont(getGreekSupportFont(14, Font.BOLD));
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(color);
        textLabel.setFont(new Font("Arial", Font.BOLD, 11));
        item.add(symbolLabel);
        item.add(textLabel);
        return item;
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος createLogPanel
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📋 Καταγραφή Γεγονότων"));
        panel.setPreferredSize(new Dimension(0, 120)); // Μικρότερο ύψος
        
        logArea = new JTextArea(6, 80); // Λιγότερες γραμμές
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 10)); // Μικρότερο font
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        logArea.setText("🚀 Σύστημα προσομοίωσης δασικής πυρκαγιάς ενεργοποιήθηκε!\n");
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος createStatusPanel
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        statusLabel = new JLabel("🟢 Σύστημα έτοιμο - Αναμονή για γεγονότα...");
        statusLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        statusLabel.setForeground(Color.BLACK);
        
        panel.add(statusLabel);
        
        return panel;
    }

    // ΠΡΟΣΘΗΚΗ: Λείπουσα μέθοδος getGreekSupportFont
    private Font getGreekSupportFont(int size, int style) {
        String[] fontNames = {
            "Noto Sans",          // Windows 10/11 default - καλύτερη υποστήριξη Unicode
            "Tahoma",            // Windows classic - αξιόπιστο για ελληνικά
            "Arial Unicode MS",  // Comprehensive Unicode support
            "DejaVu Sans",       // Linux/Cross-platform
            "Liberation Sans",   // Linux alternative
            "Noto Sans",         // Google's Unicode font
            "Dialog",            // Java default
            "SansSerif"          // Final fallback
        };
        
        String testGreek = "Αβγδεζηθικλμνξοπρστυφχψω ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ";
        
        for (String fontName : fontNames) {
            Font font = new Font(fontName, style, size);
            if (font.canDisplayUpTo(testGreek) == -1) {
                return font;
            }
        }
        
        // Αν τίποτα δεν δουλεύει, δημιουργία custom font
        return createFallbackFont(size, style);
    }

    // ΠΡΟΣΘΗΚΗ: Δημιουργία fallback font
    private Font createFallbackFont(int size, int style) {
        // Προσπάθεια για system font
        Font systemFont = new Font(Font.DIALOG, style, size);
        return systemFont;
    }

    // ΔΙΟΡΘΩΣΗ: Σωστή υλοποίηση updateResourceStats
    public void updateResourceStats(int availableTrucks, int totalTrucks, 
                                   int availableAircraft, int totalAircraft,
                                   int availableHelicopters, int totalHelicopters,
                                   int availableCrews, int totalCrews,
                                   int activeFires) {
        SwingUtilities.invokeLater(() -> {
            if (fireCountLabel != null) {
                fireCountLabel.setText("🔥 Ενεργές Εστίες: " + activeFires);
                fireCountLabel.setForeground(activeFires > 0 ? Color.RED : new Color(34, 139, 34));
            }
            
            if (trucksLabel != null) {
                trucksLabel.setText("🚒 Διαθέσιμα Οχήματα: " + availableTrucks + "/" + totalTrucks);
                trucksLabel.setForeground(availableTrucks > 0 ? new Color(34, 139, 34) : Color.RED);
            }
            
            if (aircraftLabel != null) {
                aircraftLabel.setText("✈️ Διαθέσιμα Αεροσκάφη: " + availableAircraft + "/" + totalAircraft);
                aircraftLabel.setForeground(availableAircraft > 0 ? new Color(34, 139, 34) : Color.RED);
            }
            
            if (helicoptersLabel != null) {
                helicoptersLabel.setText("🚁 Διαθέσιμα Ελικόπτερα: " + availableHelicopters + "/" + totalHelicopters);
                helicoptersLabel.setForeground(availableHelicopters > 0 ? new Color(34, 139, 34) : Color.RED);
            }
            
            if (crewsLabel != null) {
                crewsLabel.setText("👥 Διαθέσιμες Ομάδες: " + availableCrews + "/" + totalCrews);
                crewsLabel.setForeground(availableCrews > 0 ? new Color(34, 139, 34) : Color.RED);
            }
        });
    }
    
    // Προσθέστε αυτή τη μέθοδο:

    // ΠΡΟΣΘΗΚΗ: Απλή μέθοδος για ενημέρωση ενός στατιστικού
    public void updateSingleStat(String statType, int available, int total) {
        SwingUtilities.invokeLater(() -> {
            switch (statType.toLowerCase()) {
                case "trucks":
                    if (trucksLabel != null) {
                        trucksLabel.setText("🚒 Διαθέσιμα Οχήματα: " + available + "/" + total);
                        trucksLabel.setForeground(available > 0 ? new Color(34, 139, 34) : Color.RED);
                    }
                    break;
                case "aircraft":
                    if (aircraftLabel != null) {
                        aircraftLabel.setText("✈️ Διαθέσιμα Αεροσκάφη: " + available + "/" + total);
                        aircraftLabel.setForeground(available > 0 ? new Color(34, 139, 34) : Color.RED);
                    }
                    break;
                case "helicopters":
                    if (helicoptersLabel != null) {
                        helicoptersLabel.setText("🚁 Διαθέσιμα Ελικόπτερα: " + available + "/" + total);
                        helicoptersLabel.setForeground(available > 0 ? new Color(34, 139, 34) : Color.RED);
                    }
                    break;
                case "crews":
                    if (crewsLabel != null) {
                        crewsLabel.setText("👥 Διαθέσιμες Ομάδες: " + available + "/" + total);
                        crewsLabel.setForeground(available > 0 ? new Color(34, 139, 34) : Color.RED);
                    }
                    break;
                case "fires":
                    if (fireCountLabel != null) {
                        fireCountLabel.setText("🔥 Ενεργές Εστίες: " + available);
                        fireCountLabel.setForeground(available > 0 ? Color.RED : new Color(34, 139, 34));
                    }
                    break;
            }
        });
    }
}