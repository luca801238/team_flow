import java.sql.*;
import java.util.Scanner;

public class Task implements Issue {

    @Override
    public void create(Scanner scanner) {
        System.out.print("Voor welke user story wil je deze taak aanmaken? (geef story ID, zoals 1.3): ");
        String storyId = scanner.nextLine().trim();

        if (!storyExists(storyId)) {
            System.out.println("User story met ID " + storyId + " bestaat niet.");
            return;
        }

        System.out.print("Voer de naam van de taak in: ");
        String name = scanner.nextLine();

        String nextId = generateNextTaskId(storyId);

        String query = "INSERT INTO tasks (id, name, story_id) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nextId);
            stmt.setString(2, name);
            stmt.setString(3, storyId);
            stmt.executeUpdate();

            System.out.println("Taak aangemaakt met ID: " + nextId);

        } catch (SQLException e) {
            System.out.println("Fout bij aanmaken van taak: " + e.getMessage());
        }
    }

    private boolean storyExists(String storyId) {
        String query = "SELECT 1 FROM user_stories WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, storyId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Fout bij controleren van story: " + e.getMessage());
            return false;
        }
    }

    private String generateNextTaskId(String storyId) {
        String query = "SELECT id FROM tasks WHERE story_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, storyId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String lastId = rs.getString("id");
                String[] parts = lastId.split("\\.");
                int lastPart = Integer.parseInt(parts[2]);
                return storyId + "." + (lastPart + 1);
            } else {
                return storyId + ".1";
            }

        } catch (SQLException e) {
            System.out.println("Fout bij genereren van task ID: " + e.getMessage());
            return storyId + ".1";
        }
    }

    @Override
    public void list() {
        String query = "SELECT id, name FROM tasks ORDER BY id ASC";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Taken:");
            while (rs.next()) {
                System.out.println(rs.getString("id") + ": " + rs.getString("name"));
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen van taken: " + e.getMessage());
        }
    }
}