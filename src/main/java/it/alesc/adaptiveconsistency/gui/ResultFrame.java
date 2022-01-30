package it.alesc.adaptiveconsistency.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import it.alesc.adaptiveconsistency.logic.ProblemSolver;
import it.alesc.adaptiveconsistency.logic.csp.StartInformation;
import it.alesc.adaptiveconsistency.logic.exceptions.NotSatisfiableException;

/**
 * It is the window where the progression of the computation is shown. It allows
 * the user to save the text displayed in a file.
 * 
 * @author Alessandro Schio
 * @version 2.0 09 Jan 2014
 * 
 */
public class ResultFrame extends JFrame {
	@Serial
	private static final long serialVersionUID = -5266436189112789407L;
	private final StartInformation startingInfo;
	private final JTextArea computationArea = new JTextArea();
	private final JMenuItem saveMenuItem = new JMenuItem("Salva computazione");
	private final JMenuBar resultFrameMenuBar = new JMenuBar();

	/**
	 * The main constructor of the class. It requires the information obtained
	 * by parsing the file.
	 *
	 * @param startingInfo
	 *            the information that is result of parsing the source file
	 */
	public ResultFrame(
			final StartInformation startingInfo) {
		super("Risoluzione CSP");
		this.startingInfo = startingInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.Window#show()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void show() {
		createFrame();
		super.show();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		if (startingInfo != null) {
			startComputation();
		}
	}

	private void createFrame() {
		setSize(500, 700);

		computationArea.setEditable(false);
		computationArea.setFont(new Font(null, Font.PLAIN, 14));
		computationArea.setWrapStyleWord(true);

		var mainPanel = new JScrollPane(computationArea);

		saveMenuItem.addActionListener(new SaveListener());

		resultFrameMenuBar.add(saveMenuItem);
		setJMenuBar(resultFrameMenuBar);

		add(mainPanel);
	}

	private void startComputation() {
		computationArea.setText("Informazioni iniziali:\n\nVariabili: "
				+ startingInfo.variables() + "\nVincoli: "
				+ startingInfo.constraints() + "\nOrdinamento: "
				+ startingInfo.variableOrder());

		try {
			Map<String, String> solution = new ProblemSolver(startingInfo, this)
					.solve();

			updateTextArea("\n\nSoluzione: " + solution, true);
		} catch (NotSatisfiableException e) {
			updateTextArea("\n\nIl problema non ha soluzioni", true);
		}
	}

	/**
	 * Updates the text displayed in the frame. If the specified guard is
	 * <code>true</code> the specified text is appended, otherwise the current
	 * displayed text is replaced with the specified text.
	 * 
	 * @param text
	 *            the text to add or replace
	 * @param append
	 *            if <code>true</code> the text is appended, otherwise the text
	 *            replaces the current
	 */
	public void updateTextArea(final String text, final boolean append) {
		if (append) {
			String newText = computationArea.getText() + text;
			computationArea.setText(newText);
		} else {
			computationArea.setText(text);
		}
	}

	private class SaveListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();

			int chooserValue = fileChooser.showSaveDialog(null);
			if (chooserValue != JFileChooser.APPROVE_OPTION) {
				return;
			}

			File selFile = fileChooser.getSelectedFile();
			try (FileWriter fileWriter = getFileWriter(selFile)) {
				if (fileWriter != null) {
					fileWriter.write(computationArea.getText());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(
								null,
								"Errore durante l'apertura o la creazione del file di output",
								"Errore", JOptionPane.ERROR_MESSAGE);
			}
		}

		private FileWriter getFileWriter(File selFile) throws IOException {
			if (selFile.createNewFile()) {
				return new FileWriter(selFile);
			}
			int answer = JOptionPane.showConfirmDialog(
					null,
					"Il file "
							+ selFile.getName()
							+ " esiste già.\nVuoi sovrascrivere il file?",
					"File esistente",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (answer == JOptionPane.YES_OPTION) {
				return new FileWriter(selFile);
			}
			return null;
		}

	}
}
