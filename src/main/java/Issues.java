import java.sql.*;
import java.util.Scanner;

public class Issues {

    public static void createIssue(Scanner scanner) {
        System.out.println("Welk type issue wil je aanmaken? (epic/story/task)");
        String type = scanner.nextLine().trim().toLowerCase();

        Issue issue = switch (type) {
            case "epic" -> new Epic();
            case "story" -> new UserStory();
            case "task" -> new Task();
            default -> null;
        };

        if (issue != null) {
            issue.create(scanner);
        } else {
            System.out.println("Ongeldig type issue.");
        }
    }

    public static void listAllIssues() {
        String epicQuery = "SELECT id, name FROM epics ORDER BY id ASC";
        String storyQuery = "SELECT id, name, epic_id FROM user_stories WHERE epic_id = ? ORDER BY id ASC";
        String taskQuery = "SELECT id, name, story_id FROM tasks WHERE story_id = ? ORDER BY id ASC";

        try (Connection conn = Database.getConnection()) {
            System.out.println("=== Alle issues ===");

            try (PreparedStatement epicStmt = conn.prepareStatement(epicQuery);
                 ResultSet epicRs = epicStmt.executeQuery()) {

                while (epicRs.next()) {
                    String epicId = epicRs.getString("id");
                    String epicName = epicRs.getString("name");
                    System.out.println(epicId + ": " + epicName);

                    try (PreparedStatement storyStmt = conn.prepareStatement(storyQuery)) {
                        storyStmt.setString(1, epicId);
                        try (ResultSet storyRs = storyStmt.executeQuery()) {
                            while (storyRs.next()) {
                                String storyId = storyRs.getString("id");
                                String storyName = storyRs.getString("name");
                                System.out.println("    " + storyId + ": " + storyName);

                                try (PreparedStatement taskStmt = conn.prepareStatement(taskQuery)) {
                                    taskStmt.setString(1, storyId);
                                    try (ResultSet taskRs = taskStmt.executeQuery()) {
                                        while (taskRs.next()) {
                                            String taskId = taskRs.getString("id");
                                            String taskName = taskRs.getString("name");
                                            System.out.println("        " + taskId + ": " + taskName);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen van issues: " + e.getMessage());
        }
    }
}