import java.net.URL;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.CheckboxMenuItem;
import javax.swing.ImageIcon;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

public class Tray extends EventLogger {

	public Tray() {
		if (!SystemTray.isSupported()) System.exit(0);
		initUI();
	}

	private Image createIcon(String path, String description) {
		URL imageURL = Tray.class.getResource(path);
		return new ImageIcon(imageURL, description).getImage();
	}

	private void initUI(){
		// TRAY MENU===================================================
		final PopupMenu trayMenu = new PopupMenu();

			// SHOW====================================================
			MenuItem display = new MenuItem("Show");
			trayMenu.add(display);
			
			display.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(true);
				}
			});

			// HIDE====================================================
			MenuItem hide = new MenuItem("Hide");
			trayMenu.add(hide);
			
			hide.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
		
			trayMenu.addSeparator();

			// LISTENER MENU============================================
			Menu menuListener = new Menu("Listeners");
			trayMenu.add(menuListener);

				// LISTENERS CHECKBOX===================================
				CheckboxMenuItem toggleKeyListener = new CheckboxMenuItem("Keyboard Events");
				menuListener.add(toggleKeyListener);
				toggleKeyListener.setState(menuItemKeyboardEvents.getState());
				toggleKeyListener.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) toggleNativeKeyListener(true);
						else toggleNativeKeyListener(false);
					}
				});

				CheckboxMenuItem toggleButtonListener = new CheckboxMenuItem("Button Events");
				menuListener.add(toggleButtonListener);
				toggleButtonListener.setState(menuItemButtonEvents.getState());
				toggleButtonListener.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) toggleNativeMouseListener(true);
						else toggleNativeMouseListener(false);
					}
				});

				CheckboxMenuItem toggleMotionListener = new CheckboxMenuItem("Motion Events");
				menuListener.add(toggleMotionListener);
				toggleMotionListener.setState(menuItemMotionEvents.getState());
				toggleMotionListener.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) toggleNativeMouseMotionListener(true);
						else toggleNativeMouseMotionListener(false);
					}
				});


			// TOGGLE EVENT============================================
			CheckboxMenuItem toggleEvent = new CheckboxMenuItem("Enable Logging");
			trayMenu.add(toggleEvent);
			toggleEvent.setState(GlobalScreen.isNativeHookRegistered());
			toggleEvent.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					try {
						if (e.getStateChange() == ItemEvent.SELECTED) GlobalScreen.registerNativeHook();
						else GlobalScreen.unregisterNativeHook();
					} catch (NativeHookException ex) {}

					// Set enable/disable the sub-menus based on the enable menu item's state.
					menuListener.setEnabled(toggleEvent.getState());
				}
			});

			trayMenu.addSeparator();

			// EXIT=====================================================
			MenuItem exit = new MenuItem("Exit");
			trayMenu.add(exit);
			
			exit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
			
		// TRAY ICON===================================================
		TrayIcon icon = new TrayIcon(createIcon("/res/download.png", ""), "EventLogger", trayMenu);
		icon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(true);
			}
		});

		// SYSTEM TRAY=================================================
		SystemTray tray = SystemTray.getSystemTray();
		try {
			tray.add(icon);
		} catch (Exception e) {}
	}
}
