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
package io.arlas.persistence.server.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.server.core.app.ArlasAuthConfiguration;
import io.arlas.server.core.app.ArlasCorsConfiguration;
import io.arlas.server.core.exceptions.ArlasConfigurationException;
import io.arlas.server.core.utils.StringUtil;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArlasPersistenceServerConfiguration extends Configuration {
    @Valid
    @JsonProperty("database")
    public DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("zipkin")
    public ZipkinFactory zipkinConfiguration;

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration;

    @JsonProperty("arlas-base-uri")
    public String arlasBaseUri;

    @NotNull
    @JsonProperty("arlas_cors")
    public ArlasCorsConfiguration arlasCorsConfiguration;

    @JsonProperty("arlas_auth")
    public ArlasAuthConfiguration arlasAuthConfiguration;

    @NotNull
    @JsonProperty("arlas_organization_header")
    public String organizationHeader;

    @JsonProperty("persistence_engine")
    public String engine;

    @JsonProperty("firestore_collection")
    public String firestoreCollection;

    @JsonProperty("local_folder")
    public String localFolder;

    @JsonProperty("anonymous_value")
    public String anonymousValue;

    public void check() throws ArlasConfigurationException {
        if (arlasAuthConfiguration == null) {
            arlasAuthConfiguration = new ArlasAuthConfiguration();
            arlasAuthConfiguration.enabled = false;
        }else{
            arlasAuthConfiguration.check();

        }
        if (arlasCorsConfiguration.allowedHeaders == null) {
            throw new ArlasConfigurationException("Arlas Allowed Headers Configuration configuration missing in config file.");
        } else {
            List<String> allowedHeaders = Arrays.stream(arlasCorsConfiguration.allowedHeaders.split(","))
                    .map(String::trim).collect(Collectors.toList());

            boolean allHeaderIsAllowed =
                    allowedHeaders.contains(arlasAuthConfiguration.headerUser) && allowedHeaders.contains(arlasAuthConfiguration.headerGroup) &&
                            allowedHeaders.contains(organizationHeader);
            if (!allHeaderIsAllowed) {
                throw new ArlasConfigurationException("User header or Groups header or Organization Header is missing from Arlas Allowed Headers.");
            }
        }
        if ("firestore".equals(engine)) {
            if (StringUtil.isNullOrEmpty(firestoreCollection)) {
                throw new ArlasConfigurationException("Configuration 'firestore_collection' is required when using engine 'firestore'");
            }
        }
    }
}
