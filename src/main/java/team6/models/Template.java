package team6.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity

public class Template {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	public Template() {}

}