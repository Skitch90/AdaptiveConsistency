package it.alesc.adaptiveconsistency.gui;

import it.alesc.adaptiveconsistency.logic.ProblemSolver;
import it.alesc.adaptiveconsistency.logic.csp.CSPResolutionTracker;
import it.alesc.adaptiveconsistency.logic.csp.StartInformation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serial;

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
		createFrame();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void createFrame() {
		setSize(900, 700);

		computationArea.setEditable(false);
		computationArea.setFont(new Font(null, Font.PLAIN, 14));
		computationArea.setWrapStyleWord(true);

		var mainPanel = new JScrollPane(computationArea);

		saveMenuItem.addActionListener(new SaveListener());

		resultFrameMenuBar.add(saveMenuItem);
		setJMenuBar(resultFrameMenuBar);

		add(mainPanel);
	}

	public void solveProblem() {
		final CSPResolutionTracker cspResolutionTracker = ProblemSolver.solveProblem(startingInfo);
		computationArea.setText(ComputationTextBuilder.print(cspResolutionTracker));
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
