# Google Calendar Export Tool

Ein Java-Tool zum Exportieren von Google Calendar-Ereignissen ins iCalendar (.ics) Format.

## Beschreibung

Dieses Tool verwendet die Google Calendar API, um Kalenderereignisse für einen bestimmten Zeitraum abzurufen und sie in eine .ics-Datei zu exportieren, die mit anderen Kalenderprogrammen kompatibel ist.

## Voraussetzungen

- Java 8 oder höher
- Gradle
- Google Calendar API Credentials (credentials.json)

## Installation

1. Repository klonen:
```bash
git clone <repository-url>
cd calendar-export
```

2. Projekt bauen:
```bash
./gradlew build
```

## Verwendung

Das Tool erwartet drei Kommandozeilenargumente:

```bash
java -jar build/libs/JavaCalendar-1.0.jar <start-date> <end-date> <filename>
```

### Beispiel:
```bash
java -jar build/libs/JavaCalendar-1.0.jar 2023-05-01 2023-05-31 export.ics
```

### Parameter:
- `start-date`: Startdatum im Format YYYY-MM-DD
- `end-date`: Enddatum im Format YYYY-MM-DD
- `filename`: Name der Ausgabedatei (z.B. export.ics)

## Setup Google Calendar API

1. Gehen Sie zur [Google Cloud Console](https://console.cloud.google.com/)
2. Erstellen Sie ein neues Projekt oder wählen Sie ein bestehendes aus
3. Aktivieren Sie die Google Calendar API
4. Erstellen Sie Credentials (OAuth 2.0 Client-IDs)
5. Laden Sie die credentials.json Datei herunter und platzieren Sie sie im `src/main/resources/` Verzeichnis

## Funktionen

- Export von Google Calendar-Ereignissen
- Unterstützung für Zeitraumfilterung
- iCalendar (.ics) Format Export
- Automatische Authentifizierung über OAuth 2.0

## Projektstruktur

```
calendar-export/
├── src/
│   └── main/
│       ├── java/
│       │   └── CalendarQuickstart.java
│       └── resources/
│           └── credentials.json
├── build.gradle
└── README.md
```

## Lizenz

Dieses Projekt ist für Bildungszwecke erstellt.
