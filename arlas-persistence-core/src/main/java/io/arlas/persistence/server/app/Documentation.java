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

public class Documentation {

    public static final String ID = "The id of the data.";
    public static final String KEY = "The key of the data.";
    public static final String VALUE = "Value to be persisted.";
    public static final String ZONE = "Zone of the document.";
    public static final String LIST_OPERATION = "Fetch a list of data related to a zone.";
    public static final String GET_GROUPS_OPERATION = "Returns the users' groups allowed to interact with the given zone.";
    public static final String GET_FROM_ID_OPERATION = "Fetch an entry given its id.";
    public static final String GET_FROM_KEY_ZONE_OPERATION = "Fetch an entry given its zone and key.";
    public static final String EXISTS_FROM_ID_OPERATION = "Check the existence of an entry given its id.";
    public static final String EXISTS_FROM_KEY_ZONE_OPERATION = "Check the existence of an entry given its zone and key.";
    public static final String DELETE_OPERATION = "Delete an entry given its key and id.";
    public static final String CREATE_OPERATION = "Store a new piece of data for the provided zone and key (auto generate id).";
    public static final String UPDATE_OPERATION = "Update an existing value.";
    public static final String READERS =  "Comma separated values of groups authorized to read the data.";
    public static final String WRITERS =  "Comma separated values of groups authorized to modify the data.";
    public static final String LAST_UPDATE = "Previous date value of last modification known by client.";

}
