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
import io.arlas.filter.core.IdentityParam;
import io.arlas.persistence.server.model.Data;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.arlas.persistence.server.core.PersistenceService.intersect;

@JsonSnakeCase
public class DataWithLinks extends Data {

    @JsonProperty("_links")
    public Map<String, Link> links;

    @JsonProperty("updatable")
    public boolean updatable;

    public DataWithLinks(Data data, IdentityParam identityParam) {
        this.setDocZone(data.getDocZone());
        this.setDocKey(data.getDocKey());
        this.setLastUpdateDate(data.getLastUpdateDate());
        this.setDocValue(data.getDocValue());
        this.setId(data.getId());
        this.setUpdatable(this.isUpdatable(data,identityParam));
        this.setDocOwner(data.getDocOwner());
        this.setDocReaders(data.getDocReaders());
        this.setDocWriters(data.getDocWriters());
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public DataWithLinks withLinks(Map<String, Link> links) {
        this.links = links;
        return this;
    }

    public boolean isUpdatable(Data data, IdentityParam identityParam) {
        List<String> writers = Optional.ofNullable(data.getDocWriters()).orElse(new ArrayList<>());
        return data.getDocOrganization().equals(identityParam.organisation) &&
                (data.getDocOwner().equals(identityParam.userId) || intersect(identityParam.groups, writers));
    }
}
