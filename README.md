# Twitter Review Extended (twitter-review-prozess)

> Autoren der Dokumentation: Björn Scheppler

> Dokumentation letztmals aktualisiert: 30.12.2018

**Maven**-Projekt für eine [**Camunda Prozessapplikation**](https://camunda.com/), in welchem Mitarbeiter Texte vorschlagen können, welche nach erfolgreicher Prüfung durch die Kommunikationsabteilung auf **Twitter** veröffentlicht werden.

Dieses Projekt ist eine stark erweiterte Version des [Camunda Quickstart-Beispiels](https://github.com/camunda/camunda-bpm-examples/tree/master/spring-boot-starter/example-twitter). Erarbeitet am [**Institut für Wirtschaftsinformatik** an der ZHAW School of Management and Law](http://www.zhaw.ch/iwi) im Rahmen des [Bachelor-Studiengangs Wirtschaftsinformatik](https://www.zhaw.ch/de/sml/studium/bachelor/wirtschaftsinformatik/) im [**Modul Geschäftsprozessintegration**](https://modulmanagement.sml.zhaw.ch/StaticModDescAblage/Modulbeschreibung_w.BA.XX.2GPI-WIN.XX.pdf).

## Dokumentation
Um dieses Projekt nachzubauen, gibt es eine Schritt-für-Schritt-Anleitung als PDF im Ordner \src\docs.

## Komponenten / Funktionalität
1. **Spring Boot**-Applikation mit Tomcat Server, usw.
2. **Camunda** Process Engine, REST API und Webapps (Tasklist, Cockpit, Admin)
3. **H2**-Datenbank für die Camunda Process Engine
4. **Email**-Versand
5. **Prozess** zur Verarbeitung von Tweet-Anfragen bestehend aus:
    1. **BPMN**-Modell
    2. Generierte HTML **Formulare** für:
        1. Startformular (gewünschten Tweet-Text erfassen)
        2. User Task "Tweet-Anfrage prüfen"
        3. User Task "Tweet-Anfrage überarbeiten"
    3. Service Task **Benutzer-Informationen auslesen** aufrufend:
        1. GetUserInformationDelegate-Klasse, welche ein User-Objekt basierend auf dem Benutzernamen ermittelt...
        2. ... über eine UserService-Klasse, die ihrerseits wiederum per REST auf ...
        3. ... einen **User Service** zugreift -> separates Projekt [hier](https://github.com/zhaw-gpi/userservice)
    4. Service Task **Tweet senden** aufrufend:
        1. SendTweetDelegate-Klasse, welche den gewünschten Tweet-Text veröffentlicht, ...
        2. ... über die Nutzung einer TwitterService-Klasse, welche ihrerseits per **Spring Social Twitter** auf die Twitter REST API zugreift.
    5. Service Task **Mitarbeiter benachrichtigen** aufrufend:
        1. NotifyEmployeeDelegate-Klasse, welche den Mitarbeiter bei Genehmigung oder Ablehnung per Mail benachrichtigt ...
        2. ... über eine EmailService-Klasse, welche ihrerseits über **Spring Boot Starter Mail** eine Mail versendet.
    6. **Angeheftete Zeitzwischenereignisse** an die User Tasks, welche nach 3 Tagen Inaktivität dazu führen, dass der Mitarbeiter benachrichtigt und der Prozess beendet wird.
    7. **Test Package**: Achtung, dieses funktioniert nicht mit der fertigen Lösung, sondern nur, wenn man der Anleitung Schritt-für-Schritt in src\docs folgt. Daher ist im pom.xml die skipTests-Property auf true gesetzt.

## Deployment
1. Wenn man die **Enterprise Edition** von Camunda verwenden will, benötigt man die Zugangsdaten zum Nexus Repository und eine gültige Lizenz. Wie man diese "installiert", steht in den Kommentaren im pom.xml.
2. **Erstmalig** oder bei Problemen ein **Clean & Build (Netbeans)**, respektive `mvn clean install` (Cmd) durchführen
3. Bei Änderungen am POM-File oder bei **(Neu)kompilierungsbedarf** genügt ein **Build (Netbeans)**, respektive `mvn install`
4. Damit der **Mail-Versand** funktioniert, ist der Bereich # Mail-Konfiguration in application.properties anzupassen und/oder die dort aufgeführten Umgebungsvariablen zu setzen.
5. Damit die Kommunikation mit **Twitter** funktioniert, muss eine twitter.properties-Datei angelegt werden in src\main\ressources mit den Zeilen:
    twitter.consumerKey=
    twitter.consumerSecret=
    twitter.accessToken=
    twitter.accessTokenSecret=

## Nutzung
### Normal
1. Für den **Start** ist ein **Run (Netbeans)**, respektive `java -jar .\target\NAME DES JAR-FILES.jar` (Cmd) erforderlich.
2. Das **Beenden** geschieht mit **Stop Build/Run (Netbeans)**, respektive **CTRL+C** (Cmd)
3. Falls man die bestehenden **Prozessdaten nicht mehr benötigt** und die Datenbank inzwischen recht angewachsen ist, genügt es, die Datei DATENBANKNAME.mv.db im Wurzelverzeichnis des Projekts zu löschen.
4. http://localhost:8080 aufrufen
5. Anmelden mit Benutzername a und Passwort a
6. Tasklist öffnen (und in einem separaten Tab das Cockpit)
7. "Start Process" > "Verarbeitung von Tweet-Anfragen"
8. Nun wird man durch den Prozess geführt. Folgende Hinweise:
    1. Die E-Mail-Adresse wird aus dem UserService ausgelesen, daher dort sinnvolle Testdaten setzen.
    2. Bei "Tweet-Anfrage" prüfen ist ein Claim erforderlich, da bewusst jede Person aus der Kommunikationsabteilung, die Aufgabe ausführen können soll. Der Nutzer a hat als Admin Zugriff auf alle Aufgaben. PS: Selbst wenn man einen zusätzlichen Nicht-Admin-Benutzer erstellt und diesen nicht der Gruppe kommunikationsabteilung zuweist, sieht er trotzdem alle Aufgaben für diese. Grund: Authorisierung ist standardmässig deaktiviert => Jeder Benutzer hat alle Rechte.
    3. Wenn man zu lange wartet, bevor man einen User Task erledigt, kann es sein, dass dieser Task gelöscht wird wegen dem angehefteten Zeitereignis.
9. Im Camunda Cockpit kann man bei Bedarf den Prozessfortschritt und mehr verfolgen

### Fortgeschrittene Nutzung (Datenbank-Konsole)
1. Um auf die Datenbankverwaltungs-Umgebung zuzugreifen, http://localhost:TOMCAT_PORT/console eingeben.
2. Anmeldung über:
    1. Benutzername sa
    2. Passwort: leer lassen
    3. URL jdbc:h2:./DATENBANKNAME_GEMAESS_APPLICATION.PROPERTIES

### Fortgeschrittene Nutzung (Zugriff über REST)
Die Process Engine kann auch per REST API gesteuert werden. Hierzu die Dokumentation unter https://docs.camunda.org/manual/latest/reference/rest/ lesen. Wegen Spring Boot ist die URL für die REST API minimal anders als in der Dokumentation beschrieben. Sie ist: http://localhost:TOMCAT_PORT/rest/. So gibt z.B. http://localhost:TOMCAT_PORT/rest/engine den Namen der Engine (default) zurück.