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

import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.FailedPreconditionException;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.v1.FirestoreAdminClient;
import com.google.firestore.admin.v1.Index;
import com.google.firestore.admin.v1.ParentName;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.exceptions.ConflictException;
import io.arlas.persistence.server.exceptions.ForbidenException;
import io.arlas.persistence.server.model.Data;
import io.arlas.persistence.server.model.IdentityParam;
import io.arlas.persistence.server.utils.SortOrder;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GoogleFirestorePersistenceServiceImpl implements PersistenceService {
    protected static Logger LOGGER = LoggerFactory.getLogger(GoogleFirestorePersistenceServiceImpl.class);

    private final String collection;
    private final Firestore db;

    public GoogleFirestorePersistenceServiceImpl(String collection) throws ArlasException {
        this.collection = collection;
        this.db = FirestoreOptions.getDefaultInstance().getService();

        LOGGER.info("Creating indices for collection " + collection);
        try (FirestoreAdminClient firestoreAdminClient = FirestoreAdminClient.create()) {
            ParentName parent = ParentName.of(db.getOptions().getProjectId(), db.getOptions().getDatabaseId(), collection);
            try {
                firestoreAdminClient.createIndex(parent, Index.newBuilder()
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.keyColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.zoneColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.lastUpdateDateColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .setQueryScope(Index.QueryScope.COLLECTION)
                        .build());
            } catch (AlreadyExistsException e) {
                LOGGER.debug("Firestore index1 was already created");
            } catch (Exception e) {
                LOGGER.error("Could not create Firestore index1, it will need to be created manually", e);
            }

            try {
                firestoreAdminClient.createIndex(parent, Index.newBuilder()
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.keyColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.zoneColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.lastUpdateDateColumn).setOrder(Index.IndexField.Order.DESCENDING).build())
                        .setQueryScope(Index.QueryScope.COLLECTION)
                        .build());
            } catch (AlreadyExistsException e) {
                LOGGER.debug("Firestore index2 was already created");
            } catch (Exception e) {
                LOGGER.error("Could not create Firestore index2, it will need to be created manually", e);
            }

            try {
                firestoreAdminClient.createIndex(parent, Index.newBuilder()
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.docEntitiesColumn).setArrayConfig(Index.IndexField.ArrayConfig.CONTAINS).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.organizationColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.zoneColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.lastUpdateDateColumn).setOrder(Index.IndexField.Order.DESCENDING).build())
                        .setQueryScope(Index.QueryScope.COLLECTION)
                        .build());
            } catch (AlreadyExistsException e) {
                LOGGER.debug("Firestore index3 was already created");
            } catch (Exception e) {
                LOGGER.error("Could not create Firestore index3, it will need to be created manually", e);
            }

            try {
                firestoreAdminClient.createIndex(parent, Index.newBuilder()
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.docEntitiesColumn).setArrayConfig(Index.IndexField.ArrayConfig.CONTAINS).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.organizationColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.zoneColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.lastUpdateDateColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .setQueryScope(Index.QueryScope.COLLECTION)
                        .build());
            } catch (AlreadyExistsException e) {
                LOGGER.debug("Firestore index4 was already created");
            } catch (Exception e) {
                LOGGER.error("Could not create Firestore index4, it will need to be created manually", e);
            }

        } catch (IOException e) {
            LOGGER.error("Could not create Firestore indices", e);
            throw new ArlasException("Could not connect to Firestore");
        }
    }

    private Data toData(String id, DocumentSnapshot d) throws NotFoundException {
        if (!d.exists()) {
            throw new NotFoundException("Doc not found with id: " + id);
        }
        return new Data(d.getId(),
                d.getString(Data.keyColumn),
                d.getString(Data.zoneColumn),
                d.getString(Data.valueColumn),
                d.getString(Data.ownerColumn),
                d.getString(Data.organizationColumn),
                (List<String>) d.get(Data.writersColumn),
                (List<String>) d.get(Data.readersColumn),
                (List<String>) d.get(Data.docEntitiesColumn),
                d.getDate(Data.lastUpdateDateColumn)
        );
    }

    @Override
    public Pair<Long, List<Data>> list(String zone, IdentityParam identityParam, Integer size, Integer page, SortOrder order) throws ArlasException {
        List<String> entities =  new ArrayList<>(identityParam.groups);
        entities.addAll(Stream.of(identityParam.userId).collect(Collectors.toList()));
        try {
            return Pair.of(
                    (long) db.collection(this.collection)
                            .whereEqualTo(Data.zoneColumn, zone)
                            .whereEqualTo(Data.organizationColumn, identityParam.organization)
                            .whereArrayContainsAny(Data.docEntitiesColumn, entities)
                            .get()
                            .get()
                            .size(),

                    db.collection(this.collection)
                            .whereEqualTo(Data.zoneColumn, zone)
                            .whereEqualTo(Data.organizationColumn, identityParam.organization)
                            .whereArrayContainsAny(Data.docEntitiesColumn, entities)
                            .orderBy(Data.lastUpdateDateColumn, order == SortOrder.ASC ? Query.Direction.ASCENDING : Query.Direction.DESCENDING)
                            .limit(size)
                            .offset((page - 1) * size)
                            .get()
                            .get()
                            .getDocuments()
                            .stream()
                            .map(d -> {
                                try {
                                    return toData(d.getId(), d);
                                } catch (NotFoundException e) { //can't happen in this case
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
        } catch (FailedPreconditionException e) {
            LOGGER.error(e.getMessage()); // happens when index is missing
            throw new ArlasException("Error listing document: " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Error listing document: " + e.getMessage());
        }
    }

    @Override
    public Data get(String zone, String key, IdentityParam identityParam) throws ArlasException {
        Optional<Data> data = getByZoneKeyOrga(zone, key, identityParam.organization);
        if (data.isPresent()) {
            if (PersistenceService.isReaderOnData(identityParam, data.get()) ||
                    PersistenceService.isWriterOnData(identityParam, data.get())) {
                return data
                        .orElseThrow(() -> new NotFoundException("Data with zone " + zone + " and key " + key + " not found."));
            } else {
                throw new ForbidenException("You are not authorized to get this resource");
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
            throw new ForbidenException("You are not authorized to get this resource");
        }
    }

    @Override
    public Data create(String zone, String key, IdentityParam identityParam, Set<String> readers, Set<String> writers, String value) throws ArlasException {
        try {
            Optional<Data> data = getByZoneKeyOrga(zone, key, identityParam.organization);
            if (data.isPresent()) {
                throw new ArlasException("A resource with zone " + zone + " and key " + key + " already exists.");
            } else {
                PersistenceService.checkReadersWritersGroups(zone, identityParam, readers,writers);
                DocumentReference docRef = db.collection(collection).document();
                Set<String> entities = new HashSet<>();
                entities.addAll(writers);
                entities.addAll(readers);
                entities.addAll(Stream.of(identityParam.userId).collect(Collectors.toSet()));
                Data newData = new Data(docRef.getId(), key, zone, value, identityParam.userId, identityParam.organization, new ArrayList<>(writers), new ArrayList<>(readers), new ArrayList<>(entities), new Date());
                Timestamp result = docRef.create(newData).get().getUpdateTime();
                LOGGER.debug("Created doc " + docRef.getId() + " at " + result);
                return newData;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Error creating document", e);
        }
    }

    @Override
    public Data update(String id, String key, IdentityParam identityParam, Set<String> readers, Set<String> writers, String value, Date lastUpdate) throws ArlasException {
        try {
            Data data = getById(id);
            if (PersistenceService.isWriterOnData(identityParam, data)) {
                String zone = data.getDocZone();
                PersistenceService.checkReadersWritersGroups(zone, identityParam, readers,writers);
                // If the key is updated, we need to check if a triplet Zone/Key/orga already exist with this new key
                if(Optional.ofNullable(key).isPresent() && !Optional.ofNullable(key).get().equals(data.getDocKey())){
                    Optional<Data> alreadyExisting = getByZoneKeyOrga(zone, key, data.getDocOrganization());
                    if (alreadyExisting.isPresent()) {
                        throw new ArlasException("A resource with zone " + zone + " and key " + key + " already exists.");
                    }
                }
                DocumentReference docRef = db.collection(collection).document(id);
                Data newData = toData(id, docRef.get().get());
                newData.setDocKey(Optional.ofNullable(key).orElse(data.getDocKey()));
                Set<String> readersToUpdate = Optional.ofNullable(readers).orElse(new HashSet<>(data.getDocReaders()));
                Set<String> writersToUpdate = Optional.ofNullable(writers).orElse(new HashSet<>(data.getDocWriters()));
                newData.setDocReaders(new ArrayList<>(readersToUpdate));
                newData.setDocWriters(new ArrayList<>(writersToUpdate));
                Set<String> entities = new HashSet<>();
                entities.addAll(readersToUpdate);
                entities.addAll(writersToUpdate);
                entities.addAll(Stream.of(identityParam.userId).collect(Collectors.toSet()));
                newData.setDocEntities(new ArrayList<>(entities));
                if (data.getLastUpdateDate().equals(lastUpdate)) {
                    newData.setDocValue(value, true);
                    Timestamp result = docRef.set(newData).get().getUpdateTime();
                    LOGGER.debug("Updated doc " + id + " at " + result);
                    return newData;
                } else {
                    throw new ConflictException("The data can not be update due to conflicts.");
                }
            } else {
                throw new ForbidenException("You are not authorized to update this resource");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Error updating document", e);
        }
    }

    @Override
    public Data deleteById(String id, IdentityParam identityParam) throws ArlasException {
        Data data = getById(id);
        return deleteData(data, identityParam);
    }

    @Override
    public Data delete(String zone, String key, IdentityParam identityParam) throws ArlasException {
        Optional<Data> data = getByZoneKeyOrga(zone, key, identityParam.organization);
        if (data.isPresent()) {
            return deleteData(data.get(), identityParam);
        } else {
            throw new NotFoundException("Data with zone " + zone + " and key " + key + " not found.");
        }
    }

    private Optional<Data> getByZoneKeyOrga(String zone, String key, String organization) throws ArlasException {

        try {
            return db.collection(this.collection)
                    .whereEqualTo(Data.zoneColumn, zone)
                    .whereEqualTo(Data.keyColumn, key)
                    .whereEqualTo(Data.organizationColumn, organization)
                    .limit(1)
                    .get().get()
                    .getDocuments()
                    .stream()
                    .map(d -> {
                        try {
                            return toData(d.getId(), d);
                        } catch (NotFoundException e) { //can't happen in this case
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst();
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Error listing document: " + e.getMessage());
        }
    }

    private Data getById(String id) throws ArlasException {
        try {
            return toData(id, db.collection(collection).document(id).get().get());
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Could not get document: " + e.getMessage());
        }
    }

    private Data deleteData(Data data, IdentityParam identityParam) throws ArlasException {
        if (PersistenceService.isWriterOnData(identityParam, data)) {
            try {
                DocumentReference docRef = db.collection(collection).document(data.getId());
                Data newData = toData(data.getId(), docRef.get().get());
                Timestamp result = docRef.delete().get().getUpdateTime();
                LOGGER.debug("Delete doc " + data.getId() + " at " + result);
                return newData;
            } catch (InterruptedException | ExecutionException | NotFoundException e) {
                throw new ArlasException("Error updating document", e);
            }
        } else {
            throw new ForbidenException("You are not authorized to delete this resource");
        }
    }
}