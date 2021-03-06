/*
 * Copyright 2018 FZI Forschungszentrum Informatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.streampipes.manager.matching.output;

import java.util.List;

import org.streampipes.model.schema.EventProperty;
import org.streampipes.model.schema.EventSchema;
import org.streampipes.model.SpDataStream;
import org.streampipes.model.output.FixedOutputStrategy;

public class FixedOutputSchemaGenerator implements OutputSchemaGenerator<FixedOutputStrategy> {

	private List<EventProperty> fixedProperties;
	
	public FixedOutputSchemaGenerator(List<EventProperty> fixedProperties) {
		this.fixedProperties = fixedProperties;
	}
	
	@Override
	public EventSchema buildFromOneStream(SpDataStream stream) {
		return new EventSchema(fixedProperties);
	}

	@Override
	public EventSchema buildFromTwoStreams(SpDataStream stream1,
			SpDataStream stream2) {
		return buildFromOneStream(stream1);
	}

	@Override
	public FixedOutputStrategy getModifiedOutputStrategy(
			FixedOutputStrategy strategy) {
		return strategy;
	}

}
