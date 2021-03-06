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

package org.streampipes.container.api;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.streampipes.commons.Utils;
import org.streampipes.container.declarer.DataStreamDeclarer;
import org.streampipes.container.declarer.Declarer;
import org.streampipes.container.declarer.SemanticEventProducerDeclarer;
import org.streampipes.container.init.DeclarersSingleton;
import org.streampipes.container.transform.Transformer;
import org.streampipes.empire.core.empire.SupportsRdfId;
import org.streampipes.empire.core.empire.annotation.InvalidRdfException;
import org.streampipes.model.SpDataStream;
import org.streampipes.model.base.NamedStreamPipesEntity;
import org.streampipes.model.graph.DataProcessorDescription;
import org.streampipes.model.graph.DataSinkDescription;
import org.streampipes.model.graph.DataSourceDescription;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public abstract class Element<D extends Declarer> {
    public Element() {
    }

    protected abstract List<D> getElementDeclarers();

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDescription(@PathParam("id") String elementId) {
        return getJsonLd(elementId);
    }

    protected String getJsonLd(String id) {
        NamedStreamPipesEntity elem = rewrite(getById(id));
        return toJsonLd(elem);
    }

    protected String getJsonLd(NamedStreamPipesEntity desc, String appendix) {
        return toJsonLd(rewrite(desc, appendix));
    }

    protected D getDeclarerById(String id) {
        for (D declarer : getElementDeclarers()) {
            if (declarer.declareModel().getUri().equals(id)) {
                return declarer;
            }
        }
        return null;
    }

    protected NamedStreamPipesEntity getById(String id) {
        NamedStreamPipesEntity desc = null;
        for (Declarer declarer : getElementDeclarers()) {
            if (declarer.declareModel().getUri().equals(id)) {
                //TODO find a better solution to add the event streams to the SepDescription
                if (declarer instanceof SemanticEventProducerDeclarer) {
                    DataSourceDescription secDesc = ((SemanticEventProducerDeclarer) declarer).declareModel();
                    List<DataStreamDeclarer> eventStreamDeclarers = ((SemanticEventProducerDeclarer) declarer).getEventStreams();
                    for (DataStreamDeclarer esd : eventStreamDeclarers) {
                        secDesc.addEventStream(esd.declareModel(secDesc));
                    }

                    desc = secDesc;
                } else {
                    desc = declarer.declareModel();
                }
            }
        }
        return desc;
    }

    protected NamedStreamPipesEntity rewrite(NamedStreamPipesEntity desc) {
        return rewrite(desc, "");
    }

    protected NamedStreamPipesEntity rewrite(NamedStreamPipesEntity desc, String appendix) {

        //TODO remove this and find a better solution
        if (desc != null) {
            String type = "";

            if (desc instanceof DataProcessorDescription) {
                type = "sepa/";
            } else if (desc instanceof DataSourceDescription) {
                type = "sep/";
            } else if (desc instanceof DataSinkDescription) {
                type = "sec/";
            } else if (desc instanceof SpDataStream) {
                type = "sep/" + appendix + "/";
            }

            String originalId = desc.getUri();
            String uri = DeclarersSingleton.getInstance().getBaseUri()+ type + desc.getUri();
            desc.setUri(uri);
            desc.setRdfId(new SupportsRdfId.URIKey(URI.create(uri)));

            if (desc instanceof DataSourceDescription) {
                for(SpDataStream stream : ((DataSourceDescription) desc).getSpDataStreams()) {
                    String baseUri = DeclarersSingleton.getInstance().getBaseUri()
                            +type
                            +originalId
                            +"/"
                            +stream.getUri();
                    stream.setUri(baseUri);
                    stream.setRdfId(new SupportsRdfId.URIKey(URI.create(baseUri)));
                }
            }
        }

        return desc;
    }

    protected String toJsonLd(NamedStreamPipesEntity namedElement) {
        if (namedElement != null) {
            Graph rdfGraph;
            try {
                rdfGraph = Transformer.toJsonLd(namedElement);
                return Utils.asString(rdfGraph);
            } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException | InvalidRdfException | RDFHandlerException e) {
                e.printStackTrace();
            }
        }
        return "{}";
    }
}
