package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.*;
import gui.FireSimulationGUI; // Προσθήκη import

public class FireControlAgent extends Agent {
    private Map<String, FireInfo> fireLocations = new HashMap<>();
    private List<String> availableTrucks = new ArrayList<>();
    private List<String> availableAircraft = new ArrayList<>();
    private List<String> availableHelicopters = new ArrayList<>();
    private Map<String, GroundCrewInfo> availableGroundCrews = new HashMap<>();
    private List<String> emergencyUnits = new ArrayList<>();
    
    private WeatherInfo currentWeather = new WeatherInfo();
    private int activeFireCount = 0;
    private boolean systemInitialized = false;
    private boolean emergencyDeclared = false;
    private int consecutiveHighPriority = 0;
    
    private FireSimulationGUI gui; // Προσθήκη GUI reference
    
    // Inner classes
    private class FireInfo {
        String status;
        int intensity;
        long startTime;
        
        FireInfo(String status, int intensity) {
            this.status = status;
            this.intensity = intensity;
            this.startTime = System.currentTimeMillis();
        }
    }
    
    private class GroundCrewInfo {
        String specialization;
        int fatigueLevel;
        boolean available;
        
        GroundCrewInfo(String spec, int fatigue) {
            this.specialization = spec;
            this.fatigueLevel = fatigue;
            this.available = true;
        }
    }
    
    private class WeatherInfo {
        double windSpeed = 1.0;
        int humidity = 50;
        int temperature = 25;
        String windDirection = "N";
    }
    
    @Override
    protected void setup() {
        // Αρχικοποίηση GUI
        initializeGUI();
        
        printHeader();
        
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
        
        // Παρακολούθηση κατάστασης
        addBehaviour(new TickerBehaviour(this, 8000) {
            @Override
            protected void onTick() {
                if (!systemInitialized) {
                    checkSystemInitialization();
                } else {
                    assessOverallSituation();
                    printDetailedStatusReport();
                }
            }
        });
        
        // Αρχικοποίηση
        addBehaviour(new jade.core.behaviours.WakerBehaviour(this, 3000) {
            @Override
            protected void onWake() {
                requestInitialStatus();
            }
        });
    }
    
    private void initializeGUI() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui = FireSimulationGUI.getInstance();
            gui.setVisible(true);
            gui.addLog("🚀 JADE Agents σύστημα ενεργοποιημένο!");
            gui.updateStatus("🟡 Αρχικοποίηση συστήματος...");
        });
    }
    
    private void printHeader() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           ΚΕΝΤΡΟ ΕΛΕΓΧΟΥ ΔΑΣΙΚΩΝ ΠΥΡΚΑΓΙΩΝ v2.0              ║");
        System.out.println("║              Πολυπρακτορική Προσομοίωση                      ║");
        System.out.println("║         Διπλωματική Εργασία - Πανεπιστήμιο                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println(getLocalName() + ": Σύστημα αρχικοποιείται...");
        
        if (gui != null) {
            gui.addLog("🎯 Κέντρο Ελέγχου Δασικών Πυρκαγιών ενεργοποιημένο");
        }
    }
    
    private void requestInitialStatus() {
        System.out.println("\n" + getLocalName() + ": 🔍 Σάρωση διαθέσιμων πόρων...");
        
        ACLMessage statusRequest = new ACLMessage(ACLMessage.REQUEST);
        statusRequest.setContent("STATUS_REQUEST");
        
        // Όλα τα πυροσβεστικά μέσα
        String[] agents = {"truck1", "truck2", "truck3", "truck4", 
                          "aircraft1", "aircraft2", "helicopter1",
                          "crew1", "crew2", "crew3", "crew4", "crew5", "crew6",
                          "emergency1"};
        
        for (String agent : agents) {
            statusRequest.addReceiver(new jade.core.AID(agent, jade.core.AID.ISLOCALNAME));
        }
        send(statusRequest);
    }
    
    private void checkSystemInitialization() {
        // Πολύ ελαστικότερες συνθήκες
        boolean allReady = availableTrucks.size() >= 1 &&  // Τουλάχιστον 1 truck
                          (availableAircraft.size() >= 1 || availableHelicopters.size() >= 1 || !availableGroundCrews.isEmpty()); // Οποιοδήποτε άλλο μέσο
        
        String statusMessage = String.format("Έλεγχος συστήματος: Trucks=%d, Aircraft=%d, Helicopters=%d, Crews=%d", 
                                            availableTrucks.size(), availableAircraft.size(), 
                                            availableHelicopters.size(), availableGroundCrews.size());
        System.out.println(statusMessage);
        
        if (gui != null) {
            gui.addLog("🔍 " + statusMessage);
        }
        
        if (allReady && !systemInitialized) {
            systemInitialized = true;
            printSystemReady();
            if (gui != null) {
                gui.updateStatus("🟢 Σύστημα έτοιμο - Αναμονή γεγονότων");
                gui.addLog("✅ Σύστημα πλήρως αρχικοποιημένο!");
            }
        } else if (!systemInitialized) {
            String waitMessage = "⏳ Αναμονή για: ";
            if (availableTrucks.size() < 1) waitMessage += "trucks ";
            if (availableAircraft.size() < 1 && availableHelicopters.size() < 1 && availableGroundCrews.isEmpty()) {
                waitMessage += "aircraft/helicopters/crews ";
            }
            
            System.out.println(waitMessage);
            if (gui != null) {
                gui.addLog("⏳ " + waitMessage);
            }
        }
    }
    
    private void printSystemReady() {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ΣΥΣΤΗΜΑ ΕΤΟΙΜΟ ΓΙΑ ΔΡΑΣΗ                  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
    }
    
    private void processMessage(ACLMessage msg) {
        String content = msg.getContent();
        String senderName = msg.getSender().getLocalName();
        
        if (content.startsWith("FIRE_SPREAD")) {
            handleFireSpread(content);
        } else if (content.equals("FIRE_FULLY_EXTINGUISHED")) {
            handleFullExtinguishment();
        } else if (content.equals("TRUCK_AVAILABLE") || content.equals("STATUS_REQUEST")) {
            handleTruckAvailable(senderName);
        } else if (content.startsWith("AIRCRAFT_AVAILABLE") || content.equals("STATUS_REQUEST")) {
            handleAircraftAvailable(senderName, content);
        } else if (content.startsWith("HELICOPTER_AVAILABLE") || content.equals("STATUS_REQUEST")) {
            handleHelicopterAvailable(senderName);
        } else if (content.startsWith("GROUND_CREW_AVAILABLE") || content.equals("STATUS_REQUEST")) {
            handleGroundCrewAvailable(senderName, content);
        } else if (content.startsWith("EMERGENCY_UNIT_AVAILABLE") || content.equals("STATUS_REQUEST")) {
            handleEmergencyUnitAvailable(senderName);
        } else if (content.startsWith("WEATHER_UPDATE")) {
            handleWeatherUpdate(content);
        } else if (content.startsWith("EXTINGUISH_COMPLETED")) {
            handleExtinguishCompleted(content);
        }
    }
    
    private void handleFireSpread(String content) {
        String[] parts = content.split(" ");
        String location = parts[2]; // "from X,Y"
        int intensity = 1;
        
        // Εξαγωγή έντασης αν υπάρχει
        for (String part : parts) {
            if (part.startsWith("intensity:")) {
                try {
                    intensity = Integer.parseInt(part.substring(10));
                } catch (NumberFormatException e) {
                    intensity = 1;
                }
                break;
            }
        }
        
        FireInfo fireInfo = new FireInfo("ACTIVE", intensity);
        fireLocations.put(location, fireInfo);
        activeFireCount++;
        
        System.out.println("\n🔥 === ΝΕΑ ΕΣΤΙΑ ΦΩΤΙΑΣ ===");
        System.out.println("📍 Θέση: " + location);
        System.out.println("🌡️  Ένταση: " + intensity);
        System.out.println("📊 Συνολικές ενεργές εστίες: " + activeFireCount);
        
        // Ενημέρωση GUI
        if (gui != null) {
            String[] coords = location.split(",");
            if (coords.length == 2) {
                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    gui.showFireAt(x, y, intensity);
                } catch (NumberFormatException e) {
                    gui.addLog("🔥 ΦΩΤΙΑ στη θέση " + location + " - Ένταση: " + intensity);
                }
            }
        }
        
        // ΠΡΟΣΘΗΚΗ: Κλήση της assessSituationAndDeploy
        if (systemInitialized) {
            assessSituationAndDeploy(location, fireInfo);
        } else {
            System.out.println("⚠️ Το Σύστημα δεν είναι αρχικοποιημένο - Αναμονή...");
            if (gui != null) {
                gui.addLog("⚠️ Το Σύστημα δεν είναι αρχικοποιημένο - Αναμονή...");
            }
        }
    }
    
    private void assessSituationAndDeploy(String location, FireInfo fireInfo) {
        int priority = calculatePriority(location, fireInfo);
        
        System.out.println("🎯 Προτεραιότητα: " + priority + "/10");
        if (gui != null) {
            gui.addLog("🎯 Προτεραιότητα: " + priority + "/10");
        }
        
        if (priority >= 9) {
            consecutiveHighPriority++;
            declareEmergency();
            deployMassiveResponse(location);
        } else if (priority >= 7) {
            consecutiveHighPriority++;
            if (consecutiveHighPriority >= 3) {
                declareEmergency();
            }
            deployAllAvailableResources(location);
        } else if (priority >= 5) {
            consecutiveHighPriority = 0;
            deployGroundAndAirResources(location);
        } else {
            consecutiveHighPriority = 0;
            deployGroundResources(location);
        }
    }
    
    private int calculatePriority(String location, FireInfo fireInfo) {
        int priority = 5;
        
        // Επίδραση καιρού
        if (currentWeather.windSpeed > 4.0) priority += 3;
        else if (currentWeather.windSpeed > 2.5) priority += 1;
        
        if (currentWeather.humidity < 20) priority += 3;
        else if (currentWeather.humidity < 35) priority += 1;
        
        if (currentWeather.temperature > 40) priority += 2;
        else if (currentWeather.temperature > 35) priority += 1;
        
        // Αριθμός ενεργών εστιών
        if (activeFireCount > 5) priority += 3;
        else if (activeFireCount > 3) priority += 2;
        else if (activeFireCount > 1) priority += 1;
        
        // Ένταση φωτιάς
        if (fireInfo.intensity > 8) priority += 2;
        else if (fireInfo.intensity > 5) priority += 1;
        
        // Χρόνος από την έναρξη
        long duration = (System.currentTimeMillis() - fireInfo.startTime) / 1000;
        if (duration > 120) priority += 2; // 2 λεπτά
        else if (duration > 60) priority += 1; // 1 λεπτό
        
        return Math.min(10, priority);
    }
    
    private void declareEmergency() {
        if (!emergencyDeclared) {
            emergencyDeclared = true;
            System.out.println("\n🚨🚨🚨 ΚΗΡΥΞΗ ΚΑΤΑΣΤΑΣΗΣ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ 🚨🚨🚨");
            System.out.println("🔴 Η πυρκαγιά βγήκε εκτός ελέγχου!");
            System.out.println("📞 Κλήση ενισχύσεων από γειτονικές περιοχές...");
            
            // Ενημέρωση GUI
            if (gui != null) {
                gui.showEmergencyDeclared();
            }
            
            // Ενεργοποίηση έκτακτων μονάδων
            for (String emergency : emergencyUnits) {
                ACLMessage activate = new ACLMessage(ACLMessage.REQUEST);
                activate.setContent("ACTIVATE_EMERGENCY_PROTOCOL");
                activate.addReceiver(new jade.core.AID(emergency, jade.core.AID.ISLOCALNAME));
                send(activate);
            }
        }
    }
    
    private void deployMassiveResponse(String location) {
        System.out.println("\n🚨 === ΜΑΖΙΚΗ ΑΝΤΙΔΡΑΣΗ ===");
        System.out.println("🎯 Ανάπτυξη ΟΛΩΝ των διαθέσιμων δυνάμεων!");
        
        // Ανάπτυξη όλων των μέσων
        deployAllTrucks(location);
        deployAllAircraft(location);
        deployAllHelicopters(location);
        deployAllGroundCrews(location);
    }
    
    private void deployAllAvailableResources(String location) {
        System.out.println("\n🔥 === ΚΡΙΣΙΜΗ ΚΑΤΑΣΤΑΣΗ ===");
        System.out.println("🚒 Ανάπτυξη όλων των διαθέσιμων δυνάμεων!");
        
        deployFireTrucks(location, 3); // 3 οχήματα
        deployAircraft(location, 2);   // 2 αεροσκάφη
        deployHelicopters(location, 1); // 1 ελικόπτερο
        deployGroundCrew(location, "suppression", 2); // 2 ομάδες κατάσβεσης
        deployGroundCrew(location, "prevention", 1);   // 1 ομάδα πρόληψης
    }
    
    private void deployGroundAndAirResources(String location) {
        System.out.println("\n🛩️ === ΣΥΝΤΟΝΙΣΜΕΝΗ ΕΠΙΧΕΙΡΗΣΗ ===");
        System.out.println("🚁 Ανάπτυξη εναέριων και επίγειων δυνάμεων");
        
        deployFireTrucks(location, 2);
        deployAircraft(location, 1);
        deployHelicopters(location, 1);
        deployGroundCrew(location, "suppression", 1);
    }
    
    private void deployGroundResources(String location) {
        System.out.println("\n🚒 === ΤΥΠΙΚΗ ΑΝΤΙΔΡΑΣΗ ===");
        System.out.println("👥 Ανάπτυξη επίγειων δυνάμεων");
        
        deployFireTrucks(location, 1);
        deployGroundCrew(location, "suppression", 1);
    }
    
    private void deployFireTrucks(String location, int count) {
        int deployed = 0;
        while (deployed < count && !availableTrucks.isEmpty()) {
            String truck = availableTrucks.remove(0);
            sendDeploymentOrder(truck, "RESPOND_TO_FIRE at " + location);
            System.out.println("🚒 ✅ " + truck + " → " + location);
            
            // Ενημέρωση GUI - ΕΜΦΑΝΙΣΗ TRUCK
            if (gui != null) {
                String[] coords = location.split(",");
                if (coords.length == 2) {
                    try {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        gui.addLog("🚒 " + truck + " αναπτύσσεται στη θέση (" + x + "," + y + ")");
                        gui.showTruckAt(x, y, truck); // ΝΕΑ ΜΕΘΟΔΟΣ
                    } catch (NumberFormatException e) {
                        gui.addLog("🚒 " + truck + " αναπτύσσεται στη θέση " + location);
                    }
                }
            }
            
            deployed++;
        }
        
        if (deployed < count) {
            System.out.println("⚠️ Μόνο " + deployed + "/" + count + " οχήματα διαθέσιμα!");
            if (gui != null) {
                gui.addLog("⚠️ Μόνο " + deployed + "/" + count + " οχήματα διαθέσιμα!");
            }
        }
    }
    
    private void deployAircraft(String location, int count) {
        int deployed = 0;
        while (deployed < count && !availableAircraft.isEmpty()) {
            String aircraft = availableAircraft.remove(0);
            sendDeploymentOrder(aircraft, "DEPLOY_AIRCRAFT to (" + location + ")");
            System.out.println("✈️ ✅ " + aircraft + " → " + location);
            
            // Ενημέρωση GUI - ΕΜΦΑΝΙΣΗ AIRCRAFT
            if (gui != null) {
                String[] coords = location.split(",");
                if (coords.length == 2) {
                    try {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        gui.showAgentAt(x, y, aircraft); // ΝΕΑ ΜΕΘΟΔΟΣ
                    } catch (NumberFormatException e) {
                        gui.addLog("✈️ " + aircraft + " ρίψη νερού στη θέση " + location);
                    }
                }
            }
            
            deployed++;
        }
        
        if (deployed < count) {
            System.out.println("⚠️ Μόνο " + deployed + "/" + count + " αεροσκάφη διαθέσιμα!");
            if (gui != null) {
                gui.addLog("⚠️ Μόνο " + deployed + "/" + count + " αεροσκάφη διαθέσιμα!");
            }
        }
    }
    
    private void deployHelicopters(String location, int count) {
        int deployed = 0;
        while (deployed < count && !availableHelicopters.isEmpty()) {
            String helicopter = availableHelicopters.remove(0);
            sendDeploymentOrder(helicopter, "DEPLOY_HELICOPTER to (" + location + ")");
            System.out.println("🚁 ✅ " + helicopter + " → " + location);
            
            // Ενημέρωση GUI - ΕΜΦΑΝΙΣΗ HELICOPTER
            if (gui != null) {
                String[] coords = location.split(",");
                if (coords.length == 2) {
                    try {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        gui.showHelicopterAt(x, y, helicopter); // ΝΕΑ ΜΕΘΟΔΟΣ
                    } catch (NumberFormatException e) {
                        gui.addLog("🚁 " + helicopter + " αναπτύσσεται στη θέση " + location);
                    }
                }
            }
            
            deployed++;
        }
        
        if (deployed < count) {
            System.out.println("⚠️ Μόνο " + deployed + "/" + count + " ελικόπτερα διαθέσιμα!");
        }
    }
    
    private void deployGroundCrew(String location, String specialization, int count) {
        int deployed = 0;
        
        // Αναζήτηση ομάδων με την κατάλληλη ειδικότητα
        List<String> suitableCrews = new ArrayList<>();
        for (Map.Entry<String, GroundCrewInfo> entry : availableGroundCrews.entrySet()) {
            GroundCrewInfo crew = entry.getValue();
            if (crew.available && crew.specialization.equals(specialization) && crew.fatigueLevel < 80) {
                suitableCrews.add(entry.getKey());
            }
        }
        
        // Ανάπτυξη ομάδων
        while (deployed < count && !suitableCrews.isEmpty()) {
            String crew = suitableCrews.remove(0);
            availableGroundCrews.get(crew).available = false;
            sendDeploymentOrder(crew, "DEPLOY_GROUND_CREW to (" + location + ")");
            System.out.println("👥 ✅ " + crew + " (" + getSpecializationGreek(specialization) + ") → " + location);
            deployed++;
        }
        
        if (deployed < count) {
            System.out.println("⚠️ Μόνο " + deployed + "/" + count + " ομάδες " + 
                             getSpecializationGreek(specialization) + " διαθέσιμες!");
        }
    }
    
    private void deployAllTrucks(String location) {
        while (!availableTrucks.isEmpty()) {
            String truck = availableTrucks.remove(0);
            sendDeploymentOrder(truck, "RESPOND_TO_FIRE at " + location);
            System.out.println("🚒 ✅ " + truck + " → " + location + " (ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ)");
        }
    }
    
    private void deployAllAircraft(String location) {
        while (!availableAircraft.isEmpty()) {
            String aircraft = availableAircraft.remove(0);
            sendDeploymentOrder(aircraft, "DEPLOY_AIRCRAFT to (" + location + ")");
            System.out.println("✈️ ✅ " + aircraft + " → " + location + " (ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ)");
        }
    }
    
    private void deployAllHelicopters(String location) {
        while (!availableHelicopters.isEmpty()) {
            String helicopter = availableHelicopters.remove(0);
            sendDeploymentOrder(helicopter, "DEPLOY_HELICOPTER to (" + location + ")");
            System.out.println("🚁 ✅ " + helicopter + " → " + location + " (ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ)");
        }
    }
    
    private void deployAllGroundCrews(String location) {
        for (Map.Entry<String, GroundCrewInfo> entry : availableGroundCrews.entrySet()) {
            GroundCrewInfo crew = entry.getValue();
            if (crew.available && crew.fatigueLevel < 90) {
                crew.available = false;
                sendDeploymentOrder(entry.getKey(), "DEPLOY_GROUND_CREW to (" + location + ")");
                System.out.println("👥 ✅ " + entry.getKey() + " → " + location + " (ΕΚΤΑΚΤΗ ΑΝΑΓΚΗ)");
            }
        }
    }
    
    private void sendDeploymentOrder(String agentName, String content) {
        ACLMessage order = new ACLMessage(ACLMessage.REQUEST);
        order.setContent(content);
        order.addReceiver(new jade.core.AID(agentName, jade.core.AID.ISLOCALNAME));
        send(order);
    }
    
    private void assessOverallSituation() {
        if (activeFireCount == 0 && emergencyDeclared) {
            emergencyDeclared = false;
            consecutiveHighPriority = 0;
            System.out.println("\n✅ === ΚΑΤΑΣΤΑΣΗ ΥΠΟΛΑΛΗ ===");
            System.out.println("🎉 Άρση κατάστασης έκτακτης ανάγκης!");
        }
    }
    
    private void printDetailedStatusReport() {
        if (!systemInitialized) return;
        
        System.out.println("📊 Ενεργές εστίες: " + activeFireCount);
        System.out.println("🚒 Διαθέσιμα οχήματα: " + availableTrucks.size());
        System.out.println("✈️ Διαθέσιμα αεροσκάφη: " + availableAircraft.size());
        
        // Καιρικές συνθήκες
        System.out.println("🌤️  ΚΑΙΡΟΣ:");
        System.out.println("   💨 Άνεμος: " + String.format("%.1f", currentWeather.windSpeed) + " m/s " + currentWeather.windDirection);
        System.out.println("   💧 Υγρασία: " + currentWeather.humidity + "%");
        System.out.println("   🌡️  Θερμοκρασία: " + currentWeather.temperature + "°C");
        
        // Διαθέσιμοι πόροι
        System.out.println("📋 ΔΙΑΘΕΣΙΜΟΙ ΠΟΡΟΙ:");
        System.out.println("   🚒 Οχήματα: " + availableTrucks.size() + "/4");
        System.out.println("   ✈️  Αεροσκάφη: " + availableAircraft.size() + "/2");
        System.out.println("   🚁 Ελικόπτερα: " + availableHelicopters.size() + "/1");
        
        int availableCrews = (int) availableGroundCrews.values().stream().filter(c -> c.available).count();
        System.out.println("   👥 Ομάδες: " + availableCrews + "/6");
    }
    
    // Υπόλοιπες μέθοδοι διαχείρισης agents...
    private void handleTruckAvailable(String truckName) {
        if (!availableTrucks.contains(truckName)) {
            availableTrucks.add(truckName);
            System.out.println("🚒 " + truckName + " επέστρεψε");
            if (gui != null) {
                gui.addLog("🚒 " + truckName + " διαθέσιμο");
            }
        }
    }
    
    private void handleAircraftAvailable(String aircraftName, String content) {
        if (!availableAircraft.contains(aircraftName)) {
            availableAircraft.add(aircraftName);
            System.out.println("✈️ " + aircraftName + " επέστρεψε");
            if (gui != null) {
                gui.addLog("✈️ " + aircraftName + " διαθέσιμο");
            }
        }
    }
    
    private void handleHelicopterAvailable(String helicopterName) {
        if (!availableHelicopters.contains(helicopterName)) {
            availableHelicopters.add(helicopterName);
            System.out.println("🚁 " + helicopterName + " επέστρεψε");
        }
    }
    
    private void handleGroundCrewAvailable(String crewName, String content) {
        String specialization = "suppression";
        int fatigue = 0;
        
        // Παρσάρισμα ειδικότητας από το όνομα
        if (crewName.contains("1") || crewName.contains("4") || crewName.contains("5")) {
            specialization = "suppression";
        } else if (crewName.contains("2") || crewName.contains("6")) {
            specialization = "prevention";
        } else if (crewName.contains("3")) {
            specialization = "mop-up";
        }
        
        availableGroundCrews.put(crewName, new GroundCrewInfo(specialization, fatigue));
        System.out.println("👥 " + crewName + " (" + getSpecializationGreek(specialization) + ") επέστρεψε");
        if (gui != null) {
            gui.addLog("👥 " + crewName + " (" + getSpecializationGreek(specialization) + ") διαθέσιμη");
        }
    }
    
    private void handleEmergencyUnitAvailable(String unitName) {
        if (!emergencyUnits.contains(unitName)) {
            emergencyUnits.add(unitName);
            System.out.println("🚨 " + unitName + " διαθέσιμη");
        }
    }
    
    private void handleWeatherUpdate(String content) {
        String[] parts = content.split(" ");
        for (String part : parts) {
            try {
                if (part.startsWith("wind:")) {
                    currentWeather.windSpeed = Double.parseDouble(part.substring(5));
                } else if (part.startsWith("humidity:")) {
                    currentWeather.humidity = Integer.parseInt(part.substring(9));
                } else if (part.startsWith("temp:")) {
                    currentWeather.temperature = Integer.parseInt(part.substring(5));
                } else if (part.startsWith("direction:")) {
                    currentWeather.windDirection = part.substring(10);
                }
            } catch (NumberFormatException e) {
                // Ignore parsing errors
            }
        }
        
        assessWeatherRisk();
    }
    
    private void assessWeatherRisk() {
        int riskLevel = 1;
        
        if (currentWeather.windSpeed > 4.0) riskLevel++;
        if (currentWeather.humidity < 25) riskLevel++;
        if (currentWeather.temperature > 40) riskLevel++;
        if (activeFireCount > 0) riskLevel++;
        
        if (riskLevel >= 4) {
            System.out.println(getLocalName() + ": ⚠️ ΥΨΗΛΟΣ ΚΙΝΔΥΝΟΣ ΠΥΡΚΑΙΑΣ - Επιφυλακή!");
        }
    }
    
    private void handleFullExtinguishment() {
        activeFireCount = 0;
        fireLocations.clear();
        System.out.println("\n🎉 === ΠΛΗΡΗΣ ΚΑΤΑΣΒΕΣΗ ===");
        System.out.println("✅ Όλες οι εστίες σβήστηκαν!");
        
        // Ενημέρωση GUI
        if (gui != null) {
            gui.showAllExtinguished();
        }
    }
    
    private void handleReignition(String content) {
        String location = content.substring(content.indexOf("at") + 3, content.indexOf(")") + 1);
        FireInfo fireInfo = new FireInfo("REIGNITED", 3);
        fireLocations.put(location, fireInfo);
        activeFireCount++;
        
        System.out.println("\n🔥 === ΑΝΑΖΟΠΥΡΩΣΗ ===");
        System.out.println("📍 Θέση: " + location);
        System.out.println("⚡ Άμεση επέμβαση!");
        
        assessSituationAndDeploy(location, fireInfo);
    }
    
    private void handleExtinguishCompleted(String content) {
        if (activeFireCount > 0) {
            activeFireCount--;
        }
        System.out.println("✅ Κατάσβεση ολοκληρώθηκε");
    }
    
    private String getSpecializationGreek(String specialization) {
        switch (specialization) {
            case "prevention": return "Πρόληψη";
            case "suppression": return "Κατάσβεση";
            case "mop-up": return "Εξυγίανση";
            default: return "Γενική";
        }
    }
}