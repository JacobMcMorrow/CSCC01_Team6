package team6.models;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

@Entity
public class Query {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	private String template;
	@Lob
	private String queryString;
	@OneToMany(mappedBy = "query", cascade = CascadeType.ALL)
	private Set<ChartQuery> chartQueries;

	public Query() {}
	
	public Query(String name, String template, String queryString) {
		this.name = name;
		this.template = template;
		this.queryString = queryString;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id){
		this.id = id;
	}
	
	public String getName() {
		return this.name != null && this.name.length() > 0
			? this.name
			: "Untitled query";
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getTemplate() {
		return this.template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getQueryString() {
		return this.queryString;
	}
	
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public Set<ChartQuery> getChartQueries() {
		return chartQueries;
	}
	
	public void setChartQueries(Set<ChartQuery> chartQueries) {
		this.chartQueries = chartQueries;
	}

	public String getRoute() {
		return String.format(
			"/templates/%s?%s",
			this.template != null ? this.template : "",
			this.queryString != null ? this.queryString : ""
		);
	}

	@Override
	public String toString() {
		return String.format("Query[id=%d,name=%s]%n", this.id, this.name);
	}
}
