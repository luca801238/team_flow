import java.sql.*;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class main {
    private static final String URL = "jdbc:mysql://localhost:3306/new_schema";
    private static final String USER = "root";
    private static final String PASSWORD = "School123!";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);



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

                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
                    String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";

                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                         PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

                        checkStmt.setString(1, newUsername);

                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                System.out.println("Gebruikersnaam bestaat al. Kies een andere.");
                            } else {
                                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                                    insertStmt.setString(1, newUsername);
                                    insertStmt.setString(2, newPassword);
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
            }
            else {
                System.out.println("Ongeldig zeg ja of nee.");
            }
        }




        getMessages();

        while (true) {
            System.out.print("Voer je bericht in: ");
            String message = scanner.nextLine();
            System.out.print("Voer de issue in: ");
            String issueid = scanner.nextLine();

            while (!sendIssue(issueid)) {
                System.out.println("Issue bestaat niet, voer een geldig issue in:");
                issueid = scanner.nextLine();
            }
            System.out.println("Bericht is gekoppeld aan issue.");

            if (sendMessage(sender, message, issueid)) {
                System.out.println("Bericht succesvol verzonden!");
            } else {
                System.out.println("Er is een fout opgetreden.");
            }
        }
    }

    public static boolean sendIssue(String issueid) {
        String query = "SELECT 1 FROM issues WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, issueid);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return true;  // Issue bestaat
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }


    public static boolean sendMessage(String sender, String message, String issueid) {
        String query = "INSERT INTO messages (sender, message, issueid) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.setString(3, issueid);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Fout bij het opslaan van het bericht: " + e.getMessage());
            return false;
        }
    }

    public static String getRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("role"); // Retourneer de rol van de gebruiker
            } else {
                return null;
            }
        } catch (SQLException exception) {
            System.out.println("Error bij getRole functie: " + exception.getMessage());
            return null;
        }
    }


    public static void getMessages() {
        // haalt deze dingen uit de sql
        String query = "SELECT sender, message, timestamp, issueid FROM messages ORDER BY id ASC";
        // formatter van de datum
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm", new Locale("nl", "NL"));


        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {


            // berichten ingeladen
            System.out.println("=== Vorige berichten ===");
            while (rs.next()) {
                // datum + formatter
                Timestamp timestamp = rs.getTimestamp("timestamp");
                LocalDateTime dateTime = timestamp.toLocalDateTime();
                String formattedDate = dateTime.format(formatter);


                String sender = rs.getString("sender");
                String message = rs.getString("message");
                String issueid = rs.getString("issueid");
                System.out.println("[" + formattedDate + "] " + sender + ": " + "[" + issueid + "] " + message);
            }
            System.out.println("=========================");
            // ====


        } catch (SQLException e) {
            System.out.println("Fout bij het ophalen van berichten: " + e.getMessage());
        }
    }


}
