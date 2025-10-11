package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.*;
import gui.FireSimulationGUI; // Import for animated movement

public class GroundCrewAgent extends Agent {
    private boolean deployed = false;
    private int teamSize = 5; // αριθμός μελών ομάδας
    private int fatigueLevel = 0; // 0-100 (κούραση)
    private List<String> equipment = Arrays.asList("αξίνες", "τσάπες", "φτυάρια", "ψεκαστήρες");
    private String specialization; // "prevention", "suppression", "mop-up"
    // Αφαίρεση μη χρησιμοποιημένου field currentLocation
    
    // Movement speed constants (in milliseconds per step)
    private static final int CREW_SPEED_DELAY = 300; // Slowest speed: ~5 km/h (walking)
    
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            specialization = args[0].toString();
        } else {
            specialization = "suppression";
        }
        
        System.out.println(getLocalName() + ": Πεζοπόρα ομάδα (" + teamSize + " άτομα) - Ειδικότητα: " + 
                          getSpecializationGreek() + " - Εξοπλισμός: " + String.join(", ", equipment));
        
        // Ειδοποίηση διαθεσιμότητας
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("GROUND_CREW_AVAILABLE specialization:" + specialization + " fatigue:" + fatigueLevel);
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.startsWith("DEPLOY_GROUND_CREW") && !deployed && fatigueLevel < 80) {
                        String location = content.substring(content.indexOf("to") + 3);
                        deployToLocation(location);
                        
                    } else if (content.equals("REST_REQUEST")) {
                        if (fatigueLevel > 60) {
                            takeRest();
                        }
                    } else if (content.startsWith("SUPPORT_REQUEST")) {
                        if (!deployed && fatigueLevel < 50) {
                            String supportType = content.substring(content.indexOf("type:") + 5);
                            provideSupportService(supportType);
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
    
    private void deployToLocation(String location) {
        deployed = true;
        
        System.out.println(getLocalName() + ": Μετακίνηση προς " + location + 
                          " (Κούραση: " + fatigueLevel + "%)");
        
        // Parse coordinates from location string (e.g., "75,75" or "fire at 75,75")
        int targetX = 75, targetY = 75; // Default location
        try {
            if (location.contains(",")) {
                String coords = location;
                if (location.contains(" at ")) {
                    coords = location.substring(location.lastIndexOf(" at ") + 4);
                }
                String[] parts = coords.split(",");
                targetX = Integer.parseInt(parts[0].trim());
                targetY = Integer.parseInt(parts[1].trim());
            }
        } catch (NumberFormatException e) {
            System.out.println(getLocalName() + ": Δεν μπόρεσα να αναλύσω τις συντεταγμένες, χρήση προεπιλογής (75,75)");
        }
        
        // Use animated walking movement to location
        walkToLocation(targetX, targetY);
        
        System.out.println(getLocalName() + ": Άφιξη στην περιοχή " + location);
        
        // Διαφορετικές δραστηριότητες ανά ειδικότητα
        switch (specialization) {
            case "prevention":
                performPrevention(location);
                break;
            case "suppression":
                performSuppression(location);
                break;
            case "mop-up":
                performMopUp(location);
                break;
            default:
                performGeneralWork(location);
        }
        
        // Επιστροφή στη βάση
        System.out.println(getLocalName() + ": Ολοκλήρωση εργασιών - Επιστροφή στη βάση");
        doWait(8000);
        
        deployed = false;
        fatigueLevel += 20; // αύξηση κούρασης
        
        if (fatigueLevel >= 80) {
            System.out.println(getLocalName() + ": Η ομάδα χρειάζεται ανάπαυση!");
            takeRest();
        }
        
        sendAvailabilityStatus();
    }
    
    private void performPrevention(String location) {
        System.out.println(getLocalName() + ": Δημιουργία αντιπυρικής ζώνης στην περιοχή " + location);
        doWait(15000); // 15 δευτερόλεπτα εργασία
        
        // Μήνυμα προς δέντρα για μείωση επικινδυνότητας
        ACLMessage prevention = new ACLMessage(ACLMessage.INFORM);
        prevention.setContent("FIRE_PREVENTION applied effectiveness:0.6");
        
        // Στέλνουμε σε όλα τα γειτονικά δέντρα
        String[] coords = location.replace("(", "").replace(")", "").split(",");
        int centerX = Integer.parseInt(coords[0]);
        int centerY = Integer.parseInt(coords[1]);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                String treeName = findTreeAtPosition(centerX + dx, centerY + dy);
                if (treeName != null) {
                    prevention.addReceiver(new jade.core.AID(treeName, jade.core.AID.ISLOCALNAME));
                }
            }
        }
        send(prevention);
        
        System.out.println(getLocalName() + ": Αντιπυρική ζώνη ολοκληρώθηκε!");
    }
    
    private void performSuppression(String location) {
        System.out.println(getLocalName() + ": Άμεση κατάσβεση με χειρωνακτικά μέσα στην περιοχή " + location);
        doWait(12000); // 12 δευτερόλεπτα κατάσβεση
        
        // Στοχευμένη κατάσβεση
        ACLMessage suppress = new ACLMessage(ACLMessage.REQUEST);
        suppress.setContent("GROUND_EXTINGUISH at " + location + " effectiveness:0.7");
        suppress.addReceiver(new jade.core.AID("fire1", jade.core.AID.ISLOCALNAME));
        send(suppress);
        
        System.out.println(getLocalName() + ": Χειρωνακτική κατάσβεση ολοκληρώθηκε!");
    }
    
    private void performMopUp(String location) {
        System.out.println(getLocalName() + ": Εργασίες εξυγίανσης και παρακολούθησης στην περιοχή " + location);
        doWait(20000); // 20 δευτερόλεπτα εξυγίανση
        
        // Μήνυμα για μείωση πιθανότητας αναζοπύρωσης
        ACLMessage mopUp = new ACLMessage(ACLMessage.INFORM);
        mopUp.setContent("MOP_UP_COMPLETED at " + location + " reignition_reduction:0.8");
        mopUp.addReceiver(new jade.core.AID("fire1", jade.core.AID.ISLOCALNAME));
        send(mopUp);
        
        System.out.println(getLocalName() + ": Εξυγίανση ολοκληρώθηκε - Μειωμένος κίνδυνος αναζοπύρωσης!");
    }
    
    private void performGeneralWork(String location) {
        System.out.println(getLocalName() + ": Γενικές εργασίες πυρόσβεσης στην περιοχή " + location);
        doWait(10000);
        
        ACLMessage general = new ACLMessage(ACLMessage.REQUEST);
        general.setContent("GENERAL_FIRE_WORK at " + location + " effectiveness:0.5");
        general.addReceiver(new jade.core.AID("fire1", jade.core.AID.ISLOCALNAME));
        send(general);
        
        System.out.println(getLocalName() + ": Γενικές εργασίες ολοκληρώθηκαν!");
    }
    
    private void takeRest() {
        System.out.println(getLocalName() + ": Ανάπαυση ομάδας...");
        doWait(30000); // 30 δευτερόλεπτα ανάπαυση
        fatigueLevel = Math.max(0, fatigueLevel - 40);
        System.out.println(getLocalName() + ": Ανάπαυση ολοκληρώθηκε - Κούραση: " + fatigueLevel + "%");
    }
    
    private void provideSupportService(String supportType) {
        System.out.println(getLocalName() + ": Παροχή υποστήριξης: " + supportType);
        // Υλοποίηση διαφόρων υπηρεσιών υποστήριξης
    }
    
    private void sendAvailabilityStatus() {
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("GROUND_CREW_AVAILABLE specialization:" + specialization + " fatigue:" + fatigueLevel);
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
    }
    
    private String getSpecializationGreek() {
        switch (specialization) {
            case "prevention": return "Πρόληψη";
            case "suppression": return "Κατάσβεση";
            case "mop-up": return "Εξυγίανση";
            default: return "Γενική";
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
        if (nx == 3 && ny == 3) return "tree9";
        if (nx == 1 && ny == 4) return "tree10";
        if (nx == 2 && ny == 4) return "tree11";
        if (nx == 3 && ny == 4) return "tree12";
        return null;
    }
    
    private void walkToLocation(int targetX, int targetY) {
        // Get GUI singleton instance
        gui.FireSimulationGUI guiInstance = gui.FireSimulationGUI.getInstance();
        if (guiInstance == null) {
            System.out.println(getLocalName() + ": Δεν υπάρχει GUI - άμεση τηλεμεταφορά στην περιοχή (" + targetX + ", " + targetY + ")");
            return;
        }
        
        // Get Command Center position as starting point (crew base)
        int currentX = gui.FireSimulationGUI.getCommandCenterX();
        int currentY = gui.FireSimulationGUI.getCommandCenterY();
        
        System.out.println(getLocalName() + ": Πεζή μετακίνηση από κέντρο επιχειρήσεων (" + currentX + ", " + currentY + ") προς (" + targetX + ", " + targetY + ")");
        
        // Animated movement from Command Center to target (walking speed)
        while (currentX != targetX || currentY != targetY) {
            if (currentX < targetX) currentX++;
            else if (currentX > targetX) currentX--;
            
            if (currentY < targetY) currentY++;
            else if (currentY > targetY) currentY--;
            
            final int newX = currentX;
            final int newY = currentY;
            
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (guiInstance != null) {
                    guiInstance.showAgentAt(newX, newY, "CREW", getLocalName());
                }
            });
            
            try {
                Thread.sleep(CREW_SPEED_DELAY); // Slow walking movement
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        System.out.println(getLocalName() + ": Άφιξη στον προορισμό (" + targetX + ", " + targetY + ") - Προετοιμασία για εργασία");
    }
}