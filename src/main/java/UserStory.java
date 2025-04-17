import java.sql.*;
import java.util.Scanner;

public class UserStory implements Issue {

    @Override
    public void create(Scanner scanner) {
        System.out.print("Voor welk epic wil je deze story aanmaken? (geef epic ID): ");
        int epicId = Integer.parseInt(scanner.nextLine());

        if (!epicExists(epicId)) {
            System.out.println("Epic met ID " + epicId + " bestaat niet.");
            return;
        }

        System.out.print("Voer de naam van de story in: ");
        String name = scanner.nextLine();

        String nextId = generateNextStoryId(epicId);

        String query = "INSERT INTO user_stories (id, name, epic_id) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nextId);
            stmt.setString(2, name);
            stmt.setInt(3, epicId);
            stmt.executeUpdate();

            System.out.println("Story aangemaakt met ID: " + nextId);

        } catch (SQLException e) {
            System.out.println("Fout bij aanmaken van user story: " + e.getMessage());
        }
    }

    private boolean epicExists(int epicId) {
        String query = "SELECT 1 FROM epics WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, epicId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Fout bij controleren van epic: " + e.getMessage());
            return false;
        }
    }

    private String generateNextStoryId(int epicId) {
        String query = "SELECT id FROM user_stories WHERE epic_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, epicId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String lastId = rs.getString("id");
                int lastPart = Integer.parseInt(lastId.split("\\.")[1]);
                return epicId + "." + (lastPart + 1);
            } else {
                return epicId + ".1";
            }
        } catch (SQLException e) {
            System.out.println("Fout bij genereren van story ID: " + e.getMessage());
            return epicId + ".1";
        }
    }

    @Override
    public void list() {
        String query = "SELECT id, name FROM user_stories ORDER BY id ASC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("User Stories:");
            while (rs.next()) {
                System.out.println(rs.getString("id") + ": " + rs.getString("name"));
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen van user stories: " + e.getMessage());
        }
    }
}