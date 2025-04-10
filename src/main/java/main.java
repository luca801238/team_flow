import java.sql.*;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.sql.Connection;


public class    main {
    private static final String URL = "jdbc:mysql://localhost:3306/new_schema";
    private static final String USER = "root";
    private static final String PASSWORD = "School123!";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);



        String sender = "luca";





        // === Inlogfunctie komt hier ===
        // TODO: Vraag gebruikersnaam en wachtwoord
        // TODO: Controleer gegevens met database
        // TODO: Als succesvol, wijs sender toe met gebruikersnaam

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

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
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
