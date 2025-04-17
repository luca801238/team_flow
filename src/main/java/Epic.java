import java.sql.*;
import java.util.Scanner;

public class Epic implements Issue {

    @Override
    public void create(Scanner scanner) {
        System.out.print("Voer de naam van de epic in: ");
        String name = scanner.nextLine();

        String query = "INSERT INTO epics (name) VALUES (?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                System.out.println("Epic aangemaakt met ID: " + id);
            }

        } catch (SQLException e) {
            System.out.println("Fout bij aanmaken van epic: " + e.getMessage());
        }
    }

    @Override
    public void list() {
        String query = "SELECT id, name FROM epics ORDER BY id ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Epics:");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ": " + rs.getString("name"));
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen van epics: " + e.getMessage());
        }
    }
}