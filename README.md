# Σύστημα Διαχείρισης Δασικών Πυρκαγιών με Πολυ-Agent Αρχιτεκτονική

## Περιγραφή

Αυτό το σύστημα είναι μια προσομοίωση διαχείρισης δασικών πυρκαγιών που χρησιμοποιεί την πλατφόρμα JADE (Java Agent DEvelopment Framework) για τη δημιουργία ενός συστήματος πολλαπλών αυτόνομων agents. Το σύστημα προσομοιώνει σενάρια πυρκαγιών σε δάσος και τον συντονισμό διαφόρων πόρων πυρόσβεσης για την αντιμετώπισή τους.

## Χαρακτηριστικά

### 🔥 Διαχείριση Φωτιών
- Προσομοίωση ανάφλεξης και εξάπλωσης φωτιάς
- Δυναμική ένταση φωτιάς
- Αυτόματη κατάσβεση και πιθανότητα αναζοπύρωσης

### 🚁 Agents Πυρόσβεσης
- **FireTruckAgent**: Πυροσβεστικά οχήματα
- **AircraftAgent**: Πυροσβεστικά αεροπλάνα με μεγάλη χωρητικότητα νερού
- **HelicopterAgent**: Ελικόπτερα με γρήγορο ανεφοδιασμό
- **GroundCrewAgent**: Επίγειες ομάδες με ειδικότητες (πρόληψη, κατάσβεση, εξυγίανση)
- **EmergencyResponseAgent**: Μονάδες έκτακτης ανάγκης

### 🎯 Κεντρικός Έλεγχος
- **FireControlAgent**: Συντονιστής όλων των πόρων
- Αυτόματη ανάθεση καθηκόντων
- Διαχείριση καταστάσεων έκτακτης ανάγκης
- Παρακολούθηση διαθεσιμότητας πόρων

### 🌤️ Περιβαλλοντικοί Παράγοντες
- **WeatherAgent**: Προσομοίωση καιρικών συνθηκών
- Μέτρηση κινδύνου πυρκαγιάς βάσει ανέμου, θερμοκρασίας, υγρασίας
- Δυναμική ενημέρωση συνθηκών

### 📊 Γραφικό Περιβάλλον
- Οπτικοποίηση δάσους 5x5
- Real-time ενημέρωση καταστάσεων
- Εμφάνιση στατιστικών πόρων
- Καταγραφή γεγονότων
- Χρωματική κωδικοποίηση κινδύνου

## Απαιτήσεις Συστήματος

- Java 8 ή νεότερη έκδοση
- JADE Framework (περιλαμβάνεται στο φάκελο `lib/`)
- Windows/Linux/macOS

## Εγκατάσταση και Εκτέλεση

### 1. Κλωνοποίηση του Αποθετηρίου
```bash
git clone https://github.com/yourusername/wildfire-management-multiagent-system.git
cd wildfire-management-multiagent-system
```

### 2. Μεταγλώττιση
```bash
javac -cp "lib/jade.jar" -d bin src/**/*.java
```

### 3. Εκτέλεση
```bash
java -cp "bin;lib/jade.jar" MainContainer
```

## Δομή Έργου

```
├── src/
│   ├── MainContainer.java          # Κύρια κλάση εκκίνησης
│   ├── agents/                     # Agents του συστήματος
│   │   ├── FireAgent.java          # Agent φωτιάς
│   │   ├── FireControlAgent.java   # Κεντρικός ελεγκτής
│   │   ├── FireTruckAgent.java     # Πυροσβεστικό όχημα
│   │   ├── AircraftAgent.java      # Αεροσκάφος
│   │   ├── HelicopterAgent.java    # Ελικόπτερο
│   │   ├── GroundCrewAgent.java    # Επίγεια ομάδα
│   │   ├── WeatherAgent.java       # Καιρικές συνθήκες
│   │   └── EmergencyResponseAgent.java # Έκτακτη ανάγκη
│   └── gui/
│       └── FireSimulationGUI.java  # Γραφικό περιβάλλον
├── bin/                            # Μεταγλωττισμένα αρχεία
├── lib/
│   └── jade.jar                    # JADE Framework
└── README.md
```

## Χρήση

1. **Εκκίνηση**: Τρέξτε το MainContainer για να ξεκινήσει το σύστημα
2. **Φωτιά**: Πατήστε το κουμπί "🔥 Φωτιά" για προσομοίωση πυρκαγιάς
3. **Έκτακτη Ανάγκη**: Πατήστε "🚨 Έκτακτη" για ενεργοποίηση πρωτοκόλλου έκτακτης ανάγκης
4. **Παρακολούθηση**: Παρατηρήστε την αυτόματη αντίδραση των agents

## Συμβολισμοί GUI

- 🌲 **Δέντρο**: Υγιής βλάστηση
- 🔥 **Φωτιά**: Ενεργή εστία
- 💧 **Νερό**: Ρίψη νερού
- 🚒 **Πυροσβεστικό**: Επίγειο όχημα
- ✈️ **Αεροσκάφος**: Εναέριο μέσο
- 🚁 **Ελικόπτερο**: Ελικόπτερο
- 👥 **Ομάδα**: Επίγεια ομάδα
- 🌫️ **Σβησμένο**: Κατασβεσμένη περιοχή

## Αρχιτεκτονική Agents

### Επικοινωνία
- **FIPA ACL Messages**: Τυποποιημένη επικοινωνία μεταξύ agents
- **Inform/Request Protocols**: Ανταλλαγή πληροφοριών και αιτημάτων

### Συμπεριφορές
- **CyclicBehaviour**: Συνεχής παρακολούθηση μηνυμάτων
- **TickerBehaviour**: Περιοδικές ενέργειες
- **WakerBehaviour**: Καθυστερημένες ενέργειες

---

# Wildfire Management Multi-Agent System

## Description

This system is a wildfire management simulation using the JADE (Java Agent DEvelopment Framework) platform to create a multi-agent autonomous system. The system simulates forest fire scenarios and coordinates various firefighting resources to combat them.

## Features

### 🔥 Fire Management
- Fire ignition and spread simulation
- Dynamic fire intensity
- Automatic extinguishing and reignition probability

### 🚁 Firefighting Agents
- **FireTruckAgent**: Fire trucks
- **AircraftAgent**: Aircraft with large water capacity
- **HelicopterAgent**: Helicopters with quick refill capability
- **GroundCrewAgent**: Ground teams with specializations (prevention, suppression, mop-up)
- **EmergencyResponseAgent**: Emergency response units

### 🎯 Central Control
- **FireControlAgent**: Coordinator of all resources
- Automatic task assignment
- Emergency situation management
- Resource availability monitoring

### 🌤️ Environmental Factors
- **WeatherAgent**: Weather condition simulation
- Fire danger assessment based on wind, temperature, humidity
- Dynamic condition updates

### 📊 Graphical Interface
- 5x5 forest visualization
- Real-time status updates
- Resource statistics display
- Event logging
- Color-coded danger levels

## System Requirements

- Java 8 or newer
- JADE Framework (included in `lib/` folder)
- Windows/Linux/macOS

## Installation and Execution

### 1. Clone Repository
```bash
git clone https://github.com/yourusername/wildfire-management-multiagent-system.git
cd wildfire-management-multiagent-system
```

### 2. Compilation
```bash
javac -cp "lib/jade.jar" -d bin src/**/*.java
```

### 3. Execution
```bash
java -cp "bin;lib/jade.jar" MainContainer
```

## Project Structure

```
├── src/
│   ├── MainContainer.java          # Main startup class
│   ├── agents/                     # System agents
│   │   ├── FireAgent.java          # Fire agent
│   │   ├── FireControlAgent.java   # Central controller
│   │   ├── FireTruckAgent.java     # Fire truck
│   │   ├── AircraftAgent.java      # Aircraft
│   │   ├── HelicopterAgent.java    # Helicopter
│   │   ├── GroundCrewAgent.java    # Ground crew
│   │   ├── WeatherAgent.java       # Weather conditions
│   │   └── EmergencyResponseAgent.java # Emergency response
│   └── gui/
│       └── FireSimulationGUI.java  # Graphical interface
├── bin/                            # Compiled files
├── lib/
│   └── jade.jar                    # JADE Framework
└── README.md
```

## Usage

1. **Startup**: Run MainContainer to start the system
2. **Fire**: Click "🔥 Fire" button to simulate a fire
3. **Emergency**: Click "🚨 Emergency" to activate emergency protocol
4. **Monitor**: Observe automatic agent responses

## GUI Symbols

- 🌲 **Tree**: Healthy vegetation
- 🔥 **Fire**: Active fire
- 💧 **Water**: Water drop
- 🚒 **Fire Truck**: Ground vehicle
- ✈️ **Aircraft**: Aerial vehicle
- 🚁 **Helicopter**: Helicopter
- 👥 **Crew**: Ground team
- 🌫️ **Extinguished**: Extinguished area

## Agent Architecture

### Communication
- **FIPA ACL Messages**: Standardized agent communication
- **Inform/Request Protocols**: Information and request exchange

### Behaviors
- **CyclicBehaviour**: Continuous message monitoring
- **TickerBehaviour**: Periodic actions
- **WakerBehaviour**: Delayed actions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Authors

- Your Name - Initial work

## Acknowledgments

- JADE Framework development team
- Multi-agent systems research community