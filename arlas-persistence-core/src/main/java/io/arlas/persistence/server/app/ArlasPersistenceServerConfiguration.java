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
import io.arlas.commons.config.ArlasConfiguration;
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.commons.rest.utils.ServerConstants;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

public class ArlasPersistenceServerConfiguration extends ArlasConfiguration {

    @JsonProperty("arlas-base-uri")
    public String arlasBaseUri;

    @JsonProperty("persistence_engine")
    public String engine;

    @JsonProperty("firestore_collection")
    public String firestoreCollection;

    @JsonProperty("local_folder")
    public String localFolder;

    @Valid
    @JsonProperty("database")
    public DataSourceFactory database = new DataSourceFactory();

    public void check() throws ArlasConfigurationException {
        super.check();

        if (arlasCorsConfiguration.allowedHeaders == null) {
            throw new ArlasConfigurationException("Arlas Allowed Headers Configuration configuration missing in config file.");
        } else {
            List<String> allowedHeaders = Arrays.stream(arlasCorsConfiguration.allowedHeaders.split(","))
                    .map(String::trim).toList();

            boolean allHeaderIsAllowed =
                    allowedHeaders.contains(arlasAuthConfiguration.headerUser) && allowedHeaders.contains(arlasAuthConfiguration.headerUser) &&
                            allowedHeaders.contains(ServerConstants.ARLAS_ORGANISATION);
            if (!allHeaderIsAllowed) {
                throw new ArlasConfigurationException("User header or Groups header or Organization Header is missing from Arlas Allowed Headers.");
            }
        }
        if ("firestore".equals(engine)) {
            if (firestoreCollection == null || firestoreCollection.isEmpty()) {
                throw new ArlasConfigurationException("Configuration 'firestore_collection' is required when using engine 'firestore'");
            }
        }
    }
}
