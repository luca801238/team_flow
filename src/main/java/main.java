import java.sql.*;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class main {
    private static final String URL = "jdbc:mysql://localhost:3306/new_schema";
    private static final String USER = "root";
    private static final String PASSWORD = "wachtwoord123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String sender = "";
        boolean loggedIn = false;
        String answer = "";

        // Vraag de gebruiker of hij/zij al een account heeft.
        while (!(answer.equalsIgnoreCase("ja") || answer.equalsIgnoreCase("nee"))) {
            System.out.println("Heeft u al een account? (ja/nee)");
            answer = scanner.nextLine();
            if (answer.equalsIgnoreCase("ja")) {
                // Inloggen
                while (!loggedIn) {
                    System.out.print("Voer je gebruikersnaam in: ");
                    String username = scanner.nextLine();
                    System.out.print("Voer je wachtwoord in: ");
                    String password = scanner.nextLine();

                    String query = "SELECT * FROM user WHERE username = ? AND password = ?";

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
                // Account aanmaken
                boolean accountGemaakt = false;
                while (!accountGemaakt) {
                    System.out.print("Voer een nieuwe gebruikersnaam in: ");
                    String newUsername = scanner.nextLine();
                    System.out.print("Voer een wachtwoord in: ");
                    String newPassword = scanner.nextLine();

                    // Voor dit voorbeeld gebruiken we een standaard scrumboardId (bijv. 1) voor nieuwe gebruikers.
                    int defaultScrumboardId = 1;
                    String checkQuery = "SELECT * FROM user WHERE username = ?";
                    String insertQuery = "INSERT INTO user (username, password, rol, scrumboardId) VALUES (?, ?, ?, ?)";

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
                                    insertStmt.setString(3, "developer");
                                    insertStmt.setInt(4, defaultScrumboardId);
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
                System.out.println("Ongeldig, zeg ja of nee.");
            }
        }

        // Haal het scrumboardId op van de ingelogde gebruiker.
        int userScrumboardId = getScrumboardId(sender);
        // Toon bestaande berichten van het scrumboard van de gebruiker.
        getMessages(sender, userScrumboardId);

        // Hoofdloop: issues en berichten
        while (true) {
            String role = getRole(sender);
            if (role != null && (role.equalsIgnoreCase("scrummaster") || role.equalsIgnoreCase("product_owner"))) {
                while (true) {
                    System.out.print("Wil je een nieuwe issue aanmaken? (ja/nee): ");
                    String issueofNiet = scanner.nextLine();
                    if (issueofNiet.equalsIgnoreCase("nee")) {
                        System.out.println("");
                        break;
                    } else if (issueofNiet.equalsIgnoreCase("ja")) {
                        System.out.print("Welke issue wil je nu aanmaken? ");
                        String issue = scanner.nextLine();
                        if (sendIssue(issue)) {
                            System.out.println("Issue bestaat al.");
                        } else if (!issueFormat(issue)) {
                            System.out.println("Fout bij format van issue.");
                        } else {
                            if (issueNaarDB(issue, userScrumboardId)) {
                                System.out.println("Issue is opgeslagen.");
                            } else {
                                System.out.println("Error bij opslaan van de issue.");
                            }
                        }
                    } else {
                        System.out.println("Ongeldige invoer.");
                    }
                }
            }

            System.out.print("Voer je bericht in: ");
            String message = scanner.nextLine();
            System.out.print("Voer de issue in: ");
            String issueid = scanner.nextLine();

            while (!sendIssue(issueid)) {
                System.out.println("Issue bestaat niet, voer een geldig issue in:");
                issueid = scanner.nextLine();
            }
            System.out.println("Bericht is gekoppeld aan issue.");

            if (sendMessage(sender, message, issueid, userScrumboardId)) {
                System.out.println("Bericht succesvol verzonden!");
            } else {
                System.out.println("Er is een fout opgetreden.");
            }
        }
    }

    // Controleer of een issue (berichtId) al bestaat
    public static boolean sendIssue(String issueid) {
        String query = "SELECT 1 FROM bericht WHERE berichtId = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, issueid);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();  // Issue bestaat als er een resultaat is.
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // Sla een nieuwe issue op in de database, met koppeling naar het scrumboard
    public static boolean issueNaarDB(String issue, int scrumboardId) {
        String query = "INSERT INTO bericht (berichtId, scrumboardId) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, issue);
            pstmt.setInt(2, scrumboardId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Fout bij het opslaan van de issue: " + e.getMessage());
            return false;
        }
    }

    // Controleer of het issue-formaat klopt (bijv. "1" of "1.1" of "1.1.1")
    public static boolean issueFormat(String issue) {
        return issue.matches("^\\d+(\\.\\d{1,3}){0,2}$");
    }

    // Sla een bericht op in de database, samen met de scrumboardId
    public static boolean sendMessage(String sender, String message, String issueid, int scrumboardId) {
        String query = "INSERT INTO bericht (sender, bericht, berichtId, scrumboardId) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.setString(3, issueid);
            pstmt.setInt(4, scrumboardId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Fout bij het opslaan van het bericht: " + e.getMessage());
            return false;
        }
    }

    // Haal de rol van een gebruiker op
    public static String getRole(String username) {
        String query = "SELECT rol FROM user WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("rol");
                } else {
                    return null;
                }
            }
        } catch (SQLException exception) {
            System.out.println("Error bij getRole functie: " + exception.getMessage());
            return null;
        }
    }

    // Haal de berichten op van het scrumboard waaraan de gebruiker is gekoppeld
    public static void getMessages(String sender, int scrumboardId) {
        String query = "SELECT sender, bericht, timestamp, berichtId FROM bericht WHERE scrumboardId = ? ORDER BY berichtId ASC";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm", new Locale("nl", "NL"));
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, scrumboardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("=== Vorige berichten ===");
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("timestamp");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    String formattedDate = dateTime.format(formatter);
                    String messageSender = rs.getString("sender");
                    String message = rs.getString("bericht");
                    String issueId = rs.getString("berichtId");
                    System.out.println("[" + formattedDate + "] " + messageSender + ": " + "[" + issueId + "] " + message);
                }
                System.out.println("=========================");
            }
        } catch (SQLException e) {
            System.out.println("Fout bij het ophalen van berichten: " + e.getMessage());
        }
    }

    // Haal het scrumboardId op van een gebruiker.
    public static int getScrumboardId(String username) {
        String query = "SELECT scrumboardId FROM user WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("scrumboardId");
                } else {
                    System.out.println("Gebruiker heeft geen scrumboardId.");
                    return -1;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error bij ophalen scrumboardId: " + e.getMessage());
            return -1;
        }
    }
}
