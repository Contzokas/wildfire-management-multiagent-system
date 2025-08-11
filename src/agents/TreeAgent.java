package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Random;

public class TreeAgent extends Agent {
    private boolean burning = false;
    private boolean destroyed = false;
    private int x, y;
    private int humidity; // υγρασία (0-100)
    private int treeType; // 1=πεύκο, 2=έλατο, 3=δρυς
    private int burnTime = 0; // χρόνος καύσης
    private int fireIntensity = 0;
    private double windEffect = 1.0;
    private int temperature = 25; // θερμοκρασία
    private static final int GRID_SIZE = 150; // Updated grid size
    
    @Override
    protected void setup() {
        Object[] args = getArguments();
        Random rand = new Random();
        
        if (args != null && args.length >= 2) {
            try {
                x = Integer.parseInt(args[0].toString());
                y = Integer.parseInt(args[1].toString());
            } catch (NumberFormatException e) {
                x = 0; y = 0;
            }
        }
        
        // Τυχαίες παράμετροι για ρεαλισμό
        humidity = 30 + rand.nextInt(40); // 30-70%
        treeType = 1 + rand.nextInt(3); // 1-3
        temperature = 20 + rand.nextInt(20); // 20-40°C
        
        String typeStr = (treeType == 1) ? "Πεύκο" : (treeType == 2) ? "Έλατο" : "Δρυς";
        System.out.println(getLocalName() + ": " + typeStr + " στη θέση (" + x + "," + y + 
                          ") - Υγρασία: " + humidity + "%, Θερμοκρασία: " + temperature + "°C");
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    processMessage(msg);
                } else {
                    block();
                }
            }
        });
        
        // Συμπεριφορά καύσης
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                if (burning) {
                    burnTime++;
                    fireIntensity = Math.min(10, fireIntensity + 1);
                    
                    if (burnTime % 10 == 0) { // Reduce log frequency for large grid
                        System.out.println(getLocalName() + ": Καίγεται για " + burnTime + " δευτερόλεπτα, ένταση: " + fireIntensity);
                    }
                    
                    // Διάδοση σε γειτονικά δέντρα
                    if (burnTime % 3 == 0) {
                        spreadFire();
                    }
                    
                    // Καταστροφή μετά από πολύ καύση
                    if (burnTime >= getDestructionTime()) {
                        destroyed = true;
                        burning = false;
                        System.out.println(getLocalName() + ": Το δέντρο καταστράφηκε πλήρως!");
                        removeBehaviour(this);
                    }
                }
            }
        });
    }
    
    private void processMessage(ACLMessage msg) {
        String content = msg.getContent();
        
        if (content.startsWith("FIRE_SPREAD") && !burning && !destroyed) {
            double ignitionChance = calculateIgnitionChance();
            
            if (Math.random() < ignitionChance) {
                burning = true;
                fireIntensity = 3;
                System.out.println(getLocalName() + ": Το δέντρο έπιασε φωτιά! (Πιθανότητα: " + 
                                 String.format("%.1f", ignitionChance * 100) + "%)");
                notifyFireControl();
            }
            
        } else if (content.equals("EXTINGUISH") && burning) {
            if (Math.random() < 0.8) { // 80% επιτυχία κατάσβεσης
                burning = false;
                fireIntensity = 0;
                burnTime = 0;
                System.out.println(getLocalName() + ": Η φωτιά σβήστηκε επιτυχώς!");
            }
            
        } else if (content.startsWith("WEATHER_UPDATE")) {
            updateWeather(content);
        }
    }
    
    private double calculateIgnitionChance() {
        double baseChance = 0.3; // βασική πιθανότητα 30%
        
        // Επίδραση υγρασίας (λιγότερη υγρασία = περισσότερη πιθανότητα)
        double humidityFactor = (100 - humidity) / 100.0;
        
        // Επίδραση θερμοκρασίας
        double tempFactor = Math.max(0.5, temperature / 40.0);
        
        // Επίδραση τύπου δέντρου (πεύκα ανάβουν πιο εύκολα)
        double typeFactor = (treeType == 1) ? 1.5 : (treeType == 2) ? 1.2 : 1.0;
        
        return Math.min(0.95, baseChance * humidityFactor * tempFactor * typeFactor * windEffect);
    }
    
    private void spreadFire() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("FIRE_SPREAD from " + x + "," + y + " intensity:" + fireIntensity);
        
        // Ειδοποίηση γειτονικών δέντρων (απόσταση 1)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx, ny = y + dy;
                if (nx >= 1 && nx <= GRID_SIZE && ny >= 1 && ny <= GRID_SIZE) {
                    String neighborName = "tree_" + nx + "_" + ny;
                    msg.addReceiver(new jade.core.AID(neighborName, jade.core.AID.ISLOCALNAME));
                }
            }
        }
        
        if (msg.getAllReceiver().hasNext()) {
            send(msg);
        }
    }
    
    private void notifyFireControl() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("FIRE_DETECTED at " + x + "," + y + " intensity:" + fireIntensity);
        msg.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(msg);
    }
    
    private void updateWeather(String weatherData) {
        // Παράδειγμα: "WEATHER_UPDATE wind:2.5 humidity:40 temp:35"
        String[] parts = weatherData.split(" ");
        for (String part : parts) {
            if (part.startsWith("wind:")) {
                windEffect = Double.parseDouble(part.substring(5));
            } else if (part.startsWith("humidity:")) {
                humidity = Integer.parseInt(part.substring(9));
            } else if (part.startsWith("temp:")) {
                temperature = Integer.parseInt(part.substring(5));
            }
        }
    }
    
    private int getDestructionTime() {
        // Διαφορετικός χρόνος καταστροφής ανά τύπο δέντρου
        return (treeType == 1) ? 10 : (treeType == 2) ? 15 : 20;
    }
    
    // Getter methods for position and state
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isBurning() { return burning; }
    public boolean isDestroyed() { return destroyed; }
    public int getFireIntensity() { return fireIntensity; }
}