package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import gui.FireSimulationGUI;

public class HelicopterAgent extends Agent {
    private boolean deployed = false;
    private int waterCapacity = 500; // μικρότερη χωρητικότητα από αεροπλάνο
    private int currentWater = 500;
    private FireSimulationGUI gui;
    
    // Movement speed constants (in milliseconds per step)
    private static final int HELICOPTER_SPEED_DELAY = 80; // Fast speed: ~180 km/h
    
    @Override
    protected void setup() {
        // Αρχικοποίηση GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui = FireSimulationGUI.getInstance();
        });
        
        String message = getLocalName() + ": 🚁 Πυροσβεστικό ελικόπτερο έτοιμο (Χωρητικότητα: " + waterCapacity + "L)";
        System.out.println(message);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🚁 " + message);
            }
        });
        
        // Ειδοποίηση διαθεσιμότητας
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("HELICOPTER_AVAILABLE");
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("DEPLOY_HELICOPTER") && !deployed) {
                        String target = content.substring(content.indexOf("to") + 3);
                        deployToFire(target);
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
        
        String takeoffMessage = getLocalName() + ": 🚁 Απογείωση προς " + location;
        System.out.println(takeoffMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🚁 " + takeoffMessage);
            }
        });
        
        // Fly to target location
        flyToLocation(targetX, targetY);
        
        String arrivalMessage = getLocalName() + ": 🎯 Στοχευμένη ρίψη νερού στη θέση " + location;
        System.out.println(arrivalMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🎯 " + arrivalMessage);
            }
        });
        
        performPrecisionWaterDrop(location);
        
        String returnMessage = getLocalName() + ": 🔄 Επιστροφή για ανεφοδιασμό";
        System.out.println(returnMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🔄 " + returnMessage);
            }
        });
        
        // Fly back to command center helipad
        int helipadX = FireSimulationGUI.getCommandCenterX();
        int helipadY = Math.max(5, FireSimulationGUI.getCommandCenterY() - 5);
        flyToLocation(helipadX, helipadY);
        
        refillWater();
        deployed = false;
        
        sendAvailabilityStatus();
    }
    
    private void performPrecisionWaterDrop(String location) {
        String[] coords = location.replace("(", "").replace(")", "").split(",");
        int centerX = Integer.parseInt(coords[0]);
        int centerY = Integer.parseInt(coords[1]);
        
        int waterUsed = Math.min(currentWater, 200); // 200L ανά ρίψη
        currentWater -= waterUsed;
        
        String dropMessage = getLocalName() + ": 💧 ΡΙΨΗ " + waterUsed + "L νερού (Απομένουν: " + currentWater + "L)";
        System.out.println(dropMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("💧 " + dropMessage);
                gui.showWaterDropAt(centerX, centerY, getLocalName());
            }
        });
        
        // Στοχευμένη κατάσβεση
        ACLMessage extinguish = new ACLMessage(ACLMessage.REQUEST);
        extinguish.setContent("HELICOPTER_EXTINGUISH at " + location + " effectiveness:0.9");
        extinguish.addReceiver(new jade.core.AID("fire1", jade.core.AID.ISLOCALNAME));
        send(extinguish);
        
        // Ειδοποίηση κέντρου ελέγχου
        ACLMessage report = new ACLMessage(ACLMessage.INFORM);
        report.setContent("HELICOPTER_DROP_COMPLETED at " + location);
        report.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(report);
    }
    
    private void refillWater() {
        // Show helicopter at helipad during refill
        int helipadX = FireSimulationGUI.getCommandCenterX();
        int helipadY = Math.max(5, FireSimulationGUI.getCommandCenterY() - 5);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.showAgentAt(helipadX, helipadY, "HELICOPTER", getLocalName());
            }
        });
        
        String refillMessage = getLocalName() + ": 🔄 Γρήγορος ανεφοδιασμός...";
        System.out.println(refillMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🔄 " + refillMessage);
            }
        });
        
        doWait(5000); // γρηγορότερος ανεφοδιασμός
        currentWater = waterCapacity;
        
        String completeMessage = getLocalName() + ": ✅ Ανεφοδιασμός ολοκληρώθηκε";
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
        available.setContent("HELICOPTER_AVAILABLE");
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
    }
    
    private void flyToLocation(int targetX, int targetY) {
        // Helicopter starts from command center helipad
        int currentX = FireSimulationGUI.getCommandCenterX();
        int currentY = Math.max(5, FireSimulationGUI.getCommandCenterY() - 5); // Nearby helipad
        
        // Calculate flight path
        int steps = Math.max(Math.abs(targetX - currentX), Math.abs(targetY - currentY));
        if (steps == 0) return;
        
        double deltaX = (double)(targetX - currentX) / steps;
        double deltaY = (double)(targetY - currentY) / steps;
        
        // Animate flight with helicopter speed
        for (int i = 0; i <= steps; i++) {
            final int newX = currentX + (int)(deltaX * i);
            final int newY = currentY + (int)(deltaY * i);
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.showAgentAt(newX, newY, "HELICOPTER", getLocalName());
                }
            });
            
            doWait(HELICOPTER_SPEED_DELAY); // Fast movement for helicopters
        }
    }
}