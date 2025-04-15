import java.util.Scanner;

public class UserMenu {

    public static void start(Scanner scanner, String sender, String role) {
        while (true) {
            System.out.println("\n[1] Bericht versturen");
            System.out.println("[2] Berichten ophalen");

            // Only show the option for creating a new issue if the role is scrummaster or product_owner
            if (role.equals("scrummaster") || role.equals("product_owner")) {
                System.out.println("[3] Nieuwe issue aanmaken");
            }

            System.out.println("[0] Uitloggen");
            System.out.print("Maak een keuze: ");
            String keuze = scanner.nextLine().trim();

            switch (keuze) {
                case "1":
                    // Sending a message
                    Messages.sendMessage(scanner, sender);
                    break;
                case "2":
                    // Reading messages
                    Messages.readMessages(scanner);
                    break;
                case "3":
                    if (role.equals("scrummaster") || role.equals("product_owner")) {
                        // Creating a new issue
                        Issues.createIssue(scanner);
                    } else {
                        System.out.println("Je hebt geen rechten om issues aan te maken.");
                    }
                    break;
                case "0":
                    // Log out and return
                    System.out.println("Uitloggen...");
                    return;
                default:
                    // Handle invalid input
                    System.out.println("Ongeldige keuze, probeer het opnieuw.");
            }
        }
    }
}
