import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * About Dialog für Google Calendar GUI
 * Zeigt Informationen über die Anwendung und Entwickler
 */
public class AboutDialog extends JDialog {
    
    public AboutDialog(JFrame parent) {
        super(parent, "Über diese Anwendung", true);
        
        // FlatIntelliJLaf Theme für Dialog setzen
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            // Standard Look & Feel verwenden
        }
        
        initializeDialog();
    }
    
    /**
     * Initialisiert den About-Dialog
     */
    private void initializeDialog() {
        setSize(550, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Layout: BorderLayout
        setLayout(new BorderLayout(10, 10));
        
        // Header mit Icon und Titel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Info mit Details
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.CENTER);
        
        // Button 
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Padding
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
    
    /**
     * Erstellt das Header-Panel mit Icon und Titel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Icon (verwende System-Icon oder eigenes)
        JLabel iconLabel = new JLabel();
        try {
            // Versuche ein System-Icon zu verwenden
            Icon icon = UIManager.getIcon("OptionPane.informationIcon");
            if (icon != null) {
                iconLabel.setIcon(icon);
            }
        } catch (Exception e) {
            // Falls kein Icon verfügbar, einfach Text verwenden
            iconLabel.setText("📅");
        }
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
        
        // Titel
        JLabel titleLabel = new JLabel("Google Calendar Export Tool");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        
        // Version
        JLabel versionLabel = new JLabel("Version 2.0");
        versionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);
        
        // Layout für Titel-Bereich
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(versionLabel, BorderLayout.SOUTH);
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(titlePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Erstellt das Info-Panel mit Anwendungsdetails
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // HTML-Text für formatierte Darstellung
        String infoText = "<html>" +
            "<div style='font-family: Arial, sans-serif; font-size: 11px; width: 380px;'>" +
            "<p><b>Beschreibung:</b><br>" +
            "Diese Anwendung ermöglicht den Export von Google Calendar<br>" +
            "Einträgen in das iCalendar (.ics) Format. Die exportierten<br>" +
            "Dateien können in anderen Kalenderanwendungen importiert werden.</p>" +
            "<br>" +
            "<p><b>Entwickelt von:</b><br>" +
            "Lauand Ado, 2025<br>" +
            "Für die DHBW Mannheim<br>" +
            "Angewandte Informatik</p>" +
            "<br>" +
            "<p><b>Technische Details:</b><br>" +
            "• Java Swing GUI<br>" +
            "• Google Calendar API v3<br>" +
            "• iCalendar Format<br>" +
            "• OAuth 2.0 Authentifizierung</p>" +
            "<br>" +
            "<p><b>Entwickelt für:</b><br>" +
            "Programmieren 2</p>" +
            "</div>" +
            "</html>";
        
        JLabel infoLabel = new JLabel(infoText);
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        
        // ScrollPane für längere Texte
        JScrollPane scrollPane = new JScrollPane(infoLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Erstellt das Button-Panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(80, 30));
        
        // OK Button Action
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        panel.add(okButton);
        
        // Enter-Taste für OK
        getRootPane().setDefaultButton(okButton);
        
        // Escape-Key Binding
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okButton.doClick();
            }
        });
        
        return panel;
    }
}