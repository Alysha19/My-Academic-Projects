import java.io.*;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Main <input_file> <output_file>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            // create parent directories if they don't exist
            File outFile = new File(outputFile);
            File parentDir = outFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            BufferedReader reader = new BufferedReader(new FileReader(inputFile), 1 << 16);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile), 1 << 16));

            MatrixNet network = new MatrixNet();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                
                String result = processCommand(network, line);
                writer.println(result);
            }

            reader.close();
            writer.close();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String processCommand(MatrixNet network, String line) {
        // fast parsing without regex
        int len = line.length();
        int idx = 0;
        
        // skip leading whitespace
        while (idx < len && line.charAt(idx) == ' ') idx++;

        int cmdStart = idx;
        while (idx < len && line.charAt(idx) != ' ') idx++;
        String command = line.substring(cmdStart, idx);

        String[] tokens = new String[6];
        int tokenCount = 0;
        
        while (idx < len && tokenCount < 6) {
            // Skip whitespace
            while (idx < len && line.charAt(idx) == ' ') idx++;
            if (idx >= len) break;
            
            int tokenStart = idx;
            while (idx < len && line.charAt(idx) != ' ') idx++;
            tokens[tokenCount++] = line.substring(tokenStart, idx);
        }

        switch (command) {
            case "spawn_host":
                return network.spawnHost(tokens[0], parseInt(tokens[1]));

            case "link_backdoor":
                return network.linkBackdoor(
                        tokens[0],
                        tokens[1],
                        parseInt(tokens[2]),
                        parseInt(tokens[3]),
                        parseInt(tokens[4])
                );

            case "seal_backdoor":
                return network.sealBackdoor(tokens[0], tokens[1]);

            case "trace_route":
                return network.traceRoute(
                        tokens[0],
                        tokens[1],
                        parseInt(tokens[2]),
                        parseInt(tokens[3])
                );

            case "scan_connectivity":
                return network.scanConnectivity();

            case "simulate_breach":
                if (tokenCount == 1) {
                    return network.simulateBreachHost(tokens[0]);
                } else {
                    return network.simulateBreachBackdoor(tokens[0], tokens[1]);
                }

            case "oracle_report":
                return network.oracleReport();

            default:
                return "Unknown command: " + command;
        }
    }

    private static int parseInt(String s) {
        int result = 0;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            result = result * 10 + (s.charAt(i) - '0');
        }
        return result;
    }
}
