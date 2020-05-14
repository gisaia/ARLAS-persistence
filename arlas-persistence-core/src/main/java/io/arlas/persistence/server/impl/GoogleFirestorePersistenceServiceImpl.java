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
import com.google.longrunning.Operation;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.model.Data;
import io.arlas.persistence.server.utils.SortOrder;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GoogleFirestorePersistenceServiceImpl implements PersistenceService {
    protected static Logger LOGGER = LoggerFactory.getLogger(GoogleFirestorePersistenceServiceImpl.class);

    private final String collection;
    private final Firestore db;

    public GoogleFirestorePersistenceServiceImpl(String collection) {
        this.collection = collection;
        this.db = FirestoreOptions.getDefaultInstance().getService();

        LOGGER.info("Creating indices for collection " + collection);
        try (FirestoreAdminClient firestoreAdminClient = FirestoreAdminClient.create()) {
            ParentName parent = ParentName.of(db.getOptions().getProjectId(), db.getOptions().getDatabaseId(), collection);
            try {
                firestoreAdminClient.createIndex(parent,
                        Index.newBuilder()
                                .addFields(Index.IndexField.newBuilder()
                                        .setFieldPath(Data.keyColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                                .addFields(Index.IndexField.newBuilder()
                                        .setFieldPath(Data.typeColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                                .addFields(Index.IndexField.newBuilder()
                                        .setFieldPath(Data.dateColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
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
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.typeColumn).setOrder(Index.IndexField.Order.ASCENDING).build())
                        .addFields(Index.IndexField.newBuilder().setFieldPath(Data.dateColumn).setOrder(Index.IndexField.Order.DESCENDING).build())
                        .setQueryScope(Index.QueryScope.COLLECTION)
                        .build());
            } catch (AlreadyExistsException e) {
                LOGGER.debug("Firestore index2 was already created");
            } catch (Exception e) {
                LOGGER.error("Could not create Firestore index2, it will need to be created manually", e);
            }
        } catch (IOException e) {
            LOGGER.error("Could not create Firestore indices", e);
        }
    }

    private Data toData(String id, DocumentSnapshot d) throws NotFoundException {
        if (!d.exists()) {
            throw new NotFoundException("Doc not found with id: " + id);
        }

        return new Data(d.getString(Data.typeColumn), d.getString(Data.keyColumn),
                d.getString(Data.valueColumn), d.getId(), d.getDate(Data.dateColumn));
    }


    @Override
    public Pair<Long, List<Data>> list(String type, String key, Integer size, Integer page, SortOrder order) throws ArlasException {
        try {
            return Pair.of(
                    new Long(db.collection(this.collection)
                            .whereEqualTo(Data.typeColumn, type)
                            .whereEqualTo(Data.keyColumn, key)
                            .get()
                            .get()
                            .size()),

                    db.collection(this.collection)
                            .whereEqualTo(Data.typeColumn, type)
                            .whereEqualTo(Data.keyColumn, key)
                            .orderBy(Data.dateColumn, order == SortOrder.ASC ? Query.Direction.ASCENDING : Query.Direction.DESCENDING)
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
                            .filter(d -> d != null)
                            .collect(Collectors.toList()));
        } catch (FailedPreconditionException e) {
            LOGGER.error(e.getMessage()); // happens when index is missing
            throw new ArlasException("Error listing document: " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Error listing document: " + e.getMessage());
        }
    }

    @Override
    public Data getById(String id) throws ArlasException {
        try {
            return toData(id, db.collection(collection).document(id).get().get());
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Could not get document: " + e.getMessage());
        }
    }

    @Override
    public Data create(String type, String key, String value) throws ArlasException {
        try {
            DocumentReference docRef = db.collection(collection).document();
            Data data = new Data(type, key, value, docRef.getId());
            Timestamp result = docRef.create(data).get().getUpdateTime();
            LOGGER.debug("Created doc " + docRef.getId() + " at " + result);
            return data;
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Error creating document", e);
        }
    }

    @Override
    public Data update(String id, String value) throws ArlasException {
        try {
            DocumentReference docRef = db.collection(collection).document(id);
            Data data = toData(id, docRef.get().get());
            data.setDocValue(value);
            Timestamp result = docRef.set(data).get().getUpdateTime();
            LOGGER.debug("Updated doc " + id + " at " + result);
            return data;
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Error updating document", e);
        }
    }

    @Override
    public Data delete(String id) throws ArlasException {
        try {
            DocumentReference docRef = db.collection(collection).document(id);
            Data data = toData(id, docRef.get().get());
            Timestamp result = docRef.delete().get().getUpdateTime();
            LOGGER.debug("Delete doc " + id + " at " + result);
            return data;
        } catch (InterruptedException | ExecutionException e) {
            throw new ArlasException("Error updating document", e);
        }
    }
}