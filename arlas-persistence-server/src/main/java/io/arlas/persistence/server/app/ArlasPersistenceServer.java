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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.arlas.commons.cache.CacheFactory;
import io.arlas.commons.config.ArlasConfiguration;
import io.arlas.commons.config.ArlasCorsConfiguration;
import io.arlas.commons.exceptions.ArlasExceptionMapper;
import io.arlas.commons.exceptions.ConstraintViolationExceptionMapper;
import io.arlas.commons.exceptions.IllegalArgumentExceptionMapper;
import io.arlas.commons.exceptions.JsonProcessingExceptionMapper;
import io.arlas.commons.rest.utils.InsensitiveCaseFilter;
import io.arlas.commons.rest.utils.PrettyPrintFilter;
import io.arlas.filter.core.PolicyEnforcer;
import io.arlas.persistence.rest.PersistenceRestService;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.impl.FileSystemPersistenceServiceImpl;
import io.arlas.persistence.server.impl.GoogleFirestorePersistenceServiceImpl;
import io.arlas.persistence.server.impl.HibernatePersistenceServiceImpl;
import io.arlas.persistence.server.model.Data;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.EnumSet;

public class ArlasPersistenceServer extends Application<ArlasPersistenceServerConfiguration> {
    Logger LOGGER = LoggerFactory.getLogger(ArlasPersistenceServer.class);

    private final HibernateBundle<ArlasPersistenceServerConfiguration> hibernate = new HibernateBundle<>(Data.class) {
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
        bootstrap.getObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
        bootstrap.addBundle(new SwaggerBundle<>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ArlasPersistenceServerConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
        String engine = System.getenv("ARLAS_PERSISTENCE_ENGINE");
        if (engine != null && engine.contains("hibernate")) {
            LOGGER.info("Loading hibernate bundle.");
            bootstrap.addBundle(hibernate);
        }
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
    }

    @Override
    public void run(ArlasPersistenceServerConfiguration configuration, Environment environment) throws Exception {

        configuration.check();
        LOGGER.info("Checked configuration: " + environment.getObjectMapper().writer().writeValueAsString(configuration));

        environment.getObjectMapper().setSerializationInclusion(Include.NON_NULL);
        environment.getObjectMapper().configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        environment.jersey().register(MultiPartFeature.class);
        environment.jersey().register(new ArlasExceptionMapper());
        environment.jersey().register(new IllegalArgumentExceptionMapper());
        environment.jersey().register(new JsonProcessingExceptionMapper());
        environment.jersey().register(new ConstraintViolationExceptionMapper());

        PersistenceService persistenceService = null;
        LOGGER.info("Starting with " + configuration.engine + " persistence engine");
        switch (configuration.engine) {
            case "hibernate" -> persistenceService = new HibernatePersistenceServiceImpl(hibernate.getSessionFactory());
            case "firestore" -> persistenceService = new GoogleFirestorePersistenceServiceImpl(configuration.firestoreCollection);
            case "file" -> persistenceService = new FileSystemPersistenceServiceImpl(configuration.localFolder);
            default -> {
                LOGGER.error("Engine not supported: " + configuration.engine + " (valid values are: 'hibernate' or 'firestore' or 'file').");
                System.exit(1);
            }
        }
        environment.jersey().register(new PersistenceRestService(persistenceService, configuration));

        CacheFactory cacheFactory = (CacheFactory) Class
                .forName(configuration.arlasCacheFactoryClass)
                .getConstructor(ArlasConfiguration.class)
                .newInstance(configuration);

        // Auth
        PolicyEnforcer policyEnforcer = PolicyEnforcer.newInstance(configuration.arlasAuthPolicyClass)
                .setAuthConf(configuration.arlasAuthConfiguration)
                .setCacheManager(cacheFactory.getCacheManager());
        LOGGER.info("PolicyEnforcer: " + policyEnforcer.getClass().getCanonicalName());
        environment.jersey().register(policyEnforcer);

        //cors
        if (configuration.arlasCorsConfiguration.enabled) {
            configureCors(environment, configuration.arlasCorsConfiguration);
        } else {
            CrossOriginFilter filter = new CrossOriginFilter();
            final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CrossOriginFilter", filter);
            // Expose always HttpHeaders.WWW_AUTHENTICATE to authentify on client side a non public uri call
            cors.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, HttpHeaders.WWW_AUTHENTICATE);
        }

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
        String exposedHeader = configuration.exposedHeaders;
        // Expose always HttpHeaders.WWW_AUTHENTICATE to authentify on client side a non public uri call
        if (!configuration.exposedHeaders.contains(HttpHeaders.WWW_AUTHENTICATE)) {
            exposedHeader = configuration.exposedHeaders.concat(",").concat(HttpHeaders.WWW_AUTHENTICATE);
        }
        cors.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, exposedHeader);

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
