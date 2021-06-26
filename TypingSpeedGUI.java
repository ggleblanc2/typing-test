package com.ggl.testing;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TypingSpeedGUI implements Runnable {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new TypingSpeedGUI());
	}
	
	private final TypingText typingText;
	
	private long startTime;
	
	private JScrollPane showScrollPane;
	private JScrollPane typeScrollPane;
	
	private JTextArea showTextArea;
	private JTextArea typeTextArea;
	
	private JTextField elapsedTimeField;
	private JTextField wpmField;
	private JTextField errorsField;
	
	public TypingSpeedGUI() {
		this.typingText = new TypingText();
		this.startTime = 0L;
	}

	@Override
	public void run() {
		JFrame frame = new JFrame("Typing Speed GUI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.add(createDisplayPanel(), BorderLayout.BEFORE_FIRST_LINE);
		frame.add(createShowTextPanel(), BorderLayout.BEFORE_LINE_BEGINS);
		frame.add(createTypeTextArea(), BorderLayout.AFTER_LINE_ENDS);
		frame.add(createButtonPanel(), BorderLayout.AFTER_LAST_LINE);
		
		Synchronizer synchronizer = new Synchronizer(showScrollPane, typeScrollPane);
		showScrollPane.getVerticalScrollBar().addAdjustmentListener(synchronizer);
		showScrollPane.getHorizontalScrollBar().addAdjustmentListener(synchronizer);
		typeScrollPane.getVerticalScrollBar().addAdjustmentListener(synchronizer);
		typeScrollPane.getHorizontalScrollBar().addAdjustmentListener(synchronizer);
   
		
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}
	
	private JPanel createDisplayPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font font = panel.getFont().deriveFont(16f);
		
		JLabel label = new JLabel("Elapsed time");
		label.setFont(font);
		panel.add(label);
		
		elapsedTimeField = new JTextField(5);
		elapsedTimeField.setEditable(false);
		elapsedTimeField.setFont(font);
		elapsedTimeField.setHorizontalAlignment(JTextField.TRAILING);
		panel.add(elapsedTimeField);
		
		label = new JLabel("seconds");
		label.setFont(font);
		panel.add(label);
		
		panel.add(Box.createHorizontalStrut(20));
		
		label = new JLabel("Errors");
		label.setFont(font);
		panel.add(label);
		
		errorsField = new JTextField(5);
		errorsField.setEditable(false);
		errorsField.setFont(font);
		panel.add(errorsField);
		
		panel.add(Box.createHorizontalStrut(20));
		
		label = new JLabel("Calculated words per minute");
		label.setFont(font);
		panel.add(label);
		
		wpmField = new JTextField(5);
		wpmField.setEditable(false);
		wpmField.setFont(font);
		panel.add(wpmField);
		
		return panel;
	}
	
	private JPanel createShowTextPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font font = panel.getFont().deriveFont(16f);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridwidth = 1;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		JLabel label = new JLabel("Text to type");
		label.setFont(font);
		panel.add(label, gbc);
		
		gbc.gridy++;
		showTextArea = new JTextArea(10, 40);
		showTextArea.setEditable(false);
		showTextArea.setFont(font);
		showTextArea.setLineWrap(true);
		showTextArea.setText(typingText.getText());
		showTextArea.setWrapStyleWord(true);
		
		InputMap inputMap = showTextArea.getInputMap();
		inputMap.put(KeyStroke.getKeyStroke("control X"), "none");
		inputMap.put(KeyStroke.getKeyStroke("control C"), "none");
		
		showScrollPane = new JScrollPane(showTextArea);
		panel.add(showScrollPane, gbc);
		
		return panel;
	}
	
	private JPanel createTypeTextArea() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font font = panel.getFont().deriveFont(16f);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridwidth = 1;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		JLabel label = new JLabel("Typing area");
		label.setFont(font);
		panel.add(label, gbc);
		
		gbc.gridy++;
		typeTextArea = new JTextArea(10, 40);
		typeTextArea.requestFocusInWindow();
		typeTextArea.setFont(font);
		typeTextArea.setLineWrap(true);
		typeTextArea.setWrapStyleWord(true);
		typeTextArea.getDocument().addDocumentListener(new StartListener(this));
		
		typeScrollPane = new JScrollPane(typeTextArea);
		panel.add(typeScrollPane, gbc);
		
		return panel;
	}
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Font font = panel.getFont().deriveFont(16f);
		
		JButton button = new JButton("Restart Typing Test");
		button.setFont(font);
		button.addActionListener(new RestartListener(this));
		panel.add(button);
		
		button = new JButton("Typing Completed");
		button.setFont(font);
		button.addActionListener(new TypingEndListener(this));
		panel.add(button);
		
		return panel;
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public TypingText getTypingText() {
		return typingText;
	}

	public JTextArea getShowTextArea() {
		return showTextArea;
	}

	public JTextArea getTypeTextArea() {
		return typeTextArea;
	}

	public JTextField getElapsedTimeField() {
		return elapsedTimeField;
	}

	public JTextField getWpmField() {
		return wpmField;
	}

	public JTextField getErrorsField() {
		return errorsField;
	}

	public class StartListener implements DocumentListener {
		
		private final TypingSpeedGUI frame;

		public StartListener(TypingSpeedGUI frame) {
			this.frame = frame;
		}

		@Override
		public void insertUpdate(DocumentEvent event) {
			boolean firstTimeSwitch = frame.getStartTime() == 0L;
			if (firstTimeSwitch && frame.getTypeTextArea().getText().length() > 0) {
				startTimer();
			}
		}

		@Override
		public void removeUpdate(DocumentEvent event) {
			boolean firstTimeSwitch = frame.getStartTime() == 0L;
			if (firstTimeSwitch && frame.getTypeTextArea().getText().length() > 0) {
				startTimer();
			}
		}

		@Override
		public void changedUpdate(DocumentEvent event) {
			boolean firstTimeSwitch = frame.getStartTime() == 0L;
			if (firstTimeSwitch && frame.getTypeTextArea().getText().length() > 0) {
				startTimer();
			}
		}
		
		private void startTimer() {
			frame.setStartTime(System.currentTimeMillis());
		}
		
	}
	
	public class RestartListener implements ActionListener {
		
		private final TypingSpeedGUI frame;

		public RestartListener(TypingSpeedGUI frame) {
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			frame.setStartTime(0L);
			frame.getTypeTextArea().setText("");
			frame.getElapsedTimeField().setText("");
			frame.getErrorsField().setText("");
			frame.getWpmField().setText("");
		}
		
	}
	
	public class TypingEndListener implements ActionListener {
		
		private final TypingSpeedGUI frame;

		public TypingEndListener(TypingSpeedGUI frame) {
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			long endTime = System.currentTimeMillis();
			String time = calculateTimeInSeconds(endTime);
			frame.getElapsedTimeField().setText(time);
			frame.getErrorsField().setText(calculateErrors());
			String wpm = calculateWPM(endTime);
			frame.getWpmField().setText(wpm);
		}
		
		private String calculateTimeInSeconds(long endTime) {
			long typingTime = endTime - frame.getStartTime();
			int timeInSeconds = (int) ((typingTime + 500L) / 1000L);
			return Integer.toString(timeInSeconds);
		}
		
		private String calculateErrors() {
			String inputText = frame.getTypingText().getText();
			String outputText = frame.getTypeTextArea().getText();
			String errors;
			if (inputText.length() == outputText.length()) {
				int count = 0;
				for (int index = 0; index < inputText.length(); index++) {
					char a = inputText.charAt(index);
					char b = outputText.charAt(index);
					count += (a == b) ? 0 : 1; 
				}
				errors = Integer.toString(count);
			} else {
				errors = Integer.toString(outputText.length());
			}
			return errors;
		}
		
		private String calculateWPM(long endTime) {
			long typingTime = endTime - frame.getStartTime();
			int timeInSeconds = (int) ((typingTime + 500L) / 1000L);
			String outputText = frame.getTypeTextArea().getText();
			double cps = (double) outputText.length() / timeInSeconds;
			int wpm = (int) Math.round(cps * 12.0);
			return Integer.toString(wpm);
		}
		
	}
	
	public class Synchronizer implements AdjustmentListener {
		JScrollBar v1, h1, v2, h2;

		public Synchronizer(JScrollPane sp1, JScrollPane sp2) {
			v1 = sp1.getVerticalScrollBar();
			h1 = sp1.getHorizontalScrollBar();
			v2 = sp2.getVerticalScrollBar();
			h2 = sp2.getHorizontalScrollBar();
		}

		public void adjustmentValueChanged(AdjustmentEvent e) {
			JScrollBar scrollBar = (JScrollBar) e.getSource();
			int value = scrollBar.getValue();
			JScrollBar target = null;

			if (scrollBar == v1)
				target = v2;
			if (scrollBar == h1)
				target = h2;
			if (scrollBar == v2)
				target = v1;
			if (scrollBar == h2)
				target = h1;

			target.setValue(value);
		}
		
	}

	
	public class TypingText {
		
		private final String text;
		
		public TypingText() {
			// Here's where you'd read a file with several texts.
			// I'm going to hard code one text;
			this.text = "This is a test of the National Emergency Alert System. "
					+ "This system was developed by broadcast and cable operators "
					+ "in voluntary cooperation with the Federal Emergency "
					+ "Management Agency, the Federal Communications Commission, "
					+ "and local authorities to keep you informed in the event of "
					+ "an emergency. If this had been an actual emergency an "
					+ "official message would have followed the tone alert you "
					+ "heard at the start of this message. No action is required.";
		}

		public String getText() {
			return text;
		}
		
	}

}
