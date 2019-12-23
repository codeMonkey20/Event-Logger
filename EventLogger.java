import javax.swing.JFrame;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JFileChooser;
import javax.swing.text.BadLocationException;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

public class EventLogger
extends JFrame
implements NativeKeyListener, NativeMouseInputListener, ActionListener, ItemListener {
	protected JMenu menuSubListeners;
	protected JMenuItem menuItemClear, menuItemSave;
	protected JCheckBoxMenuItem menuItemEnableNativeHook, menuItemEnableLimitLogHistory, menuItemKeyboardEvents, menuItemButtonEvents, menuItemMotionEvents;
	protected JTextArea txtEventInfo;
	private static final Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
	protected boolean IS_LIMITING;

	public EventLogger() {
		initUI();

		// Disable parent logger and set the desired level.
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);

		// Initalize runnables.
		menuItemEnableNativeHook.setSelected(true);
		menuItemEnableLimitLogHistory.setSelected(true);
		menuItemKeyboardEvents.setSelected(true);
		menuItemButtonEvents.setSelected(false);
		menuItemMotionEvents.setSelected(false);
	}

	/*	Clean up the history to reduce memory consumption.
	*	Limit Event Log History=========================================================================================================
	*/	
	private void limitTextHistory(boolean itIsLimiting) {
		if (itIsLimiting){
			try {
				if (txtEventInfo.getLineCount() > 100) {
					txtEventInfo.replaceRange("", 0, txtEventInfo.getLineEndOffset(txtEventInfo.getLineCount() - 1 - 100));
				}

				txtEventInfo.setCaretPosition(txtEventInfo.getLineStartOffset(txtEventInfo.getLineCount() - 1));
			} catch (BadLocationException ex) {
				txtEventInfo.setCaretPosition(txtEventInfo.getDocument().getLength());
			}
		}
	}

	/*
	*	NativeKeyListener===============================================================================================================
	*/
	public void nativeKeyTyped(NativeKeyEvent e) {/* pass */}

	public void nativeKeyPressed(NativeKeyEvent e) {
		txtEventInfo.append("Keyboard: ");
		txtEventInfo.append(NativeKeyEvent.getKeyText(e.getKeyCode()) + "\n");
		limitTextHistory(IS_LIMITING);
	}

	public void nativeKeyReleased(NativeKeyEvent e) {/* pass */}

	/*
	*	NativeMouseInputListener========================================================================================================
	*/
	public void nativeMouseClicked(NativeMouseEvent e) {
		txtEventInfo.append("Mouse Button: ");
		txtEventInfo.append(e.getButton() + "\n");
		limitTextHistory(IS_LIMITING);
	}

	public void nativeMousePressed(NativeMouseEvent e) {/* pass */}

	public void nativeMouseReleased(NativeMouseEvent e) {/* pass */}

	public void nativeMouseMoved(NativeMouseEvent e) {
		txtEventInfo.append("Mouse COORD: ");
		txtEventInfo.append(e.getX() + ", " + e.getY() + " " + "\n");
		limitTextHistory(IS_LIMITING);
	}

	public void nativeMouseDragged(NativeMouseEvent e) {
		txtEventInfo.append("Mouse Dragging COORD: ");
		txtEventInfo.append(e.getX() + ", " + e.getY() + " " + "\n");
		limitTextHistory(IS_LIMITING);
	}

	/*
	*	ActionListener===============================================================================================================
	*/
	public void actionPerformed(ActionEvent e) {

		// SAVE FILE
		if (e.getSource() == menuItemSave) {

			// Stops event logging while saving
			try {
				GlobalScreen.unregisterNativeHook();
			} catch(NativeHookException ex) {}

			// Open save dialog
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Save Event History");
			fc.setFileFilter(new FileType());
			File file = new File(".txt");
			fc.setSelectedFile(file);
			int val = fc.showSaveDialog(this);

			// if successful
			if (val == JFileChooser.APPROVE_OPTION) {
				try {
					File f = fc.getSelectedFile();
					FileWriter fw = new FileWriter(f);
					fw.write(txtEventInfo.getText());
					fw.close();
				} catch(IOException ex) {}

				// Starts event logging after saving
				try {
					GlobalScreen.registerNativeHook();
				} catch(NativeHookException ex) {} 
			} 

			// Starts event logging even canceled saving
			else {
				try {
					GlobalScreen.registerNativeHook();
				} catch(NativeHookException ex) {}
			}

		}

		// CLEAR TEXT FIELD
		else if (e.getSource() == menuItemClear) txtEventInfo.setText("");
	}

	/*
	*	ItemListener===================================================================================================================
	*/
	public void itemStateChanged(ItemEvent e) {

		if (e.getItemSelectable() == menuItemEnableNativeHook) {
			try {
				if (e.getStateChange() == ItemEvent.SELECTED) GlobalScreen.registerNativeHook();
				else GlobalScreen.unregisterNativeHook();
			} catch (NativeHookException ex) {}

			// Set the enable menu item to the state of the hook.
			menuItemEnableNativeHook.setState(GlobalScreen.isNativeHookRegistered());

			// Set enable/disable the sub-menus based on the enable menu item's state.
			menuSubListeners.setEnabled(menuItemEnableNativeHook.getState());
		}
		else if (e.getItemSelectable() == menuItemKeyboardEvents) {
			if (e.getStateChange() == ItemEvent.SELECTED) toggleNativeKeyListener(true);
			else toggleNativeKeyListener(false);
		}
		else if (e.getItemSelectable() == menuItemButtonEvents) {
			if (e.getStateChange() == ItemEvent.SELECTED) toggleNativeMouseListener(true);
			else toggleNativeMouseListener(false);
		}
		else if (e.getItemSelectable() == menuItemMotionEvents) {
			if (e.getStateChange() == ItemEvent.SELECTED) toggleNativeMouseMotionListener(true);
			else toggleNativeMouseMotionListener(false);
		}
		else if (e.getItemSelectable() == menuItemEnableLimitLogHistory) {
			if (e.getStateChange() == ItemEvent.SELECTED) IS_LIMITING = true;
			else IS_LIMITING = false;
		}
	}

	/*
	*	Listener toggles================================================================================================================
	*/
	protected void toggleNativeKeyListener(boolean toggled){
		if (toggled) GlobalScreen.addNativeKeyListener(this);
		else GlobalScreen.removeNativeKeyListener(this);
	}

	protected void toggleNativeMouseListener(boolean toggled){
		if (toggled) GlobalScreen.addNativeMouseListener(this);
		else GlobalScreen.removeNativeMouseListener(this);
	}

	protected void toggleNativeMouseMotionListener(boolean toggled){
		if (toggled) GlobalScreen.addNativeMouseMotionListener(this);
		else GlobalScreen.removeNativeMouseMotionListener(this);
	}



	/*
	*	GUI DESIGN SHIT=================================================================================================================
	*/
	private void initUI() {
		// Setup the main window.
		setTitle("Event Logger");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // Window close operation will only hide the window
		setSize(600, 300);
		setVisible(false);

		// Setup menu
		JMenuBar menuBar = new JMenuBar();
			// FILE=====================================================================================================================
			JMenu menuFile = new JMenu("File");
			menuFile.setMnemonic(KeyEvent.VK_F);
			menuBar.add(menuFile);

				menuItemSave = new JMenuItem("Save Log History", KeyEvent.VK_S);
				menuItemSave.addActionListener(this);
				menuItemSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
				menuFile.add(menuItemSave);

			// VIEW=====================================================================================================================
			JMenu menuView = new JMenu("View");
			menuView.setMnemonic(KeyEvent.VK_V);
			menuBar.add(menuView);

				menuItemClear = new JMenuItem("Clear", KeyEvent.VK_C);
				menuItemClear.addActionListener(this);
				menuItemClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
				menuView.add(menuItemClear);

			menuView.addSeparator();

				menuItemEnableNativeHook = new JCheckBoxMenuItem("Enable Event Logging");
				menuItemEnableNativeHook.addItemListener(this);
				menuItemEnableNativeHook.setMnemonic(KeyEvent.VK_H);
				menuItemEnableNativeHook.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
				menuView.add(menuItemEnableNativeHook);

				menuItemEnableLimitLogHistory = new JCheckBoxMenuItem("Enable Limit Text History");
				menuItemEnableLimitLogHistory.addItemListener(this);
				menuItemEnableLimitLogHistory.setMnemonic(KeyEvent.VK_L);
				menuItemEnableLimitLogHistory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
				menuView.add(menuItemEnableLimitLogHistory);

				// LISTENERS SUB MENU===================================================================================================================
				menuSubListeners = new JMenu("Listeners");
				menuSubListeners.setMnemonic(KeyEvent.VK_L);
				menuView.add(menuSubListeners);

					menuItemKeyboardEvents = new JCheckBoxMenuItem("Keyboard Events");
					menuItemKeyboardEvents.addItemListener(this);
					menuItemKeyboardEvents.setMnemonic(KeyEvent.VK_K);
					menuItemKeyboardEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
					menuSubListeners.add(menuItemKeyboardEvents);

					menuItemButtonEvents = new JCheckBoxMenuItem("Button Events");
					menuItemButtonEvents.addItemListener(this);
					menuItemButtonEvents.setMnemonic(KeyEvent.VK_B);
					menuItemButtonEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
					menuSubListeners.add(menuItemButtonEvents);

					menuItemMotionEvents = new JCheckBoxMenuItem("Motion Events");
					menuItemMotionEvents.addItemListener(this);
					menuItemMotionEvents.setMnemonic(KeyEvent.VK_M);
					menuItemMotionEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
					menuSubListeners.add(menuItemMotionEvents);
		setJMenuBar(menuBar);

		// Text Field
		txtEventInfo = new JTextArea();
		txtEventInfo.setEditable(false);
		txtEventInfo.setText("");
		JScrollPane scrollPane = new JScrollPane(txtEventInfo);
		scrollPane.setPreferredSize(new Dimension(375, 125));
		add(scrollPane, BorderLayout.CENTER);
	}
}