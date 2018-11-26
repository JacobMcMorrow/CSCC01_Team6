package team6.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import team6.models.Organization;
import team6.repositories.OrganizationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class OrganizationControllerTest {
	@Autowired
	private OrganizationRepository organizationRepository;
	@LocalServerPort
    private int port;

	TestRestTemplate restTemplate = new TestRestTemplate();

	HttpHeaders headers = new HttpHeaders();

	@Test
	public void testReadAllView() {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = 
				restTemplate.exchange(createURL("/organizations"),
				HttpMethod.GET, entity, String.class);

		String expected = ViewGenerators.getReadListView(port, "/organizations/");

		assertEquals(expected, response.getBody());
	}

	@Test
	public void testReadByIdView() throws Exception {
		Organization organization = new Organization();
		organizationRepository.save(organization);
		String id = String.valueOf(organization.getId());
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = 
				restTemplate.exchange(createURL("/organizations/" + id),
				HttpMethod.GET, entity, String.class);

		String expected = ViewGenerators.getReadSingleView(port, "/organizations/", id);

		assertEquals(expected, response.getBody());

		organizationRepository.deleteById(organization.getId());
	}

	@Test
	public void testCreateView() {
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = 
				restTemplate.exchange(createURL("/organizations/create"),
				HttpMethod.GET, entity, String.class);

		String expected = ViewGenerators.getCreateView(port, "/organizations/");

		assertEquals(expected, response.getBody());
	}

	@Test
	public void testCreate() {
		Organization organization = new Organization();
		HttpEntity<Organization> entity = new HttpEntity<Organization>(organization, headers);

		ResponseEntity<String> response = 
				restTemplate.exchange(createURL("/organizations"),
				HttpMethod.POST, entity, String.class);

		String actual = response.getHeaders().get(HttpHeaders.LOCATION).get(0);

		assertTrue(actual.contains("/organizations"));
	}

	@Test
	public void testUpdatedOrganization() throws Exception {
		Organization organization = new Organization();
		organizationRepository.save(organization);
		String id = String.valueOf(organization.getId());
		HttpEntity<Organization> entity = new HttpEntity<Organization>(organization, headers);

		ResponseEntity<String> response = 
				restTemplate.exchange(createURL("/organizations/" + id),
				HttpMethod.POST, entity, String.class);

		String actual = response.getHeaders().get(HttpHeaders.LOCATION).get(0);

		assertTrue(actual.contains("/organizations/" + id));

		organizationRepository.deleteById(organization.getId());
	}

	@Test
	public void testUpdateByIdView() {
		Organization organization = new Organization();
		organizationRepository.save(organization);
		String id = String.valueOf(organization.getId());
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = 
				restTemplate.exchange(createURL("/organizations/" + id + 
						"/update"), HttpMethod.GET, entity, String.class);

		String expected = ViewGenerators.getUpdateView(port, "/organizations/", id);

		assertEquals(expected, response.getBody());

		organizationRepository.deleteById(organization.getId());
	}

	@Test
	public void testDeleteById() throws Exception {
		Organization organization = new Organization();
		organizationRepository.save(organization);
		String id = String.valueOf(organization.getId());
		HttpEntity<Organization> entity = new HttpEntity<Organization>(organization, headers);

		ResponseEntity<String> response = 
				restTemplate.exchange(createURL("/organizations/" + id),
				HttpMethod.DELETE, entity, String.class);

		String actual = response.getHeaders().get(HttpHeaders.LOCATION).get(0);

		assertFalse(actual.contains("/organizations/" + id));
	}

	private String createURL(String uri) {
		return "http://localhost:" + port + uri;
	}

}
