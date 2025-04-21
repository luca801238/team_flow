import java.util.Scanner;

public class Developer extends User {

    public Developer(String username) {
        super(username);
    }

    @Override
    public void showMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n[1] Bericht versturen");
            System.out.println("[2] Berichten tonen");
            System.out.println("[3] Alle issues tonen");
            System.out.println("[4] Alle sprints tonen");
            System.out.println("[0] Uitloggen");

            System.out.print("Maak een keuze: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> Messages.sendMessage(scanner, username);
                case "2" -> Messages.readMessages(scanner);
                case "3" -> Issues.listAllIssues();
                case "4" -> Sprints.listAllSprints();
                case "0" -> { return; }
                default -> System.out.println("Ongeldige keuze.");
            }
        }
    }
}