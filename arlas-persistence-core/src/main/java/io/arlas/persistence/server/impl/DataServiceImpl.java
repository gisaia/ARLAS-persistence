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

import io.arlas.persistence.server.core.DataService;
import io.arlas.persistence.server.dao.DataDao;
import io.arlas.persistence.server.model.Data;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;

import java.util.List;
import java.util.Optional;


public class DataServiceImpl implements DataService {

    private DataDao dataDao;
    public DataServiceImpl(DataDao dataDao) {
        this.dataDao = dataDao;
    }

    @Override
    public List<Data> list(String key) throws ArlasException {
        return dataDao.findByKey(key);
    }

    @Override
    public Data get(String key, String id) throws ArlasException {
        return dataDao.findById(key, id)
                .orElseThrow(() -> new NotFoundException("Data with id " + id + " not found."));
    }

    @Override
    public Data create(String key, String value) throws ArlasException {
        return dataDao.create(key, value);
    }

    @Override
    public Data update(String key, String id, String value) throws ArlasException {
        return dataDao.update(new Data(key, value, id));
    }

    @Override
    public Data delete(String key, String id) throws ArlasException {
        Data data = get(key, id);
        dataDao.delete(data);
        return data;
    }
}