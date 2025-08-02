package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class EmergencyResponseAgent extends Agent {
    private boolean activated = false;
    
    @Override
    protected void setup() {
        System.out.println(getLocalName() + ": 🚨 Μονάδα Έκτακτης Ανάγκης σε ετοιμότητα");
        
        // Ειδοποίηση διαθεσιμότητας
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("EMERGENCY_UNIT_AVAILABLE");
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                
                if (msg != null) {
                    String content = msg.getContent();
                    
                    if (content.equals("ACTIVATE_EMERGENCY_PROTOCOL")) {
                        activateEmergencyProtocol();
                    } else if (content.equals("STATUS_REQUEST")) {
                        sendAvailabilityStatus();
                    }
                } else {
                    block();
                }
            }
        });
    }
    
    private void activateEmergencyProtocol() {
        if (!activated) {
            activated = true;
            System.out.println("\n" + getLocalName() + ": 🚨🚨🚨 ΕΝΕΡΓΟΠΟΙΗΣΗ ΠΡΩΤΟΚΟΛΛΟΥ ΕΚΤΑΚΤΗΣ ΑΝΑΓΚΗΣ 🚨🚨🚨");
            System.out.println(getLocalName() + ": 📞 Κλήση ενισχύσεων από:");
            System.out.println(getLocalName() + ": - Γειτονικές πυροσβεστικές υπηρεσίες");
            System.out.println(getLocalName() + ": - Στρατιωτικές δυνάμεις");
            System.out.println(getLocalName() + ": - Εθελοντικές ομάδες");
            System.out.println(getLocalName() + ": - Διεθνή βοήθεια");
            
            doWait(3000);
            
            System.out.println(getLocalName() + ": ✅ Ενισχύσεις ειδοποιημένες - Αναμένεται άφιξη");
            
            // Προσομοίωση άφιξης ενισχύσεων
            simulateReinforcements();
        }
    }
    
    private void simulateReinforcements() {
        System.out.println(getLocalName() + ": 🚁 Άφιξη επιπλέον ελικοπτέρων");
        System.out.println(getLocalName() + ": 🚒 Άφιξη πυροσβεστικών οχημάτων από άλλες περιοχές");
        System.out.println(getLocalName() + ": 👥 Κινητοποίηση εθελοντών");
        System.out.println(getLocalName() + ": 🏥 Ετοιμότητα ιατρικών μονάδων");
        
        // Ειδοποίηση κέντρου ελέγχου
        ACLMessage reinforcements = new ACLMessage(ACLMessage.INFORM);
        reinforcements.setContent("REINFORCEMENTS_ARRIVING");
        reinforcements.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(reinforcements);
    }
    
    private void sendAvailabilityStatus() {
        ACLMessage available = new ACLMessage(ACLMessage.INFORM);
        available.setContent("EMERGENCY_UNIT_AVAILABLE");
        available.addReceiver(new jade.core.AID("firecontrol", jade.core.AID.ISLOCALNAME));
        send(available);
    }
}