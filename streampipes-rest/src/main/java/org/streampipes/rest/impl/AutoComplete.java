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

package org.streampipes.rest.impl;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;

import org.streampipes.model.client.messages.AutocompleteItem;
import org.streampipes.model.client.messages.AutocompleteResult;
import org.streampipes.model.client.ontology.OntologyQuery;
import org.streampipes.storage.management.StorageManager;
import org.streampipes.storage.rdf4j.ontology.QueryExecutor;
import org.streampipes.storage.rdf4j.sparql.QueryBuilder;

@Path("/v2/autocomplete")
public class AutoComplete extends AbstractRestInterface {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItem(@QueryParam("propertyName") String propertyName, @QueryParam("term") String term) {
        AutocompleteResult result = new AutocompleteResult();
        String query = QueryBuilder.getAutocompleteSuggestion(propertyName);

        try {
            TupleQueryResult queryResult = new QueryExecutor(StorageManager.INSTANCE.getRepository()).executeQuery(query);
            while (queryResult.hasNext()) {
                BindingSet set = queryResult.next();
                AutocompleteItem item = new AutocompleteItem(set.getValue("label").stringValue(), set.getValue("value").stringValue());
                if (item.getLabel().startsWith(term)) result.add(item);
            }
        } catch (QueryEvaluationException | RepositoryException
                | MalformedQueryException e) {

            e.printStackTrace();
        }
        return ok(result);
    }

    @POST
    @Path("/domain")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOntologyQueryResult(OntologyQuery ontologyQuery) {
        return ok(StorageManager
                .INSTANCE
                .getBackgroundKnowledgeStorage().getOntologyResult(ontologyQuery));
    }

}