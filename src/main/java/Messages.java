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
                String issueId;
                String issueType;

                while (true) {
                    System.out.print("Voer de ID van het issue in (bijv. 1, 1.1 of 1.1.1): ");
                    issueId = scanner.nextLine();
                    issueType = determineIssueType(issueId);

                    if (issueType == null) {
                        System.out.println("Ongeldig formaat. Gebruik bijvoorbeeld 1, 1.1 of 1.1.1.");
                        continue;
                    }

                    if (!issueExists(issueType, issueId)) {
                        System.out.println("Dit issue bestaat niet.");
                        continue;
                    }

                    break;
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
                String sprintName;
                while (true) {
                    System.out.print("Voer de naam van de sprint in: ");
                    sprintName = scanner.nextLine();

                    String checkSprintQuery = "SELECT 1 FROM sprints WHERE name = ?";
                    try (Connection conn = Database.getConnection();
                         PreparedStatement checkStmt = conn.prepareStatement(checkSprintQuery)) {

                        checkStmt.setString(1, sprintName);
                        ResultSet checkRs = checkStmt.executeQuery();

                        if (!checkRs.next()) {
                            System.out.println("Deze sprint bestaat niet.");
                            continue;
                        }

                        break;

                    } catch (SQLException e) {
                        System.out.println("Fout bij controleren van de sprint: " + e.getMessage());
                        return;
                    }
                }

                String query = "SELECT m.sender, m.message, m.timestamp, m.issue_id FROM messages m " +
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
                        String issueId = rs.getString("issue_id");
                        System.out.println("[" + timestamp + "] (" + issueId + ") " + sender + ": " + content);
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

    private static String determineIssueType(String issueId) {
        if (issueId.matches("^\\d+$")) {
            return "epic";
        } else if (issueId.matches("^\\d+\\.\\d+$")) {
            return "user_story";
        } else if (issueId.matches("^\\d+\\.\\d+\\.\\d+$")) {
            return "task";
        }
        return null;
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

    public static void sendMessage(Scanner scanner, String username) {
        String issueId;
        String issueType;

        while (true) {
            System.out.print("Voor welk issue wil je een bericht sturen? Voer de ID in (bijv. 1, 1.1 of 1.1.1): ");
            issueId = scanner.nextLine();

            issueType = determineIssueType(issueId);
            if (issueType == null) {
                System.out.println("Ongeldig formaat. Gebruik bijvoorbeeld 1, 1.1 of 1.1.1");
                continue;
            }

            if (!issueExists(issueType, issueId)) {
                System.out.println("Dit issue bestaat niet.");
                continue;
            }

            break;
        }

        System.out.print("Typ je bericht: ");
        String messageContent = scanner.nextLine();

        String insertQuery = """
        INSERT INTO messages (sender, message, timestamp, issue_id, issue_type)
        VALUES (?, ?, NOW(), ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, username);
            pstmt.setString(2, messageContent);
            pstmt.setString(3, issueId);
            pstmt.setString(4, issueType);

            pstmt.executeUpdate();
            System.out.println("Bericht succesvol verzonden.");

        } catch (SQLException e) {
            System.out.println("Fout bij het opslaan van het bericht: " + e.getMessage());
        }
    }
}