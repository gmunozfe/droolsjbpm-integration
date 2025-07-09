/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.controller.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.impl.KieServerControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.controller.rest.ControllerUtils.createCorrectVariant;
import static org.kie.server.controller.rest.ControllerUtils.getContentType;
import static org.kie.server.controller.rest.ControllerUtils.marshal;
import static org.kie.server.controller.rest.ControllerUtils.unmarshal;

@Path("/controller")
public class RestKieServerControllerImpl extends KieServerControllerImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestKieServerControllerImpl.class);

    @POST
    @Path("server/{serverInstanceId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updatetKieServer(@Context HttpHeaders headers,
                                     @PathParam("serverInstanceId") String id,
                                     String serverInfoPayload) {
        String contentType = getContentType(headers);
        logger.debug("Received connect request from server with id {}", id);
        KieServerStateInfo kieServerStateInfo = unmarshal(serverInfoPayload, contentType, KieServerStateInfo.class);

        logger.debug("Server info update {}", kieServerStateInfo);
        KieServerSetup serverSetup = update(kieServerStateInfo);

        String response = marshal(contentType, serverSetup);
        logger.debug("Returning response for connect of server '{}': {}", id, response);
        return createCorrectVariant(response, headers, serverSetup.hasNoErrors() ? Response.Status.OK: Response.Status.BAD_REQUEST);
    }

    @PUT
    @Path("server/{serverInstanceId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response connectKieServer(@Context HttpHeaders headers,
                                     @PathParam("serverInstanceId") String id,
                                     String serverInfoPayload) {
        String contentType = getContentType(headers);
        logger.debug("Received connect request from server with id {}", id);
        KieServerInfo serverInfo = unmarshal(serverInfoPayload, contentType, KieServerInfo.class);
        if (serverInfo == null) {
            serverInfo = new KieServerInfo();
            serverInfo.setServerId(id);
        }
        logger.debug("Server info {}", serverInfo);
        KieServerSetup serverSetup = connect(serverInfo);

        if (serverSetup.hasNoErrors()) {
            logger.info("Server with id '{}' connected", id);
        } else {
            logger.warn("Server with id '{}' failed to connect", id);
        }

        String response = marshal(contentType, serverSetup);
        logger.debug("Returning response for connect of server '{}': {}", id, response);
        return createCorrectVariant(response, headers, serverSetup.hasNoErrors() ? Response.Status.CREATED : Response.Status.BAD_REQUEST);
    }

    @DELETE
    @Path("server/{serverInstanceId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response disconnectKieServer(@Context HttpHeaders headers,
                                        @PathParam("serverInstanceId") String id,
                                        @QueryParam("location") String serverLocation) {
        try {
            String location = serverLocation == null || serverLocation.trim().length() == 0 
                    ? "" : URLDecoder.decode(serverLocation, "UTF-8");
            KieServerInfo serverInfo = new KieServerInfo(id, "", "", Collections.<String>emptyList(), location);
            disconnect(serverInfo);
            logger.info("Server with id '{}' disconnected", id);
        } catch (UnsupportedEncodingException e) {
            logger.debug("Cannot URL decode kie server location due to unsupported encoding exception", e);
        }
        return null;
    }

}
