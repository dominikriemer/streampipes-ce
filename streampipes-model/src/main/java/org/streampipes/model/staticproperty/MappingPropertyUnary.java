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

package org.streampipes.model.staticproperty;

import org.streampipes.empire.annotations.RdfProperty;
import org.streampipes.empire.annotations.RdfsClass;
import org.streampipes.vocabulary.StreamPipes;

import java.net.URI;

import javax.persistence.Entity;

@RdfsClass(StreamPipes.MAPPING_PROPERTY_UNARY)
@Entity
public class MappingPropertyUnary extends MappingProperty{

	private static final long serialVersionUID = 2903529966128844426L;
	
	@RdfProperty(StreamPipes.MAPS_TO)
	private URI mapsTo;
	
	public MappingPropertyUnary()
	{
		super(StaticPropertyType.MappingPropertyUnary);
	}
	
	public MappingPropertyUnary(MappingPropertyUnary other)
	{
		super(other);
		this.mapsTo = other.getMapsTo();
	}
	
	public MappingPropertyUnary(URI mapsFrom, String internalName, String label, String description)
	{
		super(StaticPropertyType.MappingPropertyUnary, mapsFrom, internalName, label, description);
	}
	
	public MappingPropertyUnary(String internalName, String label, String description)
	{
		super(StaticPropertyType.MappingPropertyUnary, internalName, label, description);
	}

	public URI getMapsTo() {
		return mapsTo;
	}

	public void setMapsTo(URI mapsTo) {
		this.mapsTo = mapsTo;
	}

}
