import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Sprints {

    public static void createSprint(Scanner scanner) {
        System.out.print("Voer de naam van de sprint in: ");
        String name = scanner.nextLine();

        LocalDateTime startDateTime = readDate(scanner, "startdatum (dd-MM-yyyy): ", true);
        LocalDateTime endDateTime = readDate(scanner, "einddatum (dd-MM-yyyy): ", false);

        if (endDateTime.isBefore(startDateTime)) {
            System.out.println("Einddatum mag niet voor de startdatum liggen.");
            return;
        }

        if (overlapsExistingSprint(startDateTime, endDateTime)) {
            System.out.println("Deze sprint overlapt met een bestaande sprint.");
            return;
        }

        String insertQuery = "INSERT INTO sprints (name, start_date, end_date) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            pstmt.setString(1, name);
            pstmt.setTimestamp(2, Timestamp.valueOf(startDateTime));
            pstmt.setTimestamp(3, Timestamp.valueOf(endDateTime));
            pstmt.executeUpdate();

            System.out.println("Sprint succesvol aangemaakt!");
        } catch (SQLException e) {
            System.out.println("Fout bij het aanmaken van de sprint: " + e.getMessage());
        }
    }

    private static LocalDateTime readDate(Scanner scanner, String prompt, boolean isStart) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        while (true) {
            try {
                System.out.print("Voer de " + prompt);
                String input = scanner.nextLine().trim();
                LocalDate date = LocalDate.parse(input, formatter);
                return isStart
                        ? date.atTime(0, 0, 0)       // 00:00:00 voor start
                        : date.atTime(23, 59, 59);   // 23:59:59 voor eind
            } catch (Exception e) {
                System.out.println("Ongeldige datum. Gebruik formaat: dd-MM-yyyy");
            }
        }
    }

    private static boolean overlapsExistingSprint(LocalDateTime start, LocalDateTime end) {
        String checkQuery = "SELECT 1 FROM sprints WHERE NOT (? > end_date OR ? < start_date)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = pstmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            System.out.println("Fout bij het controleren van overlappende sprints: " + e.getMessage());
            return true;
        }
    }

    public static void listAllSprints() {
        String query = "SELECT name, start_date, end_date FROM sprints ORDER BY start_date ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\n=== Sprints ===");
            LocalDateTime now = LocalDateTime.now();

            while (rs.next()) {
                String name = rs.getString("name");
                Timestamp startTs = rs.getTimestamp("start_date");
                Timestamp endTs = rs.getTimestamp("end_date");

                LocalDateTime start = startTs.toLocalDateTime();
                LocalDateTime end = endTs.toLocalDateTime();

                boolean isCurrent = (now.isEqual(start) || now.isAfter(start)) && now.isBefore(end);

                if (isCurrent) {
                    System.out.println("\033[1m" + name + ": " + start.toLocalDate() + " t/m " + end.toLocalDate() + "\033[0m");
                } else {
                    System.out.println(name + ": " + start.toLocalDate() + " t/m " + end.toLocalDate());
                }
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen van sprints: " + e.getMessage());
        }
    }
}