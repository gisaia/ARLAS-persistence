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

package io.arlas.persistence.server.model;

import io.dropwizard.jackson.JsonSnakeCase;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "user_data", indexes={@Index(columnList="docKey,docZone,docOrganization",name="doc_key_idx_orga")})
@JsonSnakeCase
public class Data {
    public static final String ID_COLUMN = "id";
    public static final String DOC_KEY = "docKey";
    public static final String ZONE_COLUMN = "docZone";
    public static final String LAST_UPDATE_DATE_COLUMN = "lastUpdateDate";
    public static final String VALUE_COLUMN = "docValue";
    public static final String OWNER_COLUMN = "docOwner";
    public static final String ORGANIZATION_COLUMN = "docOrganization";
    public static final String WRITERS_COLUMN = "docWriters";
    public static final String READERS_COLUMN = "docReaders";
    public static final String DOC_ENTITIES_COLUMN = "docEntities";

    @Id
    @Column(name = ID_COLUMN)
    private String id;

    @NotNull
    @Column(name = DOC_KEY)
    private String docKey;

    @NotNull
    @Column(name = ZONE_COLUMN)
    private String docZone;

    @NotNull
    @Column(name = LAST_UPDATE_DATE_COLUMN)
    private Date lastUpdateDate;

    @NotNull
    @Type(JsonBinaryType.class)
    @Column(name = VALUE_COLUMN, columnDefinition = "json")
    private String docValue;

    @NotNull
    @Column(name = OWNER_COLUMN)
    private String docOwner;

    @NotNull
    @Column(name = ORGANIZATION_COLUMN)
    private String docOrganization;

    @Transient
    private List<String> docEntities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @CollectionTable(name = "user_data_writers", joinColumns = @JoinColumn(name = "data_id"))
    @Column(name = "writer")
    private List<String>  docWriters = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    @CollectionTable(name = "user_data_readers", joinColumns = @JoinColumn(name = "data_id"))
    @Column(name = "reader")
    private List<String> docReaders = new ArrayList<>();

    public Data() {}

    public Data(String id, String docKey, String docZone, String docValue, String docOwner, String docOrganization, List<String> docWriters, List<String> docReaders, Date lastUpdateDate) {
        this.id = id;
        this.docKey = docKey;
        this.docZone = docZone;
        this.lastUpdateDate = lastUpdateDate;
        this.docValue = docValue;
        this.docOwner = docOwner;
        this.docOrganization = docOrganization;
        this.docWriters = docWriters;
        this.docReaders = docReaders;
    }

    public Data(String id, String docKey, String docZone, String docValue, String docOwner, String docOrganization, List<String> docWriters, List<String> docReaders,List<String> docEntities, Date lastUpdateDate  ) {
        this.id = id;
        this.docKey = docKey;
        this.docZone = docZone;
        this.lastUpdateDate =lastUpdateDate;
        this.docValue = docValue;
        this.docOwner = docOwner;
        this.docOrganization = docOrganization;
        this.docWriters = docWriters;
        this.docReaders = docReaders;
        this.docEntities =docEntities;
    }

    public List<String> getDocEntities() {
        return docEntities;
    }

    public void setDocEntities(List<String> docEntities) {
        this.docEntities = docEntities;
    }

    public String getId() {
        return id;
    }

    public String getDocKey() {
        return docKey;
    }

    public String getDocZone() {
        return docZone;
    }

    public String getDocValue() {
        return docValue;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDocKey(String doc_key) {
        this.docKey = doc_key;
    }

    public void setDocZone(String docZone) {
        this.docZone = docZone;
    }

    public void setDocValue(String docValue) {
        this.setDocValue(docValue, false);
    }

    public void setDocValue(String docValue, boolean lastUpdateDate) {
        this.docValue = docValue;
        if (lastUpdateDate) {
            this.lastUpdateDate = new Date();
        }
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getDocOwner() {
        return docOwner;
    }

    public void setDocOwner(String docOwner) {
        this.docOwner = docOwner;
    }

    public String getDocOrganization() {
        return docOrganization;
    }

    public void setDocOrganization(String docOrganization) {
        this.docOrganization = docOrganization;
    }

    public List<String> getDocWriters() {
        return docWriters;
    }

    public void setDocWriters(List<String> docWriters) {
        this.docWriters = docWriters;
    }

    public List<String> getDocReaders() {
        return docReaders;
    }

    public void setDocReaders(List<String> docReaders) {
        this.docReaders = docReaders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return Objects.equals(id, data.id) &&
                Objects.equals(docKey, data.docKey) &&
                Objects.equals(docZone, data.docZone) &&
                Objects.equals(lastUpdateDate, data.lastUpdateDate) &&
                Objects.equals(docValue, data.docValue) &&
                Objects.equals(docOwner, data.docOwner) &&
                Objects.equals(docOrganization, data.docOrganization) &&
                Objects.equals(docWriters, data.docWriters) &&
                Objects.equals(docReaders, data.docReaders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, docKey, docZone, lastUpdateDate, docValue, docOwner, docOrganization, docWriters, docReaders);
    }

    @Override
    public String toString() {
        return "Data{" +
                "id='" + id + '\'' +
                ", docKey='" + docKey + '\'' +
                ", docZone='" + docZone + '\'' +
                ", lastUpdateDate=" + lastUpdateDate +
                ", docValue='" + docValue + '\'' +
                ", docOwner='" + docOwner + '\'' +
                ", docOrganization='" + docOrganization + '\'' +
                ", docWriters='" + docWriters + '\'' +
                ", docReaders='" + docReaders + '\'' +
                '}';
    }
}