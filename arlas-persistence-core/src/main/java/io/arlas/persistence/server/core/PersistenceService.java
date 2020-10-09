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
import io.arlas.persistence.server.model.IdentityParam;
import io.arlas.persistence.server.utils.SortOrder;
import io.arlas.server.exceptions.ArlasException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface PersistenceService {


    Pair<Long, List<Data>> list(String zone,
                                IdentityParam identityParam,
                                Integer size,
                                Integer page,
                                SortOrder order) throws ArlasException;

    Data get(String zone,
             String key,
             IdentityParam identityParam) throws ArlasException;

    Data getById(String id,
                 IdentityParam identityParam) throws ArlasException;

    Data create(String zone,
                String key,
                IdentityParam identityParam,
                Set<String> readers,
                Set<String> writers,
                String value) throws ArlasException;

    Data update(String id,
                String key,
                IdentityParam identityParam,
                Set<String> readers,
                Set<String> writers,
                String value,
                Date lastUpdate) throws ArlasException;

    Data deleteById(String id,
                    IdentityParam identityParam) throws ArlasException;

    Data delete(String zone,
                String key,
                IdentityParam identityParam) throws ArlasException;

    static List<String> getGroupsForZone(String zone,
                                         IdentityParam identityParam) {
        return identityParam.groups.stream().filter(g -> g.contains(zone) || g.equals("group/public")).collect(Collectors.toList());
    }

    static boolean intersect(List<String> a, List<String> b) {
        return a.stream()
                .distinct()
                .filter(b::contains)
                .count() > 0;
    }

    static boolean isReaderOnData(IdentityParam idp, Data data) {
        return data.getDocOrganization().equals(idp.organization) &&
                (data.getDocOwner().equals(idp.userId) || intersect(idp.groups, data.getDocReaders()));
    }

    static boolean isWriterOnData(IdentityParam idp, Data data) {
        return data.getDocOrganization().equals(idp.organization) &&
                (data.getDocOwner().equals(idp.userId) || intersect(idp.groups, data.getDocWriters()));
    }
}
