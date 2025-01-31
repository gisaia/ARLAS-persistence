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

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotFoundException;
import io.arlas.filter.core.IdentityParam;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.exceptions.ConflictException;
import io.arlas.persistence.server.exceptions.ForbiddenException;
import io.arlas.persistence.server.model.Data;
import io.arlas.persistence.server.utils.SortOrder;
import io.arlas.persistence.server.utils.UUIDHelper;
import io.dropwizard.hibernate.AbstractDAO;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static io.arlas.filter.config.TechnicalRoles.GROUP_PUBLIC;

public class HibernatePersistenceServiceImpl extends AbstractDAO<Data> implements PersistenceService {
    protected static Logger LOGGER = LoggerFactory.getLogger(HibernatePersistenceServiceImpl.class);

    public HibernatePersistenceServiceImpl(SessionFactory factory) {
        super(factory);
    }

    @Override
    public Pair<Long, List<Data>> list(String zone, IdentityParam identityParam, Integer size, Integer page, SortOrder order, String key) {
        String from = " from Data ud "
                + " where ud." + Data.ZONE_COLUMN + "=:zone"
                + " and ((ud." + Data.ORGANIZATION_COLUMN + " in :organization"
                + "   and (ud." + Data.OWNER_COLUMN + "=:userId"
                + "   or " + getGroupsRequest(identityParam.groups) + "))"
                + " or (" + getGroupsRequest(List.of(GROUP_PUBLIC)) + "))";

        if(key != null){
            from = from  + " and ud." + Data.DOC_KEY + " ilike :searchKey";
        }
        Query<Long> countQuery = currentSession().createQuery("SELECT count(ud) " + from, Long.class)
                .setParameter("zone", zone)
                .setParameter("organization", identityParam.organisation)
                .setParameter("userId", identityParam.userId);
        if(key != null) {
            countQuery = countQuery.setParameter("searchKey", "%"+key+"%");

        }
        Long totalCount = countQuery.uniqueResult();
        Query<Data> query = currentSession().createQuery(from
                        + " order by ud." + Data.LAST_UPDATE_DATE_COLUMN + " " + order.toString(), Data.class)
                .setParameter("zone", zone)
                .setParameter("organization", identityParam.organisation)
                .setParameter("userId", identityParam.userId)
                .setMaxResults(size)
                .setFirstResult((page - 1) * size);

        if(key != null) {
            query = query.setParameter("searchKey", "%"+key+"%");
        }
        return Pair.of(totalCount, list(query));
    }

    @Override
    public Data getById(String id, IdentityParam identityParam) throws ArlasException {
        Data data = getById(id);
        if (PersistenceService.isReaderOnData(identityParam, data) ||
                PersistenceService.isWriterOnData(identityParam, data)) {
            return data;
        } else {
            throw new ForbiddenException("You are not authorized to get this resource.");
        }
    }

    @Override
    public Data create(String zone, String key, IdentityParam identityParam, Set<String> readers, Set<String> writers, String value) throws ArlasException {
        if (identityParam.organisation.size() != 1) {
            throw new ArlasException("A unique organisation must be set in IdParam but received: " + identityParam.organisation);
        }
        PersistenceService.checkReadersWritersGroups(zone, identityParam, readers, writers);
        Data newData = new Data(UUIDHelper.generateUUID().toString(),
                key,
                zone,
                value,
                identityParam.userId,
                identityParam.organisation.get(0),
                new ArrayList<>(writers),
                new ArrayList<>(readers),
                new Date());
        return persist(newData);
    }

    @Override
    public Data update(String id, String key, IdentityParam identityParam, Set<String> readers, Set<String> writers, String value, Date lastUpdate) throws ArlasException {
        Data data = getById(id);
        if (PersistenceService.isWriterOnData(identityParam, data)) {
            String zone = data.getDocZone();
            PersistenceService.checkReadersWritersGroups(zone, identityParam, readers,writers);
            data.setDocKey(Optional.ofNullable(key).orElse(data.getDocKey()));
            Set<String> readersToUpdate = Optional.ofNullable(readers).orElse(new HashSet<>(data.getDocReaders()));
            Set<String> writersToUpdate = Optional.ofNullable(writers).orElse(new HashSet<>(data.getDocWriters()));
            data.setDocReaders(new ArrayList<>(readersToUpdate));
            data.setDocWriters(new ArrayList<>(writersToUpdate));
            if (data.getLastUpdateDate().getTime() == lastUpdate.getTime()) {
                data.setDocValue(value,true);
                return persist(data);
            } else {
                throw new ConflictException("The data can not be updated due to conflicts.");
            }
        } else {
            throw new ForbiddenException("You are not authorized to update this resource");
        }
    }

    @Override
    public Data deleteById(String id, IdentityParam identityParam) throws ArlasException {
        Data data = getById(id);
        return deleteData(data, identityParam);
    }

    private Data getById(String id) throws ArlasException {
        return Optional.ofNullable(get(id))
                .orElseThrow(() -> new NotFoundException("Data with id " + id + " not found."));
    }

    private Data deleteData(Data data, IdentityParam identityParam) throws ForbiddenException {
        if (PersistenceService.isWriterOnData(identityParam, data)) {
            currentSession().delete(data);
            return data;
        } else {
            throw new ForbiddenException("You are not authorized to delete this resource.");
        }
    }

    private String getGroupsRequest(List<String> groups) {
        return groups.stream()
                .map(group -> "'" + group.trim() + "' member of ud." + Data.READERS_COLUMN + " or " +
                        "'" + group.trim() + "' member of ud." + Data.WRITERS_COLUMN)
                .collect(Collectors.joining(" or "));
    }
}