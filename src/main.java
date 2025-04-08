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



        String sender = "luca";





        // === Inlogfunctie komt hier ===
        // TODO: Vraag gebruikersnaam en wachtwoord
        // TODO: Controleer gegevens met database
        // TODO: Als succesvol, wijs sender toe met gebruikersnaam

        getMessages();

        while (true) {
            System.out.print("Voer je bericht in: ");
            String message = scanner.nextLine();
            System.out.println("Voer de issue in: ");
            String issue = scanner.nextLine();

            while (!sendIssue(issue)) {
                System.out.println("Issue bestaat niet, voer een geldig issue in:");
                issue = scanner.nextLine();
            }
            System.out.println("Bericht is gekoppeld aan issue.");

            if (sendMessage(sender, message)) {
                System.out.println("Bericht succesvol verzonden!");
            } else {
                System.out.println("Er is een fout opgetreden.");
            }
        }
    }

    public static boolean sendIssue(String issue) {
        if (issue in database) {
            return true;
        }
        return false;
    }


    public static boolean sendMessage(String sender, String message) {
        String query = "INSERT INTO messages (sender, message) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Fout bij het opslaan van het bericht: " + e.getMessage());
            return false;
        }
    }

    public static boolean sendIssue(String sender, String message) {
        String query = "INSERT INTO messages (sender, message) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Fout bij het opslaan van het bericht: " + e.getMessage());
            return false;
        }
    }





    public static void getMessages() {
        // haalt deze dingen uit de sql
        String query = "SELECT sender, message, timestamp FROM messages ORDER BY id ASC";
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
                System.out.println("[" + formattedDate + "] " + sender + ": " + message);
            }
            System.out.println("=========================");
            // ====


        } catch (SQLException e) {
            System.out.println("Fout bij het ophalen van berichten: " + e.getMessage());
        }
    }


}
