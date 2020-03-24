/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.persistence.rest;

import com.codahale.metrics.annotation.Timed;
import io.arlas.persistence.model.DataResource;
import io.arlas.persistence.model.DataWithLinks;
import io.arlas.persistence.server.app.ArlasPersistenceServerConfiguration;
import io.arlas.persistence.server.app.Documentation;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.model.Data;
import io.arlas.persistence.server.utils.SortOrder;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.response.Error;
import io.arlas.server.utils.ResponseFormatter;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/persistence")
@Api(value = "/persistence")
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                title = "ARLAS persistence API",
                description = "persistence REST services",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "API_VERSION"),
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS })

public class PersistenceRestService {
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    private PersistenceService persistenceService;
    private DataHALService halService;
    private String keyHeader;

    public PersistenceRestService(PersistenceService persistenceService, ArlasPersistenceServerConfiguration configuration) {
        this.persistenceService = persistenceService;
        this.halService = new DataHALService(configuration.arlasBaseUri);
        this.keyHeader = configuration.keyHeader;
    }

    @Timed
    @Path("/")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.LIST_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.LIST_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = DataResource.class),
            @ApiResponse(code = 404, message = "Key not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response list(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(
                    name = "type", value = Documentation.TYPE,
                    allowMultiple = false,
                    defaultValue = "pref",
                    required = true)
            @QueryParam(value = "type") String type,

            @ApiParam(name = "size", value = "Page Size",
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("10")
            @QueryParam(value = "size") Integer size,

            @ApiParam(name = "page", value = "Page ID",
                    defaultValue = "1",
                    allowableValues = "range[1, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("1")
            @QueryParam(value = "page") Integer page,

            @ApiParam(name = "order", value = "Date sort order",
                    defaultValue = "desc",
                    allowableValues = "desc,asc",
                    type = "string",
                    required = false)
            @QueryParam(value = "order") SortOrder order,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        String key = getKey(headers);
        return ResponseFormatter.getResultResponse(halService.dataListToResource(persistenceService.list(type, key, size, page, order), uriInfo, page, size, order));
    }

    @Timed
    @Path("/{id}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.GET_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.GET_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 404, message = "Key or id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response get(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(
                    name = "id",
                    value = Documentation.ID,
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "id") String id,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        String key = getKey(headers);
        return ResponseFormatter.getResultResponse(halService.dataWithLinks(persistenceService.getById(id), uriInfo));
    }

    @Timed
    @Path("/")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.CREATE_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.CREATE_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response create(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(
                    name = "type", value = Documentation.TYPE,
                    allowMultiple = false,
                    defaultValue = "hibernate",
                    required = true)
            @QueryParam(value = "type") String type,

            @ApiParam(
                    name = "value",
                    value = Documentation.VALUE,
                    required = true)
            @NotNull @Valid String value,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        String key = getKey(headers);
        return Response.created(uriInfo.getRequestUriBuilder().build())
                .entity(halService.dataWithLinks(persistenceService.create(type, key, value), uriInfo))
                .type("application/json")
                .build();
    }

    @Timed
    @Path("/{id}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.UPDATE_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.UPDATE_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 404, message = "Key or id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response update(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(
                    name = "id",
                    value = Documentation.ID,
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "id") String id,

            @ApiParam(
                    name = "value",
                    value = Documentation.VALUE,
                    required = true)
            @NotNull @Valid String value,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        String key = getKey(headers);
        return Response.created(uriInfo.getRequestUriBuilder().build())
                .entity(halService.dataWithLinks(persistenceService.update(id, value), uriInfo))
                .type("application/json")
                .build();
    }

    @Timed
    @Path("/{id}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.DELETE_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.DELETE_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Successful operation", response = Data.class),
            @ApiResponse(code = 404, message = "Key or id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response delete(
            @Context HttpHeaders headers,

            @ApiParam(
                    name = "id",
                    value = Documentation.ID,
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "id") String id,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        String key = getKey(headers);
        return Response.accepted().entity(persistenceService.delete(id)).type("application/json").build();
    }

    private String getKey(HttpHeaders headers) throws ArlasException {
        String key = headers.getHeaderString(keyHeader);
        if (StringUtils.isBlank(key)) {
            throw new ArlasException(keyHeader + " is empty.");
        }

        return key;
    }
}
