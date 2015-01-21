package de.fzi.cep.sepa.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.clarkparsia.empire.annotation.RdfProperty;

import de.fzi.cep.sepa.model.impl.EventStream;
import de.fzi.cep.sepa.model.impl.StaticProperty;

public abstract class ConsumableSEPAElement extends NamedSEPAElement {

	@OneToMany(fetch = FetchType.EAGER,
			   cascade = {CascadeType.ALL})
	@RdfProperty("sepa:requires")
	protected List<EventStream> eventStreams;
	
	
	@OneToMany(fetch = FetchType.EAGER,
			   cascade = {CascadeType.ALL})
	@RdfProperty("sepa:hasStaticProperty")
	protected List<StaticProperty> staticProperties;
	
	public ConsumableSEPAElement()
	{
		super();
	}
	
	public ConsumableSEPAElement(String uri, String name, String description, String iconUrl)
	{
		super(uri, name, description, iconUrl);
	}
	
	public List<EventStream> getEventStreams() {
		return eventStreams;
	}

	public void setEventStreams(List<EventStream> eventStreams) {
		this.eventStreams = eventStreams;
	}

	public List<StaticProperty> getStaticProperties() {
		return staticProperties;
	}

	public void setStaticProperties(List<StaticProperty> staticProperties) {
		this.staticProperties = staticProperties;
	}
	
	public boolean addEventStream(EventStream eventStream)
	{
		return eventStreams.add(eventStream);
	}
}