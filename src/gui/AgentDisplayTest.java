package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Simple test class to verify agent display functionality
 * without needing full JADE infrastructure
 */
public class AgentDisplayTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set encoding for proper Greek text display
                System.setProperty("file.encoding", "UTF-8");
                System.setProperty("user.language", "el");
                System.setProperty("user.country", "GR");
                
                // Create and configure the GUI
                FireSimulationGUI gui = new FireSimulationGUI();
                gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gui.setTitle("Agent Display Test - Wildfire Management System");
                gui.setVisible(true);
                
                // Test agent display after GUI is visible
                SwingUtilities.invokeLater(() -> {
                    testAgentDisplay(gui);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error starting GUI test: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private static void testAgentDisplay(FireSimulationGUI gui) {
        System.out.println("🧪 Starting Agent Display Test...");
        gui.addLog("🧪 AGENT DISPLAY TEST - Starting verification");
        
        // Test 1: Show all agent types at different positions
        System.out.println("Test 1: Placing different agent types on grid");
        gui.addLog("🔍 Test 1: Τοποθέτηση διαφορετικών τύπων agents");
        
        // Fire Truck (Red) 🚒
        gui.updateAgentPosition("truck_test", 20, 20, FireSimulationGUI.CellType.FIREFIGHTER);
        gui.addLog("🚒 Fire Truck at (20,20) - Red color");
        
        // Aircraft (Blue) ✈️
        gui.updateAgentPosition("aircraft_test", 80, 20, FireSimulationGUI.CellType.AIRCRAFT);
        gui.addLog("✈️ Aircraft at (80,20) - Blue color");
        
        // Helicopter (Magenta) 🚁
        gui.updateAgentPosition("helicopter_test", 140, 20, FireSimulationGUI.CellType.HELICOPTER);
        gui.addLog("🚁 Helicopter at (140,20) - Magenta color");
        
        // Ground Crew (Orange) 👥
        gui.updateAgentPosition("crew_test", 20, 80, FireSimulationGUI.CellType.GROUND_CREW);
        gui.addLog("👥 Ground Crew at (20,80) - Orange color");
        
        // Test 2: Multiple agents at same location
        System.out.println("Test 2: Multiple agents at same location");
        gui.addLog("🔍 Test 2: Πολλαπλοί agents στην ίδια θέση");
        
        gui.updateAgentPosition("truck1", 75, 75, FireSimulationGUI.CellType.FIREFIGHTER);
        gui.updateAgentPosition("crew1", 75, 75, FireSimulationGUI.CellType.GROUND_CREW);
        gui.updateAgentPosition("helicopter1", 75, 75, FireSimulationGUI.CellType.HELICOPTER);
        gui.addLog("🔄 Multiple agents at (75,75) - Should show overlay");
        
        // Test 3: Command Center visibility
        System.out.println("Test 3: Command Center position");
        gui.addLog("🔍 Test 3: Θέση κέντρου επιχειρήσεων");
        
        int cmdX = FireSimulationGUI.getCommandCenterX();
        int cmdY = FireSimulationGUI.getCommandCenterY();
        gui.addLog("🏢 Command Center at (" + cmdX + "," + cmdY + ") - Dark blue");
        
        // Test 4: Use built-in agent movement simulation
        Timer testTimer = new Timer(3000, e -> {
            System.out.println("Test 4: Built-in agent movement simulation");
            gui.addLog("🔍 Test 4: Προσομοίωση κίνησης agents");
            gui.simulateAgentMovements();
        });
        testTimer.setRepeats(false);
        testTimer.start();
        
        gui.addLog("✅ Agent Display Test completed - Check visual representation");
        System.out.println("🎯 Agent Display Test completed!");
        System.out.println("Expected colors:");
        System.out.println("🚒 Fire Trucks: RED");
        System.out.println("✈️ Aircraft: BLUE"); 
        System.out.println("🚁 Helicopters: MAGENTA");
        System.out.println("👥 Ground Crews: ORANGE");
        System.out.println("🏢 Command Center: DARK BLUE");
    }
}