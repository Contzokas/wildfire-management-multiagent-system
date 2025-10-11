package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import gui.FireSimulationGUI; // Προσθήκη import

public class AircraftAgent extends Agent {
    private boolean deployed = false;
    private int waterCapacity = 1000;
    private int currentWater = 1000;
    private FireSimulationGUI gui;
    
    // Movement speed constants (in milliseconds per step)
    private static final int AIRCRAFT_SPEED_DELAY = 50; // Fastest speed: ~300 km/h
    
    @Override
    protected void setup() {
        // Αρχικοποίηση GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui = FireSimulationGUI.getInstance();
        });
        
        String message = getLocalName() + ": Πυροσβεστικό αεροπλάνο έτοιμο στη βάση (Χωρητικότητα: " + waterCapacity + "L)";
        System.out.println(message);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("✈️ " + message);
            }
        });
        
        // Ειδοποίηση διαθεσιμότητας
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("AIRCRAFT_AVAILABLE capacity:" + currentWater);
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("DEPLOY_AIRCRAFT") && !deployed) {
                        String target = content.substring(content.indexOf("to") + 3);
                        deployToFire(target);
                        
                    } else if (content.startsWith("REFILL_REQUEST")) {
                        if (currentWater < waterCapacity * 0.3) {
                            refillWater();
                        }
                    } else if (content.equals("STATUS_REQUEST")) {
                        sendAvailabilityStatus();
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    private void deployToFire(String location) {
        deployed = true;
        
        // Parse target coordinates
        String[] coords = location.replace("(", "").replace(")", "").split(",");
        int targetX = Integer.parseInt(coords[0]);
        int targetY = Integer.parseInt(coords[1]);
        
        String takeoffMessage = getLocalName() + ": Απογείωση! Πτήση προς " + location;
        System.out.println(takeoffMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("✈️ " + takeoffMessage);
            }
        });
        
        // Fly to target location
        flyToLocation(targetX, targetY);
        
        String arrivalMessage = getLocalName() + ": Άφιξη στην περιοχή " + location;
        System.out.println(arrivalMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🎯 " + arrivalMessage);
            }
        });
        
        performWaterDrop(location);
        
        String returnMessage = getLocalName() + ": Επιστροφή στη βάση";
        System.out.println(returnMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🔄 " + returnMessage);
            }
        });
        
        // Fly back to base
        int airfieldX = FireSimulationGUI.getCommandCenterX();
        int airfieldY = Math.max(5, FireSimulationGUI.getCommandCenterY() - 10);
        flyToLocation(airfieldX, airfieldY);
        
        deployed = false;
        
        if (currentWater < waterCapacity * 0.5) {
            refillWater();
        }
        
        sendAvailabilityStatus();
    }
    
    private void performWaterDrop(String location) {
        String[] coords = location.replace("(", "").replace(")", "").split(",");
        int centerX = Integer.parseInt(coords[0]);
        int centerY = Integer.parseInt(coords[1]);
        
        int waterUsed = Math.min(currentWater, 300);
        currentWater -= waterUsed;
        
        String dropMessage = getLocalName() + ": ΡΙΨΗ ΝΕΡΟΥ στην περιοχή (" + centerX + "," + centerY + ") - " + 
                           waterUsed + "L (Απομένουν: " + currentWater + "L)";
        System.out.println(dropMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("💧 " + dropMessage);
                gui.showWaterDropAt(centerX, centerY, getLocalName());
            }
        });
        
        // Κατάσβεση σε ευρύτερη περιοχή
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;
                
                ACLMessage extinguish = new ACLMessage(ACLMessage.REQUEST);
                extinguish.setContent("AERIAL_EXTINGUISH at (" + x + "," + y + ") effectiveness:0.8");
                extinguish.addReceiver(new jade.core.AID("fire1", jade.core.AID.ISLOCALNAME));
                
                String treeName = findTreeAtPosition(x, y);
                if (treeName != null) {
                    extinguish.addReceiver(new jade.core.AID(treeName, jade.core.AID.ISLOCALNAME));
                }
                
                send(extinguish);
            }
        }
        
        ACLMessage report = new ACLMessage(ACLMessage.INFORM);
        report.setContent("WATER_DROP_COMPLETED at " + location + " water_used:" + waterUsed);
        report.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(report);
    }
    
    private void refillWater() {
        String refillMessage = getLocalName() + ": Ανεφοδιασμός νερού...";
        System.out.println(refillMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🔄 " + refillMessage);
            }
        });
        
        doWait(10000);
        currentWater = waterCapacity;
        
        String completeMessage = getLocalName() + ": Ανεφοδιασμός ολοκληρώθηκε - " + currentWater + "L";
        System.out.println(completeMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("✅ " + completeMessage);
            }
        });
    }
    
    private void sendAvailabilityStatus() {
        String message = getLocalName() + ": Διαθέσιμο (Νερό: " + currentWater + "L)";
        System.out.println(message);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🟢 " + message);
            }
        });
        
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("AIRCRAFT_AVAILABLE capacity:" + currentWater);
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
    }
    
    private String findTreeAtPosition(int nx, int ny) {
        if (nx == 1 && ny == 1) return "tree1";
        if (nx == 1 && ny == 2) return "tree2";
        if (nx == 2 && ny == 1) return "tree3";
        if (nx == 2 && ny == 2) return "tree4";
        if (nx == 3 && ny == 1) return "tree5";
        if (nx == 3 && ny == 2) return "tree6";
        if (nx == 1 && ny == 3) return "tree7";
        if (nx == 2 && ny == 3) return "tree8";
        if (nx == 3 && ny == 3) return "tree9";
        if (nx == 1 && ny == 4) return "tree10";
        if (nx == 2 && ny == 4) return "tree11";
        if (nx == 3 && ny == 4) return "tree12";
        return null;
    }
    
    private void flyToLocation(int targetX, int targetY) {
        // Aircraft start from airfield near command center
        int currentX = FireSimulationGUI.getCommandCenterX();
        int currentY = Math.max(5, FireSimulationGUI.getCommandCenterY() - 10);
        
        // Calculate flight path
        int steps = Math.max(Math.abs(targetX - currentX), Math.abs(targetY - currentY));
        if (steps == 0) return;
        
        double deltaX = (double)(targetX - currentX) / steps;
        double deltaY = (double)(targetY - currentY) / steps;
        
        // Animate flight
        for (int i = 0; i <= steps; i++) {
            final int newX = currentX + (int)(deltaX * i);
            final int newY = currentY + (int)(deltaY * i);
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.showAgentAt(newX, newY, "AIRCRAFT", getLocalName());
                }
            });
            
            doWait(AIRCRAFT_SPEED_DELAY); // Fastest movement for aircraft
        }
    }
}