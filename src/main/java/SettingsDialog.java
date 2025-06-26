import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * Settings Dialog für Google Calendar GUI
 * Ermöglicht Eingabe von Kalender-URL, Dateiname und Zeitraum
 */
public class SettingsDialog extends JDialog {
    
    private JTextField calendarUrlField;
    private JTextField filenameField;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    
    private boolean okPressed = false;
    
    // Eingabewerte
    private String calendarUrl;
    private String exportFilename;
    private Date startDate;
    private Date endDate;
    
    public SettingsDialog(JFrame parent, String currentCalendarUrl, String currentFilename, 
                         Date currentStartDate, Date currentEndDate) {
        super(parent, "Einstellungen", true);
        
        // FlatIntelliJLaf Theme für Dialog setzen
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Standard Look & Feel verwenden
        }
        
        // Aktuelle Werte übernehmen
        this.calendarUrl = currentCalendarUrl;
        this.exportFilename = currentFilename;
        this.startDate = new Date(currentStartDate.getTime());
        this.endDate = new Date(currentEndDate.getTime());
        
        initializeDialog();
        populateFields();
    }
    
    /**
     * Initialisiert den Dialog
     */
    private void initializeDialog() {
        setSize(450, 280);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Layout: GridBagLayout für flexible Anordnung
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Kalender URL
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("Kalender URL:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        calendarUrlField = new JTextField(20);
        add(calendarUrlField, gbc);
        
        // Export Dateiname
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(new JLabel("Export Dateiname:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        filenameField = new JTextField(20);
        add(filenameField, gbc);
        
        // Start Datum
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(new JLabel("Von Datum:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        startDateSpinner = createDateSpinner();
        add(startDateSpinner, gbc);
        
        // End Datum
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        add(new JLabel("Bis Datum:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        endDateSpinner = createDateSpinner();
        add(endDateSpinner, gbc);
        
        // Hinweis-Text
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel hintLabel = new JLabel("<html><i>Hinweis: 'primary' - zum Auswählen des eigenen Hauptkalenders belassen!</i></html>");
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.ITALIC, 11f));
        add(hintLabel, gbc);
        
        // Button Panel
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createButtonPanel(), gbc);
    }
    
    /**
     * Erstellt einen JSpinner für Datumseingabe
     */
    private JSpinner createDateSpinner() {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(dateModel);
        
        // Datum-Editor mit gewünschtem Format
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "dd.MM.yyyy");
        spinner.setEditor(editor);
        
        return spinner;
    }
    
    /**
     * Erstellt das Button-Panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        // OK Button Action
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    saveValues();
                    okPressed = true;
                    dispose();
                }
            }
        });
        
        // Cancel Button Action
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed = false;
                dispose();
            }
        });
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        // Enter-Taste für OK, Escape für Cancel
        getRootPane().setDefaultButton(okButton);
        
        // Escape-Key Binding
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButton.doClick();
            }
        });
        
        return buttonPanel;
    }
    
    /**
     * Füllt die Eingabefelder mit aktuellen Werten
     */
    private void populateFields() {
        calendarUrlField.setText(calendarUrl);
        filenameField.setText(exportFilename);
        startDateSpinner.setValue(startDate);
        endDateSpinner.setValue(endDate);
    }
    
    /**
     * Validiert die Eingaben
     */
    private boolean validateInput() {
        // URL-Validierung
        String url = calendarUrlField.getText().trim();
        if (url.isEmpty()) {
            showErrorMessage("Bitte geben Sie eine Kalender-URL ein.");
            calendarUrlField.requestFocus();
            return false;
        }
        
        // Dateiname-Validierung
        String filename = filenameField.getText().trim();
        if (filename.isEmpty()) {
            showErrorMessage("Bitte geben Sie einen Dateinamen ein.");
            filenameField.requestFocus();
            return false;
        }
        
        // ICS-Endung prüfen/hinzufügen
        if (!filename.toLowerCase().endsWith(".ics")) {
            filenameField.setText(filename + ".ics");
        }
        
        // Datum-Validierung
        Date start = (Date) startDateSpinner.getValue();
        Date end = (Date) endDateSpinner.getValue();
        
        if (start.after(end)) {
            showErrorMessage("Das Start-Datum muss vor dem End-Datum liegen.");
            startDateSpinner.requestFocus();
            return false;
        }
        
        // Prüfen, ob Zeitraum nicht zu groß ist (mehr als 2 Jahre)
        long diffInMillies = Math.abs(end.getTime() - start.getTime());
        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
        
        if (diffInDays > 730) { // 2 Jahre
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Der gewählte Zeitraum ist sehr groß (" + diffInDays + " Tage).\n" +
                "Das Laden könnte sehr lange dauern. Fortfahren?",
                "Großer Zeitraum",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (choice != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Speichert die eingegebenen Werte
     */
    private void saveValues() {
        calendarUrl = calendarUrlField.getText().trim();
        exportFilename = filenameField.getText().trim();
        startDate = (Date) startDateSpinner.getValue();
        endDate = (Date) endDateSpinner.getValue();
    }
    
    /**
     * Zeigt eine Fehlermeldung an
     */
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Eingabefehler",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Getters
    public boolean isOkPressed() {
        return okPressed;
    }
    
    public String getCalendarUrl() {
        return calendarUrl;
    }
    
    public String getExportFilename() {
        return exportFilename;
    }
    
    public Date getStartDate() {
        return new Date(startDate.getTime());
    }
    
    public Date getEndDate() {
        return new Date(endDate.getTime());
    }
}