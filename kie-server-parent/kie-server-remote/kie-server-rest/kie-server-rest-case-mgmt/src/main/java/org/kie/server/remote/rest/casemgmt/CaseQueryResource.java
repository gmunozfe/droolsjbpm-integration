/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.casemgmt;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import org.kie.api.runtime.query.QueryContext;
import org.kie.server.api.model.cases.CaseDefinitionList;
import org.kie.server.api.model.cases.CaseFileDataItemList;
import org.kie.server.api.model.cases.CaseInstanceCustomVarsList;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.api.model.cases.CaseUserTaskWithVariablesList;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.rest.RestURI;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint.EndpointType;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.CASE_ALL_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_ALL_PROCESSES_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_FILE_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_ID;
import static org.kie.server.api.rest.RestURI.CASE_INSTANCES_BY_ROLE_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_PROCESSES_BY_CONTAINER_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_QUERY_URI;
import static org.kie.server.api.rest.RestURI.CASE_ROLE_NAME;
import static org.kie.server.api.rest.RestURI.CASE_TASKS_AS_ADMIN_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_TASKS_AS_POT_OWNER_GET_URI;
import static org.kie.server.api.rest.RestURI.CASE_TASKS_AS_STAKEHOLDER_GET_URI;
import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_DEFINITIONS_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.CASE_INSTANCES_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.GET_CASE_FILE_DATA_RESPONSE_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.GET_PROCESS_DEFS_RESPONSE_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.GET_TASK_SUMMARY_RESPONSE_JSON;
import static org.kie.server.remote.rest.casemgmt.docs.ParameterSamples.JSON;
import static org.kie.server.remote.rest.common.util.RestUtils.buildConversationIdHeader;
import static org.kie.server.remote.rest.common.util.RestUtils.createCorrectVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.errorMessage;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.internalServerError;

@Api(value="Case queries")
@Path("server/" + CASE_QUERY_URI)
public class CaseQueryResource extends AbstractCaseResource {

    private static final Logger logger = LoggerFactory.getLogger(CaseQueryResource.class);

    public CaseQueryResource() {

    }

    public CaseQueryResource(
            final CaseManagementRuntimeDataServiceBase caseManagementRuntimeDataServiceBase,
            final KieServerRegistry context) {
        super(caseManagementRuntimeDataServiceBase, context);
    }

    @ApiOperation(value="Returns cases instances with authentication checks.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, response = CaseInstanceList.class, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_INSTANCES_JSON)}))})
    @GET
    @Path(CASE_ALL_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstances(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "data item name that case instances will be filtered by", required = false) @QueryParam("dataItemName") String dataItemName, 
            @ApiParam(value = "data item value that case instances will be filtered by", required = false) @QueryParam("dataItemValue") String dataItemValue,
            @ApiParam(value = "case instance owner that case instances will be filtered by", required = false) @QueryParam("owner") String owner,
            @ApiParam(value = "optional case instance status (open, closed, canceled) - defaults ot open (1) only", required = false, allowableValues="open,closed,cancelled") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder,
            @ApiParam(value = "optional flag to load data when loading case instance", required = false) @QueryParam("withData") @DefaultValue("false") boolean withData) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    CaseInstanceList responseObject = null;
                    if (dataItemName != null && !dataItemName.isEmpty() && dataItemValue != null && !dataItemValue.isEmpty()) {
                        logger.debug("About to look for case instances by data item name {} and value {} with status {}", dataItemName, dataItemValue, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByCaseFileData(dataItemName, dataItemValue, status, page, pageSize, sort, sortOrder, withData);
                    } else if (dataItemName != null && !dataItemName.isEmpty()) {
                        logger.debug("About to look for case instances by data item name {} with status {}", dataItemName, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByCaseFileData(dataItemName, null, status, page, pageSize, sort, sortOrder, withData);
                    } else if (owner != null && !owner.isEmpty()) {
                        logger.debug("About to look for case instances owned by {} with status {}", owner, status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesOwnedBy(owner,
                                                                                                           status, page, pageSize, sort, sortOrder, withData);
                    } else {
                        logger.debug("About to look for case instances with status {}", status);
                        responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesAnyRole(status, page, pageSize, sort, sortOrder, withData);
                    }

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns cases instances that involve the querying user in a specified role.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, response = CaseInstanceList.class, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_INSTANCES_JSON)}))})
    @GET
    @Path(CASE_INSTANCES_BY_ROLE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstancesByRole(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case role that instances should be found for", required = true, example = "owner") @PathParam(CASE_ROLE_NAME) String roleName, 
            @ApiParam(value = "optional case instance status (open, closed, canceled) - defaults ot open (1) only", required = false, allowableValues="open,closed,cancelled") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder,
            @ApiParam(value = "optional flag to load data when loading case instance", required = false) @QueryParam("withData") @DefaultValue("false") boolean withData) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case instances with status {}", status);
                    CaseInstanceList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseInstancesByRole(roleName, status, page, pageSize, sort, sortOrder, withData);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    /*
     * case definition methods
     */
    
    @ApiOperation(value="Returns a specified case definition from all KIE containers.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, message = "Successful response", response = CaseDefinitionList.class, examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=CASE_DEFINITIONS_JSON)}))})
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseDefinitions(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case definition id or name that case definitions will be filtered by", required = true) @QueryParam("filter") String filter,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for case definitions with filter {}", filter);
                    CaseDefinitionList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseDefinitions(filter, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    /*
     * process definition methods
     */
    @ApiOperation(value="Returns a specified process associated with case definitions from all KIE containers.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, response = ProcessDefinitionList.class, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_DEFS_RESPONSE_JSON)}))})
    @GET
    @Path(CASE_ALL_PROCESSES_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitions(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "process definition id or name that process definitions will be filtered by", required = true) @QueryParam("filter") String filter,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for process definitions with filter {}", filter);
                    ProcessDefinitionList responseObject = this.caseManagementRuntimeDataServiceBase.getProcessDefinitions(filter, null, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns processes associated with case definitions in a specified KIE container.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, response = ProcessDefinitionList.class, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_PROCESS_DEFS_RESPONSE_JSON)}))})
    @GET
    @Path(CASE_PROCESSES_BY_CONTAINER_INSTANCES_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProcessDefinitionsByContainer(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "container id that process definitions should be filtered by", required = true, example = "evaluation_1.0.0-SNAPSHOT") @PathParam(CONTAINER_ID) String containerId,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {

                    logger.debug("About to look for process definitions with container id {}", containerId);
                    ProcessDefinitionList responseObject = this.caseManagementRuntimeDataServiceBase.getProcessDefinitions(null, containerId, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    /*
     * Case tasks
     */

    @ApiOperation(value="Returns tasks for potential owners in a specified case instance.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, response = TaskSummaryList.class, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(CASE_TASKS_AS_POT_OWNER_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceTasksAsPotentialOwner(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case instance identifier that tasks should belong to", required = true, example = "CASE-00000000011") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for case instance {} tasks with status {} assigned to potential owner {}", caseId, status, user);
                    TaskSummaryList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseTasks(caseId, user, status, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns tasks for business administrators in a specified case instance.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, response = TaskSummaryList.class, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(CASE_TASKS_AS_ADMIN_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceTasksAsAdmin(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case instance identifier that tasks should belong to", required = true, example = "CASE-00000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for case instance {} tasks with status {} assigned to business admin {}", caseId, status, user);
                    TaskSummaryList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseTasksAsBusinessAdmin(caseId, user, status, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns tasks for stakeholders in a specified case instance.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, response = TaskSummaryList.class, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_TASK_SUMMARY_RESPONSE_JSON)}))})
    @GET
    @Path(CASE_TASKS_AS_STAKEHOLDER_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceTasksAsStakeholder(@javax.ws.rs.core.Context HttpHeaders headers,
            @ApiParam(value = "case instance identifier that tasks should belong to", required = true, example = "CASE-0000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optional user id to be used instead of authenticated user - only when bypass authenticated user is enabled", required = false) @QueryParam("user") String user, 
            @ApiParam(value = "optional task status (Created, Ready, Reserved, InProgress, Suspended, Completed, Failed, Error, Exited, Obsolete)", required = false, allowableValues="Created, Ready, Reserved,InProgress,Suspended,Completed,Failed,Error,Exited,Obsolete") @QueryParam("status") List<String> status,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize,
            @ApiParam(value = "optional sort column, no default", required = false) @QueryParam("sort") String sort, 
            @ApiParam(value = "optional sort direction (asc, desc) - defaults to asc", required = false) @QueryParam("sortOrder") @DefaultValue("true") boolean sortOrder) {

        return invokeCaseOperation(headers,
                "",
                null,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to look for case instance {} tasks with status {} assigned to stakeholder {}", caseId, status, user);
                    TaskSummaryList responseObject = this.caseManagementRuntimeDataServiceBase.getCaseTasksAsStakeholder(caseId, user, status, page, pageSize, sort, sortOrder);

                    logger.debug("Returning OK response with content '{}'", responseObject);
                    return createCorrectVariant(responseObject, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value="Returns case file data items for a specified case instance.")
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Unexpected error"), 
            @ApiResponse(code = 200, response = CaseFileDataItemList.class, message = "Successful response", examples=@Example(value= {
                    @ExampleProperty(mediaType=JSON, value=GET_CASE_FILE_DATA_RESPONSE_JSON)}))})
    @GET
    @Path(CASE_FILE_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCaseInstanceDataItems(@javax.ws.rs.core.Context HttpHeaders headers, 
            @ApiParam(value = "case instance identifier that data items should belong to", required = true, example = "CASE-0000000001") @PathParam(CASE_ID) String caseId,
            @ApiParam(value = "optionally filter by data item names", required = false) @QueryParam("name") List<String> names, 
            @ApiParam(value = "optionally filter by data item types", required = false) @QueryParam("type") List<String> types,
            @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
            @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        return invokeCaseOperation(headers,
                "",
                caseId,
                (Variant v, String type, Header... customHeaders) -> {
                    logger.debug("About to load case file data items of case {}", caseId);
                    CaseFileDataItemList response = this.caseManagementRuntimeDataServiceBase.getCaseInstanceDataItems(caseId, names, types, page, pageSize);

                    logger.debug("Returning OK response with content '{}'", response);
                    return createCorrectVariant(response, headers, Response.Status.OK, customHeaders);
                });
    }

    @ApiOperation(value = "Queries cases by variables and tasks", response = CaseInstanceCustomVarsList.class)
    @POST
    @Path(RestURI.VARIABLES_CASES_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @KieServerEndpoint(categories = {EndpointType.DEFAULT, EndpointType.HISTORY})
    public Response queryCaseByVariables(@Context HttpHeaders headers, String payload,
                                         @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
                                         @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        Variant v = getVariant(headers);
        try {
            String type = getContentType(headers);
            CaseInstanceCustomVarsList processVariableSummaryList = caseManagementRuntimeDataServiceBase.queryCasesByVariables(payload, type, new QueryContext(page * pageSize, pageSize));
            logger.debug("Returning result of case instance search: {}", processVariableSummaryList);

            return createCorrectVariant(processVariableSummaryList, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }

    }

    @ApiOperation(value = "Queries cases tasks by variables", response = CaseUserTaskWithVariablesList.class)
    @POST
    @Path(RestURI.VARIABLES_TASKS_CASES_URI)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @KieServerEndpoint(categories = {EndpointType.DEFAULT, EndpointType.HISTORY})
    public Response queryCaseUserTasksByVariables(@Context HttpHeaders headers,
                                                  String payload,
                                                  @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page, 
                                                  @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Header conversationIdHeader = buildConversationIdHeader("", context, headers);
        Variant v = getVariant(headers);
        try {
            String type = getContentType(headers);
            CaseUserTaskWithVariablesList taskVariableSummaryList = caseManagementRuntimeDataServiceBase.queryUserTasksByVariables(payload, type, new QueryContext((page * pageSize), pageSize));
            logger.debug("Returning result of case instance user task search: {}", taskVariableSummaryList);

            return createCorrectVariant(taskVariableSummaryList, headers, Response.Status.OK, conversationIdHeader);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            return internalServerError(errorMessage(e), v, conversationIdHeader);
        }
    }
}
