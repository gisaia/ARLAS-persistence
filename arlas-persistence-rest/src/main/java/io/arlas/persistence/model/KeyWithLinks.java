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

package io.arlas.persistence.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arlas.persistence.server.model.Data;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Map;

@JsonSnakeCase
public class KeyWithLinks  {
    @JsonProperty("_links")
    public Map<String, Link> links;

    @JsonProperty("key")
    public String key;

    public KeyWithLinks(String key) {
        this.key = key;
    }

    public KeyWithLinks withLinks(Map<String, Link> links) {
        this.links = links;
        return this;
    }
}
