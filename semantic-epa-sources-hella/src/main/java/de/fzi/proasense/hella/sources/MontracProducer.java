package de.fzi.proasense.hella.sources;

import java.util.ArrayList;
import java.util.List;

import de.fzi.cep.sepa.desc.declarer.EventStreamDeclarer;
import de.fzi.cep.sepa.desc.declarer.SemanticEventProducerDeclarer;
import de.fzi.cep.sepa.model.impl.graph.SepDescription;
import de.fzi.proasense.hella.streams.MaterialMovementStream;

public class MontracProducer implements SemanticEventProducerDeclarer {

	@Override
	public SepDescription declareModel() {
		
		SepDescription sep = new SepDescription("source/montrac", "Montrac", "Provides streams generated by the Montrac transportation system");
		
		return sep;
	}

	
	@Override
	public List<EventStreamDeclarer> getEventStreams() {
		
		List<EventStreamDeclarer> streams = new ArrayList<EventStreamDeclarer>();
		
		streams.add(new MaterialMovementStream());
		
		return streams;
	}
}