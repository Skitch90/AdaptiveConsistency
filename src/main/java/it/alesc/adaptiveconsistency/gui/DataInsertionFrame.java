package it.alesc.adaptiveconsistency.gui;

import com.google.gson.Gson;
import io.vavr.control.Try;
import it.alesc.adaptiveconsistency.logic.validation.ProblemSpecificationValidator;
import it.alesc.adaptiveconsistency.logic.csp.StartInformation;
import it.alesc.adaptiveconsistency.specification.ProblemSpecification;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serial;
import java.nio.charset.StandardCharsets;

/**
 * It is the window to select the source file and start the computation to solve
 * the CSP.
 * 
 * @author Alessandro Schio
 * @version 1.0 08 Jan 2014
 * 
 */
public class DataInsertionFrame extends JFrame {
	@Serial
	private static final long serialVersionUID = 1216585958133368789L;
	public static final String SOURCE_FILE_ERROR_MESSAGE = "Errore nel file sorgente";
	private final JPanel mainPanel = new JPanel();
	private final JLabel sourceFileLabel = new JLabel("File sorgente");
	private final JTextField sourceFileText = new JTextField();
	private final JButton sourceFileButton = new JButton("Scegli");
	private final JFileChooser sourceFileChooser = new JFileChooser();
	private final JButton startButton = new JButton("Procedi");

	/**
	 * The main constructor of the class.
	 */
	public DataInsertionFrame() {
		super("Inserimento Dati");
		createFrame();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void createFrame() {
		setSize(500, 130);

		JPanel sourcePanel = new JPanel();
		mainPanel.add(sourcePanel);

		sourcePanel.add(sourceFileLabel);

		sourceFileText.setEditable(false);
		sourceFileText.setColumns(25);
		sourcePanel.add(sourceFileText);

		sourceFileButton.addActionListener(e -> {
			int choose = sourceFileChooser.showOpenDialog(null);

			if (choose == JFileChooser.APPROVE_OPTION) {
				File file = sourceFileChooser.getSelectedFile();
				sourceFileText.setText(file.getAbsolutePath());
			}
		});
		sourcePanel.add(sourceFileButton);

		sourceFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		sourceFileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (json, txt)", "json", "txt"));

		startButton.addActionListener(new StartListener());
		mainPanel.add(startButton);

		add(mainPanel);
	}

	private class StartListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			final Try<ProblemSpecification> tryReadInputFile = Try.of(() -> {
				final File inputFile = new File(sourceFileText.getText());
				final String specificationString = FileUtils.readFileToString(inputFile, StandardCharsets.UTF_8);
				return new Gson().fromJson(specificationString, ProblemSpecification.class);
			});

			if (tryReadInputFile.isFailure()) {
				JOptionPane.showMessageDialog(null,
						"Si è verificato un errore nell'apertura del file",
						"Errore", JOptionPane.ERROR_MESSAGE);
				return;
			}

			var specificationValidation = ProblemSpecificationValidator.validate(tryReadInputFile.get());
			if (specificationValidation.isInvalid()) {
				JOptionPane.showMessageDialog(null,
						specificationValidation.getError().get(0),
						SOURCE_FILE_ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE);
				return;
			}

			dispose();
			var startInformation = StartInformation.buildStartInformation(specificationValidation.get());
			ResultFrame nextFrame = new ResultFrame(startInformation);
			nextFrame.solveProblem();
			nextFrame.setVisible(true);
		}
	}
}
