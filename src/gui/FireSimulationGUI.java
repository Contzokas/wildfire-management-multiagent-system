package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.font.FontRenderContext;

public class FireSimulationGUI extends JFrame {
    private static final int GRID_SIZE = 150;
    private static final int MIN_CELL_SIZE = 2;
    private static final int MAX_CELL_SIZE = 20;
    private static final int DEFAULT_CELL_SIZE = 4;
    
    // Modern color scheme
    private static final Color BACKGROUND_COLOR = new Color(0x2b2b2b);
    private static final Color PANEL_COLOR = new Color(0x3c3f41);
    private static final Color ACCENT_COLOR = new Color(0x4CAF50);
    private static final Color DANGER_COLOR = new Color(0xF44336);
    private static final Color WARNING_COLOR = new Color(0xFF9800);
    private static final Color INFO_COLOR = new Color(0x2196F3);
    private static final Color TEXT_COLOR = new Color(0xffffff);
    private static final Color BORDER_COLOR = new Color(0x555555);
    
    // Grid components
    private GridPanel gridPanel;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JLabel coordinatesLabel;
    private JLabel zoomLabel;
    private JPanel statsPanel;
    private Timer refreshTimer;
    
    // Grid data
    private Map<String, CellState> cellStates = new HashMap<>();
    private int cellSize = DEFAULT_CELL_SIZE;
    private double zoomFactor = 1.0;
    
    // Statistics
    private int totalTrees = 0;
    private int burningTrees = 0;
    private int destroyedTrees = 0;
    private int activeFires = 0;
    
    // Emergency state
    private boolean emergencyActive = false;
    private Timer emergencyTimer;
    
    // Labels for dynamic stats update
    private JLabel fireCountLabel;
    private JLabel trucksLabel;
    private JLabel aircraftLabel;
    private JLabel helicoptersLabel;
    private JLabel crewsLabel;
    
    // Weather panel fields
    private JLabel windSpeedLabel;
    private JLabel windDirectionLabel;
    private JLabel temperatureLabel;
    private JLabel humidityLabel;
    private JLabel weatherConditionLabel;
    private JPanel weatherPanel;
    
    // Control buttons
    private JButton emergencyBtn;
    private JButton stopEmergencyBtn;
    
    // Active fires list for proper extinguishing
    private List<String> activeFireLocations = new ArrayList<>();
    
    // ENHANCED FONT SYSTEM - Combining Noto and Segoe UI
    private Font primaryUIFont;           // Segoe UI for main UI
    private Font emojiFont;              // Noto Color Emoji for emojis
    private Font combinedFont;           // Combined font for text with emojis
    private FontRenderContext fontContext;
    
    // Singleton instance
    private static FireSimulationGUI instance;
    private static final Object lock = new Object();
    
    // ΝΕΟΣ ΚΩΔΙΚΑΣ: Resource configuration fields
    private ResourceConfig resourceConfig;
    
    // Resource configuration class
    private static class ResourceConfig {
        public int totalTrucks = 4;
        public int availableTrucks = 4;
        public int totalAircraft = 2;
        public int availableAircraft = 2;
        public int totalHelicopters = 1;
        public int availableHelicopters = 1;
        public int totalCrews = 6;
        public int availableCrews = 6;
        
        public ResourceConfig() {}
        
        public ResourceConfig(int trucks, int aircraft, int helicopters, int crews) {
            this.totalTrucks = this.availableTrucks = trucks;
            this.totalAircraft = this.availableAircraft = aircraft;
            this.totalHelicopters = this.availableHelicopters = helicopters;
            this.totalCrews = this.availableCrews = crews;
        }
    }
    
    // Cell State Classes
    enum CellType {
        EMPTY, TREE, FIRE, BURNING_TREE, DESTROYED, WATER, FIREFIGHTER, AIRCRAFT, HELICOPTER, GROUND_CREW
    }
    
    static class CellState {
        CellType type;
        int intensity;
        long lastUpdate;
        boolean canBeExtinguished;
        
        public CellState(CellType type) {
            this.type = type;
            this.intensity = 0;
            this.lastUpdate = System.currentTimeMillis();
            this.canBeExtinguished = (type == CellType.FIRE || type == CellType.BURNING_TREE);
        }
        
        public CellState(CellType type, int intensity) {
            this.type = type;
            this.intensity = intensity;
            this.lastUpdate = System.currentTimeMillis();
            this.canBeExtinguished = (type == CellType.FIRE || type == CellType.BURNING_TREE);
        }
    }
    
    // ΝΕΟΣ ΚΩΔΙΚΑΣ: Static method to set initial resource configuration
    public static void setInitialResourceConfig(int trucks, int aircraft, int helicopters, int crews) {
        initialTrucks = trucks;
        initialAircraft = aircraft;
        initialHelicopters = helicopters;
        initialCrews = crews;
    }
    
    // Static variables for initial configuration
    private static int initialTrucks = 4;
    private static int initialAircraft = 2;
    private static int initialHelicopters = 1;
    private static int initialCrews = 6;
    
    public FireSimulationGUI() {
        // ΕΝΗΜΕΡΩΣΗ: Initialize resource configuration with command line values
        resourceConfig = new ResourceConfig(initialTrucks, initialAircraft, initialHelicopters, initialCrews);
        
        // ENHANCED EMOJI AND FONT SETUP
        setupAdvancedFontSupport();
        
        // Unicode and rendering settings
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("user.language", "el");
        System.setProperty("user.country", "GR");
        
        // Enhanced emoji rendering properties
        System.setProperty("java.awt.useSystemAAFontSettings", "gasp");
        System.setProperty("swing.useSystemAAFontSettings", "on");
        System.setProperty("java2d.uiScale", "1.0");
        System.setProperty("awt.font.desktophints", "on");
        
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            setupModernTheme();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initializeGrid();
        initializeGUI();
        
        // Setup timers without automatic fires
        SwingUtilities.invokeLater(() -> {
            simulateWeatherUpdate();
            updateResourceStats();
            
            Timer weatherUpdateTimer = new Timer(30000, e -> {
                simulateWeatherUpdate();
                // ΑΦΑΙΡΕΣΗ: Remove log message
                // addLog("🔄 Αυτόματη ενημέρωση καιρικών συνθηκών");
            });
            weatherUpdateTimer.start();
            
            Timer statsUpdateTimer = new Timer(10000, e -> {
                updateFireCount();
                updateResourceStats();
            });
            statsUpdateTimer.start();
            
            // ΑΦΑΙΡΕΣΗ: Remove log messages
            // addLog("⚙️ Σύστημα έτοιμο - Πατήστε 'Νέα Φωτιά' για να ξεκινήσετε");
            // addLog("🌤️ Καιρός: Ενημέρωση κάθε 30 δευτερόλεπτα");
            // addLog("📊 Στατιστικά: Ενημέρωση κάθε 10 δευτερόλεπτα");
        });
        
        startRefreshTimer();
    }
    
    // ENHANCED FONT SUPPORT - Combining Noto and Segoe UI
    // ΔΙΟΡΘΩΣΗ: Απλοποίηση font setup και χρήση text εναλλακτικών
    private void setupAdvancedFontSupport() {
        try {
            // Χρησιμοποίησε μόνο Segoe UI για όλα
            primaryUIFont = new Font("Segoe UI", Font.PLAIN, 14);
            emojiFont = new Font("Segoe UI", Font.PLAIN, 16);
            combinedFont = primaryUIFont;
            
            System.out.println("🎨 SIMPLE FONT SETUP:");
            System.out.println("├─ Primary UI: " + primaryUIFont.getFamily());
            System.out.println("├─ Emoji Font: " + emojiFont.getFamily());
            System.out.println("└─ Combined Font: " + combinedFont.getFamily());
            
            // Initialize font context
            BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = dummy.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            fontContext = g2d.getFontRenderContext();
            g2d.dispose();
            
        } catch (Exception e) {
            System.err.println("❌ Error in font setup: " + e.getMessage());
            
            // Emergency fallback
            primaryUIFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
            emojiFont = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
            combinedFont = primaryUIFont;
        }
    }
    
    // Create a combined font that prefers emoji font for emojis, UI font for text
    private Font createCombinedFont(Font uiFont, Font emojiFont) {
        // This creates a composite font approach
        // We'll handle this in the rendering methods
        return uiFont; // Base font
    }
    
    // Enhanced text rendering method that combines fonts
    private void drawTextWithEmojis(Graphics2D g2d, String text, int x, int y) {
        if (text == null || text.isEmpty()) return;
        
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        // Split text into emoji and non-emoji parts
        FontMetrics uiFM = g2d.getFontMetrics(primaryUIFont);
        FontMetrics emojiFM = g2d.getFontMetrics(emojiFont);
        
        int currentX = x;
        
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            String currentChar = text.substring(i, i + charCount);
            
            // Check if it's an emoji (roughly)
            boolean isEmoji = (codePoint >= 0x1F600 && codePoint <= 0x1F64F) ||  // Emoticons
                             (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) ||  // Misc Symbols
                             (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) ||  // Transport
                             (codePoint >= 0x1F1E0 && codePoint <= 0x1F1FF) ||  // Flags
                             (codePoint >= 0x2600 && codePoint <= 0x26FF) ||   // Misc symbols
                             (codePoint >= 0x2700 && codePoint <= 0x27BF);     // Dingbats
        
            if (isEmoji && emojiFont.canDisplay(codePoint)) {
                // Use emoji font
                Font oldFont = g2d.getFont();
                g2d.setFont(emojiFont);
                g2d.drawString(currentChar, currentX, y);
                currentX += emojiFM.stringWidth(currentChar);
                g2d.setFont(oldFont);
            } else {
                // Use UI font
                Font oldFont = g2d.getFont();
                g2d.setFont(primaryUIFont);
                g2d.drawString(currentChar, currentX, y);
                currentX += uiFM.stringWidth(currentChar);
                g2d.setFont(oldFont);
            }
            
            i += charCount;
        }
    }
    
    // FIXED: Complete getInstance implementation
    public static FireSimulationGUI getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    System.out.println("🔄 Δημιουργία νέου FireSimulationGUI instance...");
                    
                    if (SwingUtilities.isEventDispatchThread()) {
                        instance = new FireSimulationGUI();
                        instance.setVisible(true);
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            if (instance == null) {
                                instance = new FireSimulationGUI();
                                instance.setVisible(true);
                            }
                        });
                        
                        int maxWait = 50;
                        int waited = 0;
                        while (instance == null && waited < maxWait) {
                            try {
                                Thread.sleep(100);
                                waited++;
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                        
                        if (instance == null) {
                            System.err.println("❌ TIMEOUT δημιουργίας GUI - δημιουργία fallback instance");
                            instance = new FireSimulationGUI();
                        }
                    }
                }
            }
        }
        return instance;
    }
    
    private void setupModernTheme() {
        // ΚΛΕΙΔΙ: Χρησιμοποίησε primaryUIFont για όλα τα UI elements
        UIManager.put("Panel.background", PANEL_COLOR);
        UIManager.put("Panel.foreground", TEXT_COLOR);
        
        UIManager.put("Button.background", PANEL_COLOR);
        UIManager.put("Button.foreground", TEXT_COLOR);
        UIManager.put("Button.font", primaryUIFont.deriveFont(Font.BOLD, 12f)); // ΑΛΛΑΓΗ
        UIManager.put("Button.border", new LineBorder(BORDER_COLOR, 1));
        UIManager.put("Button.focusPainted", false);
        
        UIManager.put("Label.foreground", TEXT_COLOR);
        UIManager.put("Label.font", primaryUIFont); // ΑΛΛΑΓΗ
        
        UIManager.put("TextArea.background", BACKGROUND_COLOR);
        UIManager.put("TextArea.foreground", TEXT_COLOR);
        UIManager.put("TextArea.caretForeground", TEXT_COLOR);
        UIManager.put("TextArea.font", primaryUIFont.deriveFont(12f)); // ΑΛΛΑΓΗ
        UIManager.put("TextArea.selectionBackground", INFO_COLOR);
        
        UIManager.put("ScrollPane.background", BACKGROUND_COLOR);
        UIManager.put("ScrollBar.background", PANEL_COLOR);
        UIManager.put("ScrollBar.thumb", BORDER_COLOR);
        
        UIManager.put("Menu.font", primaryUIFont.deriveFont(Font.BOLD, 14f)); // ΑΛΛΑΓΗ
        UIManager.put("MenuItem.font", primaryUIFont.deriveFont(Font.BOLD, 14f)); // ΑΛΛΑΓΗ
        UIManager.put("Menu.foreground", TEXT_COLOR);
        UIManager.put("MenuItem.foreground", TEXT_COLOR);
        UIManager.put("MenuBar.background", PANEL_COLOR);
        UIManager.put("Menu.background", PANEL_COLOR);
        UIManager.put("MenuItem.background", PANEL_COLOR);
        
        UIManager.put("TitledBorder.titleColor", TEXT_COLOR);
        UIManager.put("TitledBorder.border", new LineBorder(BORDER_COLOR));
    }
    
    private void initializeGUI() {
        setTitle("FIRE Προσομοίωση Δασικής Πυρκαγιάς - Πολυπρακτορικό Σύστημα (150x150)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        
        createGridPanel();
        createMenuBar();
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setBackground(BACKGROUND_COLOR);
        mainSplit.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainSplit.setLeftComponent(createLeftPanel());
        mainSplit.setRightComponent(createRightPanel());
        mainSplit.setDividerLocation(800);
        mainSplit.setDividerSize(8);
        
        add(mainSplit, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        
        setMinimumSize(new Dimension(1400, 900));
        
        try {
            setIconImage(createFireIcon());
        } catch (Exception e) {
            // Ignore icon errors
        }
    }
    
    private Image createFireIcon() {
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(DANGER_COLOR);
        g2d.fillOval(8, 12, 16, 16);
        g2d.setColor(WARNING_COLOR);
        g2d.fillOval(10, 8, 12, 20);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(12, 10, 8, 16);
        
        g2d.dispose();
        return icon;
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PANEL_COLOR);
        menuBar.setBorder(new LineBorder(BORDER_COLOR));
        
        JMenu viewMenu = createEmojiMenu("VIEW Προβολή");
        
        JMenuItem zoomIn = createEmojiMenuItem("ZOOM+ Μεγέθυνση", KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK);
        zoomIn.addActionListener(e -> zoomIn());
        
        JMenuItem zoomOut = createEmojiMenuItem("ZOOM- Σμίκρυνση", KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK);
        zoomOut.addActionListener(e -> zoomOut());
        
        JMenuItem resetView = createEmojiMenuItem("HOME Επαναφορά Προβολής", KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK);
        resetView.addActionListener(e -> resetView());
        
        JMenuItem centerView = createEmojiMenuItem("TARGET Κεντράρισμα", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        centerView.addActionListener(e -> centerView());
        
        viewMenu.add(zoomIn);
        viewMenu.add(zoomOut);
        viewMenu.addSeparator();
        viewMenu.add(resetView);
        viewMenu.add(centerView);
        
        JMenu simMenu = createEmojiMenu("SIM Προσομοίωση");
        
        JMenuItem startFire = createEmojiMenuItem("FIRE Νέα Φωτιά", 0, 0);
        startFire.addActionListener(e -> simulateRandomFire());
        
        JMenuItem emergency = createEmojiMenuItem("ALERT Κατάσταση Έκτακτης Ανάγκης", 0, 0);
        emergency.addActionListener(e -> startEmergency());
        
        JMenuItem clearAll = createEmojiMenuItem("CLEAR Καθαρισμός Όλων", 0, 0);
        clearAll.addActionListener(e -> clearAllFires());
        
        simMenu.add(startFire);
        simMenu.add(emergency);
        simMenu.addSeparator();
        simMenu.add(clearAll);
        
        // ΝΕΟΣ ΚΩΔΙΚΑΣ: Add resources menu
        JMenu resourcesMenu = createEmojiMenu("RES Πόροι");
        
        JMenuItem configResources = createEmojiMenuItem("GEAR Διαμόρφωση Πόρων", KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK);
        configResources.addActionListener(e -> showResourceConfigDialog());
        
        JMenuItem resetResources = createEmojiMenuItem("RESET Επαναφορά Πόρων", 0, 0);
        resetResources.addActionListener(e -> resetResources());
        
        resourcesMenu.add(configResources);
        resourcesMenu.add(resetResources);
        
        menuBar.add(viewMenu);
        menuBar.add(simMenu);
        menuBar.add(resourcesMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JMenu createEmojiMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(TEXT_COLOR);
        menu.setOpaque(true);
        menu.setBackground(PANEL_COLOR);
        menu.setFont(primaryUIFont.deriveFont(Font.BOLD, 14f));
        return menu;
    }

    private JMenuItem createEmojiMenuItem(String text, int keyCode, int modifiers) {
        JMenuItem item = new JMenuItem(text);
        item.setForeground(TEXT_COLOR);
        item.setBackground(PANEL_COLOR);
        item.setOpaque(true);
        item.setFont(primaryUIFont.deriveFont(14f));
        if (keyCode != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        }
        return item;
    }
    
    private JButton createEmojiButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(new LineBorder(color.darker(), 1));
        button.setFocusPainted(false);
        button.setFont(primaryUIFont.deriveFont(Font.BOLD, 16f));
        button.setPreferredSize(new Dimension(50, 30));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private JLabel createEmojiLabel(String text, int style, float size) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(primaryUIFont.deriveFont(style, size));
        return label;
    }
    
    private JButton createEmojiControlButton(String text, String tooltip, Color color) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(new LineBorder(color.darker(), 1));
        button.setFocusPainted(false);
        button.setFont(primaryUIFont.deriveFont(Font.BOLD, 14f));
        button.setPreferredSize(new Dimension(250, 45));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private JLabel createEmojiInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(primaryUIFont.deriveFont(14f));
        label.setPreferredSize(new Dimension(200, 20));
        return label;
    }
    
    private void createGridPanel() {
        gridPanel = new GridPanel();
        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setPreferredSize(new Dimension(800, 600));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 2));
        
        gridPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    if (e.getWheelRotation() < 0) {
                        zoomIn();
                    } else {
                        zoomOut();
                    }
                }
            }
        });
        
        gridPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleGridClick(e);
            }
        });
        
        gridPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateCoordinateDisplay(e);
            }
        });
    }
    
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setBackground(BACKGROUND_COLOR);
        gridContainer.add(createGridToolbar(), BorderLayout.NORTH);
        gridContainer.add(scrollPane, BorderLayout.CENTER);
        gridContainer.add(createControlPanel(), BorderLayout.SOUTH);
        
        leftPanel.add(gridContainer, BorderLayout.CENTER);
        return leftPanel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(400, 0));
        rightPanel.setBackground(BACKGROUND_COLOR);
        
        rightPanel.add(createWeatherPanel(), BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(createStatsPanel());
        centerPanel.add(createLegendPanel());
        rightPanel.add(centerPanel, BorderLayout.CENTER);
        
        // ΑΦΑΙΡΕΣΗ: Remove log panel
        // rightPanel.add(createLogPanel(), BorderLayout.SOUTH);
        
        return rightPanel;
    }
    
    private JPanel createGridToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(PANEL_COLOR);
        toolbar.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        JButton zoomInBtn = createEmojiButton("+", "Μεγέθυνση", ACCENT_COLOR);
        zoomInBtn.addActionListener(e -> zoomIn());
        
        JButton zoomOutBtn = createEmojiButton("-", "Σμίκρυνση", ACCENT_COLOR);
        zoomOutBtn.addActionListener(e -> zoomOut());
        
        JButton resetBtn = createEmojiButton("HOME", "Επαναφορά Προβολής", INFO_COLOR);
        resetBtn.addActionListener(e -> resetView());
        
        zoomLabel = createEmojiLabel("ZOOM Zoom: 100%", Font.BOLD, 12f);
        coordinatesLabel = createEmojiLabel("POS Συντεταγμένες: (0, 0)", Font.PLAIN, 12f);
        
        toolbar.add(zoomInBtn);
        toolbar.add(zoomOutBtn);
        toolbar.add(resetBtn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(zoomLabel);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(coordinatesLabel);
        
        return toolbar;
    }
    
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(PANEL_COLOR);
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        emergencyBtn = createEmojiButton("ALERT Κατάσταση Έκτακτης Ανάγκης", "Έναρξη κατάστασης έκτακτης ανάγκης", DANGER_COLOR);
        emergencyBtn.addActionListener(e -> startEmergency());
        
        stopEmergencyBtn = createEmojiButton("STOP Έκτακτη Ανάγκη", "Τερματισμός κατάστασης έκτακτης ανάγκης", WARNING_COLOR);
        stopEmergencyBtn.addActionListener(e -> stopEmergency());
        stopEmergencyBtn.setEnabled(false);
        
        controlPanel.add(emergencyBtn);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(stopEmergencyBtn);
        
        return controlPanel;
    }
    
    private JPanel createWeatherPanel() {
        weatherPanel = new JPanel(new GridLayout(5, 2));
        weatherPanel.setBackground(PANEL_COLOR);
        weatherPanel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(BORDER_COLOR),
            "🌤️ Καιρικές Συνθήκες",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            primaryUIFont.deriveFont(Font.BOLD, 14f),
            TEXT_COLOR
        ));
        
        windSpeedLabel = createEmojiLabel("WIND Άνεμος: 0 m/s", Font.PLAIN, 12f);
        windDirectionLabel = createEmojiLabel("DIR Διεύθυνση: ", Font.PLAIN, 12f);
        temperatureLabel = createEmojiLabel("TEMP Θερμοκρασία: 0°C", Font.PLAIN, 12f);
        humidityLabel = createEmojiLabel("HUM Υγρασία: 0%", Font.PLAIN, 12f);
        weatherConditionLabel = createEmojiLabel("COND Κατάσταση: ", Font.PLAIN, 12f);
        
        weatherPanel.add(createEmojiLabel("Άνεμος:", Font.BOLD, 12f));
        weatherPanel.add(windSpeedLabel);
        weatherPanel.add(createEmojiLabel("Διεύθυνση:", Font.BOLD, 12f));
        weatherPanel.add(windDirectionLabel);
        weatherPanel.add(createEmojiLabel("Θερμοκρασία:", Font.BOLD, 12f));
        weatherPanel.add(temperatureLabel);
        weatherPanel.add(createEmojiLabel("Υγρασία:", Font.BOLD, 12f));
        weatherPanel.add(humidityLabel);
        weatherPanel.add(createEmojiLabel("Κατάσταση:", Font.BOLD, 12f));
        weatherPanel.add(weatherConditionLabel);
        
        return weatherPanel;
    }
    
    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(5, 2));
        statsPanel.setBackground(PANEL_COLOR);
        statsPanel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(BORDER_COLOR),
            "📊 Στατιστικά",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            primaryUIFont.deriveFont(Font.BOLD, 14f),
            TEXT_COLOR
        ));
        
        fireCountLabel = createEmojiLabel("FIRE Ενεργές φωτιές: 0", Font.PLAIN, 12f);
        JLabel totalTreesLabel = createEmojiLabel("🌲 Συνολικά Δέντρα: " + totalTrees, Font.PLAIN, 12f);
        JLabel burningTreesLabel = createEmojiLabel("🔥 Καίγονται Δέντρα: " + burningTrees, Font.PLAIN, 12f);
        JLabel destroyedTreesLabel = createEmojiLabel("💔 Κατεστραμμένα Δέντρα: " + destroyedTrees, Font.PLAIN, 12f);
        trucksLabel = createEmojiLabel("TRUCK Οχήματα: " + resourceConfig.availableTrucks + "/" + resourceConfig.totalTrucks, Font.PLAIN, 12f);
        aircraftLabel = createEmojiLabel("PLANE Αεροσκάφη: " + resourceConfig.availableAircraft + "/" + resourceConfig.totalAircraft, Font.PLAIN, 12f);
        helicoptersLabel = createEmojiLabel("HELI Ελικόπτερα: " + resourceConfig.availableHelicopters + "/" + resourceConfig.totalHelicopters, Font.PLAIN, 12f);
        crewsLabel = createEmojiLabel("CREW Ομάδες: " + resourceConfig.availableCrews + "/" + resourceConfig.totalCrews, Font.PLAIN, 12f);
        
        statsPanel.add(createEmojiLabel("Ενεργές φωτιές:", Font.BOLD, 12f));
        statsPanel.add(fireCountLabel);
        statsPanel.add(createEmojiLabel("Συνολικά δέντρα:", Font.BOLD, 12f));
        statsPanel.add(totalTreesLabel);
        statsPanel.add(createEmojiLabel("Καίγονται δέντρα:", Font.BOLD, 12f));
        statsPanel.add(burningTreesLabel);
        statsPanel.add(createEmojiLabel("Κατεστραμμένα δέντρα:", Font.BOLD, 12f));
        statsPanel.add(destroyedTreesLabel);
        statsPanel.add(createEmojiLabel("Διαθέσιμα οχήματα:", Font.BOLD, 12f));
        statsPanel.add(trucksLabel);
        statsPanel.add(createEmojiLabel("Διαθέσιμα αεροσκάφη:", Font.BOLD, 12f));
        statsPanel.add(aircraftLabel);
        statsPanel.add(createEmojiLabel("Διαθέσιμα ελικόπτερα:", Font.BOLD, 12f));
        statsPanel.add(helicoptersLabel);
        statsPanel.add(createEmojiLabel("Διαθέσιμες ομάδες:", Font.BOLD, 12f));
        statsPanel.add(crewsLabel);
        
        return statsPanel;
    }
    
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel();
        legendPanel.setBackground(PANEL_COLOR);
        legendPanel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(BORDER_COLOR),
            "📚 Θρύλοι",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            primaryUIFont.deriveFont(Font.BOLD, 14f),
            TEXT_COLOR
        ));
        
        String[] legendItems = {
            "🌲 Δέντρο",
            "🔥 Φωτιά",
            "💧 Νερό",
            "🚒 Πυροσβεστικό Όχημα",
            "✈️ Αεροσκάφος",
            "🚁 Ελικόπτερο",
            "👥 Επίγειες Ομάδες",
            "❌ Κατεστραμμένο Δέντρο"
        };
        
        JPanel grid = new JPanel(new GridLayout(0, 2));
        grid.setBackground(PANEL_COLOR);
        
        for (String item : legendItems) {
            JLabel label = createEmojiLabel(item, Font.PLAIN, 12f);
            label.setHorizontalAlignment(SwingConstants.LEFT);
            grid.add(label);
        }
        
        legendPanel.setLayout(new BorderLayout());
        legendPanel.add(grid, BorderLayout.CENTER);
        
        return legendPanel;
    }
    
    // ΝΕΟΣ ΚΩΔΙΚΑΣ: Resource configuration dialog
    private void showResourceConfigDialog() {
        JDialog dialog = new JDialog(this, "Διαμόρφωση Πυροσβεστικών Πόρων", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = createEmojiLabel("GEAR Διαμόρφωση Πυροσβεστικών Μέσων", Font.BOLD, 18f);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Info label
        JLabel infoLabel = createEmojiLabel("INFO Τρέχουσα διάταξη: " + 
            resourceConfig.totalTrucks + " οχήματα, " + 
            resourceConfig.totalAircraft + " αεροσκάφη, " + 
            resourceConfig.totalHelicopters + " ελικόπτερα, " + 
            resourceConfig.totalCrews + " ομάδες", Font.ITALIC, 12f);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create spinners for each resource
        JSpinner trucksSpinner = createResourceSpinner(resourceConfig.totalTrucks, 0, 50);
        JSpinner aircraftSpinner = createResourceSpinner(resourceConfig.totalAircraft, 0, 20);
        JSpinner helicoptersSpinner = createResourceSpinner(resourceConfig.totalHelicopters, 0, 20);
        JSpinner crewsSpinner = createResourceSpinner(resourceConfig.totalCrews, 0, 100);
        
        // Add form elements
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createEmojiLabel("TRUCK Πυροσβεστικά Οχήματα:", Font.BOLD, 14f), gbc);
        gbc.gridx = 1;
        formPanel.add(trucksSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createEmojiLabel("PLANE Αεροσκάφη:", Font.BOLD, 14f), gbc);
        gbc.gridx = 1;
        formPanel.add(aircraftSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createEmojiLabel("HELI Ελικόπτερα:", Font.BOLD, 14f), gbc);
        gbc.gridx = 1;
        formPanel.add(helicoptersSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createEmojiLabel("CREW Επίγειες Ομάδες:", Font.BOLD, 14f), gbc);
        gbc.gridx = 1;
        formPanel.add(crewsSpinner, gbc);
        
        // Warning label
        JLabel warningLabel = createEmojiLabel("WARN Σημείωση: Αλλαγή μόνο για επόμενη εκτέλεση", Font.ITALIC, 11f);
        warningLabel.setForeground(WARNING_COLOR);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(warningLabel, gbc);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        
        JButton cancelBtn = createEmojiControlButton("CANCEL Ακύρωση", "Ακύρωση αλλαγών", BORDER_COLOR);
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        JButton resetBtn = createEmojiControlButton("RESET Επαναφορά", "Επαναφορά προεπιλογών", WARNING_COLOR);
        resetBtn.addActionListener(e -> {
            trucksSpinner.setValue(4);
            aircraftSpinner.setValue(2);
            helicoptersSpinner.setValue(1);
            crewsSpinner.setValue(6);
        });
        
        JButton saveBtn = createEmojiControlButton("SAVE Αποθήκευση", "Αποθήκευση ρυθμίσεων", ACCENT_COLOR);
        saveBtn.addActionListener(e -> {
            // Update resource configuration for display only
            resourceConfig.totalTrucks = (Integer) trucksSpinner.getValue();
            resourceConfig.totalAircraft = (Integer) aircraftSpinner.getValue();
            resourceConfig.totalHelicopters = (Integer) helicoptersSpinner.getValue();
            resourceConfig.totalCrews = (Integer) crewsSpinner.getValue();
            
            // Keep available equal to total for display
            resourceConfig.availableTrucks = resourceConfig.totalTrucks;
            resourceConfig.availableAircraft = resourceConfig.totalAircraft;
            resourceConfig.availableHelicopters = resourceConfig.totalHelicopters;
            resourceConfig.availableCrews = resourceConfig.totalCrews;
            
            // Update display
            updateResourceStats();
            
            dialog.dispose();
            
            // Show confirmation with note about restart
            JOptionPane.showMessageDialog(this, 
                "Οι ρυθμίσεις ενημερώθηκαν για την οθόνη.\n" +
                "Για πλήρη εφαρμογή, επανεκκινήστε με τα νέα νούμερα.", 
                "Επιτυχής Ενημέρωση", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        buttonsPanel.add(cancelBtn);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(resetBtn);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(saveBtn);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(infoLabel, BorderLayout.SOUTH);
        
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    // ΝΕΟΣ ΚΩΔΙΚΑΣ: Helper method to create resource spinners
    private JSpinner createResourceSpinner(int initialValue, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialValue, min, max, 1));
        spinner.setPreferredSize(new Dimension(80, 30));
        
        // Style the spinner
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setBackground(BACKGROUND_COLOR);
            textField.setForeground(TEXT_COLOR);
            textField.setFont(primaryUIFont.deriveFont(14f));
        }
        
        return spinner;
    }
    
    // ΕΝΗΜΕΡΩΣΗ: Update all methods that used addLog to remove logging
    private void startFireAt(int x, int y) {
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        
        // Only start fire if location is empty or has a tree
        if (existing == null || existing.type == CellType.EMPTY || existing.type == CellType.TREE) {
            cellStates.put(key, new CellState(CellType.FIRE, 5));
            activeFireLocations.add(key);
            activeFires++;
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("🔥 Φωτιά ξεκίνησε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void placeTreeAt(int x, int y) {
        String key = x + "," + y;
        CellState current = cellStates.get(key);
        
        if (current == null || current.type == CellType.EMPTY || current.type == CellType.DESTROYED) {
            cellStates.put(key, new CellState(CellType.TREE));
            totalTrees++;
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("🌲 Δέντρο τοποθετήθηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void clearAllFires() {
        // Stop emergency if active
        if (emergencyActive) {
            stopEmergency();
        }
        
        // Clear all fires and restore previous states
        for (Map.Entry<String, CellState> entry : cellStates.entrySet()) {
            CellState state = entry.getValue();
            if (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE) {
                entry.setValue(new CellState(CellType.DESTROYED));
            }
        }
        
        activeFireLocations.clear();
        activeFires = 0;
        burningTrees = 0;
        
        // ΑΦΑΙΡΕΣΗ: Remove log message
        // addLog("🧹 Όλες οι φωτιές καθαρίστηκαν");
        gridPanel.repaint();
        updateStatsDisplay();
        updateStatus("🟢 Όλες οι φωτιές έχουν σβήσει");
    }
    
    private void extinguishFireAt(int x, int y) {
        String key = x + "," + y;
        CellState state = cellStates.get(key);
        
        if (state != null && (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE)) {
            cellStates.put(key, new CellState(CellType.DESTROYED));
            activeFireLocations.remove(key);
            activeFires = Math.max(0, activeFires - 1);
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("✅ Φωτιά σβήστηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void resetResources() {
        resourceConfig = new ResourceConfig(initialTrucks, initialAircraft, initialHelicopters, initialCrews);
        updateResourceStats();
        
        // ΑΦΑΙΡΕΣΗ: Remove log message
        // addLog("RESET Πόροι επαναφέρθηκαν στις προεπιλογές");
        
        JOptionPane.showMessageDialog(this, 
            "Οι πυροσβεστικοί πόροι επαναφέρθηκαν στις αρχικές τιμές!", 
            "Επαναφορά Πόρων", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Emergency system methods
    private void startEmergency() {
        if (!emergencyActive) {
            emergencyActive = true;
            emergencyBtn.setEnabled(false);
            stopEmergencyBtn.setEnabled(true);
            
            // addLog("🚨 ΚΑΤΑΣΤΑΣΗ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ ΚΗΡΥΧΘΗΚΕ!");
            updateStatus("🚨 ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ - Δημιουργία πολλαπλών φωτιών");
            
            emergencyTimer = new Timer(3000, e -> createEmergencyFire());
            emergencyTimer.start();
            
            // Create initial fires
            for (int i = 0; i < 5; i++) {
                createEmergencyFire();
            }
        }
    }
    
    private void stopEmergency() {
        if (emergencyActive) {
            emergencyActive = false;
            emergencyBtn.setEnabled(true);
            stopEmergencyBtn.setEnabled(false);
            
            if (emergencyTimer != null) {
                emergencyTimer.stop();
                emergencyTimer = null;
            }
            
            // addLog("✅ Κατάσταση έκτακτης ανάγκης τερματίστηκε");
            updateStatus("🟢 Κανονική λειτουργία - Έκτακτη ανάγκη τερματίστηκε");
        }
    }
    
    private void createEmergencyFire() {
        Random rand = new Random();
        int x = 1 + rand.nextInt(GRID_SIZE);
        int y = 1 + rand.nextInt(GRID_SIZE);
        
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        if (existing == null || existing.type != CellType.FIRE) {
            startFireAt(x, y);
            // addLog("🚨 ΕΚΤΑΚΤΗ ΦΩΤΙΑ στη θέση (" + x + ", " + y + ")");
        }
    }
    
    // Enhanced Grid Panel Class with Combined Font Rendering
    private class GridPanel extends JPanel {
        
        public GridPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            
            // Enhanced rendering hints for emoji support
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            Rectangle visibleRect = getVisibleRect();
            int startX = Math.max(1, visibleRect.x / cellSize);
            int endX = Math.min(GRID_SIZE, (visibleRect.x + visibleRect.width) / cellSize + 1);
            int startY = Math.max(1, visibleRect.y / cellSize);
            int endY = Math.min(GRID_SIZE, (visibleRect.y + visibleRect.height) / cellSize + 1);
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    drawCell(g2d, x, y);
                }
            }
            
            if (cellSize >= 8) {
                drawGridLines(g2d, startX, endX, startY, endY);
            }
            
            g2d.dispose();
        }
        
        private void drawCell(Graphics2D g2d, int x, int y) {
            String key = x + "," + y;
            CellState state = cellStates.get(key);
            
            if (state == null) state = new CellState(CellType.EMPTY);
            
            int pixelX = (x - 1) * cellSize;
            int pixelY = (y - 1) * cellSize;
            
            g2d.setColor(getCellColor(state));
            g2d.fillRect(pixelX, pixelY, cellSize, cellSize);
            
            if (cellSize >= 8) {
                drawCellIcon(g2d, pixelX, pixelY, state);
            }
        }
        
        private void drawGridLines(Graphics2D g2d, int startX, int endX, int startY, int endY) {
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.setStroke(new BasicStroke(0.5f));
            
            for (int x = startX; x <= endX; x++) {
                int pixelX = (x - 1) * cellSize;
                g2d.drawLine(pixelX, (startY - 1) * cellSize, pixelX, endY * cellSize);
            }
            
            for (int y = startY; y <= endY; y++) {
                int pixelY = (y - 1) * cellSize;
                g2d.drawLine((startX - 1) * cellSize, pixelY, endX * cellSize, pixelY);
            }
        }
        
        private void drawCellIcon(Graphics2D g2d, int x, int y, CellState state) {
            String icon = getCellIcon(state);
            if (icon != null && !icon.isEmpty()) {
                g2d.setColor(Color.BLACK);
                
                // Use emoji font for better emoji rendering
                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, Math.min(cellSize - 1, 16)));
                FontMetrics fm = g2d.getFontMetrics();
                
                int iconX = x + (cellSize - fm.stringWidth(icon)) / 2;
                int iconY = y + (cellSize + fm.getAscent()) / 2;
                
                // Enhanced emoji rendering
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.drawString(icon, iconX, iconY);
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize);
        }
    }
    
    // Utility Methods
    private Color getCellColor(CellState state) {
        switch (state.type) {
            case TREE: return new Color(34, 139, 34);
            case FIRE: return new Color(255, Math.max(0, 255 - state.intensity * 20), 0);
            case BURNING_TREE: return new Color(255, 165 - state.intensity * 10, 0);
            case DESTROYED: return new Color(64, 64, 64);
            case WATER: return new Color(0, 191, 255);
            case FIREFIGHTER: return Color.RED;
            case AIRCRAFT: return Color.BLUE;
            case HELICOPTER: return Color.MAGENTA;
            case GROUND_CREW: return Color.ORANGE;
            default: return new Color(240, 240, 240);
        }
    }
    
    private String getCellIcon(CellState state) {
        switch (state.type) {
            case TREE: return "T";        // Αντί για 🌲
            case FIRE: return "F";        // Αντί για 🔥
            case BURNING_TREE: return "B"; // Αντί για 🔥
            case DESTROYED: return "X";    // Αντί για 🌫️
            case WATER: return "W";        // Αντί για 💧
            case FIREFIGHTER: return "R"; // Αντί για 🚒
            case AIRCRAFT: return "A";     // Αντί για ✈️
            case HELICOPTER: return "H";   // Αντί για 🚁
            case GROUND_CREW: return "G";  // Αντί για 👥
            default: return "";
        }
    }
    
    // Event Handlers
    private void handleGridClick(MouseEvent e) {
        int gridX = (e.getX() / cellSize) + 1;
        int gridY = (e.getY() / cellSize) + 1;
        
        if (gridX >= 1 && gridX <= GRID_SIZE && gridY >= 1 && gridY <= GRID_SIZE) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                startFireAt(gridX, gridY);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                placeTreeAt(gridX, gridY);
            }
        }
    }
    
    private void updateCoordinateDisplay(MouseEvent e) {
        int gridX = (e.getX() / cellSize) + 1;
        int gridY = (e.getY() / cellSize) + 1;
        
        if (gridX >= 1 && gridX <= GRID_SIZE && gridY >= 1 && gridY <= GRID_SIZE) {
            coordinatesLabel.setText("POS Συντεταγμένες: (" + gridX + ", " + gridY + ")");
        }
    }
    
    // Zoom and View Controls
    private void zoomIn() {
        if (cellSize < MAX_CELL_SIZE) {
            cellSize = Math.min(MAX_CELL_SIZE, cellSize + 1);
            updateZoom();
        }
    }
    
    private void zoomOut() {
        if (cellSize > MIN_CELL_SIZE) {
            cellSize = Math.max(MIN_CELL_SIZE, cellSize - 1);
            updateZoom();
        }
    }
    
    private void resetView() {
        cellSize = DEFAULT_CELL_SIZE;
        updateZoom();
        centerView();
    }
    
    private void centerView() {
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            Dimension viewSize = viewport.getExtentSize();
            Dimension gridSize = gridPanel.getPreferredSize();
            
            int x = Math.max(0, (gridSize.width - viewSize.width) / 2);
            int y = Math.max(0, (gridSize.height - viewSize.height) / 2);
            
            viewport.setViewPosition(new Point(x, y));
        });
    }
    
    private void updateZoom() {
        gridPanel.setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
        gridPanel.revalidate();
        gridPanel.repaint();
        
        zoomFactor = (double) cellSize / DEFAULT_CELL_SIZE;
        zoomLabel.setText("📐 Zoom: " + Math.round(zoomFactor * 100) + "%");
    }
    
    // Enhanced emoji rendering
    private void enhancedEmojiRendering() {
        // This can be called on startup or when changing settings
        System.setProperty("java.awt.useSystemAAFontSettings", "gasp");
        System.setProperty("swing.useSystemAAFontSettings", "on");
        System.setProperty("java2d.uiScale", "1.0");
        System.setProperty("awt.font.desktophints", "on");
    }
    
    // Initialize grid with empty state
    private void initializeGrid() {
        for (int x = 1; x <= GRID_SIZE; x++) {
            for (int y = 1; y <= GRID_SIZE; y++) {
                cellStates.put(x + "," + y, new CellState(CellType.EMPTY));
            }
        }
        updateStatsDisplay();
    }

    // Updates the statistics display labels
    private void updateStatsDisplay() {
        SwingUtilities.invokeLater(() -> {
            if (fireCountLabel != null) {
                fireCountLabel.setText("FIRE Ενεργές φωτιές: " + activeFires);
            }
        });
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(PANEL_COLOR);
        statusBar.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        statusLabel = createEmojiLabel("OK Σύστημα έτοιμο - Grid: 150x150", Font.BOLD, 12f);
        statusBar.add(statusLabel);
        
        return statusBar;
    }
    
    // Simulation Controls - FIXED: Only start fires manually
    private void simulateRandomFire() {
        Random rand = new Random();
        int x = 1 + rand.nextInt(GRID_SIZE);
        int y = 1 + rand.nextInt(GRID_SIZE);
        
        // Ensure we don't start fire on existing fire
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        
        int attempts = 0;
        while (existing != null && existing.type == CellType.FIRE && attempts < 10) {
            x = 1 + rand.nextInt(GRID_SIZE);
            y = 1 + rand.nextInt(GRID_SIZE);
            key = x + "," + y;
            existing = cellStates.get(key);
            attempts++;
        }
        
        startFireAt(x, y);
    }
    
    private void startFireAt(int x, int y) {
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        
        // Only start fire if location is empty or has a tree
        if (existing == null || existing.type == CellType.EMPTY || existing.type == CellType.TREE) {
            cellStates.put(key, new CellState(CellType.FIRE, 5));
            activeFireLocations.add(key);
            activeFires++;
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("🔥 Φωτιά ξεκίνησε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void placeTreeAt(int x, int y) {
        String key = x + "," + y;
        CellState current = cellStates.get(key);
        
        if (current == null || current.type == CellType.EMPTY || current.type == CellType.DESTROYED) {
            cellStates.put(key, new CellState(CellType.TREE));
            totalTrees++;
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("🌲 Δέντρο τοποθετήθηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void clearAllFires() {
        // Stop emergency if active
        if (emergencyActive) {
            stopEmergency();
        }
        
        // Clear all fires and restore previous states
        for (Map.Entry<String, CellState> entry : cellStates.entrySet()) {
            CellState state = entry.getValue();
            if (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE) {
                entry.setValue(new CellState(CellType.DESTROYED));
            }
        }
        
        activeFireLocations.clear();
        activeFires = 0;
        burningTrees = 0;
        
        // ΑΦΑΙΡΕΣΗ: Remove log message
        // addLog("🧹 Όλες οι φωτιές καθαρίστηκαν");
        gridPanel.repaint();
        updateStatsDisplay();
        updateStatus("🟢 Όλες οι φωτιές έχουν σβήσει");
    }
    
    private void extinguishFireAt(int x, int y) {
        String key = x + "," + y;
        CellState state = cellStates.get(key);
        
        if (state != null && (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE)) {
            cellStates.put(key, new CellState(CellType.DESTROYED));
            activeFireLocations.remove(key);
            activeFires = Math.max(0, activeFires - 1);
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("✅ Φωτιά σβήστηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void resetResources() {
        resourceConfig = new ResourceConfig(initialTrucks, initialAircraft, initialHelicopters, initialCrews);
        updateResourceStats();
        
        // ΑΦΑΙΡΕΣΗ: Remove log message
        // addLog("RESET Πόροι επαναφέρθηκαν στις προεπιλογές");
        
        JOptionPane.showMessageDialog(this, 
            "Οι πυροσβεστικοί πόροι επαναφέρθηκαν στις αρχικές τιμές!", 
            "Επαναφορά Πόρων", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Emergency system methods
    private void startEmergency() {
        if (!emergencyActive) {
            emergencyActive = true;
            emergencyBtn.setEnabled(false);
            stopEmergencyBtn.setEnabled(true);
            
            // addLog("🚨 ΚΑΤΑΣΤΑΣΗ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ ΚΗΡΥΧΘΗΚΕ!");
            updateStatus("🚨 ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ - Δημιουργία πολλαπλών φωτιών");
            
            emergencyTimer = new Timer(3000, e -> createEmergencyFire());
            emergencyTimer.start();
            
            // Create initial fires
            for (int i = 0; i < 5; i++) {
                createEmergencyFire();
            }
        }
    }
    
    private void stopEmergency() {
        if (emergencyActive) {
            emergencyActive = false;
            emergencyBtn.setEnabled(true);
            stopEmergencyBtn.setEnabled(false);
            
            if (emergencyTimer != null) {
                emergencyTimer.stop();
                emergencyTimer = null;
            }
            
            // addLog("✅ Κατάσταση έκτακτης ανάγκης τερματίστηκε");
            updateStatus("🟢 Κανονική λειτουργία - Έκτακτη ανάγκη τερματίστηκε");
        }
    }
    
    private void createEmergencyFire() {
        Random rand = new Random();
        int x = 1 + rand.nextInt(GRID_SIZE);
        int y = 1 + rand.nextInt(GRID_SIZE);
        
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        if (existing == null || existing.type != CellType.FIRE) {
            startFireAt(x, y);
            // addLog("🚨 ΕΚΤΑΚΤΗ ΦΩΤΙΑ στη θέση (" + x + ", " + y + ")");
        }
    }
    
    // Enhanced Grid Panel Class with Combined Font Rendering
    private class GridPanel extends JPanel {
        
        public GridPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            
            // Enhanced rendering hints for emoji support
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            Rectangle visibleRect = getVisibleRect();
            int startX = Math.max(1, visibleRect.x / cellSize);
            int endX = Math.min(GRID_SIZE, (visibleRect.x + visibleRect.width) / cellSize + 1);
            int startY = Math.max(1, visibleRect.y / cellSize);
            int endY = Math.min(GRID_SIZE, (visibleRect.y + visibleRect.height) / cellSize + 1);
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    drawCell(g2d, x, y);
                }
            }
            
            if (cellSize >= 8) {
                drawGridLines(g2d, startX, endX, startY, endY);
            }
            
            g2d.dispose();
        }
        
        private void drawCell(Graphics2D g2d, int x, int y) {
            String key = x + "," + y;
            CellState state = cellStates.get(key);
            
            if (state == null) state = new CellState(CellType.EMPTY);
            
            int pixelX = (x - 1) * cellSize;
            int pixelY = (y - 1) * cellSize;
            
            g2d.setColor(getCellColor(state));
            g2d.fillRect(pixelX, pixelY, cellSize, cellSize);
            
            if (cellSize >= 8) {
                drawCellIcon(g2d, pixelX, pixelY, state);
            }
        }
        
        private void drawGridLines(Graphics2D g2d, int startX, int endX, int startY, int endY) {
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.setStroke(new BasicStroke(0.5f));
            
            for (int x = startX; x <= endX; x++) {
                int pixelX = (x - 1) * cellSize;
                g2d.drawLine(pixelX, (startY - 1) * cellSize, pixelX, endY * cellSize);
            }
            
            for (int y = startY; y <= endY; y++) {
                int pixelY = (y - 1) * cellSize;
                g2d.drawLine((startX - 1) * cellSize, pixelY, endX * cellSize, pixelY);
            }
        }
        
        private void drawCellIcon(Graphics2D g2d, int x, int y, CellState state) {
            String icon = getCellIcon(state);
            if (icon != null && !icon.isEmpty()) {
                g2d.setColor(Color.BLACK);
                
                // Use emoji font for better emoji rendering
                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, Math.min(cellSize - 1, 16)));
                FontMetrics fm = g2d.getFontMetrics();
                
                int iconX = x + (cellSize - fm.stringWidth(icon)) / 2;
                int iconY = y + (cellSize + fm.getAscent()) / 2;
                
                // Enhanced emoji rendering
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.drawString(icon, iconX, iconY);
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize);
        }
    }
    
    // Utility Methods
    private Color getCellColor(CellState state) {
        switch (state.type) {
            case TREE: return new Color(34, 139, 34);
            case FIRE: return new Color(255, Math.max(0, 255 - state.intensity * 20), 0);
            case BURNING_TREE: return new Color(255, 165 - state.intensity * 10, 0);
            case DESTROYED: return new Color(64, 64, 64);
            case WATER: return new Color(0, 191, 255);
            case FIREFIGHTER: return Color.RED;
            case AIRCRAFT: return Color.BLUE;
            case HELICOPTER: return Color.MAGENTA;
            case GROUND_CREW: return Color.ORANGE;
            default: return new Color(240, 240, 240);
        }
    }
    
    private String getCellIcon(CellState state) {
        switch (state.type) {
            case TREE: return "T";        // Αντί για 🌲
            case FIRE: return "F";        // Αντί για 🔥
            case BURNING_TREE: return "B"; // Αντί για 🔥
            case DESTROYED: return "X";    // Αντί για 🌫️
            case WATER: return "W";        // Αντί για 💧
            case FIREFIGHTER: return "R"; // Αντί για 🚒
            case AIRCRAFT: return "A";     // Αντί για ✈️
            case HELICOPTER: return "H";   // Αντί για 🚁
            case GROUND_CREW: return "G";  // Αντί για 👥
            default: return "";
        }
    }
    
    // Event Handlers
    private void handleGridClick(MouseEvent e) {
        int gridX = (e.getX() / cellSize) + 1;
        int gridY = (e.getY() / cellSize) + 1;
        
        if (gridX >= 1 && gridX <= GRID_SIZE && gridY >= 1 && gridY <= GRID_SIZE) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                startFireAt(gridX, gridY);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                placeTreeAt(gridX, gridY);
            }
        }
    }
    
    private void updateCoordinateDisplay(MouseEvent e) {
        int gridX = (e.getX() / cellSize) + 1;
        int gridY = (e.getY() / cellSize) + 1;
        
        if (gridX >= 1 && gridX <= GRID_SIZE && gridY >= 1 && gridY <= GRID_SIZE) {
            coordinatesLabel.setText("POS Συντεταγμένες: (" + gridX + ", " + gridY + ")");
        }
    }
    
    // Zoom and View Controls
    private void zoomIn() {
        if (cellSize < MAX_CELL_SIZE) {
            cellSize = Math.min(MAX_CELL_SIZE, cellSize + 1);
            updateZoom();
        }
    }
    
    private void zoomOut() {
        if (cellSize > MIN_CELL_SIZE) {
            cellSize = Math.max(MIN_CELL_SIZE, cellSize - 1);
            updateZoom();
        }
    }
    
    private void resetView() {
        cellSize = DEFAULT_CELL_SIZE;
        updateZoom();
        centerView();
    }
    
    private void centerView() {
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            Dimension viewSize = viewport.getExtentSize();
            Dimension gridSize = gridPanel.getPreferredSize();
            
            int x = Math.max(0, (gridSize.width - viewSize.width) / 2);
            int y = Math.max(0, (gridSize.height - viewSize.height) / 2);
            
            viewport.setViewPosition(new Point(x, y));
        });
    }
    
    private void updateZoom() {
        gridPanel.setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
        gridPanel.revalidate();
        gridPanel.repaint();
        
        zoomFactor = (double) cellSize / DEFAULT_CELL_SIZE;
        zoomLabel.setText("📐 Zoom: " + Math.round(zoomFactor * 100) + "%");
    }
    
    // Enhanced emoji rendering
    private void enhancedEmojiRendering() {
        // This can be called on startup or when changing settings
        System.setProperty("java.awt.useSystemAAFontSettings", "gasp");
        System.setProperty("swing.useSystemAAFontSettings", "on");
        System.setProperty("java2d.uiScale", "1.0");
        System.setProperty("awt.font.desktophints", "on");
    }
    
    // Initialize grid with empty state
    private void initializeGrid() {
        for (int x = 1; x <= GRID_SIZE; x++) {
            for (int y = 1; y <= GRID_SIZE; y++) {
                cellStates.put(x + "," + y, new CellState(CellType.EMPTY));
            }
        }
        updateStatsDisplay();
    }

    // Updates the statistics display labels
    private void updateStatsDisplay() {
        SwingUtilities.invokeLater(() -> {
            if (fireCountLabel != null) {
                fireCountLabel.setText("FIRE Ενεργές φωτιές: " + activeFires);
            }
        });
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(PANEL_COLOR);
        statusBar.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        statusLabel = createEmojiLabel("OK Σύστημα έτοιμο - Grid: 150x150", Font.BOLD, 12f);
        statusBar.add(statusLabel);
        
        return statusBar;
    }
    
    // Simulation Controls - FIXED: Only start fires manually
    private void simulateRandomFire() {
        Random rand = new Random();
        int x = 1 + rand.nextInt(GRID_SIZE);
        int y = 1 + rand.nextInt(GRID_SIZE);
        
        // Ensure we don't start fire on existing fire
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        
        int attempts = 0;
        while (existing != null && existing.type == CellType.FIRE && attempts < 10) {
            x = 1 + rand.nextInt(GRID_SIZE);
            y = 1 + rand.nextInt(GRID_SIZE);
            key = x + "," + y;
            existing = cellStates.get(key);
            attempts++;
        }
        
        startFireAt(x, y);
    }
    
    private void startFireAt(int x, int y) {
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        
        // Only start fire if location is empty or has a tree
        if (existing == null || existing.type == CellType.EMPTY || existing.type == CellType.TREE) {
            cellStates.put(key, new CellState(CellType.FIRE, 5));
            activeFireLocations.add(key);
            activeFires++;
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("🔥 Φωτιά ξεκίνησε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void placeTreeAt(int x, int y) {
        String key = x + "," + y;
        CellState current = cellStates.get(key);
        
        if (current == null || current.type == CellType.EMPTY || current.type == CellType.DESTROYED) {
            cellStates.put(key, new CellState(CellType.TREE));
            totalTrees++;
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("🌲 Δέντρο τοποθετήθηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    
    private void clearAllFires() {
        // Stop emergency if active
        if (emergencyActive) {
            stopEmergency();
        }
        
        // Clear all fires and restore previous states
        for (Map.Entry<String, CellState> entry : cellStates.entrySet()) {
            CellState state = entry.getValue();
            if (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE) {
                entry.setValue(new CellState(CellType.DESTROYED));
            }
        }
        
        activeFireLocations.clear();
        activeFires = 0;
        burningTrees = 0;
        
        // ΑΦΑΙΡΕΣΗ: Remove log message
        // addLog("🧹 Όλες οι φωτιές καθαρίστηκαν");
        gridPanel.repaint();
        updateStatsDisplay();
        updateStatus("🟢 Όλες οι φωτιές έχουν σβήσει");
    }
    
    private void extinguishFireAt(int x, int y) {
        String key = x + "," + y;
        CellState state = cellStates.get(key);
        
        if (state != null && (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE)) {
            cellStates.put(key, new CellState(CellType.DESTROYED));
            activeFireLocations.remove(key);
            activeFires = Math.max(0, activeFires - 1);
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("✅ Φωτιά σβήστηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void resetResources() {
        resourceConfig = new ResourceConfig(initialTrucks, initialAircraft, initialHelicopters, initialCrews);
        updateResourceStats();
        
        // ΑΦΑΙΡΕΣΗ: Remove log message
        // addLog("RESET Πόροι επαναφέρθηκαν στις προεπιλογές");
        
        JOptionPane.showMessageDialog(this, 
            "Οι πυροσβεστικοί πόροι επαναφέρθηκαν στις αρχικές τιμές!", 
            "Επαναφορά Πόρων", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Emergency system methods
    private void startEmergency() {
        if (!emergencyActive) {
            emergencyActive = true;
            emergencyBtn.setEnabled(false);
            stopEmergencyBtn.setEnabled(true);
            
            // addLog("🚨 ΚΑΤΑΣΤΑΣΗ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ ΚΗΡΥΧΘΗΚΕ!");
            updateStatus("🚨 ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ - Δημιουργία πολλαπλών φωτιών");
            
            emergencyTimer = new Timer(3000, e -> createEmergencyFire());
            emergencyTimer.start();
            
            // Create initial fires
            for (int i = 0; i < 5; i++) {
                createEmergencyFire();
            }
        }
    }
    
    private void stopEmergency() {
        if (emergencyActive) {
            emergencyActive = false;
            emergencyBtn.setEnabled(true);
            stopEmergencyBtn.setEnabled(false);
            
            if (emergencyTimer != null) {
                emergencyTimer.stop();
                emergencyTimer = null;
            }
            
            // addLog("✅ Κατάσταση έκτακτης ανάγκης τερματίστηκε");
            updateStatus("🟢 Κανονική λειτουργία - Έκτακτη ανάγκη τερματίστηκε");
        }
    }
    
    private void createEmergencyFire() {
        Random rand = new Random();
        int x = 1 + rand.nextInt(GRID_SIZE);
        int y = 1 + rand.nextInt(GRID_SIZE);
        
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        if (existing == null || existing.type != CellType.FIRE) {
            startFireAt(x, y);
            // addLog("🚨 ΕΚΤΑΚΤΗ ΦΩΤΙΑ στη θέση (" + x + ", " + y + ")");
        }
    }
    
    // Enhanced Grid Panel Class with Combined Font Rendering
    private class GridPanel extends JPanel {
        
        public GridPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            
            // Enhanced rendering hints for emoji support
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            Rectangle visibleRect = getVisibleRect();
            int startX = Math.max(1, visibleRect.x / cellSize);
            int endX = Math.min(GRID_SIZE, (visibleRect.x + visibleRect.width) / cellSize + 1);
            int startY = Math.max(1, visibleRect.y / cellSize);
            int endY = Math.min(GRID_SIZE, (visibleRect.y + visibleRect.height) / cellSize + 1);
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    drawCell(g2d, x, y);
                }
            }
            
            if (cellSize >= 8) {
                drawGridLines(g2d, startX, endX, startY, endY);
            }
            
            g2d.dispose();
        }
        
        private void drawCell(Graphics2D g2d, int x, int y) {
            String key = x + "," + y;
            CellState state = cellStates.get(key);
            
            if (state == null) state = new CellState(CellType.EMPTY);
            
            int pixelX = (x - 1) * cellSize;
            int pixelY = (y - 1) * cellSize;
            
            g2d.setColor(getCellColor(state));
            g2d.fillRect(pixelX, pixelY, cellSize, cellSize);
            
            if (cellSize >= 8) {
                drawCellIcon(g2d, pixelX, pixelY, state);
            }
        }
        
        private void drawGridLines(Graphics2D g2d, int startX, int endX, int startY, int endY) {
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.setStroke(new BasicStroke(0.5f));
            
            for (int x = startX; x <= endX; x++) {
                int pixelX = (x - 1) * cellSize;
                g2d.drawLine(pixelX, (startY - 1) * cellSize, pixelX, endY * cellSize);
            }
            
            for (int y = startY; y <= endY; y++) {
                int pixelY = (y - 1) * cellSize;
                g2d.drawLine((startX - 1) * cellSize, pixelY, endX * cellSize, pixelY);
            }
        }
        
        private void drawCellIcon(Graphics2D g2d, int x, int y, CellState state) {
            String icon = getCellIcon(state);
            if (icon != null && !icon.isEmpty()) {
                g2d.setColor(Color.BLACK);
                
                // Use emoji font for better emoji rendering
                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, Math.min(cellSize - 1, 16)));
                FontMetrics fm = g2d.getFontMetrics();
                
                int iconX = x + (cellSize - fm.stringWidth(icon)) / 2;
                int iconY = y + (cellSize + fm.getAscent()) / 2;
                
                // Enhanced emoji rendering
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.drawString(icon, iconX, iconY);
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize);
        }
    }
    
    // Utility Methods
    private Color getCellColor(CellState state) {
        switch (state.type) {
            case TREE: return new Color(34, 139, 34);
            case FIRE: return new Color(255, Math.max(0, 255 - state.intensity * 20), 0);
            case BURNING_TREE: return new Color(255, 165 - state.intensity * 10, 0);
            case DESTROYED: return new Color(64, 64, 64);
            case WATER: return new Color(0, 191, 255);
            case FIREFIGHTER: return Color.RED;
            case AIRCRAFT: return Color.BLUE;
            case HELICOPTER: return Color.MAGENTA;
            case GROUND_CREW: return Color.ORANGE;
            default: return new Color(240, 240, 240);
        }
    }
    
    private String getCellIcon(CellState state) {
        switch (state.type) {
            case TREE: return "T";        // Αντί για 🌲
            case FIRE: return "F";        // Αντί για 🔥
            case BURNING_TREE: return "B"; // Αντί για 🔥
            case DESTROYED: return "X";    // Αντί για 🌫️
            case WATER: return "W";        // Αντί για 💧
            case FIREFIGHTER: return "R"; // Αντί για 🚒
            case AIRCRAFT: return "A";     // Αντί για ✈️
            case HELICOPTER: return "H";   // Αντί για 🚁
            case GROUND_CREW: return "G";  // Αντί για 👥
            default: return "";
        }
    }
    
    // Event Handlers
    private void handleGridClick(MouseEvent e) {
        int gridX = (e.getX() / cellSize) + 1;
        int gridY = (e.getY() / cellSize) + 1;
        
        if (gridX >= 1 && gridX <= GRID_SIZE && gridY >= 1 && gridY <= GRID_SIZE) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                startFireAt(gridX, gridY);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                placeTreeAt(gridX, gridY);
            }
        }
    }
    
    private void updateCoordinateDisplay(MouseEvent e) {
        int gridX = (e.getX() / cellSize) + 1;
        int gridY = (e.getY() / cellSize) + 1;
        
        if (gridX >= 1 && gridX <= GRID_SIZE && gridY >= 1 && gridY <= GRID_SIZE) {
            coordinatesLabel.setText("POS Συντεταγμένες: (" + gridX + ", " + gridY + ")");
        }
    }
    
    // Zoom and View Controls
    private void zoomIn() {
        if (cellSize < MAX_CELL_SIZE) {
            cellSize = Math.min(MAX_CELL_SIZE, cellSize + 1);
            updateZoom();
        }
    }
    
    private void zoomOut() {
        if (cellSize > MIN_CELL_SIZE) {
            cellSize = Math.max(MIN_CELL_SIZE, cellSize - 1);
            updateZoom();
        }
    }
    
    private void resetView() {
        cellSize = DEFAULT_CELL_SIZE;
        updateZoom();
        centerView();
    }
    
    private void centerView() {
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            Dimension viewSize = viewport.getExtentSize();
            Dimension gridSize = gridPanel.getPreferredSize();
            
            int x = Math.max(0, (gridSize.width - viewSize.width) / 2);
            int y = Math.max(0, (gridSize.height - viewSize.height) / 2);
            
            viewport.setViewPosition(new Point(x, y));
        });
    }
    
    private void updateZoom() {
        gridPanel.setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
        gridPanel.revalidate();
        gridPanel.repaint();
        
        zoomFactor = (double) cellSize / DEFAULT_CELL_SIZE;
        zoomLabel.setText("📐 Zoom: " + Math.round(zoomFactor * 100) + "%");
    }
    
    // Enhanced emoji rendering
    private void enhancedEmojiRendering() {
        // This can be called on startup or when changing settings
        System.setProperty("java.awt.useSystemAAFontSettings", "gasp");
        System.setProperty("swing.useSystemAAFontSettings", "on");
        System.setProperty("java2d.uiScale", "1.0");
        System.setProperty("awt.font.desktophints", "on");
    }
    
    // Initialize grid with empty state
    private void initializeGrid() {
        for (int x = 1; x <= GRID_SIZE; x++) {
            for (int y = 1; y <= GRID_SIZE; y++) {
                cellStates.put(x + "," + y, new CellState(CellType.EMPTY));
            }
        }
        updateStatsDisplay();
    }

    // Updates the statistics display labels
    private void updateStatsDisplay() {
        SwingUtilities.invokeLater(() -> {
            if (fireCountLabel != null) {
                fireCountLabel.setText("FIRE Ενεργές φωτιές: " + activeFires);
            }
        });
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(PANEL_COLOR);
        statusBar.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        statusLabel = createEmojiLabel("OK Σύστημα έτοιμο - Grid: 150x150", Font.BOLD, 12f);
        statusBar.add(statusLabel);
        
        return statusBar;
    }
    
    // Simulation Controls - FIXED: Only start fires manually
    private void simulateRandomFire() {
        Random rand = new Random();
        int x = 1 + rand.nextInt(GRID_SIZE);
        int y = 1 + rand.nextInt(GRID_SIZE);
        
        // Ensure we don't start fire on existing fire
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        
        int attempts = 0;
        while (existing != null && existing.type == CellType.FIRE && attempts < 10) {
            x = 1 + rand.nextInt(GRID_SIZE);
            y = 1 + rand.nextInt(GRID_SIZE);
            key = x + "," + y;
            existing = cellStates.get(key);
            attempts++;
        }
        
        startFireAt(x, y);
    }
    
    private void startFireAt(int x, int y) {
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        
        // Only start fire if location is empty or has a tree
        if (existing == null || existing.type == CellType.EMPTY || existing.type == CellType.TREE) {
            cellStates.put(key, new CellState(CellType.FIRE, 5));
            activeFireLocations.add(key);
            activeFires++;
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("🔥 Φωτιά ξεκίνησε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void placeTreeAt(int x, int y) {
        String key = x + "," + y;
        CellState current = cellStates.get(key);
        
        if (current == null || current.type == CellType.EMPTY || current.type == CellType.DESTROYED) {
            cellStates.put(key, new CellState(CellType.TREE));
            totalTrees++;
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("🌲 Δέντρο τοποθετήθηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void clearAllFires() {
        // Stop emergency if active
        if (emergencyActive) {
            stopEmergency();
        }
        
        // Clear all fires and restore previous states
        for (Map.Entry<String, CellState> entry : cellStates.entrySet()) {
            CellState state = entry.getValue();
            if (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE) {
                entry.setValue(new CellState(CellType.DESTROYED));
            }
        }
        
        activeFireLocations.clear();
        activeFires = 0;
        burningTrees = 0;
        
        // ΑΦΑΙΡΕΣΗ: Remove log message
        // addLog("🧹 Όλες οι φωτιές καθαρίστηκαν");
        gridPanel.repaint();
        updateStatsDisplay();
        updateStatus("🟢 Όλες οι φωτιές έχουν σβήσει");
    }
    
    private void extinguishFireAt(int x, int y) {
        String key = x + "," + y;
        CellState state = cellStates.get(key);
        
        if (state != null && (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE)) {
            cellStates.put(key, new CellState(CellType.DESTROYED));
            activeFireLocations.remove(key);
            activeFires = Math.max(0, activeFires - 1);
            
            // ΑΦΑΙΡΕΣΗ: Remove log message
            // addLog("✅ Φωτιά σβήστηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    private void resetResources() {
        resourceConfig = new ResourceConfig(initialTrucks, initialAircraft, initialHelicopters, initialCrews);
        updateResourceStats();
        
        // ΑΦΑΙΡΕΣΗ: Remove log message
        // addLog("RESET Πόροι επαναφέρθηκαν στις προεπιλογές");
        
        JOptionPane.showMessageDialog(this, 
            "Οι πυροσβεστικοί πόροι επαναφέρθηκαν στις αρχικές τιμές!", 
            "Επαναφορά Πόρων", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Emergency system methods
    private void startEmergency() {
        if (!emergencyActive) {
            emergencyActive = true;
            emergencyBtn.setEnabled(false);
            stopEmergencyBtn.setEnabled(true);
            
            // addLog("🚨 ΚΑΤΑΣΤΑΣΗ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ ΚΗΡΥΧΘΗΚΕ!");
            updateStatus("🚨 ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ - Δημιουργία πολλαπλών φωτιών");
            
            emergencyTimer = new Timer(3000, e -> createEmergencyFire());
            emergencyTimer.start();
            
            // Create initial fires
            for (int i = 0; i < 5; i++) {
                createEmergencyFire();
            }
        }
    }
    
    private void stopEmergency() {
        if (emergencyActive) {
            emergencyActive = false;
            emergencyBtn.setEnabled(true);
            stopEmergencyBtn.setEnabled(false);
            
            if (emergencyTimer != null) {
                emergencyTimer.stop();
                emergencyTimer = null;
            }
            
            // addLog("✅ Κατάσταση έκτακτης ανάγκης τερματίστηκε");
            updateStatus("🟢 Κανονική λειτουργία - Έκτακτη ανάγκη τερματίστηκε");
        }
    }
    
    private void createEmergencyFire() {
        Random rand = new Random();
        int x = 1 + rand.nextInt(GRID_SIZE);
        int y = 1 + rand.nextInt(GRID_SIZE);
        
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        if (existing == null || existing.type != CellType.FIRE) {
            startFireAt(x, y);
            // addLog("🚨 ΕΚΤΑΚΤΗ ΦΩΤΙΑ στη θέση (" + x + ", " + y + ")");
        }
    }
    
    // Enhanced Grid Panel Class with Combined Font Rendering
    private class GridPanel extends JPanel {
        
        public GridPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            
            // Enhanced rendering hints for emoji support
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            Rectangle visibleRect = getVisibleRect();
            int startX = Math.max(1, visibleRect.x / cellSize);
            int endX = Math.min(GRID_SIZE, (visibleRect.x + visibleRect.width) / cellSize + 1);
            int startY = Math.max(1, visibleRect.y / cellSize);
            int endY = Math.min(GRID_SIZE, (visibleRect.y + visibleRect.height) / cellSize + 1);
            
            for (int x = startX; x <= endX; x++) {
                for (int y = startY; y <= endY; y++) {
                    drawCell(g2d, x, y);
                }
            }
            
            if (cellSize >= 8) {
                drawGridLines(g2d, startX, endX, startY, endY);
            }
            
            g2d.dispose();
        }
        
        private void drawCell(Graphics2D g2d, int x, int y) {
            String key = x + "," + y;
            CellState state = cellStates.get(key);
            
            if (state == null) state = new CellState(CellType.EMPTY);
            
            int pixelX = (x - 1) * cellSize;
            int pixelY = (y - 1) * cellSize;
            
            g2d.setColor(getCellColor(state));
            g2d.fillRect(pixelX, pixelY, cellSize, cellSize);
            
            if (cellSize >= 8) {
                drawCellIcon(g2d, pixelX, pixelY, state);
            }
        }
        
        private void drawGridLines(Graphics2D g2d, int startX, int endX, int startY, int endY) {
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.setStroke(new BasicStroke(0.5f));
            
            for (int x = startX; x <= endX; x++) {
                int pixelX = (x - 1) * cellSize;
                g2d.drawLine(pixelX, (startY - 1) * cellSize, pixelX, endY * cellSize);
            }
            
            for (int y = startY; y <= endY; y++) {
                int pixelY = (y - 1) * cellSize;
                g2d.drawLine((startX - 1) * cellSize, pixelY, endX * cellSize, pixelY);
            }
        }
        
        private void drawCellIcon(Graphics2D g2d, int x, int y, CellState state) {
            String icon = getCellIcon(state);
            if (icon != null && !icon.isEmpty()) {
                g2d.setColor(Color.BLACK);
                
                // Use emoji font for better emoji rendering
                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, Math.min(cellSize - 1, 16)));
                FontMetrics fm = g2d.getFontMetrics();
                
                int iconX = x + (cellSize - fm.stringWidth(icon)) / 2;
                int iconY = y + (cellSize + fm.getAscent()) / 2;
                
                // Enhanced emoji rendering
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.drawString(icon, iconX, iconY);
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize);
        }
    }
    
    // Utility Methods
    private Color getCellColor(CellState state) {
        switch (state.type) {
            case TREE: return new Color(34, 139, 34);
            case FIRE: return new Color(255, Math.max(0, 255 - state.intensity * 20), 0);
            case BURNING_TREE: return new Color(255, 165 - state.intensity * 10, 0);
            case DESTROYED: return new Color(64, 64, 64);
            case WATER: return new Color(0, 191, 255);
            case FIREFIGHTER: return Color.RED;
            case AIRCRAFT: return Color.BLUE;
            case HELICOPTER: return Color.MAGENTA;
            case GROUND_CREW: return Color.ORANGE;
            default: return new Color(240, 240, 240);
        }
    }
    
    private String getCellIcon(CellState state) {
        switch (state.type) {
            case TREE: return "T";        // Αντί για 🌲
            case FIRE: return "F";        // Αντί για 🔥
            case BURNING_TREE: return "B"; // Αντί για 🔥
            case DESTROYED: return "X";    // Αντί για 🌫️
            case WATER: return "W";        // Αντί για 💧
            case FIREFIGHTER: return "R"; // Αντί για 🚒
            case AIRCRAFT: return "A";     // Αντί για ✈️
            case HELICOPTER: return "H";   // Αντί για 🚁
            case GROUND_CREW: return "G";  // Αντί για 👥
            default: return "";
        }
    }
    
    // Event Handlers
    private void handleGridClick(MouseEvent e) {
        int gridX = (e.getX() / cellSize) + 1;
        int gridY = (e.getY() / cellSize) + 1;
        
        if (gridX >= 1 && gridX <= GRID_SIZE && gridY >= 1 && gridY <= GRID_SIZE) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                startFireAt(gridX, gridY);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                placeTreeAt(gridX, gridY);
            }
        }
    }
    
    private void updateCoordinateDisplay(MouseEvent e) {
        int gridX = (e.getX() / cellSize) + 1;
        int gridY = (e.getY() / cellSize) + 1;
        
        if (gridX >= 1 && gridX <= GRID_SIZE && gridY >= 1 && gridY <= GRID_SIZE) {
            coordinatesLabel.setText("POS Συντεταγμένες: (" + gridX + ", " + gridY + ")");
        }
    }
    
    // Zoom and View Controls
    private void zoomIn() {
        if (cellSize < MAX_CELL_SIZE) {
            cellSize = Math.min(MAX_CELL_SIZE, cellSize + 1);
            updateZoom();
        }
    }
    
    private void zoomOut() {
        if (cellSize > MIN_CELL_SIZE) {
            cellSize = Math.max(MIN_CELL_SIZE, cellSize - 1);
            updateZoom();
        }
    }
    
    private void resetView() {
        cellSize = DEFAULT_CELL_SIZE;
        updateZoom();
        centerView();
    }
    
    private void centerView() {
        SwingUtilities.invokeLater(() -> {
            JViewport viewport = scrollPane.getViewport();
            Dimension viewSize = viewport.getExtentSize();
            Dimension gridSize = gridPanel.getPreferredSize();
            
            int x = Math.max(0, (gridSize.width - viewSize.width) / 2);
            int y = Math.max(0, (gridSize.height - viewSize.height) / 2);
            
            viewport.setViewPosition(new Point(x, y));
        });
    }
    
    private void updateZoom() {
        gridPanel.setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
        gridPanel.revalidate();
        gridPanel.repaint();
        
        zoomFactor = (double) cellSize / DEFAULT_CELL_SIZE;
        zoomLabel.setText("📐 Zoom: " + Math.round(zoomFactor * 100) + "%");
    }
    
    // Enhanced emoji rendering
    private void enhancedEmojiRendering() {
        // This can be called on startup or when changing settings
        System.setProperty("java.awt.useSystemAAFontSettings", "gasp");
        System.setProperty("swing.useSystemAAFontSettings", "on");
        System.setProperty("java2d.uiScale", "1.0");
        System.setProperty("awt.font.desktophints", "on");
    }
    
    // Initialize grid with empty state
    private void initializeGrid() {
        for (int x = 1; x <= GRID_SIZE; x++) {
            for (int y = 1; y <= GRID_SIZE; y++) {
                cellStates.put(x + "," + y, new CellState(CellType.EMPTY));
            }
        }
        updateStatsDisplay();
    }

    // Updates the statistics display labels
    private void updateStatsDisplay() {
        SwingUtilities.invokeLater(() -> {
            if (fireCountLabel != null) {
                fireCountLabel.setText("FIRE Ενεργές φωτιές: " + activeFires);
            }
        });
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(PANEL_COLOR);
        statusBar.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        statusLabel = createEmojiLabel("OK Σύστημα έτοιμο - Grid: 150x150", Font.BOLD, 12f);
        statusBar.add(statusLabel);
        
        return statusBar;
    }
    
    // Simulation Controls - FIXED: Only start fires manually
    private void simulateRandomFire() {
        Random rand = new Random();
        int x = 1 + rand.nextInt(GRID_SIZE);