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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streampipes.commons.exceptions.SepaParseException;
import org.streampipes.manager.endpoint.EndpointItemParser;
import org.streampipes.manager.operations.Operations;
import org.streampipes.manager.storage.UserService;
import org.streampipes.model.client.messages.Message;
import org.streampipes.model.client.messages.Notification;
import org.streampipes.model.client.messages.NotificationType;
import org.streampipes.model.client.messages.Notifications;
import org.streampipes.storage.api.IPipelineElementDescriptionStorage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Path("/v2/users/{username}/element")
public class PipelineElementImport extends AbstractRestInterface {

	static Logger  LOG = LoggerFactory.getLogger(PipelineElementImport.class);

	@POST
	@Path("/batch")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addBatch(@PathParam("username") String username, @FormParam("uri") String uri, @FormParam("publicElement") boolean publicElement)
	{
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
			JsonElement element = new JsonParser().parse(parseURIContent(uri, "application/json"));
			List<Message> messages = new ArrayList<>();
			if (element.isJsonArray()) {
				for(JsonElement jsonObj : element.getAsJsonArray()) {
					 messages.add(verifyAndAddElement(jsonObj.getAsString(), username, publicElement));
				}
			}
			return ok(messages);
		} catch (Exception e) {
			e.printStackTrace();
			return statusMessage(Notifications.error(NotificationType.PARSE_ERROR));
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response addElement(@PathParam("username") String username, @FormParam("uri") String uri, @FormParam("publicElement") boolean publicElement)
	{
		if (!authorized(username)) return ok(Notifications.error(NotificationType.UNAUTHORIZED));
		return ok(verifyAndAddElement(uri, username, publicElement));
	}

	private Message verifyAndAddElement(String uri, String username, boolean publicElement) {
		return new EndpointItemParser().parseAndAddEndpointItem(uri, username, publicElement);
	}
	
	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateElement(@PathParam("username") String username, @PathParam("id") String uri)
	{
		if (!authorized(username)) return ok(Notifications.error(NotificationType.UNAUTHORIZED));
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
			String payload = parseURIContent(uri);
			return ok(Operations.verifyAndUpdateElement(payload, username));
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
			return constructErrorMessage(new Notification(NotificationType.PARSE_ERROR.title(), NotificationType.PARSE_ERROR.description(), e.getMessage()));
		} catch (SepaParseException e) {
			e.printStackTrace();
			return constructErrorMessage(new Notification(NotificationType.PARSE_ERROR.title(), NotificationType.PARSE_ERROR.description(), e.getMessage()));
		}
	}
	
	@Path("/{id}")
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteElement(@PathParam("username") String username, @PathParam("id") String elementId) {

		UserService userService = getUserService();
		IPipelineElementDescriptionStorage requestor = getPipelineElementRdfStorage();
		try {
			if (requestor.getSEPAById(elementId) != null) 
				{
					requestor.deleteSEPA(requestor.getSEPAById(elementId));
					userService.deleteOwnSepa(username, elementId);
				}
			else if (requestor.getSEPById(elementId) != null) 
				{
					requestor.deleteSEP(requestor.getSEPById(elementId));
					userService.deleteOwnSource(username, elementId);
				}
			else if (requestor.getSECById(elementId) != null) 
				{
					requestor.deleteSEC(requestor.getSECById(elementId));
					userService.deleteOwnAction(username, elementId);
				}
			else return constructErrorMessage(new Notification(NotificationType.STORAGE_ERROR.title(), NotificationType.STORAGE_ERROR.description()));
		} catch (URISyntaxException e) {
			return constructErrorMessage(new Notification(NotificationType.STORAGE_ERROR.title(), NotificationType.STORAGE_ERROR.description()));
		}
		return constructSuccessMessage(NotificationType.STORAGE_SUCCESS.uiNotification());
	}
	
	@Path("{id}/jsonld")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getActionAsJsonLd(@PathParam("id") String elementId)
	{
		IPipelineElementDescriptionStorage requestor = getPipelineElementRdfStorage();
		elementId = decode(elementId);
		try {
			if (requestor.getSEPAById(elementId) != null) return ok(toJsonLd(requestor.getSEPAById(elementId)));
			else if (requestor.getSEPById(elementId) != null) return ok(toJsonLd(requestor.getSEPById(elementId)));
			else if (requestor.getSECById(elementId) != null) return ok(toJsonLd(requestor.getSECById(elementId)));
			else return ok(Notifications.create(NotificationType.UNKNOWN_ERROR));
		} catch (URISyntaxException e) {
			return constructErrorMessage(new Notification(NotificationType.UNKNOWN_ERROR.title(), NotificationType.UNKNOWN_ERROR.description(), e.getMessage()));
		}
	}
}
