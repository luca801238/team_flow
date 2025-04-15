import java.util.Scanner;

public class UserMenu {

    public static void start(Scanner scanner, String sender, String role) {
        while (true) {
            System.out.println("\n[1] Bericht versturen");
            System.out.println("[2] Berichten tonen");
            System.out.println("[3] Alle issues tonen");
            System.out.println("[4] Alle sprints tonen");

            if (role.equals("scrum_master") || role.equals("product_owner")) {
                System.out.println("[5] Nieuwe issue aanmaken");
            }

            if (role.equals("scrum_master")) {
                System.out.println("[6] Nieuwe sprint aanmaken");
            }

            System.out.println("[0] Uitloggen");
            System.out.print("Maak een keuze: ");
            String keuze = scanner.nextLine().trim();

            switch (keuze) {
                case "1":
                    Messages.sendMessage(scanner, sender);
                    break;
                case "2":
                    Messages.readMessages(scanner);
                    break;
                case "3":
                    Issues.listAllIssues();
                    break;
                case "4":
                    Sprints.listAllSprints();
                    break;
                case "5":
                    if (role.equals("scrum_master") || role.equals("product_owner")) {
                        Issues.createIssue(scanner);
                    } else {
                        System.out.println("Je hebt geen rechten om issues aan te maken.");
                    }
                    break;
                case "6":
                    if (role.equals("scrum_master")) {
                        Sprints.createSprint(scanner);
                    } else {
                        System.out.println("Je hebt geen rechten om sprints aan te maken.");
                    }
                    break;
                case "0":
                    System.out.println("Uitloggen...");
                    return;
                default:
                    System.out.println("Ongeldige keuze, probeer het opnieuw.");
            }
        }
    }
}