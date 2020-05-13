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
import io.arlas.persistence.server.core.PersistenceService;
import io.dropwizard.jackson.JsonSnakeCase;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;


@Entity
@Table(name = PersistenceService.collection)
@TypeDef(name = "json", typeClass = JsonBinaryType.class)
@JsonSnakeCase
public class Data {
    public static final String keyColumn = "docKey";
    public static final String dateColumn = "creationDate";
    public static final String valueColumn = "docValue";
    public static final String typeColumn = "docType";

    @Id
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = keyColumn)
    private String docKey;

    @Column(name = dateColumn)
    private Date creationDate;

    @Type(type = "json")
    @Column(name = valueColumn, columnDefinition = "json")
    private String docValue;

    @NotNull
    @Column(name = typeColumn)
    private String docType;


    public Data() {}

    public Data(String docType, String docKey, String docValue, String id) {
        this(docType, docKey, docValue, id, new Date());
    }

    public Data(String docType, String docKey, String docValue, String id, Date creationDate) {
        this.docType = docType;
        this.docKey = docKey;
        this.creationDate = creationDate;
        this.docValue = docValue;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDocKey() {
        return docKey;
    }

    public String getDocType() {
        return docType;
    }

    public Date getCreationDate() {
        return creationDate;
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

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public void setCreationDate(Date date) { this.creationDate = date; }

    public void setDocValue(String docValue) {
        this.setDocValue(docValue, false);
    }

    public void setDocValue(String docValue, boolean updateCreationDate) {
        this.docValue = docValue;
        if (updateCreationDate) {
            this.creationDate = new Date();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return getId().equals(data.getId()) &&
                getDocKey().equals(data.getDocKey()) &&
                getCreationDate().equals(data.getCreationDate()) &&
                Objects.equals(getDocValue(), data.getDocValue()) &&
                getDocType().equals(data.getDocType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDocKey(), getCreationDate(), getDocValue(), getDocType());
    }

    @Override
    public String toString() {
        return "Data{" +
                "id='" + id + '\'' +
                ", doc_key='" + docKey + '\'' +
                ", creation_date=" + creationDate +
                ", doc_value='" + docValue + '\'' +
                ", doc_type='" + docType + '\'' +
                '}';
    }
}