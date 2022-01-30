package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import logic.InputParser;
import logic.csp.Constraint;
import logic.csp.Variable;
import logic.exceptions.DuplicateVariableNameException;
import logic.exceptions.UnknownVariableException;
import logic.exceptions.WrongVariablesNumberException;

import org.javatuples.Triplet;

/**
 * It is the window to select the source file and start the computation to solve
 * the CSP.
 * 
 * @author Alessandro Schio
 * @version 1.0 08 Jan 2014
 * 
 */
public class DataInsertionFrame extends JFrame {
	private static final long serialVersionUID = 1216585958133368789L;
	private JPanel mainPanel = new JPanel();
	private JLabel sourceFileLabel = new JLabel("File sorgente");
	private JTextField sourceFileText = new JTextField();
	private JButton sourceFileButton = new JButton("Scegli");
	private JFileChooser sourceFileChooser = new JFileChooser();
	private JButton startButton = new JButton("Procedi");

	/**
	 * The main constructor of the class.
	 */
	public DataInsertionFrame() {
		super("Inserimento Dati");
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#show()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void show() {
		createFrame();
		super.show();
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

		sourceFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int choose = sourceFileChooser.showOpenDialog(null);

				if (choose == JFileChooser.APPROVE_OPTION) {
					File file = sourceFileChooser.getSelectedFile();
					sourceFileText.setText(file.getAbsolutePath());
				}
			}
		});
		sourcePanel.add(sourceFileButton);

		sourceFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		startButton.addActionListener(new StartListener());
		mainPanel.add(startButton);

		add(mainPanel);
	}

	private class StartListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String filePath = sourceFileText.getText();

			InputParser parser = new InputParser(filePath);

			try {
				Triplet<Set<Variable>, Set<Constraint>, List<String>> result = parser
						.parseFile();

				dispose();

				ResultFrame nextFrame = new ResultFrame(result);
				nextFrame.show();
			} catch (ParseException parseExc) {
				JOptionPane.showMessageDialog(null, parseExc.getMessage(),
						"Errore di Parsing", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ioExc) {
				JOptionPane.showMessageDialog(null,
						"Si è verificato un errore nell'apertura del file",
						"Errore", JOptionPane.ERROR_MESSAGE);
			} catch (DuplicateVariableNameException duplicVarExc) {
				JOptionPane.showMessageDialog(null,
						"Il nome " + duplicVarExc.getName()
								+ "è usato come nome in più di una variable",
						"Errore nel file sorgente", JOptionPane.ERROR_MESSAGE);
			} catch (UnknownVariableException unknownVarExc) {
				String message = "";
				switch (unknownVarExc.getCategory()) {
				case Constraints:
					message = "Nei vincoli ";
					break;
				case OrderLine:
					message = "Nell'ordinamento ";
					break;
				default:
					break;
				}
				message += " è presente il nome "
						+ unknownVarExc.getName()
						+ " che non corrisponde ad alcuna variabile dichiarata precedentemente";

				JOptionPane.showMessageDialog(null, message,
						"Errore nel file sorgente", JOptionPane.ERROR_MESSAGE);
			} catch (WrongVariablesNumberException wrongVarNumberExc) {
				JOptionPane
						.showMessageDialog(
								null,
								"Il numero delle variabili nell'ordinamento è diverso dal numero della variabili dichiarate",
								"Errore nel file sorgente",
								JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
