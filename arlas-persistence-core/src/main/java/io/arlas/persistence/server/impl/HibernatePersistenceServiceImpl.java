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

import java.util.*;
import java.util.stream.Collectors;

public class HibernatePersistenceServiceImpl extends AbstractDAO<Data> implements PersistenceService {

    public HibernatePersistenceServiceImpl(SessionFactory factory) {
        super(factory);
    }

    @Override
    public Pair list(String zone, IdentityParam identityParam, Integer size, Integer page, SortOrder order) {

        Long totalCount = currentSession().createQuery("SELECT count(ud) FROM Data ud  "
                        + "    where ud." + Data.zoneColumn + "=:zone"
                        + (identityParam.isAnonymous ? "" : "    and ud." + Data.organizationColumn + " in :organization")
                        + "    and " +
                        "( " +
                        "ud." + Data.ownerColumn + "=:userId "
                        + "or " + getGroupsRequest(identityParam.groups) + ")"
                , Long.class)
                .setParameter("zone", zone)
                .setParameter("organization", identityParam.organisation)
                .setParameter("userId", identityParam.userId)
                .uniqueResult();

        Query query = currentSession().createQuery(" from Data ud "
                + "    where ud." + Data.zoneColumn + "=:zone"
                + (identityParam.isAnonymous ? "" : "    and ud." + Data.organizationColumn + " in :organization")
                + "    and " +
                "( " +
                "ud." + Data.ownerColumn + "=:userId " +
                "or" + getGroupsRequest(identityParam.groups) + ")" +
                " order by ud." + Data.lastUpdateDateColumn + " " + order.toString(), Data.class)
                .setParameter("zone", zone)
                .setParameter("organization", identityParam.organisation)
                .setParameter("userId", identityParam.userId)
                .setMaxResults(size)
                .setFirstResult((page - 1) * size);
        return Pair.of(totalCount, list(query));
    }

    @Override
    public Data get(String zone, String key, IdentityParam identityParam) throws ArlasException {
        Optional<Data> data = getByZoneKeyOrga(zone, key, identityParam.organisation);
        if (data.isPresent()) {
            if (PersistenceService.isReaderOnData(identityParam, data.get()) ||
                    PersistenceService.isWriterOnData(identityParam, data.get())) {
                return data
                        .orElseThrow(() -> new NotFoundException("Data with zone " + zone + " and key " + key + " not found."));
            } else {
                throw new ForbiddenException("You are not authorized to get this resource.");
            }
        } else {
            throw new NotFoundException("Data with zone " + zone + " and key " +key +" not found.");
        }
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
        Optional<Data> data = getByZoneKeyOrga(zone, key, identityParam.organisation);
        if (data.isPresent()) {
            throw new ArlasException("A resource with zone " + zone + " and key " + key + " already exists.");
        } else {
            PersistenceService.checkReadersWritersGroups(zone, identityParam, readers,writers);
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
    }

    @Override
    public Data update(String id, String key, IdentityParam identityParam, Set<String> readers, Set<String> writers, String value, Date lastUpdate) throws ArlasException {
        if (identityParam.organisation.size() != 1) {
            throw new ArlasException("A unique organisation must be set in IdParam but received: " + identityParam.organisation);
        }
        Data data = getById(id);
        if (PersistenceService.isWriterOnData(identityParam, data)) {
            String zone = data.getDocZone();
            PersistenceService.checkReadersWritersGroups(zone, identityParam, readers,writers);
            // If the key is updated, we need to check if a triplet Zone/Key/orga already exist with this new key
            if(Optional.ofNullable(key).isPresent() && !Optional.ofNullable(key).get().equals(data.getDocKey())){
                Optional<Data> alreadyExisting = getByZoneKeyOrga(zone, key, List.of(data.getDocOrganization()));
                if (alreadyExisting.isPresent()) {
                    throw new ArlasException("A resource with zone " + zone + " and key " + key + " already exists.");
                }
            }
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

    @Override
    public Data delete(String zone, String key, IdentityParam identityParam) throws ArlasException {
        Optional<Data> data = getByZoneKeyOrga(zone, key, identityParam.organisation);
        if (data.isPresent()) {
            return deleteData(data.get(), identityParam);
        } else {
            throw new NotFoundException("Data with zone " + zone + " and key " +key +" not found.");
        }
    }

    private Data getById(String id) throws ArlasException {
        return Optional.ofNullable(get(id))
                .orElseThrow(() -> new NotFoundException("Data with id " + id + " not found."));
    }

    private Optional<Data> getByZoneKeyOrga(String zone, String key, List<String> organization) {
        Data data = currentSession().createQuery("from Data ud"
                + "    where ud." + Data.zoneColumn + "=:zone"
                + "    and ud." + Data.keyColumn + "=:key"
                + "    and ud." + Data.organizationColumn + " in :organization", Data.class)
                .setParameter("zone", zone)
                .setParameter("key", key)
                .setParameter("organization", organization)
                .uniqueResult();
        return Optional.ofNullable(data);
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
                .map(group -> "'" + group.trim() + "' member of ud." + Data.readersColumn + " or " +
                        "'" + group.trim() + "' member of ud." + Data.writersColumn)
                .collect(Collectors.joining(" or "));
    }
}