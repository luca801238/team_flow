import java.util.Scanner;

public class DeveloperMenu {
    public static void show(String username) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nWelkom " + username + " (Developer)");
            System.out.println("1. Bekijk taken");
            System.out.println("2. Voeg bericht toe");
            System.out.println("0. Uitloggen");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    // Call method to view tasks
                    break;
                case "2":
                    // Call method to send message
                    break;
                case "0":
                    System.out.println("Tot ziens!");
                    return;
                default:
                    System.out.println("Ongeldige keuze");
            }
        }
    }
}