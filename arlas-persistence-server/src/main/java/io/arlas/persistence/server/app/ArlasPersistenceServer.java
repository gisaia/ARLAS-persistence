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

import brave.http.HttpTracing;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smoketurner.dropwizard.zipkin.ZipkinBundle;
import com.smoketurner.dropwizard.zipkin.ZipkinFactory;
import io.arlas.persistence.rest.PersistenceRestService;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.impl.GoogleFirestorePersistenceServiceImpl;
import io.arlas.persistence.server.impl.HibernatePersistenceServiceImpl;
import io.arlas.persistence.server.model.Data;
import io.arlas.server.auth.AuthenticationFilter;
import io.arlas.server.auth.AuthorizationFilter;
import io.arlas.server.exceptions.ArlasExceptionMapper;
import io.arlas.server.exceptions.ConstraintViolationExceptionMapper;
import io.arlas.server.exceptions.IllegalArgumentExceptionMapper;
import io.arlas.server.utils.InsensitiveCaseFilter;
import io.arlas.server.utils.PrettyPrintFilter;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;
import java.util.Optional;

public class ArlasPersistenceServer extends Application<ArlasPersistenceServerConfiguration> {
    Logger LOGGER = LoggerFactory.getLogger(ArlasPersistenceServer.class);

    private final HibernateBundle<ArlasPersistenceServerConfiguration> hibernate = new HibernateBundle<ArlasPersistenceServerConfiguration>(Data.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(ArlasPersistenceServerConfiguration configuration) {
            return configuration.database;
        }
    };
    public static void main(String... args) throws Exception {
        new ArlasPersistenceServer().run(args);
    }

    @Override
    public void initialize(Bootstrap<ArlasPersistenceServerConfiguration> bootstrap) {
        bootstrap.registerMetrics();
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
        bootstrap.addBundle(new SwaggerBundle<ArlasPersistenceServerConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ArlasPersistenceServerConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
        bootstrap.addBundle(new ZipkinBundle<ArlasPersistenceServerConfiguration>(getName()) {
            @Override
            public ZipkinFactory getZipkinFactory(ArlasPersistenceServerConfiguration configuration) {
                return configuration.zipkinConfiguration;
            }
        });
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(ArlasPersistenceServerConfiguration configuration, Environment environment) throws Exception {
        LOGGER.info("Raw configuration: " + (new ObjectMapper()).writer().writeValueAsString(configuration));
        configuration.check();
        LOGGER.info("Checked configuration: " + (new ObjectMapper()).writer().writeValueAsString(configuration));

        if (configuration.zipkinConfiguration != null) {
            Optional<HttpTracing> tracing = configuration.zipkinConfiguration.build(environment);
        }

        environment.getObjectMapper().setSerializationInclusion(Include.NON_NULL);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(new ArlasExceptionMapper());
        environment.jersey().register(new IllegalArgumentExceptionMapper());
        environment.jersey().register(new JsonProcessingExceptionMapper());
        environment.jersey().register(new ConstraintViolationExceptionMapper());


        PersistenceService persistenceService = null;
        switch (configuration.engine) {
            case "hibernate":
                persistenceService = new HibernatePersistenceServiceImpl(hibernate.getSessionFactory());
                break;
            case "firestore":
                persistenceService = new GoogleFirestorePersistenceServiceImpl();
                break;
            default:
                LOGGER.error("Engine not supported: " + configuration.engine +  " (valid values are: 'hibernate' or 'firestore').");
                System.exit(1);
                break;
        }
        environment.jersey().register(new PersistenceRestService(persistenceService, configuration));

        // Auth
        if (configuration.arlasAuthConfiguration.enabled) {
            environment.jersey().register(new AuthenticationFilter(configuration.arlasAuthConfiguration));
            environment.jersey().register(new AuthorizationFilter(configuration.arlasAuthConfiguration));
        }

        //cors
        configureCors(environment,configuration.arlasCorsConfiguration);

        //filters
        environment.jersey().register(PrettyPrintFilter.class);
        environment.jersey().register(InsensitiveCaseFilter.class);
    }

    private void configureCors(Environment environment, ArlasCorsConfiguration configuration) {
        CrossOriginFilter filter = new CrossOriginFilter();
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CrossOriginFilter", filter);

        // Configure CORS parameters
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, configuration.allowedOrigins);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, configuration.allowedHeaders);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, configuration.allowedMethods);
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, String.valueOf(configuration.allowedCredentials));
        cors.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, configuration.exposedHeaders);

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
