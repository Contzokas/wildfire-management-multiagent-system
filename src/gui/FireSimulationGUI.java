package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
    private static final int MAX_CELL_SIZE = 50; // Increased from 20 to 50 for bigger display
    private static final int DEFAULT_CELL_SIZE = 4;
    
    // Rectangular cell dimensions: ~12.8px width × 7.2px height
    private static final int CELL_WIDTH_MULTIPLIER = 13;  // For rectangular cells: ~13px width
    private static final int CELL_HEIGHT_MULTIPLIER = 7;  // For rectangular cells: ~7px height
    
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
    private JTextArea logArea;
    private JPanel statsPanel;
    private Timer refreshTimer;
    
    // Grid data
    private Map<String, CellState> cellStates = new HashMap<>();
    private int cellSize = DEFAULT_CELL_SIZE;
    private double zoomFactor = 1.0;
    
    // Agent position tracking
    private Map<String, Point> agentPositions = new HashMap<>();
    private Map<String, CellType> agentTypes = new HashMap<>();
    
    // Agent movement tracking
    private Map<String, java.util.List<Point>> agentTrails = new HashMap<>();
    private Map<String, Long> lastMoveTime = new HashMap<>();
    private final int MAX_TRAIL_LENGTH = 10; // Maximum trail points to keep
    private boolean showTrails = true; // Toggle trail visibility
    
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
    
    // Fullscreen mode
    private boolean isFullscreenMode = false;
    private JPanel rightPanel;
    
    // Separate windows for multi-window mode
    private JFrame statsWindow;
    private JFrame weatherWindow;
    private JFrame controlsWindow;
    private JFrame logsWindow;
    private boolean multiWindowMode = true; // Start in multi-window mode
    
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
    
    // Initial resource configuration
    private static int initialTrucks = 4;
    private static int initialAircraft = 2;
    private static int initialHelicopters = 1;
    private static int initialCrews = 6;
    
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
    
    public FireSimulationGUI() {
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
            updateResourceStats(initialTrucks, initialTrucks, initialAircraft, initialAircraft, 
                              initialHelicopters, initialHelicopters, initialCrews, initialCrews, 0);
            
            Timer weatherUpdateTimer = new Timer(30000, e -> {
                simulateWeatherUpdate();
                addLog("🔄 Αυτόματη ενημέρωση καιρικών συνθηκών");
            });
            weatherUpdateTimer.start();
            
            Timer statsUpdateTimer = new Timer(10000, e -> {
                updateFireCount();
                updateResourceStats(initialTrucks, initialTrucks, initialAircraft, initialAircraft, 
                                  initialHelicopters, initialHelicopters, initialCrews, initialCrews, activeFires);
            });
            statsUpdateTimer.start();
            
            addLog("⚙️ Σύστημα έτοιμο - Πατήστε 'Νέα Φωτιά' για να ξεκινήσετε");
            addLog("🌤️ Καιρός: Ενημέρωση κάθε 30 δευτερόλεπτα");
            addLog("📊 Στατιστικά: Ενημέρωση κάθε 10 δευτερόλεπτα");
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
        if (text == null || text.isEmpty()) return; // ΠΡΟΣΘΗΚΗ: Έλλειπε το semicolon (;)
        
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
    
    // Static method to set initial resource configuration before GUI creation
    public static void setInitialResourceConfig(int trucks, int aircraft, int helicopters, int crews) {
        initialTrucks = trucks;
        initialAircraft = aircraft;
        initialHelicopters = helicopters;
        initialCrews = crews;
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
        
        UIManager.put("Menu.font", primaryUIFont); // ΑΛΛΑΓΗ
        UIManager.put("MenuItem.font", primaryUIFont); // ΑΛΛΑΓΗ
        UIManager.put("Menu.foreground", TEXT_COLOR);
        UIManager.put("MenuItem.foreground", TEXT_COLOR);
        UIManager.put("MenuBar.background", PANEL_COLOR);
        UIManager.put("Menu.background", PANEL_COLOR);
        UIManager.put("MenuItem.background", PANEL_COLOR);
        
        UIManager.put("TitledBorder.titleColor", TEXT_COLOR);
        UIManager.put("TitledBorder.border", new LineBorder(BORDER_COLOR));
    }
    
    private void initializeGUI() {
        System.out.println("🚀 GUI initialization started - multiWindowMode: " + multiWindowMode);
        setTitle("🔥 Προσομοίωση Δασικής Πυρκαγιάς - Πολυπρακτορικό Σύστημα (150x150)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        
        createGridPanel();
        createMenuBar();
        
        if (multiWindowMode) {
            // Full screen grid mode with separate windows
            setupFullscreenGridMode();
        } else {
            // Traditional split pane mode
            setupTraditionalMode();
        }
        
        try {
            setIconImage(createFireIcon());
        } catch (Exception e) {
            // Ignore icon errors
        }
        System.out.println("✅ GUI initialization completed");
    }
    
    private void setupFullscreenGridMode() {
        // Add only the grid and toolbar to main window
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.add(createGridToolbar(), BorderLayout.NORTH);
        
        // Add grid directly to top-left without centering
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
        
        // Add resize listener to recalculate grid size
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                calculateOptimalCellSize();
            }
        });
        
        // Calculate optimal cell size for full screen
        calculateOptimalCellSize();
        
        // Create separate windows
        createSeparateWindows();
    }
    
    private void setupTraditionalMode() {
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
        
        JMenuItem zoom100 = createEmojiMenuItem("💯 Zoom 100%", KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK);
        zoom100.addActionListener(e -> setZoom100());
        
        JMenuItem resetView = createEmojiMenuItem("HOME Επαναφορά Προβολής", KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK);
        resetView.addActionListener(e -> resetView());
        
        JMenuItem centerView = createEmojiMenuItem("TARGET Κεντράρισμα", KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        centerView.addActionListener(e -> centerView());
        
        viewMenu.add(zoomIn);
        viewMenu.add(zoomOut);
        viewMenu.add(zoom100);
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
        
        menuBar.add(viewMenu);
        menuBar.add(simMenu);
        
        setJMenuBar(menuBar);
    }
    
    private JMenu createEmojiMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(TEXT_COLOR);
        menu.setOpaque(true);
        menu.setBackground(PANEL_COLOR);
        
        // ΚΛΕΙΔΙ: Χρησιμοποίησε primaryUIFont αντί για combinedFont
        menu.setFont(primaryUIFont.deriveFont(Font.BOLD, 14f));
        return menu;
    }

    private JMenuItem createEmojiMenuItem(String text, int keyCode, int modifiers) {
        JMenuItem item = new JMenuItem(text);
        item.setForeground(TEXT_COLOR);
        item.setBackground(PANEL_COLOR);
        item.setOpaque(true);
        
        // ΚΛΕΙΔΙ: Χρησιμοποίησε primaryUIFont αντί για combinedFont
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
        
        // ΚΛΕΙΔΙ: Χρησιμοποίησε primaryUIFont αντί για combinedFont
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
    
    // ΚΛΕΙΔΙ: Χρησιμοποίησε primaryUIFont αντί για combinedFont
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
    
    // ΚΛΕΙΔΙ: Χρησιμοποίησε primaryUIFont αντί για combinedFont
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
        
        // ΚΛΕΙΔΙ: Χρησιμοποίησε primaryUIFont αντί για combinedFont
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
        
        // Hide scrollbars initially for full screen mode
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
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
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0)); // Reduced from 400 to 300
        rightPanel.setBackground(BACKGROUND_COLOR);
        
        rightPanel.add(createWeatherPanel(), BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(createStatsPanel());
        centerPanel.add(createLegendPanel());
        rightPanel.add(centerPanel, BorderLayout.CENTER);
        
        rightPanel.add(createLogPanel(), BorderLayout.SOUTH);
        
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
        
        JButton fullscreenBtn = createEmojiButton("Fullscreen", "Εναλλαγή Πλήρους Οθόνης", ACCENT_COLOR);
        fullscreenBtn.addActionListener(e -> toggleFullscreen());
        
        JButton fitScreenBtn = createEmojiButton("Windowed", "Προσαρμογή στην Οθόνη", WARNING_COLOR);
        fitScreenBtn.addActionListener(e -> calculateOptimalCellSize());
        
        JButton zoom100Btn = createEmojiButton("100%", "Zoom 100% (Πραγματικό Μέγεθος)", new Color(0x9C27B0));
        zoom100Btn.addActionListener(e -> setZoom100());
        
        zoomLabel = createEmojiLabel("ZOOM Zoom: 100%", Font.BOLD, 12f);
        coordinatesLabel = createEmojiLabel("POS Συντεταγμένες: (0, 0)", Font.PLAIN, 12f);
        
        toolbar.add(zoomInBtn);
        toolbar.add(zoomOutBtn);
        toolbar.add(resetBtn);
        toolbar.add(fullscreenBtn);
        toolbar.add(fitScreenBtn);
        toolbar.add(zoom100Btn);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(zoomLabel);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(coordinatesLabel);
        
        return toolbar;
    }
    
    private JPanel createControlPanel() {
    JPanel controlPanel = new JPanel(new GridBagLayout());
    controlPanel.setBackground(PANEL_COLOR);
    controlPanel.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(BORDER_COLOR, 1),
        new EmptyBorder(10, 10, 10, 10)
    ));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    JButton fireBtn = createEmojiControlButton("FIRE Νέα Φωτιά", "Δημιουργία νέας φωτιάς", DANGER_COLOR);
    fireBtn.addActionListener(e -> simulateRandomFire());
    
    emergencyBtn = createEmojiControlButton("ALERT Έκτακτη Ανάγκη", "Κήρυξη κατάστασης έκτακτης ανάγκης", WARNING_COLOR);
    emergencyBtn.addActionListener(e -> startEmergency());
    
    stopEmergencyBtn = createEmojiControlButton("STOP Τερματισμός Έκτακτης", "Τερματισμός κατάστασης έκτακτης ανάγκης", INFO_COLOR);
    stopEmergencyBtn.setEnabled(false);
    stopEmergencyBtn.addActionListener(e -> stopEmergency());
    
    JButton clearBtn = createEmojiControlButton("CLEAR Καθαρισμός", "Καθαρισμός όλων των φωτιών", ACCENT_COLOR);
    clearBtn.addActionListener(e -> clearAllFires());
    
    JButton weatherBtn = createEmojiControlButton("WEATHER Ενημέρωση Καιρού", "Ενημέρωση καιρικών συνθηκών", INFO_COLOR);
    weatherBtn.addActionListener(e -> simulateWeatherUpdate());
    
    JButton resetBtn = createEmojiControlButton("🔄 RESET Επαναφορά", "Επαναφορά προσομοίωσης στην αρχική κατάσταση", new Color(0x607D8B));
    resetBtn.addActionListener(e -> resetSimulation());
    
    JButton showAgentsBtn = createEmojiControlButton("🚁 SHOW Agents", "Εμφάνιση πυροσβεστικών μέσων στο grid", new Color(0x9C27B0));
    showAgentsBtn.addActionListener(e -> simulateAgentMovements());
    
    JButton toggleTrailsBtn = createEmojiControlButton("📍 TRAILS " + (showTrails ? "ON" : "OFF"), 
                                                      "Ενεργοποίηση/Απενεργοποίηση εμφάνισης διαδρομών", 
                                                      new Color(0x607D8B));
    toggleTrailsBtn.addActionListener(e -> {
        showTrails = !showTrails;
        toggleTrailsBtn.setText("📍 TRAILS " + (showTrails ? "ON" : "OFF"));
        gridPanel.repaint();
        addLog("📍 Trails " + (showTrails ? "enabled" : "disabled"));
    });
    
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
    controlPanel.add(fireBtn, gbc);
    
    gbc.gridy = 1;
    controlPanel.add(emergencyBtn, gbc);
    
    gbc.gridy = 2;
    controlPanel.add(stopEmergencyBtn, gbc);
    
    gbc.gridy = 3;
    controlPanel.add(clearBtn, gbc);
    
    gbc.gridy = 4;
    controlPanel.add(weatherBtn, gbc);
    
    gbc.gridy = 5;
    controlPanel.add(resetBtn, gbc);
    
    gbc.gridy = 6;
    controlPanel.add(showAgentsBtn, gbc);
    
    gbc.gridy = 7;
    controlPanel.add(toggleTrailsBtn, gbc);
    
    return controlPanel;
}
    
    private JPanel createWeatherPanel() {
        weatherPanel = new JPanel(new GridBagLayout());
        weatherPanel.setBackground(PANEL_COLOR);
        weatherPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = createEmojiLabel("WEATHER Καιρικές Συνθήκες", Font.BOLD, 16f);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        windSpeedLabel = createEmojiInfoLabel("WIND Άνεμος: 2.5 m/s");
        windDirectionLabel = createEmojiInfoLabel("DIR Διεύθυνση: ΒΑ");
        temperatureLabel = createEmojiInfoLabel("TEMP Θερμοκρασία: 28°C");
        humidityLabel = createEmojiInfoLabel("HUM Υγρασία: 45%");
        weatherConditionLabel = createEmojiInfoLabel("COND Κατάσταση: Αίθριος");
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        weatherPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1; gbc.gridy = 1;
        weatherPanel.add(windSpeedLabel, gbc);
        
        gbc.gridy = 2;
        weatherPanel.add(windDirectionLabel, gbc);
        
        gbc.gridy = 3;
        weatherPanel.add(temperatureLabel, gbc);
        
        gbc.gridy = 4;
        weatherPanel.add(humidityLabel, gbc);
        
        gbc.gridy = 5;
        weatherPanel.add(weatherConditionLabel, gbc);
        
        return weatherPanel;
    }
    
    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBackground(PANEL_COLOR);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = createEmojiLabel("STATS Πόροι Πυρόσβεσης", Font.BOLD, 16f);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        fireCountLabel = createEmojiInfoLabel("FIRE Ενεργές φωτιές: 0");
        trucksLabel = createEmojiInfoLabel("TRUCK Οχήματα: " + initialTrucks + "/" + initialTrucks);
        aircraftLabel = createEmojiInfoLabel("PLANE Αεροσκάφη: " + initialAircraft + "/" + initialAircraft);
        helicoptersLabel = createEmojiInfoLabel("HELI Ελικόπτερα: " + initialHelicopters + "/" + initialHelicopters);
        crewsLabel = createEmojiInfoLabel("CREW Ομάδες: " + initialCrews + "/" + initialCrews);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        statsPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1; gbc.gridy = 1;
        statsPanel.add(fireCountLabel, gbc);
        
        gbc.gridy = 2;
        statsPanel.add(trucksLabel, gbc);
        
        gbc.gridy = 3;
        statsPanel.add(aircraftLabel, gbc);
        
        gbc.gridy = 4;
        statsPanel.add(helicoptersLabel, gbc);
        
        gbc.gridy = 5;
        statsPanel.add(crewsLabel, gbc);
        
        return statsPanel;
    }
    
    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new GridBagLayout());
        legendPanel.setBackground(PANEL_COLOR);
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = createEmojiLabel("MAP Συμβολισμοί", Font.BOLD, 16f);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 5, 1, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        String[] legendItems = {
            "T Δέντρο", "F Φωτιά", "W Νερό", "R Πυροσβεστικό",
            "A Αεροσκάφος", "H Ελικόπτερο", "G Ομάδα", "X Σβησμένο"
        };
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        legendPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        for (int i = 0; i < legendItems.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            JLabel label = createEmojiInfoLabel(legendItems[i]);
            label.setFont(combinedFont.deriveFont(13f));
            legendPanel.add(label, gbc);
        }
        
        return legendPanel;
    }
    
   
    
    private JPanel createLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(PANEL_COLOR);
        logPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        JLabel titleLabel = createEmojiLabel("LOG Αρχείο Καταγραφής", Font.BOLD, 16f);
        
        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        logArea.setFont(combinedFont.deriveFont(12f));
        logArea.setBackground(BACKGROUND_COLOR);
        logArea.setForeground(TEXT_COLOR);
        logArea.setCaretColor(TEXT_COLOR);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScroll.setBackground(BACKGROUND_COLOR);
        logScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        
        JButton clearLogBtn = createEmojiButton("DEL", "Καθαρισμός αρχείου καταγραφής", DANGER_COLOR);
        clearLogBtn.addActionListener(e -> {
            logArea.setText("");
            addLog("LOG Αρχείο καταγραφής καθαρίστηκε");
        });
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PANEL_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(clearLogBtn, BorderLayout.EAST);
        
        logPanel.add(topPanel, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);
        
        return logPanel;
    }
    
    // Emergency system methods
    private void startEmergency() {
        if (!emergencyActive) {
            emergencyActive = true;
            emergencyBtn.setEnabled(false);
            stopEmergencyBtn.setEnabled(true);
            
            addLog("🚨 ΚΑΤΑΣΤΑΣΗ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ ΚΗΡΥΧΘΗΚΕ!");
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
            
            addLog("✅ Κατάσταση έκτακτης ανάγκης ΤΕΡΜΑΤΙΣΤΗΚΕ");
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
            addLog("🚨 ΕΚΤΑΚΤΗ ΦΩΤΙΑ στη θέση (" + x + ", " + y + ")");
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
            
            // Draw agent trails before drawing grid lines
            if (showTrails) {
                drawAgentTrails(g2d);
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
            
            // Use rectangular cells with custom dimensions: width ≈ 12.8px, height ≈ 7.2px
            int actualCellWidth = cellSize * CELL_WIDTH_MULTIPLIER / 4;  // 13 pixels when cellSize=4
            int actualCellHeight = cellSize * CELL_HEIGHT_MULTIPLIER / 4;  // 7 pixels when cellSize=4
            
            int pixelX = (x - 1) * actualCellWidth;
            int pixelY = (y - 1) * actualCellHeight;
            
            // Draw the base cell (terrain, fire, etc.)
            g2d.setColor(getCellColor(state));
            g2d.fillRect(pixelX, pixelY, actualCellWidth, actualCellHeight);
            
            if (Math.min(actualCellWidth, actualCellHeight) >= 8) {
                drawCellIcon(g2d, pixelX, pixelY, state, actualCellWidth, actualCellHeight);
            }
            
            // Check if any agents are at this position and draw them on top
            drawAgentsAtPosition(g2d, x, y, pixelX, pixelY, actualCellWidth, actualCellHeight);
        }
        
        private void drawAgentsAtPosition(Graphics2D g2d, int gridX, int gridY, int pixelX, int pixelY, int cellWidth, int cellHeight) {
            // Find all agents at this position
            java.util.List<String> agentsAtPosition = new java.util.ArrayList<>();
            
            for (Map.Entry<String, Point> entry : agentPositions.entrySet()) {
                Point pos = entry.getValue();
                if (pos.x == gridX && pos.y == gridY) {
                    agentsAtPosition.add(entry.getKey());
                }
            }
            
            // If there are agents at this position, draw them
            if (!agentsAtPosition.isEmpty()) {
                // Draw a semi-transparent overlay to indicate agent presence
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRect(pixelX + 1, pixelY + 1, cellWidth - 2, cellHeight - 2);
                
                // Draw agent icons
                int iconSize = Math.max(6, Math.min(cellWidth, cellHeight) - 2);
                
                for (int i = 0; i < Math.min(agentsAtPosition.size(), 4); i++) {
                    String agentName = agentsAtPosition.get(i);
                    CellType agentType = agentTypes.get(agentName);
                    
                    if (agentType != null) {
                        // Calculate position for multiple agents
                        int offsetX = (i % 2) * (cellWidth / 2);
                        int offsetY = (i / 2) * (cellHeight / 2);
                        
                        // Draw agent with type-specific color and icon
                        g2d.setColor(getAgentColor(agentType));
                        g2d.fillOval(pixelX + offsetX + 2, pixelY + offsetY + 2, iconSize, iconSize);
                        
                        // Draw agent icon
                        if (iconSize >= 8) {
                            g2d.setColor(Color.WHITE);
                            g2d.setFont(emojiFont.deriveFont(Font.BOLD, iconSize - 2));
                            FontMetrics fm = g2d.getFontMetrics();
                            String agentIcon = getAgentIcon(agentType);
                            
                            int iconX = pixelX + offsetX + (cellWidth / 2 - fm.stringWidth(agentIcon)) / 2;
                            int iconY = pixelY + offsetY + (cellHeight / 2 + fm.getAscent()) / 2;
                            
                            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                            g2d.drawString(agentIcon, iconX, iconY);
                        }
                    }
                }
            }
        }
        
        private Color getAgentColor(CellType agentType) {
            switch (agentType) {
                case FIREFIGHTER: return Color.RED;
                case AIRCRAFT: return Color.BLUE;
                case HELICOPTER: return Color.MAGENTA;
                case GROUND_CREW: return Color.ORANGE;
                default: return Color.GRAY;
            }
        }
        
        private String getAgentIcon(CellType agentType) {
            switch (agentType) {
                case FIREFIGHTER: return "🚒";
                case AIRCRAFT: return "✈️";
                case HELICOPTER: return "🚁";
                case GROUND_CREW: return "👥";
                default: return "?";
            }
        }
        
        private void drawAgentTrails(Graphics2D g2d) {
            // Use rectangular cell dimensions
            int actualCellWidth = cellSize * CELL_WIDTH_MULTIPLIER / 4;
            int actualCellHeight = cellSize * CELL_HEIGHT_MULTIPLIER / 4;
            
            for (Map.Entry<String, java.util.List<Point>> entry : agentTrails.entrySet()) {
                String agentName = entry.getKey();
                java.util.List<Point> trail = entry.getValue();
                CellType agentType = agentTypes.get(agentName);
                
                if (trail.size() > 1 && agentType != null) {
                    // Get agent color with transparency for trail
                    Color agentColor = getAgentColor(agentType);
                    Color trailColor = new Color(agentColor.getRed(), agentColor.getGreen(), agentColor.getBlue(), 100);
                    
                    g2d.setColor(trailColor);
                    g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    // Draw trail as connected lines
                    for (int i = 0; i < trail.size() - 1; i++) {
                        Point p1 = trail.get(i);
                        Point p2 = trail.get(i + 1);
                        
                        // Convert grid coordinates to pixel coordinates (center of cells)
                        int x1 = (p1.x - 1) * actualCellWidth + actualCellWidth / 2;
                        int y1 = (p1.y - 1) * actualCellHeight + actualCellHeight / 2;
                        int x2 = (p2.x - 1) * actualCellWidth + actualCellWidth / 2;
                        int y2 = (p2.y - 1) * actualCellHeight + actualCellHeight / 2;
                        
                        // Draw line with fade effect (older positions more transparent)
                        float alpha = (float)(i + 1) / trail.size() * 0.7f + 0.3f; // 0.3 to 1.0
                        Color lineColor = new Color(agentColor.getRed(), agentColor.getGreen(), agentColor.getBlue(), 
                                                   (int)(alpha * 150));
                        g2d.setColor(lineColor);
                        
                        g2d.drawLine(x1, y1, x2, y2);
                        
                        // Draw small circles at trail points
                        if (i < trail.size() - 2) { // Don't draw circle at last point (current position)
                            g2d.fillOval(x2 - 2, y2 - 2, 4, 4);
                        }
                    }
                    
                    // Connect last trail point to current position
                    Point currentPos = agentPositions.get(agentName);
                    if (currentPos != null && !trail.isEmpty()) {
                        Point lastTrail = trail.get(trail.size() - 1);
                        int x1 = (lastTrail.x - 1) * actualCellWidth + actualCellWidth / 2;
                        int y1 = (lastTrail.y - 1) * actualCellHeight + actualCellHeight / 2;
                        int x2 = (currentPos.x - 1) * actualCellWidth + actualCellWidth / 2;
                        int y2 = (currentPos.y - 1) * actualCellHeight + actualCellHeight / 2;
                        
                        g2d.setColor(agentColor);
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                }
            }
            
            // Reset stroke for other drawings
            g2d.setStroke(new BasicStroke(1.0f));
        }
        
        private void drawGridLines(Graphics2D g2d, int startX, int endX, int startY, int endY) {
            g2d.setColor(new Color(200, 200, 200, 100));
            g2d.setStroke(new BasicStroke(0.5f));
            
            // Use rectangular cell dimensions
            int actualCellWidth = cellSize * CELL_WIDTH_MULTIPLIER / 4;
            int actualCellHeight = cellSize * CELL_HEIGHT_MULTIPLIER / 4;
            
            for (int x = startX; x <= endX; x++) {
                int pixelX = (x - 1) * actualCellWidth;
                g2d.drawLine(pixelX, (startY - 1) * actualCellHeight, pixelX, endY * actualCellHeight);
            }
            
            for (int y = startY; y <= endY; y++) {
                int pixelY = (y - 1) * actualCellHeight;
                g2d.drawLine((startX - 1) * actualCellWidth, pixelY, endX * actualCellWidth, pixelY);
            }
        }
        
        private void drawCellIcon(Graphics2D g2d, int x, int y, CellState state, int cellWidth, int cellHeight) {
            String icon = getCellIcon(state);
            if (icon != null && !icon.isEmpty()) {
                g2d.setColor(Color.BLACK);
                
                // Use emoji font for better emoji rendering
                int fontSize = Math.min(Math.min(cellWidth - 1, cellHeight - 1), 16);
                g2d.setFont(emojiFont.deriveFont(Font.PLAIN, fontSize));
                FontMetrics fm = g2d.getFontMetrics();
                
                int iconX = x + (cellWidth - fm.stringWidth(icon)) / 2;
                int iconY = y + (cellHeight + fm.getAscent()) / 2;
                
                // Enhanced emoji rendering
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.drawString(icon, iconX, iconY);
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            // Use rectangular cell dimensions for grid size calculation
            int actualCellWidth = cellSize * CELL_WIDTH_MULTIPLIER / 4;
            int actualCellHeight = cellSize * CELL_HEIGHT_MULTIPLIER / 4;
            
            int gridPixelWidth = GRID_SIZE * actualCellWidth;
            int gridPixelHeight = GRID_SIZE * actualCellHeight;
            
            return new Dimension(gridPixelWidth, gridPixelHeight);
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
    
    private void setZoom100() {
        cellSize = DEFAULT_CELL_SIZE; // This gives exactly 100% zoom
        updateZoom();
        centerView();
        System.out.println("🎯 Zoom set to exactly 100% (cellSize: " + cellSize + ")");
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
        // Use rectangular cell dimensions for grid size calculation
        int actualCellWidth = cellSize * CELL_WIDTH_MULTIPLIER / 4;
        int actualCellHeight = cellSize * CELL_HEIGHT_MULTIPLIER / 4;
        
        int gridPixelWidth = GRID_SIZE * actualCellWidth;
        int gridPixelHeight = GRID_SIZE * actualCellHeight;
        
        gridPanel.setPreferredSize(new Dimension(gridPixelWidth, gridPixelHeight));
        gridPanel.revalidate();
        gridPanel.repaint();
        
        zoomFactor = (double) cellSize / DEFAULT_CELL_SIZE;
        zoomLabel.setText("📐 Zoom: " + Math.round(zoomFactor * 100) + "%");
    }
    
    private void toggleFullscreen() {
        if (multiWindowMode) {
            // In multi-window mode, toggle window decorations instead
            if (isFullscreenMode) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                dispose();
                setUndecorated(false);
                setVisible(true);
                isFullscreenMode = false;
            } else {
                setExtendedState(JFrame.NORMAL);
                dispose();
                setUndecorated(true);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setVisible(true);
                isFullscreenMode = true;
            }
        } else {
            // Traditional mode - toggle right panel
            isFullscreenMode = !isFullscreenMode;
            
            if (rightPanel != null) {
                if (isFullscreenMode) {
                    rightPanel.setVisible(false);
                } else {
                    rightPanel.setVisible(true);
                }
            }
        }
        
        // Force layout update
        revalidate();
        repaint();
    }
    
    private void calculateOptimalCellSize() {
        System.out.println("🔧 Starting cellSize calculation...");
        
        // Use SwingUtilities.invokeLater for UI thread safety with delay
        Timer timer = new Timer(100, e -> {
            System.out.println("⏰ Timer triggered for cellSize calculation");
            
            // Get the content pane size
            Dimension contentSize = getContentPane().getSize();
            System.out.println("📏 Content pane size: " + contentSize.width + "x" + contentSize.height);
            
            // If content pane size is not available, use screen size
            if (contentSize.width <= 0 || contentSize.height <= 0) {
                System.out.println("⚠️ Content pane not ready, using screen size");
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                contentSize = new Dimension(screenSize.width, screenSize.height - 50); // Only remove taskbar
                System.out.println("�️ Using screen size: " + contentSize.width + "x" + contentSize.height);
            }
            
            // Calculate available space with minimal margins for maximum screen usage
            int toolbarHeight = 40; // Reduced toolbar estimate
            int statusBarHeight = 25; // Reduced status bar estimate  
            int minMargin = 5; // Minimal margin
            
            int availableWidth = contentSize.width - (minMargin * 2);
            int availableHeight = contentSize.height - toolbarHeight - statusBarHeight - minMargin;
            
            System.out.println("📐 Available space: " + availableWidth + "x" + availableHeight);
            
            // Calculate maximum cell size that fits
            int maxCellWidth = availableWidth / GRID_SIZE;
            int maxCellHeight = availableHeight / GRID_SIZE;
            System.out.println("� Max cell dimensions: " + maxCellWidth + "x" + maxCellHeight);
            
            // Take the smaller dimension to ensure square cells fit
            int newCellSize = Math.min(maxCellWidth, maxCellHeight);
            System.out.println("⚖️ Initial cellSize: " + newCellSize);
            
            // Ensure cellSize is within bounds but prioritize large size
            newCellSize = Math.max(MIN_CELL_SIZE, Math.min(newCellSize, MAX_CELL_SIZE));
            System.out.println("✅ Final cellSize (after bounds check): " + newCellSize);
            
            // Try to squeeze more pixels if possible by testing larger sizes
            while (newCellSize < MAX_CELL_SIZE) {
                int testSize = newCellSize + 1;
                int testWidth = testSize * GRID_SIZE;
                int testHeight = testSize * GRID_SIZE;
                
                if (testWidth <= availableWidth && testHeight <= availableHeight) {
                    newCellSize = testSize;
                    System.out.println("� Increased cellSize to: " + newCellSize);
                } else {
                    break;
                }
            }
            
            // Apply the new cell size
            cellSize = newCellSize;
            if (gridPanel != null) {
                // Update the grid panel size and repaint
                gridPanel.setPreferredSize(new Dimension(GRID_SIZE * cellSize, GRID_SIZE * cellSize));
                gridPanel.revalidate();
                gridPanel.repaint();
                
                // Update zoom factor and label
                zoomFactor = (double) cellSize / DEFAULT_CELL_SIZE;
                if (zoomLabel != null) {
                    zoomLabel.setText("📐 Zoom: " + Math.round(zoomFactor * 100) + "%");
                }
                
                // Center the view to make grid appear in center of screen
                SwingUtilities.invokeLater(() -> {
                    Timer centerTimer = new Timer(200, evt -> {
                        centerView();
                        ((Timer) evt.getSource()).stop();
                    });
                    centerTimer.setRepeats(false);
                    centerTimer.start();
                });
            }
            
            // Calculate screen usage percentage
            int totalGridWidth = cellSize * GRID_SIZE;
            int totalGridHeight = cellSize * GRID_SIZE;
            double widthUsage = (double) totalGridWidth / contentSize.width * 100;
            double heightUsage = (double) totalGridHeight / contentSize.height * 100;
            
            System.out.println("📊 🚀 MAXIMIZED cellSize: " + cellSize + "px");
            System.out.println("🎯 Grid dimensions: " + totalGridWidth + "x" + totalGridHeight + " pixels");
            System.out.println("📺 Screen usage: " + String.format("%.1f%% width, %.1f%% height", widthUsage, heightUsage));
                       
            ((Timer) e.getSource()).stop(); // Stop the timer
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void createSeparateWindows() {
        SwingUtilities.invokeLater(() -> {
            createStatsWindow();
            createWeatherWindow();
            createControlsWindow();
            createLogsWindow();
            
            // Position windows on screen
            positionSeparateWindows();
        });
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
            
            addLog("🔥 Φωτιά ξεκίνησε στη θέση (" + x + ", " + y + ")");
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
            
            addLog("🌲 Δέντρο τοποθετήθηκε στη θέση (" + x + ", " + y + ")");
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
        
        addLog("🧹 Όλες οι φωτιές καθαρίστηκαν");
        gridPanel.repaint();
        updateStatsDisplay();
        updateStatus("🟢 Όλες οι φωτιές έχουν σβήσει");
    }
    
    private void resetSimulation() {
        // Stop emergency if active
        if (emergencyActive) {
            stopEmergency();
        }
        
        // Clear all cell states and restore to EMPTY
        cellStates.clear();
        activeFireLocations.clear();
        activeFires = 0;
        burningTrees = 0;
        totalTrees = 0;
        destroyedTrees = 0;
        
        // Reset view to default
        cellSize = DEFAULT_CELL_SIZE;
        updateZoom();
        
        // Clear agent positions
        agentPositions.clear();
        agentTypes.clear();
        agentTrails.clear();
        lastMoveTime.clear();
        
        addLog("🔄 Προσομοίωση επαναφέρθηκε στην αρχική κατάσταση");
        gridPanel.repaint();
        updateStatsDisplay();
        updateStatus("🟢 Σύστημα επαναφέρθηκε - Έτοιμο για νέα προσομοίωση");
    }
    
    // Agent position tracking methods
    public void updateAgentPosition(String agentName, int x, int y, CellType agentType) {
        Point oldPosition = agentPositions.get(agentName);
        Point newPosition = new Point(x, y);
        
        // Update current position and type
        agentPositions.put(agentName, newPosition);
        agentTypes.put(agentName, agentType);
        
        // Track movement trail
        if (oldPosition != null && !oldPosition.equals(newPosition)) {
            // Initialize trail if it doesn't exist
            agentTrails.computeIfAbsent(agentName, k -> new java.util.ArrayList<>());
            
            // Add old position to trail
            java.util.List<Point> trail = agentTrails.get(agentName);
            trail.add(new Point(oldPosition.x, oldPosition.y));
            
            // Keep trail length under control
            if (trail.size() > MAX_TRAIL_LENGTH) {
                trail.remove(0);
            }
            
            // Update movement timestamp
            lastMoveTime.put(agentName, System.currentTimeMillis());
            
            addLog("📍 " + agentName + " moved to (" + x + ", " + y + ")");
        }
        
        // Repaint the grid to show the updated positions
        if (gridPanel != null) {
            gridPanel.repaint();
        }
    }
    
    public void removeAgent(String agentName) {
        agentPositions.remove(agentName);
        agentTypes.remove(agentName);
        agentTrails.remove(agentName);
        lastMoveTime.remove(agentName);
        
        if (gridPanel != null) {
            gridPanel.repaint();
        }
    }
    
    public Point getAgentPosition(String agentName) {
        return agentPositions.get(agentName);
    }
    
    // Method to simulate agent movements for testing
    public void simulateAgentMovements() {
        // Initialize agents at starting positions
        updateAgentPosition("truck1", 10, 10, CellType.FIREFIGHTER);
        updateAgentPosition("truck2", 140, 140, CellType.FIREFIGHTER);
        updateAgentPosition("aircraft1", 75, 20, CellType.AIRCRAFT);
        updateAgentPosition("helicopter1", 20, 75, CellType.HELICOPTER);
        updateAgentPosition("crew1", 50, 50, CellType.GROUND_CREW);
        updateAgentPosition("crew2", 100, 100, CellType.GROUND_CREW);
        
        // Start movement simulation
        startMovementSimulation();
        
        addLog("🚁 Agents διαθέσιμοι στο grid με tracking");
    }
    
    private Timer movementTimer;
    private int moveStep = 0;
    
    private void startMovementSimulation() {
        if (movementTimer != null) {
            movementTimer.stop();
        }
        
        moveStep = 0;
        movementTimer = new Timer(1000, e -> {
            moveStep++;
            
            // Simulate realistic movements - agents moving towards fires or patrolling
            simulateRealisticMovement("truck1", 10, 10, 50, 50);
            simulateRealisticMovement("truck2", 140, 140, 75, 75);
            simulateRealisticMovement("aircraft1", 75, 20, 100, 130);
            simulateRealisticMovement("helicopter1", 20, 75, 130, 25);
            simulateRealisticMovement("crew1", 50, 50, 25, 25);
            simulateRealisticMovement("crew2", 100, 100, 125, 125);
            
            // Stop after 20 movements
            if (moveStep >= 20) {
                movementTimer.stop();
                addLog("📍 Movement simulation completed");
            }
        });
        
        movementTimer.start();
    }
    
    private void simulateRealisticMovement(String agentName, int startX, int startY, int targetX, int targetY) {
        Point currentPos = agentPositions.get(agentName);
        CellType agentType = agentTypes.get(agentName);
        
        if (currentPos != null && agentType != null) {
            // Calculate next step towards target with some randomness
            int currentX = currentPos.x;
            int currentY = currentPos.y;
            
            // Move towards target with some patrol behavior
            int deltaX = Integer.compare(targetX, currentX);
            int deltaY = Integer.compare(targetY, currentY);
            
            // Add some randomness for realistic movement
            if (Math.random() < 0.3) { // 30% chance of random movement
                deltaX = (int)(Math.random() * 3) - 1; // -1, 0, or 1
                deltaY = (int)(Math.random() * 3) - 1;
            }
            
            int newX = Math.max(1, Math.min(GRID_SIZE, currentX + deltaX));
            int newY = Math.max(1, Math.min(GRID_SIZE, currentY + deltaY));
            
            // Only update if position actually changed
            if (newX != currentX || newY != currentY) {
                updateAgentPosition(agentName, newX, newY, agentType);
            }
        }
    }
    
    // FIXED: Proper fire extinguishing method
    private void extinguishFireAt(int x, int y) {
        String key = x + "," + y;
        CellState state = cellStates.get(key);
        
        if (state != null && (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE)) {
            cellStates.put(key, new CellState(CellType.DESTROYED));
            activeFireLocations.remove(key);
            activeFires = Math.max(0, activeFires - 1);
            
            addLog("✅ Φωτιά σβήστηκε στη θέση (" + x + ", " + y + ")");
            gridPanel.repaint();
            updateStatsDisplay();
        }
    }
    
    // Update fire count from actual grid state
    private void updateFireCount() {
        int count = 0;
        activeFireLocations.clear();
        
        for (Map.Entry<String, CellState> entry : cellStates.entrySet()) {
            CellState state = entry.getValue();
            if (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE) {
                activeFireLocations.add(entry.getKey());
                count++;
            }
        }
        
        activeFires = count;
    }
    
    // Weather simulation method
    public void simulateWeatherUpdate() {
        SwingUtilities.invokeLater(() -> {
            Random rand = new Random();
            
            double windSpeed = 1.0 + rand.nextDouble() * 4.0;
            String[] directions = {"Β", "ΒΑ", "Α", "ΝΑ", "Ν", "ΝΔ", "Δ", "ΒΔ"};
            String windDirection = directions[rand.nextInt(directions.length)];
            int temperature = 20 + rand.nextInt(20);
            int humidity = 20 + rand.nextInt(60);
            
            String[] conditions = {"Αίθριος", "Αίθριος-Συννεφιά", "Συννεφιά", "Βροχή"};
            String condition = conditions[rand.nextInt(conditions.length)];
            
            windSpeedLabel.setText("WIND Άνεμος: " + String.format("%.1f", windSpeed) + " m/s");
            windDirectionLabel.setText("DIR Διεύθυνση: " + windDirection);
            temperatureLabel.setText("TEMP Θερμοκρασία: " + temperature + "°C");
            humidityLabel.setText("HUM Υγρασία: " + humidity + "%");
            weatherConditionLabel.setText("COND Κατάσταση: " + condition);
        });
    }
    
    public void updateResourceStats(int totalTrucks, int availableTrucks, 
                                  int totalAircraft, int availableAircraft,
                                  int totalHelicopters, int availableHelicopters,
                                  int totalCrews, int availableCrews, int fires) {
        SwingUtilities.invokeLater(() -> {
            if (fireCountLabel != null) {
                fireCountLabel.setText("FIRE Ενεργές φωτιές: " + fires);
            }
            if (trucksLabel != null) {
                trucksLabel.setText("TRUCK Οχήματα: " + availableTrucks + "/" + totalTrucks);
            }
            if (aircraftLabel != null) {
                aircraftLabel.setText("PLANE Αεροσκάφη: " + availableAircraft + "/" + totalAircraft);
            }
            if (helicoptersLabel != null) {
                helicoptersLabel.setText("HELI Ελικόπτερα: " + availableHelicopters + "/" + totalHelicopters);
            }
            if (crewsLabel != null) {
                crewsLabel.setText("CREW Ομάδες: " + availableCrews + "/" + totalCrews);
            }
        });
    }
    
    public void showEmergencyDialog() {
        startEmergency();
    }
    
    // Agent interface methods
    public void showFireAt(int x, int y, int intensity) {
        updateCell(x, y, "FIRE", intensity);
        
        String key = x + "," + y;
        if (cellStates.get(key) == null || cellStates.get(key).type != CellType.FIRE) {
            addLog("🔥 ΝΕΑ ΦΩΤΙΑ στη θέση (" + x + "," + y + ") - Ένταση: " + intensity);
            updateStatus("🔴 ΕΝΕΡΓΗ ΠΥΡΚΑΙΑ - Θέση: (" + x + "," + y + ")");
        }
        
        cellStates.put(key, new CellState(CellType.FIRE, intensity));
        if (!activeFireLocations.contains(key)) {
            activeFireLocations.add(key);
            activeFires++;
        }
        gridPanel.repaint();
    }
    
    public void updateCell(int x, int y, String state, int intensity) {
        if (x < 1 || x > GRID_SIZE || y < 1 || y > GRID_SIZE) return;
        
        String key = x + "," + y;
        CellType cellType;
        
        switch (state.toUpperCase()) {
            case "FIRE": cellType = CellType.FIRE; break;
            case "TREE": cellType = CellType.TREE; break;
            case "WATER": cellType = CellType.WATER; break;
            case "EXTINGUISHED": cellType = CellType.DESTROYED; break;
            default: cellType = CellType.EMPTY;
        }
        
        CellState oldState = cellStates.get(key);
        cellStates.put(key, new CellState(cellType, intensity));
        
        // Update fire tracking
        if (cellType == CellType.FIRE && !activeFireLocations.contains(key)) {
            activeFireLocations.add(key);
            activeFires++;
        } else if (cellType != CellType.FIRE && activeFireLocations.contains(key)) {
            activeFireLocations.remove(key);
            activeFires = Math.max(0, activeFires - 1);
        }
        
        gridPanel.repaint();
    }
    
    public void updateCell(int x, int y, String state) {
        updateCell(x, y, state, 0);
    }
    
    // FIXED: Proper water drop and fire extinguishing
    public void showWaterDropAt(int x, int y, String agentName) {
        String key = x + "," + y;
        CellState existing = cellStates.get(key);
        
        // Extinguish fire if present
        if (existing != null && (existing.type == CellType.FIRE || existing.type == CellType.BURNING_TREE)) {
            extinguishFireAt(x, y);
            addLog("💧 " + agentName + " έσβησε φωτιά στη θέση (" + x + "," + y + ")");
        } else {
            // Show water drop temporarily
            cellStates.put(key, new CellState(CellType.WATER));
            addLog("💧 " + agentName + " ρίψη νερού στη θέση (" + x + "," + y + ")");
            
            SwingUtilities.invokeLater(() -> {
                gridPanel.repaint();
            });
            
            // Remove water visualization after 3 seconds
            Timer timer = new Timer(3000, e -> {
                cellStates.put(key, new CellState(CellType.EMPTY));
                SwingUtilities.invokeLater(() -> gridPanel.repaint());
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
    
    public void showTruckAt(int x, int y, String truckName) {
        showAgentAt(x, y, "FIRETRUCK", truckName);
    }
    
    public void showHelicopterAt(int x, int y, String helicopterName) {
        showAgentAt(x, y, "HELICOPTER", helicopterName);
    }
    
    public void showAgentAt(int x, int y, String agentType, String agentName) {
        if (x < 1 || x > GRID_SIZE || y < 1 || y > GRID_SIZE) return;
        
        String key = x + "," + y;
        CellType type;
        
        switch (agentType.toUpperCase()) {
            case "TRUCK":
            case "FIRETRUCK": type = CellType.FIREFIGHTER; break;
            case "AIRCRAFT": type = CellType.AIRCRAFT; break;
            case "HELICOPTER": type = CellType.HELICOPTER; break;
            case "CREW":
            case "GROUNDCREW": type = CellType.GROUND_CREW; break;
            default: return;
        }
        
        CellState oldState = cellStates.get(key);
        cellStates.put(key, new CellState(type));
        addLog("📍 " + agentName + " στη θέση (" + x + "," + y + ")");
        gridPanel.repaint();
        
        // Remove agent visualization after 3 seconds
        Timer timer = new Timer(3000, e -> {
            if (oldState != null) {
                cellStates.put(key, oldState);
            } else {
                cellStates.put(key, new CellState(CellType.EMPTY));
            }
            gridPanel.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    public void showAgentAt(int x, int y, String agentName) {
        // Determine agent type from name
        String agentType = "TRUCK";
        if (agentName.contains("aircraft")) agentType = "AIRCRAFT";
        else if (agentName.contains("helicopter")) agentType = "HELICOPTER";
        else if (agentName.contains("crew")) agentType = "CREW";
        
        showAgentAt(x, y, agentType, agentName);
    }
    
    public void showExtinguishedAt(int x, int y) {
        String key = x + "," + y;
        cellStates.put(key, new CellState(CellType.DESTROYED));
        addLog("✅ Κατάσβεση ολοκληρώθηκε στη θέση (" + x + "," + y + ")");
        
        SwingUtilities.invokeLater(() -> {
            gridPanel.repaint();
        });
    }
    
    public void showAllExtinguished() {
        SwingUtilities.invokeLater(() -> {
            addLog("🌊 Όλες οι φωτιές έχουν σβήσει!");
            updateStatus("🟢 Όλες οι εστίες κατασβέστηκαν");
            
            // Clear all fire states
            for (Map.Entry<String, CellState> entry : cellStates.entrySet()) {
                CellState state = entry.getValue();
                if (state.type == CellType.FIRE || state.type == CellType.BURNING_TREE) {
                    entry.setValue(new CellState(CellType.DESTROYED));
                }
            }
            
            activeFireLocations.clear();
            activeFires = 0;
            burningTrees = 0;
            gridPanel.repaint();
            updateStatsDisplay();
        });
    }
    
    public void showEmergencyDeclared() {
        SwingUtilities.invokeLater(() -> {
            addLog("🚨 ΚΑΤΑΣΤΑΣΗ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ ΚΗΡΥΧΘΗΚΕ!");
            updateStatus("🚨 ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ - Δημιουργία πολλαπλών φωτιών");
        });
    }
    
    public void showEmergencyEnded() {
        SwingUtilities.invokeLater(() -> {
            addLog("✅ Κατάσταση έκτακτης ανάγκης τερματίστηκε");
            updateStatus("🟢 Κανονική λειτουργία - Έκτακτη ανάγκη τερματίστηκε");
        });
    }
    
    // Status updates for UI
    public void updateStatus(String text) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
        });
    }
    
    // Log methods
    public void addLog(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        logArea.append("[" + timestamp + "] " + message + "\n");
        
        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
    
    private void startRefreshTimer() {
        refreshTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Refresh logic here if needed
            }
        });
        refreshTimer.start();
    }
    
    // ========== SEPARATE WINDOWS METHODS ==========
    
    private void createStatsWindow() {
        statsWindow = new JFrame("📊 Στατιστικά Προσομοίωσης");
        statsWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        statsWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Don't close individually, close all together
            }
        });
        
        statsWindow.setBackground(BACKGROUND_COLOR);
        statsWindow.add(createStatsPanel(), BorderLayout.CENTER);
        statsWindow.pack();
        statsWindow.setAlwaysOnTop(true);
        statsWindow.setVisible(true);
    }
    
    private void createWeatherWindow() {
        weatherWindow = new JFrame("🌤️ Καιρικές Συνθήκες");
        weatherWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        weatherWindow.setBackground(BACKGROUND_COLOR);
        weatherWindow.add(createWeatherPanel(), BorderLayout.CENTER);
        weatherWindow.pack();
        weatherWindow.setAlwaysOnTop(true);
        weatherWindow.setVisible(true);
    }
    
    private void createControlsWindow() {
        controlsWindow = new JFrame("🎮 Χειριστήρια");
        controlsWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        controlsWindow.setBackground(BACKGROUND_COLOR);
        controlsWindow.add(createControlPanel(), BorderLayout.CENTER);
        controlsWindow.pack();
        controlsWindow.setAlwaysOnTop(true);
        controlsWindow.setVisible(true);
    }
    
    private void createLogsWindow() {
        logsWindow = new JFrame("📝 Αρχείο Καταγραφής");
        logsWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        logsWindow.setBackground(BACKGROUND_COLOR);
        logsWindow.add(createLogPanel(), BorderLayout.CENTER);
        logsWindow.pack();
        logsWindow.setAlwaysOnTop(true);
        logsWindow.setVisible(true);
    }
    
    private void positionSeparateWindows() {
        Dimension screenSize = getToolkit().getScreenSize();
        
        // Position stats window - top right
        statsWindow.setLocation(screenSize.width - statsWindow.getWidth() - 10, 10);
        
        // Position weather window - top right, below stats
        weatherWindow.setLocation(screenSize.width - weatherWindow.getWidth() - 10, 
                                 statsWindow.getY() + statsWindow.getHeight() + 10);
        
        // Position controls window - bottom right
        controlsWindow.setLocation(screenSize.width - controlsWindow.getWidth() - 10, 
                                  screenSize.height - controlsWindow.getHeight() - 100);
        
        // Position logs window - bottom left
        logsWindow.setLocation(10, screenSize.height - logsWindow.getHeight() - 100);
    }
    
    @Override
    public void dispose() {
        // Close all separate windows when main window closes
        if (multiWindowMode) {
            if (statsWindow != null) statsWindow.dispose();
            if (weatherWindow != null) weatherWindow.dispose();
            if (controlsWindow != null) controlsWindow.dispose();
            if (logsWindow != null) logsWindow.dispose();
        }
        super.dispose();
    }
}
