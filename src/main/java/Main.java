import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Authentication process
        Authentication authentication = new Authentication();
        String sender = authentication.authenticate();  // Assume it returns the sender's username
        String role = Users.getRole(sender);  // Get the role of the user

        // Role-based menu navigation
        switch (role) {
            case "administrator":
                AdministratorMenu.start(scanner, sender);  // Admin menu functionality
                break;
            case "developer":
            case "product_owner":
            case "scrummaster":
                UserMenu.start(scanner, sender, role);  // User menu functionality
                break;
            default:
                System.out.println("Onbekende rol.");
        }
    }
}
