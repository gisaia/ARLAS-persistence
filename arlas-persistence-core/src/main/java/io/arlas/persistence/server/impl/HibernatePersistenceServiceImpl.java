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

package io.arlas.persistence.server.impl;

import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.model.Data;
import io.arlas.persistence.server.utils.UUIDHelper;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;


public class HibernatePersistenceServiceImpl extends AbstractDAO<Data> implements PersistenceService {
    protected static Logger LOGGER = LoggerFactory.getLogger(HibernatePersistenceServiceImpl.class);

    public HibernatePersistenceServiceImpl(SessionFactory factory) {
        super(factory);
    }

    @Override
    public List<Data> list(String type, String key) throws ArlasException {
        return list(
                namedQuery("io.arlas.persistence.server.app.model.Data.findByKey")
                        .setParameter("type", type)
                        .setParameter("key", key));
    }

    @Override
    public Data getById(String id) throws ArlasException {
        return Optional.ofNullable(get(id))
                .orElseThrow(() -> new NotFoundException("Data with id " + id + " not found."));
    }

    @Override
    public Data create(String type, String key, String value) throws ArlasException {
        return persist(new Data(type, key, value, UUIDHelper.generateUUID().toString()));
    }

    @Override
    public Data update(String id, String value) throws ArlasException {
        Data data = getById(id);
        data.setDocValue(value);
        return persist(data);
    }

    @Override
    public Data delete(String id) throws ArlasException {
        Data data = getById(id);
        currentSession().delete(data);
        return data;
    }
}