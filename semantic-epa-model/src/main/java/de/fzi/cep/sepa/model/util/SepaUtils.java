package de.fzi.cep.sepa.model.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import de.fzi.cep.sepa.model.ConsumableSEPAElement;
import de.fzi.cep.sepa.model.InvocableSEPAElement;
import de.fzi.cep.sepa.model.impl.AnyStaticProperty;
import de.fzi.cep.sepa.model.impl.EventProperty;
import de.fzi.cep.sepa.model.impl.EventPropertyList;
import de.fzi.cep.sepa.model.impl.EventPropertyNested;
import de.fzi.cep.sepa.model.impl.EventPropertyPrimitive;
import de.fzi.cep.sepa.model.impl.EventStream;
import de.fzi.cep.sepa.model.impl.FreeTextStaticProperty;
import de.fzi.cep.sepa.model.impl.MappingProperty;
import de.fzi.cep.sepa.model.impl.MappingPropertyNary;
import de.fzi.cep.sepa.model.impl.MappingPropertyUnary;
import de.fzi.cep.sepa.model.impl.MatchingStaticProperty;
import de.fzi.cep.sepa.model.impl.OneOfStaticProperty;
import de.fzi.cep.sepa.model.impl.Option;
import de.fzi.cep.sepa.model.impl.StaticProperty;
import de.fzi.cep.sepa.model.impl.graph.SecInvocation;
import de.fzi.cep.sepa.model.impl.graph.SepaInvocation;

public class SepaUtils {

	public static StaticProperty getStaticPropertyByName(ConsumableSEPAElement sepa, String name)
	{
		return getStaticPropertyByName(sepa.getStaticProperties(), name);
	}
	
	public static String getFreeTextStaticPropertyValue(SepaInvocation graph, String name)
	{
		StaticProperty staticProperty = getStaticPropertyByName(graph, name);
		if (staticProperty instanceof FreeTextStaticProperty)
			return ((FreeTextStaticProperty) staticProperty).getValue();
		return null;
	}
	
	public static StaticProperty getStaticPropertyByName(SepaInvocation seg, String name)
	{
		return getStaticPropertyByName(seg.getStaticProperties(), name);
	}
	
	public static StaticProperty getStaticPropertyByName(SecInvocation sec, String name)
	{
		return getStaticPropertyByName(sec.getStaticProperties(), name);
	}
	
	// TODO: fetch event property from db for given static property name
	public static String getMappingPropertyName(InvocableSEPAElement sepa, String staticPropertyName)
	{
		return getMappingPropertyName(sepa, staticPropertyName, false);
	}
	
	public static String getMappingPropertyName(InvocableSEPAElement sepa, String staticPropertyName, boolean completeNames)
	{
		URI propertyURI = getURIFromStaticProperty(sepa, staticPropertyName);
		for(EventStream stream : sepa.getInputStreams())
		{
			List<String> matchedProperties = getMappingPropertyName(stream.getEventSchema().getEventProperties(), propertyURI, completeNames, "");
			if (matchedProperties.size() > 0) return matchedProperties.get(0);
		}
		return null;
		//TODO: exceptions
	}
	
	public static List<String> getMultipleMappingPropertyNames(InvocableSEPAElement sepa, String staticPropertyName, boolean completeNames)
	{
		List<URI> propertyUris = getMultipleURIsFromStaticProperty(sepa, staticPropertyName);
		
		List<String> result = new ArrayList<String>();
		for(URI propertyUri : propertyUris)
		{
			for(EventStream stream : sepa.getInputStreams())
			{
				result.addAll(getMappingPropertyName(stream.getEventSchema().getEventProperties(), propertyUri, completeNames, ""));
			}
		}
		return result;
		//TODO: exceptions
	}
	
	//TODO fix return null
	private static List<String> getMappingPropertyName(List<EventProperty> eventProperties, URI propertyURI, boolean completeNames, String prefix)
	{
		List<String> result = new ArrayList<String>();
		for(EventProperty p : eventProperties)
		{
			if (p instanceof EventPropertyPrimitive || p instanceof EventPropertyList)
			{	
				if (p.getRdfId().toString().equals(propertyURI.toString())) 
					{
						if (!completeNames) result.add(p.getRuntimeName());
						else 
								result.add(prefix + p.getRuntimeName());
					}
			}
			else if (p instanceof EventPropertyNested)
			{
				result.addAll(getMappingPropertyName(((EventPropertyNested) p).getEventProperties(), propertyURI, completeNames, prefix + p.getRuntimeName() +"."));
			}
		}
		return result;
	}
	
	//TODO check if correct
	private static String getEventPropertyNameByPrefix(List<EventProperty> eventProperties, String namePrefix, boolean completeNames, String propertyPrefix)
	{
		for(EventProperty p : eventProperties)
		{
			if (p instanceof EventPropertyPrimitive || p instanceof EventPropertyList)
			{
				if (p.getRuntimeName().startsWith(namePrefix)) 
					{
						if (!completeNames) return p.getRuntimeName();
						else return propertyPrefix + p.getRuntimeName();
					}
			}
			else if (p instanceof EventPropertyNested)
			{
				//propertyPrefix = propertyPrefix + p.getPropertyName() +".";
				//getEventPropertyNameByPrefix(((EventPropertyNested) p).getEventProperties(), namePrefix, completeNames, propertyPrefix);
				if (p.getRuntimeName().startsWith(namePrefix))
				{
					if (!completeNames) return p.getRuntimeName();
					else return propertyPrefix + p.getRuntimeName();
				}
			}
		}
		return null;
	}
	
	public static String getEventPropertyName(List<EventProperty> properties, String namePrefix)
	{
		return getEventPropertyNameByPrefix(properties, namePrefix, true, "");
	}
	
	private static URI getURIFromStaticProperty(InvocableSEPAElement sepa, String staticPropertyName)
	{
		for(StaticProperty p : sepa.getStaticProperties())
		{		
			if (p instanceof MappingPropertyUnary)
			{
				MappingPropertyUnary mp = (MappingPropertyUnary) p;
				// check if anything else breaks
				if (mp.getInternalName().equals(staticPropertyName)) return mp.getMapsTo();
			}
		}
		return null;
		//TODO: exceptions
	}
	
	private static List<URI> getMultipleURIsFromStaticProperty(InvocableSEPAElement sepa, String staticPropertyName)
	{
		for(StaticProperty p : sepa.getStaticProperties())
		{
			if (p instanceof MappingPropertyNary)
			{
				MappingPropertyNary mp = (MappingPropertyNary) p;
				if (mp.getInternalName().equals(staticPropertyName)) return mp.getMapsTo();
			}
		}
		return null;
		//TODO: exceptions
	}
	
	public static URI getURIbyPropertyName(EventStream stream, String propertyName)
	{
		for(EventProperty p : stream.getEventSchema().getEventProperties())
		{
			if (p.getRuntimeName().equals(propertyName))
				try {
					return new URI(p.getRdfId().toString());
				} catch (URISyntaxException e) {
					return null;
				}
		}
		return null;
		//TODO exceptions
	}
	
	
	private static StaticProperty getStaticPropertyByName(List<StaticProperty> properties, String name)
	{
		for(StaticProperty p : properties)
		{
			if (p.getInternalName().equals(name)) return p;
		}
		return null;
	}

	public static String getOneOfProperty(InvocableSEPAElement sepa,
			String staticPropertyName) {
		for(StaticProperty p : sepa.getStaticProperties())
		{
			if (p.getInternalName().equals(staticPropertyName))
			{
				if (p instanceof OneOfStaticProperty)
				{
					OneOfStaticProperty thisProperty = (OneOfStaticProperty) p;
					for(Option option : thisProperty.getOptions())
					{
						if (option.isSelected()) return option.getName();
					}
				}
			}
		}
		return null;
		//TODO exceptions
	}
	
	public static List<StaticProperty> cloneStaticProperties(List<StaticProperty> staticProperties)
	{
		List<StaticProperty> result = new ArrayList<>();
		for(StaticProperty property : staticProperties)
		{
			if (property instanceof FreeTextStaticProperty)
				result.add(generateClonedFreeTextProperty((FreeTextStaticProperty) property));
			else if (property instanceof OneOfStaticProperty)
					result.add(generateClonedOneOfStaticProperty((OneOfStaticProperty) property));
				else if (property instanceof MappingProperty)
					result.add(generateClonedMappingProperty((MappingProperty) property));
					else if (property instanceof MatchingStaticProperty)
						result.add(generateClonedMatchingStaticProperty((MatchingStaticProperty) property));
					else if (property instanceof AnyStaticProperty)
						result.add(generateAnyStaticProperty((AnyStaticProperty) property));
		}
		return result;
	}

	private static StaticProperty generateAnyStaticProperty(
			AnyStaticProperty property) {
		AnyStaticProperty newProperty = new AnyStaticProperty(property.getInternalName(), property.getDescription());
		newProperty.setOptions(cloneOptions(property.getOptions()));
		return newProperty;
		
	}

	private static List<Option> cloneOptions(List<Option> options) {
		List<Option> result = new ArrayList<Option>();
		for(Option option : options)
		{
			Option newOption = new Option(option.getName(), option.isSelected());
			result.add(newOption);
		}
		return result;
	}

	private static StaticProperty generateClonedMatchingStaticProperty(
			MatchingStaticProperty property) {
		MatchingStaticProperty mp = new MatchingStaticProperty(property.getInternalName(), property.getDescription());
		mp.setMatchLeft(property.getMatchLeft());
		mp.setMatchRight(property.getMatchRight());
		return mp;
	}

	private static StaticProperty generateClonedMappingProperty(
			MappingProperty property) {
		
		if (property instanceof MappingPropertyUnary)
		{
			MappingPropertyUnary unaryProperty = (MappingPropertyUnary) property;
			MappingPropertyUnary mp = new MappingPropertyUnary(unaryProperty.getMapsFrom(), unaryProperty.getInternalName(), unaryProperty.getDescription());
			mp.setMapsTo(unaryProperty.getMapsTo());
			return mp;
		}
		else {
			MappingPropertyNary naryProperty = (MappingPropertyNary) property;
			MappingPropertyNary mp = new MappingPropertyNary(naryProperty.getMapsFrom(), naryProperty.getInternalName(), naryProperty.getDescription());
			mp.setMapsTo(naryProperty.getMapsTo());
			return mp;
		}
	}

	private static StaticProperty generateClonedOneOfStaticProperty(
			OneOfStaticProperty property) {
		OneOfStaticProperty osp = new OneOfStaticProperty(property.getInternalName(), property.getDescription());
		osp.setOptions(cloneOptions(osp.getOptions()));
		return osp;
	}

	private static StaticProperty generateClonedFreeTextProperty(
			FreeTextStaticProperty property) {
		FreeTextStaticProperty ftsp = new FreeTextStaticProperty(property.getInternalName(), property.getDescription());
		ftsp.setType(property.getType());
		ftsp.setValue(property.getValue());
		return ftsp;
	}
	
	public static String getFullPropertyName(EventProperty property, List<EventProperty> topLevelProperties, String initialPrefix, char delimiter)
	{
		for(EventProperty schemaProperty : topLevelProperties)
		{
			if (property.getRdfId().toString().equals(schemaProperty.getRdfId().toString())) return initialPrefix + property.getRuntimeName();
			else if (schemaProperty instanceof EventPropertyNested)
			{
				return getFullPropertyName(property, ((EventPropertyNested) schemaProperty).getEventProperties(), initialPrefix +schemaProperty.getRuntimeName() +delimiter, delimiter);
			}
		}
		return null;
	}
}