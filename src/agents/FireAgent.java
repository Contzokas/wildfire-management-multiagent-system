package agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.*;
import java.nio.charset.StandardCharsets;
import gui.FireSimulationGUI;

public class FireAgent extends Agent {
    private int intensity = 5;
    private Set<String> fireLocations = new HashSet<>();
    private Set<String> extinguishedLocations = new HashSet<>();
    private int currentX = 1, currentY = 1;
    private boolean fullyExtinguished = false;
    private int extinguishedTime = 0;
    private FireSimulationGUI gui; // Προσθήκη GUI reference
    
    @Override
    protected void setup() {
        // ΠΡΟΣΘΗΚΗ: Ρύθμιση encoding
        System.setProperty("file.encoding", "UTF-8");
        
        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            try {
                currentX = Integer.parseInt(args[0].toString());
                currentY = Integer.parseInt(args[1].toString());
            } catch (NumberFormatException e) {
                currentX = 1; currentY = 1;
            }
        }
        
        // Αρχικοποίηση GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui = FireSimulationGUI.getInstance();
        });
        
        fireLocations.add(currentX + "," + currentY);
        String message = getLocalName() + ": Εστία φωτιάς ενεργή στη θέση (" + currentX + "," + currentY + ")";
        System.out.println(message);
        if (gui != null) {
            gui.addLog("🔥 " + message);
        }

        // ΠΡΟΣΘΗΚΗ: CyclicBehaviour για διαχείριση μηνυμάτων κατάσβεσης
        addBehaviour(new jade.core.behaviours.CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    processExtinguishMessage(msg);
                } else {
                    block();
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 3000) {
            @Override
            protected void onTick() {
                if (fireLocations.isEmpty()) {
                    handleFullExtinguishment();
                } else {
                    handleActiveFire();
                }
                checkForReignition();
            }
        });
    }
    
    private void handleActiveFire() {
        intensity++;
        fullyExtinguished = false;
        extinguishedTime = 0;
        
        // Επέκταση φωτιάς
        if (Math.random() < 0.3 && fireLocations.size() < 6) { // Μειωμένη πιθανότητα
            spreadToNewLocation();
        }
        
        // Αναφορά για όλες τις ενεργές θέσεις
        for (String location : fireLocations) {
            String[] coords = location.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            
            String message = getLocalName() + ": Φωτιά στη θέση (" + x + "," + y + ") - Ένταση: " + intensity;
            System.out.println(message);
            
            // ΕΝΗΜΕΡΩΣΗ GUI ΜΕ ΕΜΦΑΝΙΣΗ ΕΝΤΑΣΗΣ
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.showFireAt(x, y, intensity);
                    // Δεν προσθέτουμε log για κάθε update - μόνο για νέες εστίες
                }
            });
            
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("FIRE_SPREAD from " + x + "," + y + " intensity:" + intensity);
            msg.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
            send(msg);
            
            notifyNeighboringTrees(x, y);
        }
    }
    
    private void handleFullExtinguishment() {
        if (!fullyExtinguished) {
            fullyExtinguished = true;
            intensity = 0;
            String message = getLocalName() + ": *** ΠΛΗΡΗΣ ΚΑΤΑΣΒΕΣΗ - Όλες οι εστίες έχουν σβήσει! ***";
            System.out.println(message);
            
            // Ενημέρωση GUI
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.addLog("✅ " + message);
                    gui.showAllExtinguished();
                }
            });
            
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setContent("FIRE_FULLY_EXTINGUISHED");
            msg.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
            send(msg);
        }
        
        extinguishedTime++;
        
        if (extinguishedTime % 5 == 0) {
            String message = getLocalName() + ": Παρακολούθηση για αναζοπύρωση... (" + 
                           (extinguishedTime * 3) + " δευτερόλεπτα από την κατάσβεση)";
            System.out.println(message);
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.addLog("🔍 " + message);
                }
            });
        }
    }
    
    private void checkForReignition() {
        if (fullyExtinguished && !extinguishedLocations.isEmpty()) {
            double reignitionChance = Math.min(0.15, extinguishedTime * 0.002);
            
            if (Math.random() < reignitionChance) {
                List<String> possibleReignition = new ArrayList<>(extinguishedLocations);
                String reignitionLocation = possibleReignition.get(new Random().nextInt(possibleReignition.size()));
                
                fireLocations.add(reignitionLocation);
                extinguishedLocations.remove(reignitionLocation);
                intensity = 3;
                fullyExtinguished = false;
                extinguishedTime = 0;
                
                String[] coords = reignitionLocation.split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                
                String message = getLocalName() + ": *** ΑΝΑΖΟΠΥΡΩΣΗ στη θέση (" + x + "," + y + ")! ***";
                System.out.println(message);
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (gui != null) {
                        gui.addLog("🔥 " + message);
                        gui.showFireAt(x, y, intensity);
                    }
                });
                
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("FIRE_REIGNITION at (" + x + "," + y + ")");
                msg.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
                send(msg);
            }
        }
    }
    
    private void spreadToNewLocation() {
        List<String> activeLocations = new ArrayList<>(fireLocations);
        String baseLocation = activeLocations.get(new Random().nextInt(activeLocations.size()));
        
        String[] coords = baseLocation.split(",");
        int baseX = Integer.parseInt(coords[0]);
        int baseY = Integer.parseInt(coords[1]);
        
        List<String> possibleExpansions = new ArrayList<>();
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int newX = baseX + dx;
                int newY = baseY + dy;
                String newLocation = newX + "," + newY;
                
                if (newX >= 1 && newX <= 5 && newY >= 1 && newY <= 5 && 
                    !fireLocations.contains(newLocation) && 
                    findTreeAtPosition(newX, newY) != null) {
                    possibleExpansions.add(newLocation);
                }
            }
        }
        
        if (!possibleExpansions.isEmpty()) {
            String newLocation = possibleExpansions.get(new Random().nextInt(possibleExpansions.size()));
            fireLocations.add(newLocation);
            
            String[] newCoords = newLocation.split(",");
            int newX = Integer.parseInt(newCoords[0]);
            int newY = Integer.parseInt(newCoords[1]);
            
            String message = getLocalName() + ": Η φωτιά εξαπλώθηκε στη θέση (" + newX + "," + newY + ")!";
            System.out.println(message);
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.addLog("🔥 " + message);
                    gui.showFireAt(newX, newY, intensity);
                }
            });
        }
    }
    
    private void notifyNeighboringTrees(int x, int y) {
        ACLMessage fireSpread = new ACLMessage(ACLMessage.INFORM);
        fireSpread.setContent("FIRE_SPREAD from " + x + "," + y + " intensity:" + intensity);
        
        boolean hasNeighbors = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx, ny = y + dy;
                String treeName = findTreeAtPosition(nx, ny);
                if (treeName != null) {
                    fireSpread.addReceiver(new jade.core.AID(treeName, jade.core.AID.ISLOCALNAME));
                    hasNeighbors = true;
                }
            }
        }
        
        if (hasNeighbors) {
            send(fireSpread);
        }
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
        return null;
    }
    
    // ΠΡΟΣΘΗΚΗ: Νέα μέθοδος για διαχείριση μηνυμάτων κατάσβεσης
    private void processExtinguishMessage(ACLMessage msg) {
        String content = msg.getContent();
        
        if (content.startsWith("EXTINGUISH_AT")) {
            String location = content.substring(13); // Μετά το "EXTINGUISH_AT "
            String[] coords = location.split(",");
            if (coords.length == 2) {
                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    extinguishAt(x, y);
                } catch (NumberFormatException e) {
                    System.out.println("Σφάλμα παρσαρίσματος συντεταγμένων: " + location);
                }
            }
        } else if (content.contains("EXTINGUISH") || content.contains("AERIAL_EXTINGUISH") || 
                   content.contains("HELICOPTER_EXTINGUISH") || content.contains("GROUND_EXTINGUISH")) {
            
            // Εξαγωγή αποτελεσματικότητας
            double effectiveness = 0.8; // default
            if (content.contains("effectiveness:")) {
                try {
                    String effStr = content.substring(content.indexOf("effectiveness:") + 14);
                    effectiveness = Double.parseDouble(effStr.split(" ")[0]);
                } catch (Exception e) {
                    effectiveness = 0.8;
                }
            }
            
            // Εξαγωγή θέσης
            String location = null;
            if (content.contains("at (")) {
                int start = content.indexOf("at (") + 4;
                int end = content.indexOf(")", start);
                if (end > start) {
                    location = content.substring(start, end);
                }
            } else if (content.contains("at ")) {
                int start = content.indexOf("at ") + 3;
                String[] parts = content.substring(start).split(" ");
                if (parts.length > 0) {
                    location = parts[0];
                }
            }
            
            if (location != null) {
                String[] coords = location.split(",");
                if (coords.length == 2) {
                    try {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        
                        // Έλεγχος για επιτυχή κατάσβεση
                        if (Math.random() < effectiveness) {
                            extinguishAt(x, y);
                        } else {
                            System.out.println(getLocalName() + ": Η κατάσβεση στη θέση (" + x + "," + y + ") απέτυχε!");
                            if (gui != null) {
                                gui.addLog("❌ Κατάσβεση απέτυχε στη θέση (" + x + "," + y + ")");
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Σφάλμα παρσαρίσματος συντεταγμένων: " + location);
                    }
                }
            }
        }
    }
    
    // ΠΡΟΣΘΗΚΗ: Μέθοδος extinguishAt που λείπει
    public void extinguishAt(int x, int y) {
        String location = x + "," + y;
        if (fireLocations.remove(location)) {
            extinguishedLocations.add(location);
            String message = getLocalName() + ": Η φωτιά σβήστηκε στη θέση (" + x + "," + y + ")";
            System.out.println(message);
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (gui != null) {
                    gui.addLog("✅ " + message);
                    gui.showExtinguishedAt(x, y);
                }
            });
            
            // ΠΡΟΣΘΗΚΗ: Στέλνουμε μήνυμα στο FireControlAgent
            ACLMessage completed = new ACLMessage(ACLMessage.INFORM);
            completed.setContent("EXTINGUISH_COMPLETED at " + x + "," + y);
            completed.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
            send(completed);
            
            if (intensity > 1) {
                intensity = Math.max(1, intensity - 2);
            }
            
            // Έλεγχος αν όλες οι εστίες έχουν σβήσει
            if (fireLocations.isEmpty() && !fullyExtinguished) {
                fullyExtinguished = true;
                handleFullExtinguishment();
            }
        }
    }
}
