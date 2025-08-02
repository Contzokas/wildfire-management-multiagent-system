import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime;

public class MainContainer {
    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");

        ContainerController cc = rt.createMainContainer(p);

        try {
            AgentController fire = cc.createNewAgent("fire1", "agents.FireAgent", null);
            AgentController truck = cc.createNewAgent("truck1", "agents.FireTruckAgent", null);

            fire.start();
            truck.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
