import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Issues {

    public static void createIssue(Scanner scanner) {
        System.out.print("Welke issue wil je nu aanmaken? ");
        String issue = scanner.nextLine();

        if (!issueFormat(issue)) {
            System.out.println("Fout bij format van issue. Gebruik bijvoorbeeld 1 of 1.1 of 1.1.1");
            return;
        }

        if (issueExists(issue)) {
            System.out.println("Issue bestaat al.");
            return;
        }

        if (saveIssueToDatabase(issue)) {
            System.out.println("Issue is opgeslagen.");
        } else {
            System.out.println("Er is iets fout gegaan bij het opslaan van de issue.");
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

    private static boolean saveIssueToDatabase(String issueId) {
        String query = "INSERT INTO issues (id) VALUES (?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, issueId);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Fout bij opslaan van issue: " + e.getMessage());
            return false;
        }
    }
}
