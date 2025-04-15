import java.sql.*;
import java.util.Scanner;

public class Authentication {
    private final Scanner scanner = new Scanner(System.in);

    public String authenticate() {
        String sender = "";
        boolean loggedIn = false;
        String answer = "";

        while (!(answer.equalsIgnoreCase("ja") || answer.equalsIgnoreCase("nee"))) {
            System.out.println("Heeft u al een account?");
            answer = scanner.nextLine();

            if (answer.equalsIgnoreCase("ja")) {
                while (!loggedIn) {
                    System.out.print("Voer je gebruikersnaam in: ");
                    String username = scanner.nextLine();
                    System.out.print("Voer je wachtwoord in: ");
                    String password = scanner.nextLine();

                    String query = "SELECT * FROM users WHERE username = ? AND password = ?";

                    try (Connection conn = Database.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(query)) {

                        pstmt.setString(1, username);
                        pstmt.setString(2, password);

                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                loggedIn = true;
                                sender = username;
                            } else {
                                System.out.println("Onjuiste gebruikersnaam of wachtwoord.");
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else if (answer.equalsIgnoreCase("nee")) {
                boolean accountGemaakt = false;

                while (!accountGemaakt) {
                    System.out.print("Voer een nieuwe gebruikersnaam in: ");
                    String newUsername = scanner.nextLine();

                    System.out.print("Voer een wachtwoord in: ");
                    String newPassword = scanner.nextLine();

                    String checkQuery = "SELECT * FROM users WHERE username = ?";
                    String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

                    try (Connection conn = Database.getConnection();
                         PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

                        checkStmt.setString(1, newUsername);

                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                System.out.println("Gebruikersnaam bestaat al. Kies een andere.");
                            } else {
                                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                                    insertStmt.setString(1, newUsername);
                                    insertStmt.setString(2, newPassword);
                                    insertStmt.setString(3, "developer");
                                    insertStmt.executeUpdate();

                                    System.out.println("Account succesvol aangemaakt! Je bent nu ingelogd.");
                                    loggedIn = true;
                                    accountGemaakt = true;
                                    sender = newUsername;
                                }
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Ongeldig zeg ja of nee.");
            }
        }

        return sender;
    }
}
