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
import io.arlas.persistence.server.app.Documentation;
import io.arlas.persistence.server.app.core.DataService;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.response.Error;
import io.arlas.server.utils.ResponseFormatter;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected static Logger LOGGER = LoggerFactory.getLogger(PersistenceRestService.class);
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    private DataService dataService;
    private String keyHeader;

    public PersistenceRestService(DataService dataService, String keyHeader) {
        this.dataService = dataService;
        this.keyHeader = keyHeader;
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
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Key not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response list(
            @Context HttpHeaders headers,

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
        return ResponseFormatter.getResultResponse(dataService.list(key));
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
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Key or id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response get(
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
        return ResponseFormatter.getResultResponse(dataService.get(key, id));
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
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = String.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response create(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

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
        return Response.created(uriInfo.getRequestUriBuilder().build()).entity(dataService.create(key, value)).type("application/json").build();
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
    @ApiResponses(value = {@ApiResponse(code = 201, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Key or id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

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
        return Response.created(uriInfo.getRequestUriBuilder().build()).entity(dataService.update(key, id, value)).type("application/json").build();
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
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Key or id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

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
        return Response.accepted().entity(dataService.delete(key, id)).type("application/json").build();
    }

    private String getKey(HttpHeaders headers) throws ArlasException {

        String key = headers.getHeaderString(keyHeader);
        if (StringUtils.isBlank(key)) {
            throw new ArlasException(keyHeader + " is empty.");
        }

        return key;
    }
}
