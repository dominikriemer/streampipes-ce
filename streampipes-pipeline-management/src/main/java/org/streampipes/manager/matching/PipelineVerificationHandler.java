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

package org.streampipes.manager.matching;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.streampipes.commons.exceptions.NoMatchingJsonSchemaException;
import org.streampipes.commons.exceptions.NoSepaInPipelineException;
import org.streampipes.commons.exceptions.RemoteServerNotAccessibleException;
import org.streampipes.manager.data.PipelineGraph;
import org.streampipes.manager.data.PipelineGraphBuilder;
import org.streampipes.manager.matching.v2.ElementVerification;
import org.streampipes.manager.matching.v2.mapping.MappingPropertyCalculator;
import org.streampipes.manager.util.PipelineVerificationUtils;
import org.streampipes.manager.util.TreeUtils;
import org.streampipes.model.SpDataStream;
import org.streampipes.model.base.InvocableStreamPipesEntity;
import org.streampipes.model.base.NamedStreamPipesEntity;
import org.streampipes.model.client.connection.Connection;
import org.streampipes.model.client.exception.InvalidConnectionException;
import org.streampipes.model.client.pipeline.Pipeline;
import org.streampipes.model.client.pipeline.PipelineModification;
import org.streampipes.model.client.pipeline.PipelineModificationMessage;
import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.model.output.CustomOutputStrategy;
import org.streampipes.model.schema.EventProperty;
import org.streampipes.model.schema.EventPropertyList;
import org.streampipes.model.schema.EventPropertyNested;
import org.streampipes.model.schema.EventPropertyPrimitive;
import org.streampipes.model.staticproperty.MappingProperty;
import org.streampipes.model.staticproperty.Option;
import org.streampipes.model.staticproperty.RemoteOneOfStaticProperty;
import org.streampipes.model.staticproperty.StaticProperty;
import org.streampipes.storage.management.StorageDispatcher;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PipelineVerificationHandler {

    Pipeline pipeline;
    PipelineModificationMessage pipelineModificationMessage;

    List<InvocableStreamPipesEntity> invocationGraphs;

    InvocableStreamPipesEntity rdfRootElement;

    public PipelineVerificationHandler(Pipeline pipeline) throws NoSepaInPipelineException {

        this.pipeline = pipeline;
        this.rdfRootElement = PipelineVerificationUtils.getRootNode(pipeline);
        this.invocationGraphs = new ArrayList<>();

        // prepare a list of all pipeline elements without the root element
        List<NamedStreamPipesEntity> sepaElements = new ArrayList<>();
        sepaElements.addAll(pipeline.getSepas());
        sepaElements.addAll(pipeline.getStreams());
        sepaElements.addAll(pipeline.getActions());
        sepaElements.remove(rdfRootElement);

        pipelineModificationMessage = new PipelineModificationMessage();
    }

    public PipelineVerificationHandler validateConnection() throws InvalidConnectionException {

        ElementVerification verifier = new ElementVerification();
        boolean verified = true;
        // current root element can be either an action or a SEPA
        InvocableStreamPipesEntity rightElement = rdfRootElement;
        List<String> connectedTo = rdfRootElement.getConnectedTo();

        Iterator<String> it = connectedTo.iterator();

        while (it.hasNext()) {
            String domId = it.next();
            NamedStreamPipesEntity element = TreeUtils.findSEPAElement(domId, pipeline.getSepas(), pipeline.getStreams());
            if (element instanceof SpDataStream) {
                SpDataStream leftSpDataStream = (SpDataStream) element;

                if (!(verifier.verify(leftSpDataStream, rightElement))) verified = false;
            } else {
                invocationGraphs.addAll(makeInvocationGraphs(element));
                DataProcessorInvocation ancestor = findInvocationGraph(invocationGraphs, element.getDOM());
                if (!(verifier.verify(ancestor, rightElement))) verified = false;
            }
        }

        if (!verified) throw new InvalidConnectionException(verifier.getErrorLog());
        return this;
    }
    /**
     * dummy method to compute mapping properties (based on EXACT input/output
     * matching)
     *
     * @return PipelineValidationHandler
     */
   public PipelineVerificationHandler computeMappingProperties() throws RemoteServerNotAccessibleException, NoMatchingJsonSchemaException {
       return computeMappingProperties("");
   }

    /**
     * The username in the signature is used for the streamsets integration. Remove when it is no longer required
     * dummy method to compute mapping properties (based on EXACT input/output
     * matching)
     *
     * @return PipelineValidationHandler
     */

    public PipelineVerificationHandler computeMappingProperties(String username) throws RemoteServerNotAccessibleException, NoMatchingJsonSchemaException {
        List<String> connectedTo = rdfRootElement.getConnectedTo();
        String domId = rdfRootElement.getDOM();

        List<SpDataStream> tempStreams = new ArrayList<>();

        for (int i = 0; i < connectedTo.size(); i++) {
            NamedStreamPipesEntity element = TreeUtils.findSEPAElement(rdfRootElement
                    .getConnectedTo().get(i), pipeline.getSepas(), pipeline
                    .getStreams());

            SpDataStream incomingStream;

            if (element instanceof DataProcessorInvocation || element instanceof SpDataStream) {

                if (element instanceof DataProcessorInvocation) {

                    DataProcessorInvocation ancestor = (DataProcessorInvocation) TreeUtils.findByDomId(
                            connectedTo.get(i), invocationGraphs);

                    incomingStream = ancestor.getOutputStream();
                    updateStaticProperties(ancestor.getOutputStream(), i, username);
                    updateOutputStrategy(ancestor.getOutputStream(), i);

                } else {

                    SpDataStream stream = (SpDataStream) element;
                    incomingStream = stream;
                    updateStaticProperties(stream, i, username);
                    updateOutputStrategy(stream, i);

                }

                tempStreams.add(incomingStream);
                if (rdfRootElement.getStreamRequirements().size() - 1 == i) {
                    PipelineModification modification = new PipelineModification(
                            domId,
                            rdfRootElement.getElementId(),
                            rdfRootElement.getStaticProperties());
                    modification.setInputStreams(tempStreams);
                    if (rdfRootElement instanceof DataProcessorInvocation)
                        modification.setOutputStrategies(((DataProcessorInvocation) rdfRootElement).getOutputStrategies());
                    pipelineModificationMessage
                            .addPipelineModification(modification);
                }
            }
        }
        return this;
    }

    public void updateStaticProperties(SpDataStream stream, Integer count, String username) throws RemoteServerNotAccessibleException, NoMatchingJsonSchemaException {

        rdfRootElement
                .getStaticProperties()
                .stream()
                .filter(property -> property instanceof MappingProperty)
                .forEach(property -> {
                    try {

                        MappingProperty mappingProperty = (MappingProperty) property;
                        if (mappingProperty.getMapsFrom() != null) {
                            if (inStream(rdfRootElement.getStreamRequirements().get(count), mappingProperty.getMapsFrom())) {
                                mappingProperty.setMapsFromOptions(new ArrayList<>());
                                ((MappingProperty) property)
                                        .setMapsFromOptions(findSupportedEventProperties(stream,
                                                rdfRootElement.getStreamRequirements(),
                                                mappingProperty.getMapsFrom()));
                            }
                        } else {
                            mappingProperty.setMapsFromOptions(new ArrayList<>());
                            for (EventProperty streamProperty : stream
                                    .getEventSchema().getEventProperties()) {

                                if ((streamProperty instanceof EventPropertyPrimitive) || streamProperty instanceof EventPropertyList) {
                                    mappingProperty.getMapsFromOptions().add(streamProperty);
                                } else {
                                    mappingProperty.getMapsFromOptions().addAll(addNestedProperties((EventPropertyNested) streamProperty));
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });


        List<StaticProperty> allProperties = rdfRootElement
                .getStaticProperties()
                .stream()
                .filter(property -> property instanceof RemoteOneOfStaticProperty)
                .collect(Collectors.toList());;

        for (StaticProperty property : allProperties) {
            updateRemoteOneOfStaticProperty((RemoteOneOfStaticProperty) property, username);
        }

    }

    /**
     * Calls the remote URL and uses the result to set the options of the OneOfStaticProperty
     * @param property
     */
    private void updateRemoteOneOfStaticProperty(RemoteOneOfStaticProperty property, String username) throws RemoteServerNotAccessibleException, NoMatchingJsonSchemaException {

        String label = property.getLabelFieldName();
        String description = property.getDescriptionFieldName();
        String value = property.getValueFieldName();

        try {
            //TODO make this part generic currently it just works with the streamstory component
            String operation = "";
            if (rdfRootElement.getBelongsTo().contains("activity")) {
                operation = "Activity";
            } else {
               operation = "Prediction";
            }

            String url = property.getRemoteUrl() + "streampipes/models?username=" + username + "&analyticsOperation=" + operation;
            Response res = Request.Get(url).useExpectContinue().addHeader("Content-Type", "application/json")
                    .version(HttpVersion.HTTP_1_1)
                    .execute();


            JSONArray data = new JSONArray(res.returnContent().toString());

            List<Option> options = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject object = data.getJSONObject(i);
                options.add(new Option(object.getString(value)));
            }

            property.setOptions(options);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new NoMatchingJsonSchemaException();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteServerNotAccessibleException(e.toString(), property.getRemoteUrl());
        }

    }

    private boolean inStream(SpDataStream stream, URI mapsFrom) {
        return stream
                .getEventSchema()
                .getEventProperties()
                .stream().anyMatch(ep -> ep.getElementId().equals(mapsFrom.toString()));
    }

    private List<EventProperty> findSupportedEventProperties(SpDataStream streamOffer, List<SpDataStream> streamRequirements, URI mapsFrom) {
        EventProperty mapsFromProperty = findPropertyRequirement(mapsFrom);

        return new MappingPropertyCalculator().matchesProperties(streamOffer.getEventSchema().getEventProperties(), mapsFromProperty);
    }

    private EventProperty findPropertyRequirement(URI mapsFrom) {
      return TreeUtils
              .findEventProperty(mapsFrom.toString(), rdfRootElement.getStreamRequirements());
    }

    private void updateOutputStrategy(SpDataStream stream, Integer count) {

        if (rdfRootElement instanceof DataProcessorInvocation) {
            ((DataProcessorInvocation) rdfRootElement)
                    .getOutputStrategies()
                    .stream()
                    .filter(strategy -> strategy instanceof CustomOutputStrategy)
                    .forEach(strategy -> {
                        CustomOutputStrategy outputStrategy = (CustomOutputStrategy) strategy;
                        if (count == 0) {
                            outputStrategy.setProvidesProperties(new ArrayList<>());
                        }
                        if (outputStrategy.isOutputRight() && count > 0)
                            outputStrategy.setProvidesProperties(stream.getEventSchema().getEventProperties());
                        else {
                            if (outputStrategy.getProvidesProperties() == null) {
                                outputStrategy.setProvidesProperties(new ArrayList<>());
                            }
                            outputStrategy.getProvidesProperties().addAll(stream.getEventSchema().getEventProperties());
                        }
                    });
        }
    }

    private List<EventProperty> addNestedProperties(EventPropertyNested properties) {
        List<EventProperty> options = new ArrayList<>();
        for (EventProperty p : properties.getEventProperties()) {
            if (p instanceof EventPropertyPrimitive) options.add(p);
            else options.addAll(addNestedProperties(properties));
        }
        return options;
    }

    public PipelineVerificationHandler storeConnection() {
        String fromId = rdfRootElement.getConnectedTo().get(rdfRootElement.getConnectedTo().size() - 1);
        NamedStreamPipesEntity sepaElement = TreeUtils.findSEPAElement(fromId, pipeline.getSepas(), pipeline.getStreams());
        String sourceId;
        if (sepaElement instanceof SpDataStream) {
            sourceId = sepaElement.getElementId();
        } else {
            sourceId = ((InvocableStreamPipesEntity) sepaElement).getBelongsTo();
        }
        Connection connection = new Connection(sourceId, rdfRootElement.getBelongsTo());
        StorageDispatcher.INSTANCE.getNoSqlStore().getConnectionStorageApi().addConnection(connection);
        return this;
    }

    public PipelineModificationMessage getPipelineModificationMessage() {
        return pipelineModificationMessage;
    }


    private List<InvocableStreamPipesEntity> makeInvocationGraphs(NamedStreamPipesEntity rootElement) {
        PipelineGraph pipelineGraph = new PipelineGraphBuilder(pipeline).buildGraph();
        return new InvocationGraphBuilder(pipelineGraph, null).buildGraphs();
    }

    private DataProcessorInvocation findInvocationGraph(List<InvocableStreamPipesEntity> graphs, String domId) {
        return (DataProcessorInvocation) TreeUtils.findByDomId(domId, graphs);
    }

}