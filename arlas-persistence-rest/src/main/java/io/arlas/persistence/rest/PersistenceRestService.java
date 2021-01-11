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
import io.arlas.persistence.model.Exists;
import io.arlas.persistence.server.app.ArlasPersistenceServerConfiguration;
import io.arlas.persistence.server.app.Documentation;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.model.IdentityParam;
import io.arlas.persistence.server.utils.SortOrder;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.response.Error;
import io.arlas.server.utils.ResponseFormatter;
import io.dropwizard.hibernate.UnitOfWork;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.stream.Collectors;


@Path("/persist")
@Api(value = "/persist")
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                title = "ARLAS persistence API",
                description = "persistence REST services",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "13.0.0-beta.3"),
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS })

public class PersistenceRestService {
    Logger LOGGER = LoggerFactory.getLogger(PersistenceRestService.class);
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    private final PersistenceService persistenceService;
    private final DataHALService halService;
    private final String userHeader;
    private final String organizationHeader;
    private final String groupsHeader;
    private final String anonymousValue;


    public PersistenceRestService(PersistenceService persistenceService, ArlasPersistenceServerConfiguration configuration) {
        this.persistenceService = persistenceService;
        this.halService = new DataHALService(configuration.arlasBaseUri);
        this.userHeader = configuration.arlasAuthConfiguration.headerUser;
        this.organizationHeader = configuration.organizationHeader;
        this.groupsHeader = configuration.arlasAuthConfiguration.headerGroup;
        this.anonymousValue = configuration.anonymousValue;
    }

    @Timed
    @Path("resources/{zone}")
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
            @ApiResponse(code = 404, message = "Zone not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Persistence Error.", response = Error.class)})

    @UnitOfWork
    public Response list(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(name = "zone", value = Documentation.ZONE,
                    defaultValue = "pref",
                    required = true)
            @PathParam(value = "zone") String zone,

            @ApiParam(name = "size", value = "Page Size",
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer")
            @DefaultValue("10")
            @QueryParam(value = "size") Integer size,

            @ApiParam(name = "page", value = "Page ID",
                    defaultValue = "1",
                    allowableValues = "range[1, infinity]",
                    type = "integer")
            @DefaultValue("1")
            @QueryParam(value = "page") Integer page,

            @ApiParam(name = "order", value = "Date sort order",
                    defaultValue = "desc",
                    allowableValues = "desc,asc",
                    type = "string")
            @QueryParam(value = "order") SortOrder order,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        return ResponseFormatter.getResultResponse(
                halService.dataListToResource(
                        persistenceService.list(zone, identityparam, size, page, order), uriInfo, page, size, order, identityparam));
    }

    @Timed
    @Path("resource/{zone}/{key}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.GET_FROM_KEY_ZONE_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.GET_FROM_KEY_ZONE_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 404, message = "Key or zone not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Persistence Error.", response = Error.class)})

    @UnitOfWork
    public Response getByKey(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(name = "zone", value = Documentation.ZONE,
                    defaultValue = "pref",
                    required = true)
            @PathParam(value = "zone") String zone,

            @ApiParam(name = "key", value = Documentation.KEY,
                    required = true)
            @PathParam(value = "key") String key,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        DataWithLinks dataWithLinks = new DataWithLinks(persistenceService.get(zone, key, identityparam), identityparam);
        return ResponseFormatter.getResultResponse(halService.dataWithLinks(dataWithLinks, uriInfo, identityparam));
    }

    @Timed
    @Path("resource/exists/{zone}/{key}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.EXISTS_FROM_KEY_ZONE_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.EXISTS_FROM_KEY_ZONE_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Exists.class),
            @ApiResponse(code = 500, message = "Arlas Persistence Error.", response = Error.class)})

    @UnitOfWork
    public Response existsByKey(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(name = "zone", value = Documentation.ZONE,
                    defaultValue = "pref",
                    required = true)
            @PathParam(value = "zone") String zone,

            @ApiParam(name = "key", value = Documentation.KEY,
                    required = true)
            @PathParam(value = "key") String key,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        try {
            persistenceService.get(zone, key, identityparam);
            return Response.ok(new Exists(true)).build();
        } catch (NotFoundException e) {
            return Response.ok(new Exists(false)).build();
        }
    }

    @Timed
    @Path("resource/id/{id}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.GET_FROM_ID_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.GET_FROM_ID_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 404, message = "Id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Persistence Error.", response = Error.class)})

    @UnitOfWork
    public Response getById(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(name = "id",
                    value = Documentation.ID,
                    required = true)
            @PathParam(value = "id") String id,


            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
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
    @ApiOperation(
            value = Documentation.EXISTS_FROM_ID_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.EXISTS_FROM_ID_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Exists.class),
            @ApiResponse(code = 500, message = "Arlas Persistence Error.", response = Error.class)})

    @UnitOfWork
    public Response existsById(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(name = "id",
                    value = Documentation.ID,
                    required = true)
            @PathParam(value = "id") String id,


            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
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
    @ApiOperation(
            value = Documentation.GET_GROUPS_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.GET_GROUPS_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = String[].class),
            @ApiResponse(code = 404, message = "Zone not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Persistence Error.", response = Error.class)})

    @UnitOfWork
    public Response getGroupsByZone(
            @Context UriInfo uriInfo,
            @Context HttpHeaders headers,

            @ApiParam(name = "zone", value = Documentation.ZONE,
                    defaultValue = "pref",
                    required = true)
            @PathParam(value = "zone") String zone,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
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

            @ApiParam(name = "zone", value = Documentation.ZONE,
                    defaultValue = "pref",
                    required = true)
            @PathParam(value = "zone") String zone,

            @ApiParam(name = "key", value = Documentation.KEY,
                    required = true)
            @PathParam(value = "key") String key,

            @ApiParam(name = "readers", value = Documentation.READERS)
            @QueryParam(value = "readers") List<String> readers,

            @ApiParam(name = "writers", value = Documentation.WRITERS)
            @QueryParam(value = "writers") List<String> writers,

            @ApiParam(name = "value",
                    value = Documentation.VALUE,
                    required = true)
            @NotNull @Valid String value,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
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

            @ApiParam(name = "id",
                    value = Documentation.ID,
                    required = true)
            @PathParam(value = "id") String id,

            @ApiParam(name = "key", value = Documentation.KEY)
            @QueryParam(value = "key") String key,

            @ApiParam(name = "readers", value = Documentation.READERS)
            @QueryParam(value = "readers") List<String> readers,

            @ApiParam(name = "writers", value = Documentation.WRITERS)
            @QueryParam(value = "writers") List<String> writers,

            @ApiParam(name = "value",
                    value = Documentation.VALUE,
                    required = true)
            @NotNull @Valid String value,

            @ApiParam(name = "last_update",
                    value = Documentation.LAST_UPDATE,
                    required = true)
            @QueryParam(value = "last_update")  Long lastUpdate,


            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
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
    @ApiOperation(
            value = Documentation.DELETE_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.DELETE_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 404, message = "Key or id not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response deleteById(
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo,
            @ApiParam(name = "id",
                    value = Documentation.ID,
                    required = true)
            @PathParam(value = "id") String id,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        DataWithLinks dataWithLinks =new DataWithLinks(persistenceService.deleteById(id, identityparam), identityparam);
        return Response.accepted().entity(halService.dataWithLinks(dataWithLinks, uriInfo, identityparam))
                .type("application/json")
                .build();
    }

    @Timed
    @Path("resource/{zone}/{key}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = Documentation.DELETE_OPERATION,
            produces = UTF8JSON,
            notes = Documentation.DELETE_OPERATION,
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 202, message = "Successful operation", response = DataWithLinks.class),
            @ApiResponse(code = 404, message = "Zone or key not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    @UnitOfWork
    public Response delete(
            @Context HttpHeaders headers,
            @Context UriInfo uriInfo,

            @ApiParam(name = "zone", value = Documentation.ZONE,
                    defaultValue = "pref",
                    required = true)
            @PathParam(value = "zone") String zone,

            @ApiParam(name = "key", value = Documentation.KEY,
                    required = true)
            @PathParam(value = "key") String key,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = io.arlas.server.app.Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        IdentityParam identityparam = getIdentityParam(headers);
        DataWithLinks dataWithLinks =new DataWithLinks(persistenceService.delete(zone,key,identityparam),identityparam);
        return Response.accepted().entity(halService.dataWithLinks(dataWithLinks, uriInfo,identityparam))
                .type("application/json")
                .build();
    }

    private IdentityParam getIdentityParam(HttpHeaders headers) {
        String userId = Optional.ofNullable(headers.getHeaderString(this.userHeader))
                .orElse(this.anonymousValue);

        String organization = Optional.ofNullable(headers.getHeaderString(this.organizationHeader))
                .orElse(""); // in a context where resources are publicly available, no organisation is defined

        List<String> groups = Arrays.stream(
                Optional.ofNullable(headers.getHeaderString(this.groupsHeader)).orElse("group/public").split(","))
                .map(g -> g.trim())
                .collect(Collectors.toList());

        LOGGER.info("User='" + userId + "' / Org='" + organization + "' / Groups='" + groups.toString() + "'");
        return new IdentityParam(userId, organization, groups);
    }
}
