import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Google Calendar GUI Application - Teil 2
 * Erweiterte Version mit grafischer Benutzeroberfläche
 */
public class CalendarGUI extends JFrame {
    
    // Google Calendar API Konstanten
    private static final String APPLICATION_NAME = "Google Calendar API Java GUI";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    
    // GUI Komponenten
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private JMenuBar menuBar;
    
    // Einstellungen
    private String calendarUrl = "primary";
    private String exportFilename = "calendar_export.ics";
    private Date startDate = new Date();
    private Date endDate = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000); // +30 Tage
    
    // Properties für Einstellungen
    private Properties settings;
    private static final String SETTINGS_FILE = "calendar_settings.properties";
    
    public CalendarGUI() {
        initializeSettings();
        initializeGUI();
        loadSettings();
    }
    
    /**
     * Initialisiert die Einstellungen
     */
    private void initializeSettings() {
        settings = new Properties();
    }
    
    /**
     * Initialisiert die grafische Benutzeroberfläche
     */
    private void initializeGUI() {
        setTitle("Google Calendar Export Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Menü Bar erstellen
        createMenuBar();
        
        // Hauptpanel mit Tabelle
        createMainPanel();
        
        // Window Closing Event für Einstellungen speichern
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveSettings();
                System.exit(0);
            }
        });
    }
    
    /**
     * Erstellt die Menüleiste
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        saveItem.addActionListener(e -> saveCalendarData());
        exitItem.addActionListener(e -> {
            saveSettings();
            System.exit(0);
        });
        
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Application Menu
        JMenu appMenu = new JMenu("Application");
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> showSettingsDialog());
        appMenu.add(settingsItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(appMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Erstellt das Hauptfenster mit der Tabelle
     */
    private void createMainPanel() {
        // Tabellen-Model mit Spalten definieren
        String[] columnNames = {"Ereignis", "Von", "Bis"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabelle nicht editierbar
            }
        };
        
        eventTable = new JTable(tableModel);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.getTableHeader().setReorderingAllowed(false);
        
        // Tabellenstil anpassen für besseren Kontrast
        eventTable.setGridColor(new Color(180, 180, 180)); // Dunkleres Grau für Grid-Linien
        eventTable.setShowGrid(true);
        eventTable.setIntercellSpacing(new Dimension(1, 1));
        
        // Spaltenbreiten setzen
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        // ScrollPane für Tabelle
        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setPreferredSize(new Dimension(750, 500));
        
        // Layout Manager: BorderLayout für automatische Größenanpassung
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        
        // Status-Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("Zum Loslegen bitte über 'Application > Settings' den gewünschten Zeitraum auswählen.");
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Zeigt den Settings-Dialog
     */
    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this, calendarUrl, exportFilename, startDate, endDate);
        dialog.setVisible(true);
        
        if (dialog.isOkPressed()) {
            calendarUrl = dialog.getCalendarUrl();
            exportFilename = dialog.getExportFilename();
            startDate = dialog.getStartDate();
            endDate = dialog.getEndDate();
            
            // Kalenderdaten laden
            loadCalendarData();
        }
    }
    
    /**
     * Zeigt den About-Dialog
     */
    private void showAboutDialog() {
        AboutDialog dialog = new AboutDialog(this);
        dialog.setVisible(true);
    }
    
    /**
     * Lädt Kalenderdaten und zeigt sie in der Tabelle an
     */
    private void loadCalendarData() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Tabelle leeren
                tableModel.setRowCount(0);
                
                // Google Calendar Service erstellen
                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
                
                // Datum formatieren
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                DateTime startDateTime = new DateTime(dateFormat.format(startDate) + "T00:00:00.000Z");
                DateTime endDateTime = new DateTime(dateFormat.format(endDate) + "T23:59:59.999Z");
                
                // Events abfragen
                Events events = service.events().list(calendarUrl)
                        .setTimeMin(startDateTime)
                        .setTimeMax(endDateTime)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                
                List<Event> items = events.getItems();
                
                // Events zur Tabelle hinzufügen
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                for (Event event : items) {
                    String summary = event.getSummary() != null ? event.getSummary() : "Kein Titel";
                    
                    // Start-Zeit
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                        start = event.getStart().getDate();
                    }
                    String startStr = displayFormat.format(new Date(start.getValue()));
                    
                    // End-Zeit
                    DateTime end = event.getEnd().getDateTime();
                    if (end == null) {
                        end = event.getEnd().getDate();
                    }
                    String endStr = displayFormat.format(new Date(end.getValue()));
                    
                    tableModel.addRow(new Object[]{summary, startStr, endStr});
                }
                
                JOptionPane.showMessageDialog(this, 
                    items.size() + " Ereignisse erfolgreich geladen!", 
                    "Laden erfolgreich", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Fehler beim Laden der Kalenderdaten:\n" + e.getMessage(), 
                    "Fehler", 
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Speichert die Kalenderdaten als ICS-Datei
     */
    private void saveCalendarData() {
        try {
            int eventCount = exportCalendar(
                new SimpleDateFormat("yyyy-MM-dd").format(startDate),
                new SimpleDateFormat("yyyy-MM-dd").format(endDate),
                exportFilename
            );
            
            JOptionPane.showMessageDialog(this, 
                eventCount + " Ereignisse erfolgreich in '" + exportFilename + "' gespeichert!", 
                "Speichern erfolgreich", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Fehler beim Speichern:\n" + e.getMessage(), 
                "Fehler", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Lädt Einstellungen aus Properties-Datei
     */
    private void loadSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILE);
            if (settingsFile.exists()) {
                settings.load(new FileInputStream(settingsFile));
                
                calendarUrl = settings.getProperty("calendar.url", "primary");
                exportFilename = settings.getProperty("export.filename", "calendar_export.ics");
                
                // Datumseinstellungen laden
                String startDateStr = settings.getProperty("date.start");
                String endDateStr = settings.getProperty("date.end");
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                if (startDateStr != null) {
                    try {
                        startDate = dateFormat.parse(startDateStr);
                    } catch (Exception e) {
                        // Standardwert beibehalten
                    }
                }
                if (endDateStr != null) {
                    try {
                        endDate = dateFormat.parse(endDateStr);
                    } catch (Exception e) {
                        // Standardwert beibehalten
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Keine Einstellungen gefunden, verwende Standardwerte.");
        }
    }
    
    /**
     * Speichert Einstellungen in Properties-Datei
     */
    private void saveSettings() {
        try {
            settings.setProperty("calendar.url", calendarUrl);
            settings.setProperty("export.filename", exportFilename);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            settings.setProperty("date.start", dateFormat.format(startDate));
            settings.setProperty("date.end", dateFormat.format(endDate));
            
            settings.store(new FileOutputStream(SETTINGS_FILE), "Calendar GUI Settings");
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Einstellungen: " + e.getMessage());
        }
    }
    
    // GOOGLE CALENDAR API METHODEN aus 1 NICHTS ÄNDERN
    
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = CalendarGUI.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    private static void exportToICS(List<Event> events, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("BEGIN:VCALENDAR\r\n");
            writer.write("VERSION:2.0\r\n");
            writer.write("PRODID:-//Google Calendar Export//DE\r\n");
            
            for (Event event : events) {
                writer.write("BEGIN:VEVENT\r\n");
                writer.write("UID:" + event.getId() + "\r\n");
                
                if (event.getSummary() != null) {
                    writer.write("SUMMARY:" + escapeText(event.getSummary()) + "\r\n");
                }
                
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                writer.write("DTSTART:" + formatDateTime(start) + "\r\n");
                
                DateTime end = event.getEnd().getDateTime();
                if (end == null) {
                    end = event.getEnd().getDate();
                }
                writer.write("DTEND:" + formatDateTime(end) + "\r\n");
                
                if (event.getDescription() != null) {
                    writer.write("DESCRIPTION:" + escapeText(event.getDescription()) + "\r\n");
                }
                
                writer.write("END:VEVENT\r\n");
            }
            
            writer.write("END:VCALENDAR\r\n");
        }
    }
    
    private static String formatDateTime(DateTime dateTime) {
        return dateTime.toString().replaceAll("[-:]", "").replaceAll("\\.\\d{3}", "");
    }
    
    private static String escapeText(String text) {
        return text.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;").replace("\n", "\\n");
    }
    
    public static int exportCalendar(String startDate, String endDate, String filename) 
            throws IOException, GeneralSecurityException {
        
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
        DateTime startDateTime = new DateTime(startDate + "T00:00:00.000Z");
        DateTime endDateTime = new DateTime(endDate + "T23:59:59.999Z");
        
        Events events = service.events().list("primary")
                .setTimeMin(startDateTime)
                .setTimeMax(endDateTime)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        
        List<Event> items = events.getItems();
        exportToICS(items, filename);
        
        return items.size();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf");
            } catch (Exception e) {
                // Standard Look & Feel verwenden
            }
            
            new CalendarGUI().setVisible(true);
        });
    }
}