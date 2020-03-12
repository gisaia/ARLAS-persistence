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
package io.arlas.persistence.server.core;

import io.arlas.persistence.server.model.Data;
import io.arlas.server.exceptions.ArlasException;

import java.util.List;

public interface DataService {

    List<Data> list(String key) throws ArlasException;

    Data get(String key, String id) throws ArlasException;

    Data create(String key, String value) throws ArlasException;

    Data update(String key, String id, String value) throws ArlasException;

    Data delete(String key, String id) throws ArlasException;
}
