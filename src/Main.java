import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/new_schema";
    private static final String USER = "root";
    private static final String PASSWORD = "wachtwoord123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Voer je naam in: ");
        String sender = scanner.nextLine();

        System.out.print("Voer je bericht in: ");
        String message = scanner.nextLine();

        if (sendMessage(sender, message)) {
            System.out.println("Bericht succesvol verzonden!");
        } else {
            System.out.println("Er is een fout opgetreden.");
        }

        scanner.close();
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
}
