import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class Messages {

    public static void displayMessagesBySprint() {
        String sprintQuery = "SELECT id, name, start_date, end_date FROM sprints WHERE start_date <= NOW() ORDER BY start_date ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement sprintStmt = conn.prepareStatement(sprintQuery);
             ResultSet sprintRs = sprintStmt.executeQuery()) {

            while (sprintRs.next()) {
                String sprintName = sprintRs.getString("name");
                Timestamp start = sprintRs.getTimestamp("start_date");
                Timestamp end = sprintRs.getTimestamp("end_date");

                SimpleDateFormat sprintDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String formattedStart = sprintDateFormat.format(start);
                String formattedEnd = sprintDateFormat.format(end);
                System.out.println("=== [" + formattedStart + "] " + sprintName + " [" + formattedEnd + "] ===");

                String messageQuery = """
                    SELECT sender, message, timestamp, issue_id
                    FROM messages
                    WHERE timestamp BETWEEN ? AND ?
                    ORDER BY timestamp ASC
                    """;

                try (PreparedStatement msgStmt = conn.prepareStatement(messageQuery)) {
                    msgStmt.setTimestamp(1, start);
                    msgStmt.setTimestamp(2, end);
                    ResultSet msgRs = msgStmt.executeQuery();

                    boolean hasMessages = false;
                    while (msgRs.next()) {
                        String sender = msgRs.getString("sender");
                        String msg = msgRs.getString("message");
                        String issueId = msgRs.getString("issue_id");
                        Timestamp timeStamp = msgRs.getTimestamp("timestamp");

                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        String formattedTime = sdf.format(timeStamp);

                        System.out.println("[" + formattedTime + "] (" + issueId + ") " + sender + ": " + msg);
                        hasMessages = true;
                    }

                    if (!hasMessages) {
                        System.out.println("Geen berichten in deze sprint.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen van berichten per sprint: " + e.getMessage());
        }
    }

    public static void readMessages(Scanner scanner) {
        while (true) {
            System.out.println("Wil je berichten ophalen van een bepaalde issue of sprint? (issue/sprint)");
            String keuze = scanner.nextLine();

            if (keuze.equalsIgnoreCase("issue")) {
                System.out.println("Van welk type issue? (epic/user_story/task): ");
                String issueType = scanner.nextLine().toLowerCase();

                while (!(issueType.equals("epic") || issueType.equals("user_story") || issueType.equals("task"))) {
                    System.out.println("Ongeldige type. Kies 'epic', 'user_story' of 'task'.");
                    issueType = scanner.nextLine().toLowerCase();
                }

                System.out.println("Voer de ID van het issue in: ");
                String issueId = scanner.nextLine();

                if (!issueExists(issueType, issueId)) {
                    System.out.println("Dit issue bestaat niet.");
                    continue;
                }

                String query = "SELECT sender, message, timestamp FROM messages WHERE issue_id = ? AND issue_type = ? ORDER BY timestamp ASC";

                try (Connection conn = Database.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    pstmt.setString(1, issueId);
                    pstmt.setString(2, issueType);
                    ResultSet rs = pstmt.executeQuery();

                    System.out.println("\n=== Berichten voor issue: " + issueId + " ===");

                    boolean found = false;
                    while (rs.next()) {
                        String sender = rs.getString("sender");
                        String content = rs.getString("message");
                        String timestamp = rs.getString("timestamp");
                        System.out.println("[" + timestamp + "] " + sender + ": " + content);
                        found = true;
                    }

                    if (!found) {
                        System.out.println("Geen berichten gevonden voor dit issue.");
                    }

                } catch (SQLException e) {
                    System.out.println("Fout bij ophalen van berichten: " + e.getMessage());
                }

                break;
            } else if (keuze.equalsIgnoreCase("sprint")) {
                System.out.println("Van welke sprint?");
                String sprintName = scanner.nextLine();

                String query = "SELECT m.sender, m.message, m.timestamp FROM messages m " +
                        "JOIN sprints s ON m.timestamp BETWEEN s.start_date AND s.end_date " +
                        "WHERE s.name = ? ORDER BY m.timestamp ASC";

                try (Connection conn = Database.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    pstmt.setString(1, sprintName);
                    ResultSet rs = pstmt.executeQuery();

                    System.out.println("\n=== Berichten voor sprint: " + sprintName + " ===");

                    boolean found = false;
                    while (rs.next()) {
                        String sender = rs.getString("sender");
                        String content = rs.getString("message");
                        String timestamp = rs.getString("timestamp");
                        System.out.println("[" + timestamp + "] " + sender + ": " + content);
                        found = true;
                    }

                    if (!found) {
                        System.out.println("Geen berichten gevonden voor deze sprint.");
                    }

                } catch (SQLException e) {
                    System.out.println("Fout bij ophalen van berichten: " + e.getMessage());
                }

                break;

            } else {
                System.out.println("Ongeldige keuze. Kies 'issue' of 'sprint'.");
            }
        }
    }

    private static boolean issueFormat(String issue) {
        return issue.matches("^\\d+(\\.\\d+){0,2}$");
    }

    private static boolean issueExists(String issueType, String issueId) {
        String query = "";
        switch (issueType) {
            case "epic":
                query = "SELECT 1 FROM epics WHERE id = ?";
                break;
            case "user_story":
                query = "SELECT 1 FROM user_stories WHERE id = ?";
                break;
            case "task":
                query = "SELECT 1 FROM tasks WHERE id = ?";
                break;
            default:
                return false;
        }

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

        System.out.print("Voor welk type issue wil je dit bericht sturen? (epic/user_story/task): ");
        String issueType = scanner.nextLine().toLowerCase();

        while (!(issueType.equals("epic") || issueType.equals("user_story") || issueType.equals("task"))) {
            System.out.print("Ongeldige type, kies 'epic', 'user_story', of 'task': ");
            issueType = scanner.nextLine().toLowerCase();
        }

        System.out.print("Voer de ID van het issue in: ");
        String issueId = scanner.nextLine();

        while (!issueExists(issueType, issueId)) {
            System.out.print("Issue bestaat niet, voer een geldig issue in:");
            issueId = scanner.nextLine();
        }

        String query = "INSERT INTO messages (sender, message, issue_id, issue_type) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.setString(3, issueId);   // issue_id (issue ID)
            pstmt.setString(4, issueType);  // issue_type (epic, user_story, task)
            pstmt.executeUpdate();

            System.out.println("Bericht succesvol verzonden!");

        } catch (SQLException e) {
            System.out.println("Fout bij het opslaan van het bericht: " + e.getMessage());
        }
    }
}