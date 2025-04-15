import java.sql.*;
import java.sql.SQLException;

public class Users {

    public static String getRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("role");
            } else {
                return null;
            }
        } catch (SQLException exception) {
            System.out.println("Error in getRole: " + exception.getMessage());
            return null;
        }
    }

    public static boolean isValidRole(String role) {
        return role.equals("developer") || role.equals("scrum_master") ||
                role.equals("product_owner") || role.equals("administrator");
    }

    public static boolean updateRole(String username, String newRole) {
        String query = "UPDATE users SET role = ? WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newRole);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Fout bij rol aanpassen: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteUser(String username) {
        String role = getRole(username);

        if (role == null) {
            System.out.println("Gebruiker bestaat niet.");
            return false;
        }

        if (role.equalsIgnoreCase("administrator")) {
            System.out.println("Je kunt geen administrator verwijderen.");
            return false;
        }

        String query = "DELETE FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Fout bij verwijderen gebruiker: " + e.getMessage());
            return false;
        }
    }
}