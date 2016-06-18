package de.fzi.cep.sepa.manager.matching.v2;

import java.net.URI;
import java.util.List;

import de.fzi.cep.sepa.manager.matching.v2.utils.MatchingUtils;
import de.fzi.cep.sepa.messages.MatchingResultMessage;
import de.fzi.cep.sepa.messages.MatchingResultType;
import de.fzi.cep.sepa.model.impl.eventproperty.EventPropertyPrimitive;

public class PrimitivePropertyMatch extends AbstractMatcher<EventPropertyPrimitive, EventPropertyPrimitive>{

	public PrimitivePropertyMatch() {
		super(MatchingResultType.PROPERTY_MATCH);
	}

	@Override
	public boolean match(EventPropertyPrimitive offer,
			EventPropertyPrimitive requirement, List<MatchingResultMessage> errorLog) {
		boolean matches =  MatchingUtils.nullCheck(offer, requirement) ||
				(unitMatch(offer.getMeasurementUnit(), requirement.getMeasurementUnit(), errorLog) &&
						datatypeMatch(offer.getRuntimeType(), requirement.getRuntimeType(), errorLog) &&
						domainPropertyMatch(offer.getDomainProperties(), requirement.getDomainProperties(), errorLog));
		
		if (!matches) buildErrorMessage(errorLog, buildText(requirement));
		return matches;
	}

	private String buildText(EventPropertyPrimitive requirement) {
		if (requirement == null) return "-";
		return "Required domain properties: " +requirement.getDomainProperties().size() +", "
				+"required data type: " +requirement.getRuntimeType();
	}

	private boolean domainPropertyMatch(List<URI> offer,
			List<URI> requirement, List<MatchingResultMessage> errorLog) {
		return new DomainPropertyMatch().match(offer, requirement, errorLog);
	}

	private boolean datatypeMatch(String offer, String requirement,
			List<MatchingResultMessage> errorLog) {
		return new DatatypeMatch().match(offer, requirement, errorLog);
	}

	private boolean unitMatch(URI offer, URI requirement,
			List<MatchingResultMessage> errorLog) {
		return new MeasurementUnitMatch().match(offer, requirement, errorLog);
	}

}