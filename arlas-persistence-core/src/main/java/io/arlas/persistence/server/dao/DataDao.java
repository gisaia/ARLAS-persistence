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

package io.arlas.persistence.server.dao;

import io.arlas.persistence.server.model.Data;
import io.arlas.persistence.server.utils.UUIDHelper;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class DataDao extends AbstractDAO<Data> {
    public DataDao(SessionFactory factory) {
        super(factory);
    }

    public List<Data> findByKey(String key) {
        return list(
                namedQuery("io.arlas.persistence.server.app.model.Data.findByKey")
                        .setParameter("key", key));
    }

    public Optional<Data> findById(String key, String id) {
        return Optional.ofNullable(
                uniqueResult(
                        namedQuery("io.arlas.persistence.server.app.model.Data.findById")
                                .setParameter("key", key)
                                .setParameter("id", id)));
    }

    public Data create(String key, String value) {
        return persist(new Data(key, value, UUIDHelper.generateUUID().toString()));
    }

    public Data update(Data data) {
        return persist(data);
    }

    public void delete(Data data) {
        currentSession().delete(data);
    }
}