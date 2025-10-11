package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import gui.FireSimulationGUI; // Προσθήκη import

public class FireTruckAgent extends Agent {
    private boolean busy = false;
    private FireSimulationGUI gui;
    
    // Movement speed constants (in milliseconds per step)
    private static final int TRUCK_SPEED_DELAY = 150; // Medium speed: ~60 km/h
    
    @Override
    protected void setup() {
        // Αρχικοποίηση GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui = FireSimulationGUI.getInstance();
        });
        
        String message = getLocalName() + ": Πυροσβεστικό όχημα έτοιμο στη βάση.";
        System.out.println(message);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🚒 " + message);
            }
        });
        
        // Ειδοποίηση διαθεσιμότητας
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("TRUCK_AVAILABLE");
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("RESPOND_TO_FIRE") && !busy) {
                        String location = content.substring(content.indexOf("at") + 3);
                        respondToFire(location);
                        
                    } else if (content.startsWith("Fire at location") && !busy) {
                        String message = getLocalName() + ": Λήφθηκε μήνυμα: " + content;
                        System.out.println(message);
                        
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            if (gui != null) {
                                gui.addLog("📨 " + message);
                            }
                        });
                        
                        if (!busy) {
                            String workMessage = getLocalName() + ": Επιχειρεί κατάσβεση...";
                            System.out.println(workMessage);
                            
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                if (gui != null) {
                                    gui.addLog("🚒 " + workMessage);
                                }
                            });
                            
                            busy = true;
                            doWait(5000);
                            busy = false;
                            
                            String completeMessage = getLocalName() + ": Κατάσβεση ολοκληρώθηκε.";
                            System.out.println(completeMessage);
                            
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                if (gui != null) {
                                    gui.addLog("✅ " + completeMessage);
                                }
                            });
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
    
    private void respondToFire(String location) {
        busy = true;
        
        // Parse target coordinates from location string (format: "x,y")
        String[] coords = location.split(",");
        int targetX = Integer.parseInt(coords[0].trim());
        int targetY = Integer.parseInt(coords[1].trim());
        
        String moveMessage = getLocalName() + ": Μετακίνηση προς " + location;
        System.out.println(moveMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🚒 " + moveMessage);
                // Start from command center base
                int baseX = FireSimulationGUI.getCommandCenterX();
                int baseY = FireSimulationGUI.getCommandCenterY();
                gui.showTruckAt(baseX, baseY, getLocalName());
            }
        });
        
        // Simulate movement to target location
        moveToLocation(targetX, targetY);
        
        String arrivalMessage = getLocalName() + ": Άφιξη στη θέση " + location + " - Έναρξη κατάσβεσης";
        System.out.println(arrivalMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🎯 " + arrivalMessage);
            }
        });
        
        doWait(8000);
        
        String completeMessage = getLocalName() + ": Κατάσβεση ολοκληρώθηκε στη θέση " + location;
        System.out.println(completeMessage);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("✅ " + completeMessage);
            }
        });
        
        // Ειδοποίηση ολοκλήρωσης
        ACLMessage completed = new ACLMessage(ACLMessage.INFORM);
        completed.setContent("EXTINGUISH_COMPLETED at " + location);
        completed.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(completed);
        
        busy = false;
        sendAvailabilityStatus();
    }
    
    private void sendAvailabilityStatus() {
        String message = getLocalName() + ": Διαθέσιμο για νέα αποστολή";
        System.out.println(message);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🟢 " + message);
            }
        });
        
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("TRUCK_AVAILABLE");
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
    }
    
    private void moveToLocation(int targetX, int targetY) {
        // Get current position from command center base
        int currentX = FireSimulationGUI.getCommandCenterX();
        int currentY = FireSimulationGUI.getCommandCenterY();
        
        // Calculate movement steps
        int steps = Math.max(Math.abs(targetX - currentX), Math.abs(targetY - currentY));
        if (steps == 0) return;
        
        double deltaX = (double)(targetX - currentX) / steps;
        double deltaY = (double)(targetY - currentY) / steps;
        
        // Animate movement with truck speed
        for (int i = 0; i <= steps; i++) {
            final int newX = currentX + (int)(deltaX * i);
            final int newY = currentY + (int)(deltaY * i);
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.showTruckAt(newX, newY, getLocalName());
                }
            });
            
            doWait(TRUCK_SPEED_DELAY); // Medium speed for fire trucks
        }
    }
}
