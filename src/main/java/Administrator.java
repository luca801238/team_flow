import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

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

    public static void showAllUsers() {
        String[] roleOrder = {"administrator", "scrum_master", "product_owner", "developer"};

        for (String role : roleOrder) {
            List<String> users = new ArrayList<>();

            String query = "SELECT username FROM users WHERE role = ? ORDER BY username";

            try (Connection conn = Database.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, role);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    users.add(rs.getString("username"));
                }

                if (!users.isEmpty()) {
                    System.out.println("=== " + role.toUpperCase().replace("_", " ") + "S ===");
                    for (String user : users) {
                        System.out.println("- " + user);
                    }
                }

            } catch (SQLException e) {
                System.out.println("Fout bij ophalen van gebruikers voor rol " + role + ": " + e.getMessage());
            }
        }
    }

    private void changeUserRole(Scanner scanner) {
        String targetUser;

        while (true) {
            System.out.print("Gebruikersnaam waarvan je de rol wil aanpassen: ");
            targetUser = scanner.nextLine();

            if (targetUser.equals(username)) {
                System.out.println("Je mag je eigen rol niet aanpassen.");
                return;
            }

            String currentRole = Users.getRole(targetUser);
            if (currentRole == null) {
                System.out.println("Gebruiker bestaat niet.");
            } else {
                System.out.println(targetUser + " [" + currentRole + "]");
                break;
            }
        }

        String newRole;
        while (true) {
            System.out.print("Nieuwe rol (developer, scrum_master, product_owner, administrator): ");
            newRole = scanner.nextLine();

            if (!Users.isValidRole(newRole)) {
                System.out.println("Ongeldige rol.");
            } else {
                break;
            }
        }

        if (Users.updateRole(targetUser, newRole)) {
            System.out.println("Rol succesvol aangepast.");
        } else {
            System.out.println("Kon rol niet aanpassen.");
        }
    }

    private void deleteUser(Scanner scanner) {
        String targetUser;
        String role;

        while (true) {
            System.out.print("Gebruikersnaam van de gebruiker die je wil verwijderen: ");
            targetUser = scanner.nextLine();

            if (targetUser.equals(username)) {
                System.out.println("Je kunt jezelf niet verwijderen.");
                continue;
            }

            role = Users.getRole(targetUser);
            if (role == null) {
                System.out.println("Gebruiker bestaat niet.");
                continue;
            }

            if (role.equalsIgnoreCase("administrator")) {
                System.out.println("Je kunt geen administrator verwijderen.");
                continue;
            }

            break;
        }

        while (true) {
            System.out.print("Weet je zeker dat je " + targetUser + " [" + role + "] wilt verwijderen? (ja/nee): ");
            String bevestiging = scanner.nextLine().toLowerCase();

            if (bevestiging.equals("ja")) {
                if (Users.deleteUser(targetUser)) {
                    System.out.println("Gebruiker succesvol verwijderd.");
                } else {
                    System.out.println("Kon gebruiker niet verwijderen.");
                }
                break;
            } else if (bevestiging.equals("nee")) {
                System.out.println("Verwijderen van gebruiker geannuleerd.");
                break;
            } else {
                System.out.println("Ongeldige invoer. Antwoord met 'ja' of 'nee'.");
            }
        }
    }
}