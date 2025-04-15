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


        String sender = "";
        boolean loggedIn = false;
        String answer = "";

        while (!(answer.equalsIgnoreCase("ja") || answer.equalsIgnoreCase("nee"))) {
            System.out.print("Heeft u al een account?: ");
            answer = scanner.nextLine();
            if (answer.equalsIgnoreCase("ja")) {

                while (!loggedIn) {
                    System.out.print("Voer je gebruikersnaam in: ");
                    String username = scanner.nextLine();
                    System.out.print("Voer je wachtwoord in: ");
                    String password = scanner.nextLine();

                    String query = "SELECT * FROM users WHERE username = ? AND password = ?";

                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                         PreparedStatement pstmt = conn.prepareStatement(query)) {

                        pstmt.setString(1, username);
                        pstmt.setString(2, password);

                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                loggedIn = true;
                                sender = username;
                            } else {
                                System.out.println("Onjuiste gebruikersnaam of wachtwoord.");
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else if (answer.equalsIgnoreCase("nee")) {
                boolean accountGemaakt = false;

                while (!accountGemaakt) {
                    System.out.print("Voer een nieuwe gebruikersnaam in: ");
                    String newUsername = scanner.nextLine();

                    System.out.print("Voer een wachtwoord in: ");
                    String newPassword = scanner.nextLine();

                    String checkQuery = "SELECT * FROM users WHERE username = ?";
                    String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

                    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                         PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

                        checkStmt.setString(1, newUsername);

                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                System.out.println("Gebruikersnaam bestaat al. Kies een andere.");
                            } else {
                                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                                    insertStmt.setString(1, newUsername);
                                    insertStmt.setString(2, newPassword);
                                    insertStmt.setString(3, "developer");
                                    insertStmt.executeUpdate();

                                    System.out.println("Account succesvol aangemaakt! Je bent nu ingelogd.");
                                    loggedIn = true;
                                    accountGemaakt = true;
                                    sender = newUsername;
                                }
                            }
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Ongeldig zeg ja of nee.");
            }
        }

        while (true) {
            String role = getRole(sender);
            menu1(role);
            System.out.print("Kies een optie uit het menu (of typ 'exit' om af te sluiten): ");
            String keuze = scanner.nextLine().toLowerCase();

            if (keuze.equals("exit")) {
                System.out.println("Tot de volgende keer!");
                break;
            }

            if ("administrator".equalsIgnoreCase(role)) {
                if (keuze.equals("1")) {
                    // Rol aanpassen
                    System.out.print("Gebruikersnaam van de persoon wiens rol je wil aanpassen: ");
                    String targetUser = scanner.nextLine();
                    String currentRole = getRole(targetUser);

                    if (currentRole == null) {
                        System.out.println("Gebruiker bestaat niet.");
                    } else {
                        System.out.println(targetUser + " [" + currentRole + "]");
                        System.out.print("Nieuwe rol (scrummaster, product_owner, developer, administrator): ");
                        String newRole = scanner.nextLine();

                        if (!isValidRole(newRole)) {
                            System.out.println("Ongeldige rol.");
                        } else if (updateRole(targetUser, newRole)) {
                            System.out.println("Rol succesvol aangepast.");
                        } else {
                            System.out.println("Er is een fout opgetreden.");
                        }
                    }

                } else if (keuze.equals("2")) {
                    // Gebruiker aanmaken
                    System.out.print("Nieuwe gebruikersnaam: ");
                    String newUser = scanner.nextLine();
                    System.out.print("Wachtwoord: ");
                    String newPass = scanner.nextLine();

                    if (getRole(newUser) != null) {
                        System.out.println("Gebruiker bestaat al.");
                    } else {
                        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
                            stmt.setString(1, newUser);
                            stmt.setString(2, newPass);
                            stmt.setString(3, "developer");
                            stmt.executeUpdate();
                            System.out.println("Gebruiker aangemaakt.");
                        } catch (SQLException e) {
                            System.out.println("Fout bij aanmaken: " + e.getMessage());
                        }
                    }

                } else if (keuze.equals("3")) {
                    // Gebruiker verwijderen
                    opgehaaldeGebruikers();
                    System.out.print("Gebruikersnaam om te verwijderen: ");

                    String deleteUser = scanner.nextLine();
                    if (getRole(deleteUser) == null) {
                        System.out.println("Gebruiker bestaat niet.");
                    } else if ("administrator".equalsIgnoreCase(getRole(deleteUser))) {
                        System.out.println("Je mag geen administrator verwijderen.");
                    } else {
                        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username = ?")) {
                            stmt.setString(1, deleteUser);
                            stmt.executeUpdate();
                            System.out.println("Gebruiker verwijderd.");
                        } catch (SQLException e) {
                            System.out.println("Fout bij verwijderen: " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Ongeldige keuze.");
                }

            } else if (keuze.equals("1") && ("developer".equalsIgnoreCase(role) || "scrummaster".equalsIgnoreCase(role) || "product_owner".equalsIgnoreCase(role))) {
                // Bericht versturen
                System.out.print("Voer je bericht in: ");
                String msg = scanner.nextLine();
                System.out.print("Voer de issue in: ");
                String issue = scanner.nextLine();

                while (!sendIssue(issue)) {
                    System.out.println("Issue bestaat niet, probeer opnieuw:");
                    issue = scanner.nextLine();
                }

                if (sendMessage(sender, msg, issue)) {
                    System.out.println("Bericht verzonden.");
                } else {
                    System.out.println("Er ging iets mis.");
                }
            }
            else if (keuze.equals("2")) {
                System.out.println("Sprint bekijken...");
                sprintOphalen();  // Dit roept de getSprintDetails methode aan die de sprintinformatie ophaalt en toont
            }

            else if (keuze.equals("3")) {
                // Issue bekijken
                System.out.print("Wil je berichten ophalen van een bepaalde issue? (ja/nee): ");
                String antwoord = scanner.nextLine();
                if (antwoord.equalsIgnoreCase("ja")) {
                    System.out.print("Welke issue?: ");
                    String issue = scanner.nextLine();
                    if (issueFormat(issue)) {
                        getMessages(issue);
                    } else {
                        System.out.println("Verkeerd formaat.");
                    }
                } else {
                    getMessages(null);
                }

            } else if (keuze.equals("4") && ("scrummaster".equalsIgnoreCase(role) || "product_owner".equalsIgnoreCase(role))) {
                // Nieuwe issue aanmaken
                System.out.print("Welke issue wil je aanmaken? ");
                String issue = scanner.nextLine();

                if (sendIssue(issue)) {
                    System.out.println("Issue bestaat al.");
                } else if (!issueFormat(issue)) {
                    System.out.println("Fout in format.");
                } else if (issueNaarDB(issue)) {
                    System.out.println("Issue opgeslagen.");
                } else {
                    System.out.println("Fout bij opslaan.");
                }

            } else {
                System.out.println("Ongeldige keuze of geen toegang.");
            }

            System.out.println(); // lege regel voor leesbaarheid
        }
    }







    public static boolean sendIssue(String issueid) {
        String query = "SELECT 1 FROM issues WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, issueid);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return true;  // Issue bestaat
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean issueNaarDB(String issue) {
        String query = "INSERT INTO issues (id) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, issue);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Fout bij het opslaan van de issue: " + e.getMessage());
            return false;
        }
    }

    public static boolean issueFormat(String issue) {
        return issue.matches("^\\d+(\\.\\d+){0,2}$"); // 2 punten max dus 1.1.1 of 1.1 of 1
    }


    public static boolean sendMessage(String sender, String message, String issueid) {
        String query = "INSERT INTO messages (sender, message, issueid) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, message);
            pstmt.setString(3, issueid);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Fout bij het opslaan van het bericht: " + e.getMessage());
            return false;
        }
    }

    public static String getRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("role"); // Retourneer de rol van de gebruiker
            } else {
                return null;
            }
        } catch (SQLException exception) {
            System.out.println("Error bij getRole functie: " + exception.getMessage());
            return null;
        }
    }


    public static void opgehaaldeGebruikers() {
        String query = "SELECT username, role FROM users";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("Gebruikerslijst:");
            while (rs.next()) {
                String naam = rs.getString("username");
                String rol = rs.getString("role");
                System.out.println("- " + naam + " [" + rol + "]");
            }

        } catch (SQLException e) {
            System.out.println("Fout bij ophalen van gebruikers: " + e.getMessage());
        }
    }



    public static void getMessages(String issueFilter) {
        // haalt deze dingen uit de sql
        String query;
        if (issueFilter == null) {
            query = "SELECT sender, message, timestamp, issueid FROM messages ORDER BY id ASC";
        } else {
            query = "SELECT sender, message, timestamp, issueid FROM messages WHERE issueid = '" + issueFilter + "' ORDER BY id ASC";
        }
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
                String issueid = rs.getString("issueid");
                System.out.println("[" + formattedDate + "] " + sender + ": " + "[" + issueid + "] " + message);
            }
            System.out.println("=========================");
            // ====


        } catch (SQLException e) {
            System.out.println("Fout bij het ophalen van berichten: " + e.getMessage());
        }
    }

    public static void sprintOphalen() {
        String query = "SELECT sprint_id, naam, startdatum, einddatum, status FROM sprints";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("sprint_id");
                String naam = rs.getString("naam");
                Date startDatum = rs.getDate("startdatum");
                Date eindDatum = rs.getDate("einddatum");
                String status = rs.getString("status");

                System.out.println("Sprint ID: " + id);
                System.out.println("Sprint Naam: " + naam);
                System.out.println("Startdatum: " + startDatum);
                System.out.println("Einddatum: " + eindDatum);
                System.out.println("Status: " + status);
                System.out.println("=============================");
            }

        } catch (SQLException e) {
            System.out.println("Er is een fout opgetreden bij het ophalen van de sprintinformatie.");
            e.printStackTrace();
        }
    }





    public static boolean isValidRole(String role) {
        String[] validRoles = {"scrummaster", "product_owner", "developer", "administrator"};
        for (String r : validRoles) {
            if (r.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public static boolean updateRole(String username, String newRole) {
        String query = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newRole);
            pstmt.setString(2, username);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.out.println("Fout bij het updaten van de rol: " + e.getMessage());
            return false;
        }
    }

    public static void menu1(String role){

        System.out.println("=== Menu ===");
        if ("developer".equalsIgnoreCase(role) || "scrummaster".equalsIgnoreCase(role) || "product_owner".equalsIgnoreCase(role)) {
            System.out.println("1. bericht versturen");
            System.out.println("2. sprint bekijken");
            System.out.println("3. issue bekijken");
        }
        if ("scrummaster".equalsIgnoreCase(role) || "product_owner".equalsIgnoreCase(role)){
            System.out.println("4. issue aanmaken");
        }
        if ("administrator".equalsIgnoreCase(role)) {
            System.out.println("1. Gebruikersrollen aanpassen");
            System.out.println("2. gebruiker aanmaken");
            System.out.println("3. gebruiker verwijderen");
        }


        System.out.println("============");


    }


}
