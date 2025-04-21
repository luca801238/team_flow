import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Authentication authentication = new Authentication();
        String username = authentication.authenticate();
        String role = Users.getRole(username);

        System.out.println("\nWelkom " + username + " [" + role + "]\n");

        if (!role.equals("administrator")) {
            Messages.displayMessagesBySprint();
        }

        User user;

        switch (role) {
            case "developer" -> user = new Developer(username);
            case "scrum_master" -> user = new ScrumMaster(username);
            case "product_owner" -> user = new ProductOwner(username);
            case "administrator" -> user = new Administrator(username);
            default -> {
                System.out.println("Onbekende rol: " + role);
                return;
            }
        }

        user.showMenu(scanner);
    }
}