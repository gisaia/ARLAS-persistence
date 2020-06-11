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

    // API V1
    public static final String ID = "The id of the data.";
    public static final String VALUE = "Value to be persisted.";
    public static final String TYPE = "Type of the document.";
    public static final String LIST_OPERATION = "Fetch a list of data related to a key.";
    public static final String GET_OPERATION = "Fetch an entry given its key and id.";
    public static final String DELETE_OPERATION = "Delete an entry given its key and id.";
    public static final String CREATE_OPERATION = "Store a new piece of data for the provided key (auto generate id)";
    public static final String UPDATE_OPERATION = "Update an existing value.";

    // API V2
    public static final String BOX = "Box of the document.";
    public static final String KEY =" Key of the document.";
    public static final String GET_KEY_FROM_BOX_OPERATION = "Fetch a list of key related to a box";
    public static final String GET_FROM_BOX_KEY_OPERATION = "Fetch a list of data related to a box and a key";
    public static final String DELETE_FROM_BOX_KEY_OPERATION = "Delete an entry given its key and its box.";
    public static final String CREATE_FROM_BOX_KEY_OPERATION = "Store a new piece of data for the provided couple box/key (auto generate id)";
    public static final String UPDATE_FROM_BOX_KEY_OPERATION = "Update an existing value.for the provided couple box/key";





}
