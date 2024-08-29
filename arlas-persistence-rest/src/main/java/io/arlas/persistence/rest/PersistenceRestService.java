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
import io.arlas.commons.config.ArlasAuthConfiguration;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.commons.rest.response.Error;
import io.arlas.commons.rest.utils.ResponseFormatter;
import io.arlas.filter.core.IdentityParam;
import io.arlas.persistence.model.DataResource;
import io.arlas.persistence.model.DataWithLinks;
import io.arlas.persistence.model.Exists;
import io.arlas.persistence.server.app.ArlasPersistenceServerConfiguration;
import io.arlas.persistence.server.app.Documentation;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.utils.SortOrder;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Path("/persist")
@Tag(name="persist", description="Persistence API")
@OpenAPIDefinition(
        info = @Info(
                title = "ARLAS Persistence APIs",
                description = "Persistence REST services.",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                version = "26.0.0"),
        externalDocs = @ExternalDocumentation(
                description = "API documentation",
                url="https://docs.arlas.io/arlas-api/"),
        servers = {
                @Server(url = "/arlas_persistence_server", description = "default server")
        }
)

public class PersistenceRestService {
    Logger LOGGER = LoggerFactory.getLogger(PersistenceRestService.class);
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    private final PersistenceService persistenceService;
    private final DataHALService halService;
    private final ArlasAuthConfiguration configuration;

    public PersistenceRestService(PersistenceService persistenceService, ArlasPersistenceServerConfiguration configuration) {
        this.persistenceService = persistenceService;
        this.halService = new DataHALService(configuration.arlasBaseUri);
        this.configuration = configuration.arlasAuthConfiguration;
    }

    @Timed
    @Path("resources/{zone}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = Documentation.LIST_OPERATION,
            description = Documentation.LIST_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", 
                    content = @Content(schema = @Schema(implementation = DataResource.class))),
            @ApiResponse(responseCode = "404", description = "Zone not found.", 
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Persistence Error.", 
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    @UnitOfWork
    public Response list(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @Parameter(name = "zone", 
                    description = Documentation.ZONE,
                    schema = @Schema(defaultValue = "pref"),
                    required = true)
            @PathParam(value = "zone") String zone,

            @Parameter(name = "size", 
                    description = "Page Size",
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "10"))
            @DefaultValue("10")
            @QueryParam(value = "size") Integer size,

            @Parameter(name = "page", 
                    description = "Page ID",
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "1"))
            @DefaultValue("1")
            @QueryParam(value = "page") Integer page,

            @Parameter(name = "order", 
                    description = "Date sort order",
                    schema = @Schema(defaultValue = "DESC"))
            @QueryParam(value = "order") SortOrder order,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // ----------------------- Filter -----------------------
            // --------------------------------------------------------
            @Parameter(name = "key",
                    description = Documentation.SEARCH_KEY,
                    schema = @Schema(type = "string"), required = false)
            @QueryParam(value = "key") String key
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        return ResponseFormatter.getResultResponse(
                halService.dataListToResource(
                        persistenceService.list(zone, identityparam, size, page, order, key), uriInfo, page, size, order, identityparam));
    }

    @Timed
    @Path("resource/id/{id}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = Documentation.GET_FROM_ID_OPERATION,
            description = Documentation.GET_FROM_ID_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", 
                    content = @Content(schema = @Schema(implementation = DataWithLinks.class))),
            @ApiResponse(responseCode = "404", description = "Id not found.", 
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Persistence Error.", 
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    @UnitOfWork
    public Response getById(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @Parameter(name = "id",
                    description = Documentation.ID,
                    required = true)
            @PathParam(value = "id") String id,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        DataWithLinks dataWithLinks = new DataWithLinks(persistenceService.getById(id, identityparam), identityparam);
        return ResponseFormatter.getResultResponse(halService.dataWithLinks(dataWithLinks, uriInfo, identityparam));
    }

    @Timed
    @Path("resource/exists/id/{id}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = Documentation.EXISTS_FROM_ID_OPERATION,
            description = Documentation.EXISTS_FROM_ID_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Exists.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Persistence Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    @UnitOfWork
    public Response existsById(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @Parameter(name = "id",
                    description = Documentation.ID,
                    required = true)
            @PathParam(value = "id") String id,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        try {
            persistenceService.getById(id, identityparam);
            return Response.ok(new Exists(true)).build();
        } catch (NotFoundException e) {
            return Response.ok(new Exists(false)).build();
        }
    }

    @Timed
    @Path("groups/{zone}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = Documentation.GET_GROUPS_OPERATION,
            description = Documentation.GET_GROUPS_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = String[].class))),
            @ApiResponse(responseCode = "404", description = "Zone not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Persistence Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))})

    @UnitOfWork
    public Response getGroupsByZone(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @Parameter(name = "zone",
                    description = Documentation.ZONE,
                    schema = @Schema(defaultValue = "pref"),
                    required = true)
            @PathParam(value = "zone") String zone,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty
    ) {
        IdentityParam identityparam = getIdentityParam(headers);
        return ResponseFormatter.getResultResponse(PersistenceService.getGroupsForZone(zone, identityparam));
    }


    @Timed
    @Path("resource/{zone}/{key}")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = Documentation.CREATE_OPERATION,
            description = Documentation.CREATE_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DataWithLinks.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    @UnitOfWork
    public Response create(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @Parameter(name = "zone",
                    description = Documentation.ZONE,
                    schema = @Schema(defaultValue = "pref"),
                    required = true)
            @PathParam(value = "zone") String zone,

            @Parameter(name = "key",
                    description = Documentation.KEY,
                    required = true)
            @PathParam(value = "key") String key,

            @Parameter(name = "readers",
                    description = Documentation.READERS)
            @QueryParam(value = "readers") List<String> readers,

            @Parameter(name = "writers",
                    description = Documentation.WRITERS)
            @QueryParam(value = "writers") List<String> writers,

            @Parameter(name = "description",
                    description = Documentation.VALUE,
                    required = true)
            @NotNull @Valid String value,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        Set<String> readersSet = Optional.ofNullable(readers).map(r -> new HashSet<>(readers)).orElse(new HashSet<>());
        Set<String> writersSet = Optional.ofNullable(writers).map(r -> new HashSet<>(writers)).orElse(new HashSet<>());
        DataWithLinks dataWithLinks = new DataWithLinks(
                persistenceService.create(zone, key, identityparam, readersSet, writersSet, value), identityparam);
        return Response.created(uriInfo.getRequestUriBuilder().build())
                .entity(halService.dataWithLinks(dataWithLinks, uriInfo, identityparam))
                .type("application/json")
                .build();
    }

    @Timed
    @Path("resource/id/{id}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = Documentation.UPDATE_OPERATION,
            description = Documentation.UPDATE_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DataWithLinks.class))),
            @ApiResponse(responseCode = "404", description = "Key or id not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    @UnitOfWork
    public Response update(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @Parameter(name = "id",
                    description = Documentation.ID,
                    required = true)
            @PathParam(value = "id") String id,

            @Parameter(name = "key",
                    description = Documentation.KEY)
            @QueryParam(value = "key") String key,

            @Parameter(name = "readers",
                    description = Documentation.READERS)
            @QueryParam(value = "readers") List<String> readers,

            @Parameter(name = "writers",
                    description = Documentation.WRITERS)
            @QueryParam(value = "writers") List<String> writers,

            @Parameter(name = "description",
                    description = Documentation.VALUE,
                    required = true)
            @NotNull @Valid String value,

            @Parameter(name = "last_update",
                    description = Documentation.LAST_UPDATE,
                    required = true)
            @QueryParam(value = "last_update")  Long lastUpdate,


            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty", description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        Set<String> readersSet = Optional.ofNullable(readers).map(r-> new HashSet<>(readers)).orElse(new HashSet<>());
        Set<String> writersSet = Optional.ofNullable(writers).map(r-> new HashSet<>(writers)).orElse(new HashSet<>());
        Date lastUpdateDate = new Date(lastUpdate);
        DataWithLinks dataWithLinks = new DataWithLinks(
                persistenceService.update(id, key, identityparam, readersSet, writersSet, value, lastUpdateDate), identityparam);
        return Response.created(uriInfo.getRequestUriBuilder().build())
                .entity(halService.dataWithLinks(dataWithLinks, uriInfo, identityparam))
                .type("application/json")
                .build();
    }

    @Timed
    @Path("resource/id/{id}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = Documentation.DELETE_OPERATION,
            description = Documentation.DELETE_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = DataWithLinks.class))),
            @ApiResponse(responseCode = "404", description = "Key or id not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    @UnitOfWork
    public Response deleteById(
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            @Parameter(name = "id",
                    description = Documentation.ID,
                    required = true)
            @PathParam(value = "id") String id,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        DataWithLinks dataWithLinks = new DataWithLinks(persistenceService.deleteById(id, identityparam), identityparam);
        return Response.accepted().entity(halService.dataWithLinks(dataWithLinks, uriInfo, identityparam))
                .type("application/json")
                .build();
    }

    private IdentityParam getIdentityParam(HttpHeaders headers) {
        IdentityParam idp = new IdentityParam(configuration, headers);
        LOGGER.info("User='" + idp.userId + "' / Org='" + idp.organisation + "' / Groups='" + idp.groups + "'");
        return idp;
    }
}
