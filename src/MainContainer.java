import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import utils.GridManager;
import gui.FireSimulationGUI;
import java.util.Random;

public class MainContainer {
    private static final int GRID_SIZE = 150;
    
    public static void main(String[] args) {
        // Parse command line arguments for custom resource configuration
        int numTrucks = 4, numAircraft = 2, numHelicopters = 1, numCrews = 6;
        
        if (args.length >= 5 && "custom".equals(args[0])) {
            try {
                numTrucks = Integer.parseInt(args[1]);
                numAircraft = Integer.parseInt(args[2]);
                numHelicopters = Integer.parseInt(args[3]);
                numCrews = Integer.parseInt(args[4]);
                
                System.out.println("🚁 CUSTOM RESOURCE CONFIGURATION:");
                System.out.println("├─ Πυροσβεστικά Οχήματα: " + numTrucks);
                System.out.println("├─ Αεροσκάφη: " + numAircraft);
                System.out.println("├─ Ελικόπτερα: " + numHelicopters);
                System.out.println("└─ Επίγειες Ομάδες: " + numCrews);
                
            } catch (NumberFormatException e) {
                System.err.println("❌ Invalid resource numbers, using defaults");
                System.err.println("❌ Error details: " + e.getMessage());
            }
        } else {
            System.out.println("🔧 Using default resource configuration");
        }
        
        // Set resource configuration in GUI before creating instance
        FireSimulationGUI.setInitialResourceConfig(numTrucks, numAircraft, numHelicopters, numCrews);
        
        try {
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true");
            p.setParameter(Profile.MAIN_HOST, "localhost");
            p.setParameter(Profile.MAIN_PORT, "1099");
            
            AgentContainer container = rt.createMainContainer(p);
            
            // Determine number of trees to create
            int numTrees = 1000; // Default
            if (args.length > 0) {
                if ("full".equals(args[0])) {
                    numTrees = GRID_SIZE * GRID_SIZE; // All 22500 trees
                    System.out.println("Creating FULL 150x150 grid with " + numTrees + " trees...");
                } else {
                    try {
                        numTrees = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number, using default 1000 trees");
                    }
                }
            }
            
            System.out.println("Starting JADE with " + numTrees + " trees in 150x150 grid...");
            
            // Create core agents
            createCoreAgents(container, numTrucks, numAircraft, numHelicopters, numCrews);
            
            // Create tree agents (sample or full)
            if (numTrees == GRID_SIZE * GRID_SIZE) {
                createFullGrid(container);
            } else {
                createSampleGrid(container, numTrees);
            }
            
            // Create some fire agents for testing
            createTestFires(container);
            
            System.out.println("All agents created successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void createCoreAgents(AgentContainer container, int numTrucks, int numAircraft, int numHelicopters, int numCrews) throws Exception {
        // Fire Control Agent
        AgentController fireControl = container.createNewAgent("firecontrol", 
            "agents.FireControlAgent", null);
        fireControl.start();
        
        // Weather Agent
        AgentController weather = container.createNewAgent("weather", 
            "agents.WeatherAgent", null);
        weather.start();
        
        // Firefighting resources
        for (int i = 1; i <= numTrucks; i++) {
            AgentController truck = container.createNewAgent("truck" + i, 
                "agents.FireTruckAgent", null);
            truck.start();
        }
        
        for (int i = 1; i <= numAircraft; i++) {
            AgentController aircraft = container.createNewAgent("aircraft" + i, 
                "agents.AircraftAgent", null);
            aircraft.start();
        }
        
        for (int i = 1; i <= numHelicopters; i++) {
            AgentController helicopter = container.createNewAgent("helicopter" + i, 
                "agents.HelicopterAgent", null);
            helicopter.start();
        }
        
        // Ground crews
        String[] specialties = {"suppression", "prevention", "mop-up"};
        for (int i = 1; i <= numCrews; i++) {
            String specialty = specialties[(i-1) % specialties.length];
            AgentController crew = container.createNewAgent("crew" + i, 
                "agents.GroundCrewAgent", new Object[]{specialty});
            crew.start();
        }
        
        // Emergency response
        AgentController emergency = container.createNewAgent("emergency1", 
            "agents.EmergencyResponseAgent", null);
        emergency.start();
    }
    
    private static void createFullGrid(AgentContainer container) throws Exception {
        System.out.println("Creating full 150x150 grid (this may take several minutes)...");
        
        for (int x = 1; x <= GRID_SIZE; x++) {
            for (int y = 1; y <= GRID_SIZE; y++) {
                String agentName = GridManager.generateTreeAgentName(x, y);
                AgentController tree = container.createNewAgent(agentName, 
                    "agents.TreeAgent", new Object[]{x, y});
                tree.start();
            }
            
            if (x % 10 == 0) {
                System.out.println("Created trees for row " + x + "/" + GRID_SIZE);
            }
        }
    }
    
    private static void createSampleGrid(AgentContainer container, int numTrees) throws Exception {
        System.out.println("Creating sample grid with " + numTrees + " randomly distributed trees...");
        Random rand = new Random();
        
        for (int i = 0; i < numTrees; i++) {
            int x = 1 + rand.nextInt(GRID_SIZE);
            int y = 1 + rand.nextInt(GRID_SIZE);
            
            String agentName = GridManager.generateTreeAgentName(x, y);
            
            // Check if agent already exists (avoid duplicates)
            try {
                AgentController tree = container.createNewAgent(agentName, 
                    "agents.TreeAgent", new Object[]{x, y});
                tree.start();
            } catch (Exception e) {
                // Agent might already exist, continue
                i--; // Try again with different coordinates
            }
            
            if ((i + 1) % 100 == 0) {
                System.out.println("Created " + (i + 1) + "/" + numTrees + " trees");
            }
        }
    }
    
    private static void createTestFires(AgentContainer container) throws Exception {
        // Create a few fires for testing
        int[][] firePositions = {{25, 25}, {75, 75}, {125, 125}, {50, 100}, {100, 50}};
        
        for (int i = 0; i < firePositions.length; i++) {
            int x = firePositions[i][0];
            int y = firePositions[i][1];
            
            AgentController fire = container.createNewAgent("fire" + (i + 1), 
                "agents.FireAgent", new Object[]{x, y});
            fire.start();
        }
        
        System.out.println("Created " + firePositions.length + " test fire agents");
    }
}
