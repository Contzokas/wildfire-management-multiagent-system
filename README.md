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
- Οπτικοποίηση δάσους 150x150 κελιών
- Real-time ενημέρωση καταστάσεων
- Εμφάνιση στατιστικών πόρων
- Καταγραφή γεγονότων
- Χρωματική κωδικοποίηση κινδύνου
- **Προηγμένα χειριστήρια zoom:**
  - Κουμπί "100%" για ακριβές zoom χωρίς κλιμάκωση
  - Συντομεύσεις πληκτρολογίου (Ctrl+1 για 100% zoom)
  - Αυτόματη προσαρμογή στην οθόνη
  - Δυναμικό κεντράρισμα θέας

## Απαιτήσεις Συστήματος

- **Java Development Kit (JDK) 8 ή νεότερη έκδοση**
- JADE Framework (περιλαμβάνεται στο φάκελο `lib/`)
- Windows/Linux/macOS
- **Προτεινόμενο**: VS Code για εύκολη εκτέλεση tasks
- **Μνήμη**: 4GB-16GB RAM (ανάλογα με τη διάταξη)

## Νέες Δυνατότητες (v2.0)

### 🎯 **Βελτιωμένα Χειριστήρια Zoom**
- Κουμπί "100%" για ακριβές zoom στο πραγματικό μέγεθος
- Συντομεύσεις πληκτρολογίου (Ctrl+1, Ctrl+Plus/Minus)
- Αυτόματη προσαρμογή στην οθόνη
- Δυναμικό κεντράρισμα θέας

### 🚀 **VS Code Integration**
- Προκαθορισμένα tasks για μεταγλώττιση και εκτέλεση
- Πολλαπλές διαμορφώσεις πόρων (από μικρή έως υπερμεγάλη)
- Διαδραστικό script εκτέλεσης

### 📊 **Αναβαθμισμένο GUI**
- Γρίντο 150x150 κελιών (από 5x5)
- Χρωματική κωδικοποίηση agents
- Βελτιωμένη οπτικοποίηση trails και κινήσεων

## Εγκατάσταση και Εκτέλεση

### 1. Κλωνοποίηση του Αποθετηρίου
```bash
git clone https://github.com/Contzokas/wildfire-management-multiagent-system.git
cd wildfire-management-multiagent-system
```

### 2. Μεταγλώττιση
```bash
# Με VS Code Tasks (προτεινόμενο)
Ctrl+Shift+P → "Tasks: Run Task" → "Compile JADE agents with GUI"

# Ή με command line:
javac -encoding UTF-8 -cp lib/jade.jar -d bin src/utils/*.java
javac -encoding UTF-8 -cp lib/jade.jar -d bin src/gui/*.java
javac -encoding UTF-8 -cp "lib/jade.jar;bin" -d bin src/agents/*.java
javac -encoding UTF-8 -cp "lib/jade.jar;bin" -d bin src/MainContainer.java
```

### 3. Εκτέλεση

#### **Διαδραστική Εκτέλεση (Προτεινόμενη)**
```bash
# Για Windows
run_custom_english.bat

# Ή με VS Code Task
"Run JADE 150x150 Custom Resources (Interactive)"
```

#### **Προκαθορισμένες Διαμορφώσεις**
- **Μικρή**: `2 οχήματα, 1 αεροσκάφος, 1 ελικόπτερο, 3 ομάδες` (4GB RAM)
- **Μεσαία**: `4 οχήματα, 2 αεροσκάφη, 1 ελικόπτερο, 6 ομάδες` (6GB RAM)  
- **Μεγάλη**: `8 οχήματα, 4 αεροσκάφη, 3 ελικόπτερα, 12 ομάδες` (10GB RAM)
- **Εξτρίμ**: `15 οχήματα, 8 αεροσκάφη, 5 ελικόπτερα, 30 ομάδες` (12GB RAM)
- **Υπερμεγάλη**: `20 οχήματα, 10 αεροσκάφη, 8 ελικόπτερα, 50 ομάδες` (16GB RAM)

#### **Command Line**
```bash
# Βασική εκτέλεση
java -cp "bin;lib/jade.jar" MainContainer

# Προσαρμοσμένη διάταξη (trucks, aircraft, helicopters, crews)
java -cp "bin;lib/jade.jar" MainContainer custom 4 2 1 6
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
│   ├── gui/
│   │   └── FireSimulationGUI.java  # Γραφικό περιβάλλον
│   └── utils/
│       └── GridManager.java        # Διαχείριση γριδιου
├── bin/                            # Μεταγλωττισμένα αρχεία
├── lib/
│   └── jade.jar                    # JADE Framework
├── .vscode/
│   └── tasks.json                  # VS Code tasks για εύκολη εκτέλεση
├── run_custom_english.bat          # Διαδραστικό script εκτέλεσης
└── README.md
```

## Χρήση

### **Βασικά Χειριστήρια**
1. **Εκκίνηση**: Τρέξτε το MainContainer ή το διαδραστικό script
2. **Φωτιά**: Πατήστε το κουμπί "🔥 Φωτιά" για προσομοίωση πυρκαγιάς
3. **Έκτακτη Ανάγκη**: Πατήστε "🚨 Έκτακτη" για ενεργοποίηση πρωτοκόλλου έκτακτης ανάγκης
4. **Παρακολούθηση**: Παρατηρήστε την αυτόματη αντίδραση των agents

### **Χειριστήρια Zoom & Προβολής**
- **"100%" κουμπί**: Ακριβές zoom χωρίς κλιμάκωση
- **Ctrl+1**: Γρήγορη μετάβαση σε 100% zoom
- **Ctrl+Plus/Minus**: Μεγέθυνση/Σμίκρυνση
- **Ctrl+0**: Επαναφορά προβολής
- **Ctrl+C**: Κεντράρισμα θέας
- **"📐" κουμπί**: Αυτόματη προσαρμογή στην οθόνη

### **Χρώματα Agents**
- 🚒 **Πυροσβεστικά Οχήματα**: **ΚΟΚΚΙΝΟ**
- ✈️ **Αεροσκάφη**: **ΜΠΛΕ**  
- 🚁 **Ελικόπτερα**: **ΜΑΓΕΝΤΑ** (Ροζ-Μωβ)
- 👥 **Επίγειες Ομάδες**: **ΠΟΡΤΟΚΑΛΙ**

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
- 150x150 forest grid visualization
- Real-time status updates
- Resource statistics display
- Event logging
- Color-coded danger levels
- **Advanced zoom controls:**
  - "100%" button for precise zoom without scaling
  - Keyboard shortcuts (Ctrl+1 for 100% zoom)
  - Auto-fit to screen
  - Dynamic view centering

## System Requirements

- **Java Development Kit (JDK) 8 or newer**
- JADE Framework (included in `lib/` folder)
- Windows/Linux/macOS
- **Recommended**: VS Code for easy task execution
- **Memory**: 4GB-16GB RAM (depending on configuration)

## New Features (v2.0)

### 🎯 **Enhanced Zoom Controls**
- "100%" button for precise zoom at actual size
- Keyboard shortcuts (Ctrl+1, Ctrl+Plus/Minus)
- Auto-fit to screen
- Dynamic view centering

### 🚀 **VS Code Integration**
- Pre-configured tasks for compilation and execution
- Multiple resource configurations (from small to ultra-large)
- Interactive execution script

### 📊 **Upgraded GUI**
- 150x150 cell grid (from 5x5)
- Agent color coding
- Enhanced trail and movement visualization

## Installation and Execution

### 1. Clone Repository
```bash
git clone https://github.com/Contzokas/wildfire-management-multiagent-system.git
cd wildfire-management-multiagent-system
```

### 2. Compilation
```bash
# Using VS Code Tasks (recommended)
Ctrl+Shift+P → "Tasks: Run Task" → "Compile JADE agents with GUI"

# Or command line:
javac -encoding UTF-8 -cp lib/jade.jar -d bin src/utils/*.java
javac -encoding UTF-8 -cp lib/jade.jar -d bin src/gui/*.java
javac -encoding UTF-8 -cp "lib/jade.jar;bin" -d bin src/agents/*.java
javac -encoding UTF-8 -cp "lib/jade.jar;bin" -d bin src/MainContainer.java
```

### 3. Execution

#### **Interactive Execution (Recommended)**
```bash
# For Windows
run_custom_english.bat

# Or using VS Code Task
"Run JADE 150x150 Custom Resources (Interactive)"
```

#### **Predefined Configurations**
- **Small**: `2 trucks, 1 aircraft, 1 helicopter, 3 crews` (4GB RAM)
- **Medium**: `4 trucks, 2 aircraft, 1 helicopter, 6 crews` (6GB RAM)
- **Large**: `8 trucks, 4 aircraft, 3 helicopters, 12 crews` (10GB RAM)
- **Extreme**: `15 trucks, 8 aircraft, 5 helicopters, 30 crews` (12GB RAM)
- **Ultra**: `20 trucks, 10 aircraft, 8 helicopters, 50 crews` (16GB RAM)

#### **Command Line**
```bash
# Basic execution
java -cp "bin;lib/jade.jar" MainContainer

# Custom configuration (trucks, aircraft, helicopters, crews)
java -cp "bin;lib/jade.jar" MainContainer custom 4 2 1 6
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
│   ├── gui/
│   │   └── FireSimulationGUI.java  # Graphical interface
│   └── utils/
│       └── GridManager.java        # Grid management
├── bin/                            # Compiled files
├── lib/
│   └── jade.jar                    # JADE Framework
├── .vscode/
│   └── tasks.json                  # VS Code tasks for easy execution
├── run_custom_english.bat          # Interactive execution script
└── README.md
```

## Usage

### **Basic Controls**
1. **Startup**: Run MainContainer or the interactive script
2. **Fire**: Click "🔥 Fire" button to simulate a fire
3. **Emergency**: Click "🚨 Emergency" to activate emergency protocol
4. **Monitor**: Observe automatic agent responses

### **Zoom & View Controls**
- **"100%" button**: Precise zoom without scaling
- **Ctrl+1**: Quick access to 100% zoom
- **Ctrl+Plus/Minus**: Zoom in/out
- **Ctrl+0**: Reset view
- **Ctrl+C**: Center view
- **"📐" button**: Auto-fit to screen

### **Agent Colors**
- 🚒 **Fire Trucks**: **RED**
- ✈️ **Aircraft**: **BLUE**
- 🚁 **Helicopters**: **MAGENTA** (Pink-Purple)
- 👥 **Ground Crews**: **ORANGE**

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