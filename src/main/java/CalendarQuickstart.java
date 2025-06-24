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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/* class to demonstrate use of Calendar events list API */
public class CalendarQuickstart {
  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES =
      Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = CalendarQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;
  }

  // ICS Export-Methode (NACH getCredentials, VOR main)
  private static void exportToICS(List<Event> events, String filename) throws IOException {
    try (java.io.FileWriter writer = new java.io.FileWriter(filename)) {
        // iCalendar Header
        writer.write("BEGIN:VCALENDAR\r\n");
        writer.write("VERSION:2.0\r\n");
        writer.write("PRODID:-//Google Calendar Export//DE\r\n");
        
        // Jedes Event als VEVENT schreiben
        for (Event event : events) {
            writer.write("BEGIN:VEVENT\r\n");
            
            // Event ID
            writer.write("UID:" + event.getId() + "\r\n");
            
            // Titel
            if (event.getSummary() != null) {
                writer.write("SUMMARY:" + escapeText(event.getSummary()) + "\r\n");
            }
            
            // Start-Zeit
            DateTime start = event.getStart().getDateTime();
            if (start == null) {
                start = event.getStart().getDate();
            }
            writer.write("DTSTART:" + formatDateTime(start) + "\r\n");
            
            // End-Zeit
            DateTime end = event.getEnd().getDateTime();
            if (end == null) {
                end = event.getEnd().getDate();
            }
            writer.write("DTEND:" + formatDateTime(end) + "\r\n");
            
            // Beschreibung
            if (event.getDescription() != null) {
                writer.write("DESCRIPTION:" + escapeText(event.getDescription()) + "\r\n");
            }
            
            writer.write("END:VEVENT\r\n");
        }
        
        // iCalendar Footer
        writer.write("END:VCALENDAR\r\n");
    }
  }

  // Hilfsmethoden
  private static String formatDateTime(DateTime dateTime) {
    return dateTime.toString().replaceAll("[-:]", "").replaceAll("\\.\\d{3}", "");
  }

  private static String escapeText(String text) {
    return text.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;").replace("\n", "\\n");
  }

  public static void main(String... args) throws IOException, GeneralSecurityException {
    // Argumentprüfung hinzufügen
    if (args.length != 3) {
        System.out.println("Usage: java CalendarQuickstart <start-date> <end-date> <filename>");
        System.out.println("Example: java CalendarQuickstart 2023-05-01 2023-05-31 export.ics");
        return;
    }

    String startDate = args[0]; 
    String endDate = args[1];   
    String filename = args[2];  

    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    Calendar service =
        new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();

    // Datums-Strings in DateTime-Objekte umwandeln
    DateTime startDateTime = new DateTime(startDate + "T00:00:00.000Z");
    DateTime endDateTime = new DateTime(endDate + "T23:59:59.999Z");

    // Events im gewünschten Zeitraum abfragen
    Events events = service.events().list("primary")
        .setTimeMin(startDateTime)
        .setTimeMax(endDateTime)
        .setOrderBy("startTime")
        .setSingleEvents(true)
        .execute();
    
    // Events-Liste extrahieren
    List<Event> items = events.getItems();

    // Events in .ics-Format exportieren
    exportToICS(items, filename);
    System.out.println("Exported " + items.size() + " events to " + filename);
  }
}