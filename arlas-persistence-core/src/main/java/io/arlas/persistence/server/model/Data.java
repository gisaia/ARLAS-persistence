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

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.dropwizard.jackson.JsonSnakeCase;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "user_data")
@TypeDef(name = "json", typeClass = JsonBinaryType.class)
@JsonSnakeCase
public class Data {
    public static final String idColumn = "id";
    public static final String keyColumn = "docKey";
    public static final String zoneColumn = "docZone";
    public static final String lastUpdateDateColumn = "lastUpdateDate";
    public static final String valueColumn = "docValue";
    public static final String ownerColumn = "docOwner";
    public static final String organizationColumn = "docOrganization";
    public static final String writersColumn = "docWriters";
    public static final String readersColumn = "docReaders";
    public static final String docEntitiesColumn = "docEntities";

    @Id
    @Column(name = idColumn)
    private String id;

    @NotNull
    @Column(name = keyColumn)
    private String docKey;

    @NotNull
    @Column(name = zoneColumn)
    private String docZone;

    @NotNull
    @Column(name = lastUpdateDateColumn)
    private Date lastUpdateDate;

    @NotNull
    @Type(type = "json")
    @Column(name = valueColumn, columnDefinition = "json")
    private String docValue;

    @NotNull
    @Column(name = ownerColumn)
    private String docOwner;

    @NotNull
    @Column(name = organizationColumn)
    private String docOrganization;

    @Transient
    private List<String> docEntities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_data_writers", joinColumns = @JoinColumn(name = "data_id"))
    @Column(name = "writer")
    private List<String>  docWriters = new ArrayList<>();

    @ElementCollection
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