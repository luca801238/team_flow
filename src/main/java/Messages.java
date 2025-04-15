import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Messages {

    public static void readMessages(Scanner scanner) {
        System.out.print("Wil je berichten ophalen van een bepaalde issue? (ja/nee): ");
        String antwoord = scanner.nextLine();

        if (antwoord.equalsIgnoreCase("nee")) {
            System.out.println("Geen berichten opgehaald.");
            return;
        }

        if (!antwoord.equalsIgnoreCase("ja")) {
            System.out.println("Ongeldige invoer.");
            return;
        }

        System.out.print("Van welke issue?: ");
        String issueId = scanner.nextLine();

        if (!issueFormat(issueId)) {
            System.out.println("Fout bij format van issue. Gebruik bijvoorbeeld 1, 1.1 of 1.1.1");
            return;
        }

        if (!issueExists(issueId)) {
            System.out.println("Issue bestaat niet.");
            return;
        }

        String query = "SELECT sender, message, timestamp FROM messages WHERE issueid = ? ORDER BY timestamp ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, issueId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n--- Berichten voor issue: " + issueId + " ---");

            boolean found = false;
            while (rs.next()) {
                String sender = rs.getString("sender");
                String content = rs.getString("message");
                String timestamp = rs.getString("timestamp");
                System.out.println("[" + timestamp + "] " + sender + ": " + content);
                found = true;
            }

            if (!found) {
                System.out.println("Geen berichten gevonden.");
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen van berichten: " + e.getMessage());
        }
    }

    private static boolean issueFormat(String issue) {
        return issue.matches("^\\d+(\\.\\d+){0,2}$");
    }

    private static boolean issueExists(String issueId) {
        String query = "SELECT 1 FROM issues WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, issueId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Fout bij controleren of issue bestaat: " + e.getMessage());
            return false;
        }
    }

    public static void sendMessage(Scanner scanner, String sender) {
        System.out.print("Voer je bericht in: ");
        String message = scanner.nextLine();

        System.out.print("Voer de issue in: ");
        String issueId = scanner.nextLine();

        // Validate the issue ID format
        while (!issueExists(issueId)) {
            System.out.println("Issue bestaat niet, voer een geldig issue in:");
            issueId = scanner.nextLine();
        }

        // Insert the message into the database
        String query = "INSERT INTO messages (sender, message, issueid) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.setString(3, issueId);
            pstmt.executeUpdate();

            System.out.println("Bericht succesvol verzonden!");

        } catch (SQLException e) {
            System.out.println("Fout bij het opslaan van het bericht: " + e.getMessage());
        }
    }
}
