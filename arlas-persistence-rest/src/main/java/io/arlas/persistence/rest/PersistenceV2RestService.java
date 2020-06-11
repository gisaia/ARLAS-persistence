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
import io.arlas.persistence.model.KeyWithLinks;
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

@Path("/persistence/v2")
@Api(value = "/persistence/v2")
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                title = "ARLAS persistence API",
                description = "persistence REST services",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "API_VERSION"),
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS })

public class PersistenceV2RestService {
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    private PersistenceService persistenceService;
    private DataHALService halService;

    public PersistenceV2RestService(PersistenceService persistenceService, ArlasPersistenceServerConfiguration configuration) {
        this.persistenceService = persistenceService;
        this.halService = new DataHALService(configuration.arlasBaseUri);
    }

    @Timed
    @Path("/{box}/{key}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.GET_FROM_BOX_KEY_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.GET_FROM_BOX_KEY_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 404, message = "Key or box not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response get(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(
                    name = "box",
                    value = Documentation.BOX,
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "box") String box,

            @ApiParam(
                    name = "key",
                    value = Documentation.KEY,
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "key") String key,

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
        return ResponseFormatter.getResultResponse(halService.dataWithLinks(persistenceService.getByTypeKey(box,key), uriInfo));
    }


    @Timed
    @Path("/{box}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.GET_KEY_FROM_BOX_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.GET_KEY_FROM_BOX_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = KeyWithLinks.class),
            @ApiResponse(code = 404, message = "Box not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response getKeyFromBox(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(
                    name = "box", value = Documentation.BOX,
                    allowMultiple = false,
                    defaultValue = "public",
                    required = true)
            @PathParam(value = "box") String box,

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
        return ResponseFormatter.getResultResponse(halService.keyListToResource(persistenceService.listKeyByType(box, size, page, order), uriInfo, page, size, order));
    }


    @Timed
    @Path("/{box}/{key}")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.CREATE_FROM_BOX_KEY_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.CREATE_FROM_BOX_KEY_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response create(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(
            name = "box", value = Documentation.BOX,
            allowMultiple = false,
            defaultValue = "public",
            required = true)
            @PathParam(value = "box") String box,

            @ApiParam(
                    name = "key", value = Documentation.KEY,
                    allowMultiple = false,
                    defaultValue = "public",
                    required = true)
            @PathParam(value = "key") String key,

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
        return Response.created(uriInfo.getRequestUriBuilder().build())
                .entity(halService.dataWithLinks(persistenceService.create(box, key, value), uriInfo))
                .type("application/json")
                .build();
    }

    @Timed
    @Path("/{box}/{key}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.UPDATE_FROM_BOX_KEY_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.UPDATE_FROM_BOX_KEY_OPERATION,
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
                    name = "box", value = Documentation.BOX,
                    allowMultiple = false,
                    defaultValue = "public",
                    required = true)
            @PathParam(value = "box") String box,

            @ApiParam(
                    name = "key", value = Documentation.KEY,
                    allowMultiple = false,
                    defaultValue = "public",
                    required = true)
            @PathParam(value = "key") String key,

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
        return Response.created(uriInfo.getRequestUriBuilder().build())
                .entity(halService.dataWithLinks(persistenceService.updateByTypeKey(box, key, value), uriInfo))
                .type("application/json")
                .build();
    }

    @Timed
    @Path("/{box}/{key}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.DELETE_FROM_BOX_KEY_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.DELETE_FROM_BOX_KEY_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Successful operation", response = Data.class),
            @ApiResponse(code = 404, message = "Key or id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response delete(
            @Context HttpHeaders headers,

            @ApiParam(
                    name = "box", value = Documentation.BOX,
                    allowMultiple = false,
                    defaultValue = "public",
                    required = true)
            @PathParam(value = "box") String box,

            @ApiParam(
                    name = "key", value = Documentation.KEY,
                    allowMultiple = false,
                    defaultValue = "public",
                    required = true)
            @PathParam(value = "key") String key,

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
        return Response.accepted().entity(persistenceService.deleteByTypeKey(box,key)).type("application/json").build();
    }

}
