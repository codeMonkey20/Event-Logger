import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class RunMain extends Tray {
	public static void main(String[] args) {
		try {
			new Tray();

			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}
		} catch(Exception e) {}
	}
}