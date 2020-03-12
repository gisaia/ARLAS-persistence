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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "user_data")
@NamedQueries({
        @NamedQuery(
                name = "io.arlas.persistence.server.app.model.Data.findByKey",
                query = "select ud from Data ud "
                        + "where ud.docKey=:key order by ud.creationDate"),
        @NamedQuery(
                name = "io.arlas.persistence.server.app.model.Data.findById",
                query = "select ud from Data ud "
                        + "where ud.docKey=:key and ud.id=:id")

})
@TypeDef(name = "json", typeClass = JsonBinaryType.class)
public class Data {
    @Id
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = "docKey")
    private String docKey;

    @Column(name = "creationDate")
    private Date creationDate;

    @Type(type = "json")
    @Column(name = "docValue", columnDefinition = "json")
    private String docValue;

    public Data() {}

    public Data(String docKey, String docValue, String id) {
        this.docKey = docKey;
        this.creationDate = new Date();
        this.docValue = docValue;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDocKey() {
        return docKey;
    }

    public Date getCreationDate() { return creationDate; }

    public String getDocValue() {
        return docValue;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDocKey(String doc_key) {
        this.docKey = doc_key;
    }

    public void setCreationDate(Date date) { this.creationDate = date; }

    public void setDocValue(String docValue) {
        this.docValue = docValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return getId().equals(data.getId()) &&
                getDocKey().equals(data.getDocKey()) &&
                getCreationDate().equals(data.getCreationDate()) &&
                Objects.equals(getDocValue(), data.getDocValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDocKey(), getCreationDate(), getDocValue());
    }
}