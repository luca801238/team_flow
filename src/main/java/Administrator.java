import java.sql.*;
import java.util.Scanner;

public class Administrator extends User {

    public Administrator(String username) {
        super(username);
    }

    @Override
    public void showMenu(Scanner scanner) {
        while (true) {
            System.out.println("[1] Bekijk alle gebruikers");
            System.out.println("[2] Wijzig gebruikersrol");
            System.out.println("[3] Verwijder gebruiker");
            System.out.println("[0] Uitloggen");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> showAllUsers();
                case "2" -> changeUserRole(scanner);
                case "3" -> deleteUser(scanner);
                case "0" -> { return; }
                default -> System.out.println("Ongeldige keuze.");
            }
        }
    }

    private void showAllUsers() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT username, role FROM users")) {

            System.out.println("\nGebruikers:");
            while (rs.next()) {
                String name = rs.getString("username");
                String role = rs.getString("role");
                System.out.println("- " + name + " [" + role + "]");
            }
        } catch (SQLException e) {
            System.out.println("Fout bij ophalen gebruikers: " + e.getMessage());
        }
    }

    private void changeUserRole(Scanner scanner) {
        System.out.print("Gebruikersnaam waarvan je de rol wil aanpassen: ");
        String targetUser = scanner.nextLine();

        if (targetUser.equals(username)) {
            System.out.println("Je mag je eigen rol niet aanpassen.");
            return;
        }

        String currentRole = Users.getRole(targetUser);
        if (currentRole == null) {
            System.out.println("Gebruiker bestaat niet.");
            return;
        }

        System.out.println(targetUser + " [" + currentRole + "]");
        System.out.print("Nieuwe rol (developer, scrum_master, product_owner, administrator): ");
        String newRole = scanner.nextLine();

        if (!Users.isValidRole(newRole)) {
            System.out.println("Ongeldige rol.");
            return;
        }

        if (Users.updateRole(targetUser, newRole)) {
            System.out.println("Rol succesvol aangepast.");
        } else {
            System.out.println("Kon rol niet aanpassen.");
        }
    }

    private void deleteUser(Scanner scanner) {
        System.out.print("Gebruikersnaam van de gebruiker die je wil verwijderen: ");
        String targetUser = scanner.nextLine();

        if (Users.deleteUser(targetUser)) {
            System.out.println("Gebruiker succesvol verwijderd.");
        }
    }
}