package de.fzi.cep.sepa.esper.drillingstop.single;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.rio.RDFHandlerException;

import com.clarkparsia.empire.annotation.InvalidRdfException;

import de.fzi.cep.sepa.commons.Utils;
import de.fzi.cep.sepa.esper.EsperDeclarer;
import de.fzi.cep.sepa.esper.config.EsperConfig;
import de.fzi.cep.sepa.esper.util.StandardTransportFormat;
import de.fzi.cep.sepa.model.impl.Domain;
import de.fzi.cep.sepa.model.impl.EventProperty;
import de.fzi.cep.sepa.model.impl.EventPropertyPrimitive;
import de.fzi.cep.sepa.model.impl.EventSchema;
import de.fzi.cep.sepa.model.impl.EventStream;
import de.fzi.cep.sepa.model.impl.FreeTextStaticProperty;
import de.fzi.cep.sepa.model.impl.MappingPropertyUnary;
import de.fzi.cep.sepa.model.impl.StaticProperty;
import de.fzi.cep.sepa.model.impl.graph.SEPA;
import de.fzi.cep.sepa.model.impl.graph.SEPAInvocationGraph;
import de.fzi.cep.sepa.model.impl.output.AppendOutputStrategy;
import de.fzi.cep.sepa.model.impl.output.OutputStrategy;
import de.fzi.cep.sepa.model.transform.JsonLdTransformer;
import de.fzi.cep.sepa.model.util.SEPAUtils;
import de.fzi.cep.sepa.model.vocabulary.MhWirth;
import de.fzi.cep.sepa.model.vocabulary.XSD;

public class DrillingStopEnrichedController extends EsperDeclarer<DrillingStopEnrichedParameters>{

	@Override
	public boolean invokeRuntime(SEPAInvocationGraph sepa) {
		
		try {
			System.out.println(Utils.asString(new JsonLdTransformer().toJsonLd(sepa)));
		} catch (RDFHandlerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidRdfException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int minRpm = Integer.parseInt(SEPAUtils.getFreeTextStaticPropertyValue(sepa, "rpm"));
		int minTorque = Integer.parseInt(SEPAUtils.getFreeTextStaticPropertyValue(sepa, "torque"));
		
		String latPropertyName = SEPAUtils.getMappingPropertyName(sepa, "rpm");
		String lngPropertyName = SEPAUtils.getMappingPropertyName(sepa, "torque");	
	
		System.out.println(minRpm +", " +minTorque +", " +latPropertyName +", " +lngPropertyName);
		DrillingStopEnrichedParameters staticParam = new DrillingStopEnrichedParameters(
				sepa, 
				minRpm,
				minTorque,
				latPropertyName,
				lngPropertyName);
	
		try {
			return invokeEPRuntime(staticParam, DrillingStopEnriched::new, sepa);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public SEPA declareModel() {
		List<String> domains = new ArrayList<String>();
		domains.add(Domain.DOMAIN_PROASENSE.toString());
		
		EventStream stream1 = new EventStream();
		
		EventSchema schema1 = new EventSchema();
		EventPropertyPrimitive p1 = new EventPropertyPrimitive(Utils.createURI(MhWirth.Rpm));
		schema1.addEventProperty(p1);
		
		EventPropertyPrimitive p2 = new EventPropertyPrimitive(Utils.createURI(MhWirth.Torque));
		schema1.addEventProperty(p2);
		
		
		SEPA desc = new SEPA("/sepa/drillingstopenriched", "Drilling Stop", "Detects stop of a drilling process (starting from single event source)", "", "/sepa/drillingstopenriched", domains);
		desc.setIconUrl(EsperConfig.iconBaseUrl + "/Drilling_Stop_HQ.png");
		
		
		stream1.setUri(EsperConfig.serverUrl +"/" +Utils.getRandomString());
		stream1.setEventSchema(schema1);
		desc.addEventStream(stream1);
		
		List<OutputStrategy> strategies = new ArrayList<OutputStrategy>();
		List<EventProperty> appendProperties = new ArrayList<EventProperty>();			
		
		EventProperty result = new EventPropertyPrimitive(XSD._boolean.toString(),
				"drillingStatus", "", de.fzi.cep.sepa.commons.Utils.createURI(MhWirth.DrillingStatus));;
	
		appendProperties.add(result);
		strategies.add(new AppendOutputStrategy(appendProperties));
		desc.setOutputStrategies(strategies);
		
		List<StaticProperty> staticProperties = new ArrayList<StaticProperty>();
		
		FreeTextStaticProperty rpmThreshold = new FreeTextStaticProperty("rpm", "RPM threshold");
		FreeTextStaticProperty torqueThreshold = new FreeTextStaticProperty("torque", "Torque threshold");
		staticProperties.add(rpmThreshold);
		staticProperties.add(torqueThreshold);
		
		staticProperties.add(new MappingPropertyUnary(URI.create(p1.getElementName()), "rpm", "Select RPM Mapping"));
		staticProperties.add(new MappingPropertyUnary(URI.create(p2.getElementName()), "torque", "Select Torque Mapping"));
		desc.setStaticProperties(staticProperties);
		desc.setSupportedGrounding(StandardTransportFormat.getSupportedGrounding());
		return desc;
	}
	
}