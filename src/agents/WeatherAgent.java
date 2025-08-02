package agents;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Random;
import gui.FireSimulationGUI; // Προσθήκη import

public class WeatherAgent extends Agent {
    private double windSpeed = 1.0; // 0-5 (m/s)
    private int humidity = 50; // 0-100%
    private int temperature = 25; // 15-45°C
    private String windDirection = "N"; // N, S, E, W
    private FireSimulationGUI gui;
    
    @Override
    protected void setup() {
        // Αρχικοποίηση GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui = FireSimulationGUI.getInstance();
        });
        
        String message = getLocalName() + ": Μετεωρολογικός σταθμός ενεργός.";
        System.out.println(message);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🌤️ " + message);
            }
        });
        
        addBehaviour(new TickerBehaviour(this, 10000) { // ενημέρωση κάθε 10 δευτερόλεπτα
            @Override
            protected void onTick() {
                updateWeather();
                broadcastWeather();
            }
        });
    }
    
    private void updateWeather() {
        Random rand = new Random();
        
        // Σταδιακές αλλαγές στον καιρό
        windSpeed += (rand.nextDouble() - 0.5) * 0.5;
        windSpeed = Math.max(0, Math.min(5, windSpeed));
        
        humidity += rand.nextInt(11) - 5; // ±5%
        humidity = Math.max(20, Math.min(90, humidity));
        
        temperature += rand.nextInt(5) - 2; // ±2°C
        temperature = Math.max(15, Math.min(45, temperature));
        
        String[] directions = {"N", "S", "E", "W", "NE", "NW", "SE", "SW"};
        if (rand.nextDouble() < 0.3) { // 30% πιθανότητα αλλαγής κατεύθυνσης
            windDirection = directions[rand.nextInt(directions.length)];
        }
        
        String message = getLocalName() + ": Καιρός - Άνεμος: " + String.format("%.1f", windSpeed) + 
                        "m/s " + windDirection + ", Υγρασία: " + humidity + "%, Θερμοκρασία: " + temperature + "°C";
        System.out.println(message);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (gui != null) {
                gui.addLog("🌤️ " + message);
                
                // Ειδική ειδοποίηση για επικίνδυνες συνθήκες
                if (windSpeed > 3.0 || humidity < 30 || temperature > 35) {
                    gui.addLog("⚠️ ΕΠΙΚΙΝΔΥΝΕΣ ΚΑΙΡΙΚΕΣ ΣΥΝΘΗΚΕΣ!");
                }
            }
        });
    }
    
    private void broadcastWeather() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent("WEATHER_UPDATE wind:" + windSpeed + " humidity:" + humidity + 
                      " temp:" + temperature + " direction:" + windDirection);
        
        // Ειδοποίηση όλων των agents
        msg.addReceiver(new jade.core.AID("tree1", jade.core.AID.ISLOCALNAME));
        msg.addReceiver(new jade.core.AID("tree2", jade.core.AID.ISLOCALNAME));
        msg.addReceiver(new jade.core.AID("tree3", jade.core.AID.ISLOCALNAME));
        msg.addReceiver(new jade.core.AID("tree4", jade.core.AID.ISLOCALNAME));
        msg.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        
        send(msg);
    }
}