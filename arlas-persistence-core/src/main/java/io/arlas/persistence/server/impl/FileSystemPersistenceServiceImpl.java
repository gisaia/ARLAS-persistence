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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.persistence.server.core.PersistenceService;
import io.arlas.persistence.server.exceptions.ConflictException;
import io.arlas.persistence.server.exceptions.ForbidenException;
import io.arlas.persistence.server.model.Data;
import io.arlas.persistence.server.model.FileWrapper;
import io.arlas.persistence.server.model.IdentityParam;
import io.arlas.persistence.server.utils.SortOrder;
import io.arlas.persistence.server.utils.UUIDHelper;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemPersistenceServiceImpl implements PersistenceService {
    private final String storageFolder;
    private final static ObjectMapper objectMapper = new ObjectMapper();


    public FileSystemPersistenceServiceImpl(String localFolder) {
        storageFolder = localFolder.endsWith("/") ? localFolder : localFolder + "/";
    }

    @Override
    public Pair<Long, List<Data>> list(String zone, IdentityParam identityParam, Integer size, Integer page, SortOrder order) throws ArlasException {
        List<Data> list = getByFilenameFilter(
                prefixFilter(zone, identityParam.organization),
                identityParam, true).stream()
                .map(fw -> fw.data)
                .collect(Collectors.toList());
        return Pair.of(Long.valueOf(list.size()),
                (page - 1) * size > list.size() ? Collections.emptyList() : list.subList((page - 1) * size, Math.min(list.size(), page * size)));
    }

    @Override
    public Data get(String zone, String key, IdentityParam identityParam) throws ArlasException {
        Optional<FileWrapper> fw = getByZoneKeyOrga(zone, key, identityParam);
        if (fw.isPresent()) {
            if (PersistenceService.isReaderOnData(identityParam, fw.get().data) ||
                    PersistenceService.isWriterOnData(identityParam, fw.get().data)) {
                return fw.get().data;
            } else {
                throw new ForbidenException("You are not authorized to view this resource");
            }
        } else {
            throw new NotFoundException("Data with zone " + zone + " and key " + key + " not found.");
        }
    }

    @Override
    public Data getById(String id, IdentityParam identityParam) throws ArlasException {
        List<FileWrapper> list = getByFilenameFilter(suffixFilter(id), identityParam, false);
        if (list.size() == 1) {
            if (PersistenceService.isReaderOnData(identityParam, list.get(0).data) ||
                    PersistenceService.isWriterOnData(identityParam, list.get(0).data)) {
                return list.get(0).data;
            } else {
                throw new ForbidenException("You are not authorized to view this resource");
            }
        } else {
            throw new NotFoundException("Data with id " + id + " not found.");
        }
    }

    @Override
    public Data create(String zone, String key, IdentityParam identityParam, Set<String> readers, Set<String> writers, String value) throws ArlasException {
        Optional<FileWrapper> data = getByZoneKeyOrga(zone, key, identityParam);
        if (data.isPresent()) {
            throw new ArlasException("A resource with zone " + zone + " and key " + key + " already exists.");
        } else {
            Data newData = new Data(UUIDHelper.generateUUID().toString(),
                    key,
                    zone,
                    value,
                    identityParam.userId,
                    identityParam.organization,
                    new ArrayList<>(writers),
                    new ArrayList<>(readers),
                    new Date());
            try (FileOutputStream fos = new FileOutputStream(storageFolder.concat(getFileName(newData, identityParam)))) {
                objectMapper.writeValue(fos, newData);
                return newData;
            } catch (IOException e) {
                throw new ArlasException("An error occur in writing file: " + e.getMessage());
            }
        }
    }

    @Override
    public Data update(String id, String key, IdentityParam identityParam, Set<String> readers, Set<String> writers, String value, Date lastUpdate) throws ArlasException {
        List<FileWrapper> list = getByFilenameFilter(suffixFilter(id), identityParam, false);
        if (list.size() == 1) {
            Data data = list.get(0).data;
            if (PersistenceService.isWriterOnData(identityParam, data)) {
                data.setDocKey(Optional.ofNullable(key).orElse(data.getDocKey()));
                Set<String> readersToUpdate = Optional.ofNullable(readers).orElse(new HashSet<>(data.getDocReaders()));
                Set<String> writersToUpdate = Optional.ofNullable(writers).orElse(new HashSet<>(data.getDocWriters()));
                data.setDocReaders(new ArrayList<>(readersToUpdate));
                data.setDocWriters(new ArrayList<>(writersToUpdate));
                if (data.getLastUpdateDate().getTime() == lastUpdate.getTime()) {
                    data.setDocValue(value,true);
                    try (FileOutputStream fos = new FileOutputStream(list.get(0).file.getAbsolutePath())) {
                        objectMapper.writeValue(fos, data);
                        return data;
                    } catch (IOException e) {
                        throw new ArlasException("An error occur in writing file: " + e.getMessage());
                    }
                } else {
                    throw new ConflictException("The data can not be updated due to conflicts.");
                }
            } else {
                throw new ForbidenException("You are not authorized to update this resource");
            }
        } else {
            throw new NotFoundException("Data with id " + id + " not found.");
        }
    }

    @Override
    public Data deleteById(String id, IdentityParam identityParam) throws ArlasException {
        List<FileWrapper> list = getByFilenameFilter(suffixFilter(id), identityParam, false);
        if (list.size() == 1) {
            if (PersistenceService.isWriterOnData(identityParam, list.get(0).data)) {
                try {
                    list.get(0).file.delete();
                } catch (Exception e) {
                    throw new ArlasException("Could not delete data: " + e.getMessage());
                }
                return list.get(0).data;
            } else {
                throw new ForbidenException("You are not authorized to delete this resource");
            }
        } else {
            throw new NotFoundException("Data with id " + id + " not found.");
        }
    }

    @Override
    public Data delete(String zone, String key, IdentityParam identityParam) throws ArlasException {
        Optional<FileWrapper> fw = getByZoneKeyOrga(zone, key, identityParam);
        if (fw.isPresent()) {
            if (PersistenceService.isWriterOnData(identityParam, fw.get().data)) {
                try {
                    fw.get().file.delete();
                } catch (Exception e) {
                    throw new ArlasException("Could not delete data: " + e.getMessage());
                }
                return fw.get().data;
            } else {
                throw new ForbidenException("You are not authorized to delete this resource");
            }
        } else {
            throw new NotFoundException("Data with zone " + zone + " and key " + key + " not found.");
        }
    }

    private String getFileName(Data data, IdentityParam identityParam){
        // zone_org_userid_key_id
        return String.join("_",
                data.getDocZone(),
                data.getDocOrganization(),
                data.getDocKey(),
                identityParam.userId,
                data.getId());
    }

    private Optional<FileWrapper> getByZoneKeyOrga(String zone, String key, IdentityParam identityParam) throws ArlasException {
        List<FileWrapper> list = getByFilenameFilter(
                prefixFilter(zone, identityParam.organization, key),
                identityParam, false);
        return list.size() > 0 ? Optional.of(list.get(0)) : Optional.empty();
    }

    private Predicate<Path> prefixFilter(String... prefix) {
        return p -> p.getFileName().toString().startsWith(String.join("_", prefix).concat("_"));
    }

    private Predicate<Path> suffixFilter(String suffix) {
        return p -> p.getFileName().toString().endsWith(suffix);
    }

    private List<FileWrapper> getByFilenameFilter(Predicate<Path> fileFilter, IdentityParam identityParam, boolean filterOnRights) throws ArlasException {
        try (Stream<Path> paths = Files.walk(Paths.get(storageFolder))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(fileFilter)
                    .map(Path::toFile)
                    .map(f -> {
                        try {
                            return new FileWrapper(f, objectMapper.readValue(f, Data.class));
                        } catch (IOException e) {
                            throw new RuntimeException();
                        }
                    })
                    .filter(fw -> !filterOnRights || PersistenceService.isReaderOnData(identityParam, fw.data) ||
                            PersistenceService.isWriterOnData(identityParam, fw.data))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ArlasException("Error");
        }
    }
}
