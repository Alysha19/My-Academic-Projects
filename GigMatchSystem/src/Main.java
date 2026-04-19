import java.io.*;
import java.util.Locale;

/**
 * Main entry point for GigMatch Pro platform.
 */
public class Main {
    private static GigMatchSystem system = new GigMatchSystem();
    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                processCommand(line, writer);
            }

        } catch (IOException e) {
            System.err.println("Error reading/writing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processCommand(String command, BufferedWriter writer)
            throws IOException {

        String[] parts = command.split("\\s+");
        String operation = parts[0];

        try {
            String result = "";

            switch (operation) {
                case "register_customer":
                    // Format: register_customer customerID
                    result = system.registerCustomer(parts[1]);
                    break;

                case "register_freelancer":
                    // Format: register_freelancer freelancerID serviceName basePrice T C R E A
                    result = system.registerFreelancer(
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6]),
                            Integer.parseInt(parts[7]),
                            Integer.parseInt(parts[8])

                    );
                    break;

                case "request_job":
                    // Format: request_job customerID serviceName topK
                    result = system.requestJob(
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3])
                    );

                    break;

                case "employ_freelancer":
                    // Format: employ_freelancer customerID freelancerID
                    result = system.employFreelancer(parts[1] , parts[2]);
                    break;

                case "complete_and_rate":
                    // Format: complete_and_rate freelancerID rating
                    result = system.completeAndRate(parts[1], Integer.parseInt(parts[2]));
                    break;

                case "cancel_by_freelancer":
                    // Format: cancel_by_freelancer freelancerID
                    result = system.cancelByFreelancer(parts[1]);
                    break;

                case "cancel_by_customer":
                    // Format: cancel_by_customer customerID freelancerID
                    result = system.cancelByCustomer(parts[1] , parts[2]);
                    break;

                case "blacklist":
                    // Format: blacklist customerID freelancerID
                    result = system.blacklist(parts[1] , parts[2]);
                    break;

                case "unblacklist":
                    // Format: unblacklist customerID freelancerID
                    result = system.unblacklist(parts[1] , parts[2]);
                    break;

                case "change_service":
                    // Format: change_service freelancerID newService newPrice
                    result = system.changeService(
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3])
                    );
                    break;

                case "simulate_month":
                    // Format: simulate_month
                    result = system.simulateMonth();
                    break;

                case "query_freelancer":
                    // Format: query_freelancer freelancerID
                    result = system.queryFreelancer(parts[1]);
                    break;

                case "query_customer":
                    // Format: query_customer customerID
                    result = system.queryCustomer(parts[1]);
                    break;

                case "update_skill":
                    // Format: update_skill freelancerID T C R E A
                    result = system.updateSkill(
                            parts[1],
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6])
                    );
                    break;

                default:
                    result = "Unknown command: " + operation;
            }

            writer.write(result);
            writer.newLine();

        } catch (Exception e) {
            writer.write("Error processing command: " + command);
            writer.newLine();
        }
    }
}