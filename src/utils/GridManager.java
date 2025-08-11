package utils;

public class GridManager {
    public static final int GRID_SIZE = 150;
    
    public static String generateTreeAgentName(int x, int y) {
        return "tree_" + x + "_" + y;
    }
    
    public static String generateTreeAgentList() {
        StringBuilder agentList = new StringBuilder();
        
        for (int x = 1; x <= GRID_SIZE; x++) {
            for (int y = 1; y <= GRID_SIZE; y++) {
                if (agentList.length() > 0) {
                    agentList.append(";");
                }
                agentList.append(generateTreeAgentName(x, y))
                         .append(":agents.TreeAgent(")
                         .append(x).append(",").append(y).append(")");
            }
        }
        
        return agentList.toString();
    }
    
    public static String generateSampleFireAgents(int numFires) {
        StringBuilder fireList = new StringBuilder();
        java.util.Random rand = new java.util.Random();
        
        for (int i = 1; i <= numFires; i++) {
            int x = 1 + rand.nextInt(GRID_SIZE);
            int y = 1 + rand.nextInt(GRID_SIZE);
            
            if (fireList.length() > 0) {
                fireList.append(";");
            }
            fireList.append("fire").append(i)
                    .append(":agents.FireAgent(")
                    .append(x).append(",").append(y).append(")");
        }
        
        return fireList.toString();
    }
}