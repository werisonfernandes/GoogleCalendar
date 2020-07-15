package br.com.werison.calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class GoogleCalendarApplication {

	private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart Amil";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
	private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = GoogleCalendarApplication.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
		
		//GoogleCredential.fro

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static void main(String[] args) throws GeneralSecurityException, IOException {
		SpringApplication.run(GoogleCalendarApplication.class, args);

		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();
		
		//Insert event
		DateTime startDateTime = new DateTime("2020-04-17T09:00:00-03:00");
		DateTime endDateTime = new DateTime("2020-04-17T17:00:00-03:00");
		Event content = new Event();
		content.setSummary("Evento criado pela api na conta da amil");
		content.setDescription("Evento criado pela api na conta da amil.");
		
		EventDateTime start = new EventDateTime()
			    .setDateTime(startDateTime)
			    .setTimeZone("America/Sao_Paulo");
			content.setStart(start);
			
		EventDateTime end = new EventDateTime()
			    .setDateTime(endDateTime)
			    .setTimeZone("America/Sao_Paulo");
			content.setEnd(end);
		
		ConferenceData conferenceData = new ConferenceData();
		CreateConferenceRequest createRequest = new CreateConferenceRequest();
		ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey(); 
		
		createRequest.setRequestId(UUID.randomUUID().toString());
		conferenceSolutionKey.setType("hangoutsMeet"); 
		
		createRequest.setConferenceSolutionKey(conferenceSolutionKey);		
		conferenceData.setCreateRequest(createRequest);
		
		content.setConferenceData(conferenceData); //conferenceDataVersion
		
		service.events().insert("medico@telemedicinabrasil.page", content).setConferenceDataVersion(1).execute();

		// List the next 10 events from the primary calendar.
		DateTime now = new DateTime(System.currentTimeMillis());
		Events events = service.events().list("medico@telemedicinabrasil.page").setMaxResults(10).setTimeMin(now).setOrderBy("startTime")
				.setSingleEvents(true).execute();
		List<Event> items = events.getItems();
		if (items.isEmpty()) {
			System.out.println("No upcoming events found.");
		} else {
			System.out.println("Upcoming events");
			for (Event event : items) {
				DateTime start2 = event.getStart().getDateTime();
				if (start2 == null) {
					start2 = event.getStart().getDate();
				}
				System.out.printf("%s (%s)\n", event.getSummary(), start2);
			}
		}
	}

}
