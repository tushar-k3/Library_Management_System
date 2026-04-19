package librarymanagementsystem;

import javax.swing.*;
import java.sql.Connection;

 
public class Main {

    public static void main(String[] args) {
        // Use cross-platform LAF so our custom colours render correctly
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Test DB connection before launching UI
        SwingUtilities.invokeLater(() -> {
            try {
                Connection c = DatabaseConnection.get();
                if (c == null || c.isClosed()) throw new Exception("Connection is null.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Cannot connect to the database.\n\n"
                    + "Error: " + e.getMessage() + "\n\n"
                    + "Please make sure:\n"
                    + "  1. MySQL is running on localhost:3306\n"
                    + "  2. library_db exists (run database_setup.sql)\n"
                    + "  3. Credentials in DatabaseConnection.java are correct\n"
                    + "  4. mysql-connector-j is on the build path",
                    "Connection Failed", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
