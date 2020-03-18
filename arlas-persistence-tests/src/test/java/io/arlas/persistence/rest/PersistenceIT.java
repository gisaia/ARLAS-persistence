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

package io.arlas.persistence.rest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistenceIT {
    protected static String arlasAppPath;
    private static final String identityHeader;
    private static final String dataType;
    private static String id;

    static {
        identityHeader = Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_IDENTITY_HEADER")).orElse("X-Forwarded-User");
        dataType = Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_DATA_TYPE")).orElse("user_pref");
        String arlasHost = Optional.ofNullable(System.getenv("ARLAS_SERVER_HOST")).orElse("localhost");
        int arlasPort = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_SERVER_PORT")).orElse("9997"));
        RestAssured.baseURI = "http://" + arlasHost;
        RestAssured.port = arlasPort;
        RestAssured.basePath = "";
        String arlasPrefix = Optional.ofNullable(System.getenv("ARLAS_SERVER_PREFIX")).orElse("/arlas_persistence_server");
        arlasAppPath = Optional.ofNullable(System.getenv("ARLAS_SERVER_APP_PATH")).orElse("/");
        if (arlasAppPath.endsWith("/"))
            arlasAppPath = arlasAppPath.substring(0, arlasAppPath.length() - 1);
        arlasAppPath = arlasAppPath + arlasPrefix;
        if (arlasAppPath.endsWith("//"))
            arlasAppPath = arlasAppPath.substring(0, arlasAppPath.length() - 1);
        if (!arlasAppPath.endsWith("/"))
            arlasAppPath = arlasAppPath + "/persistence/";
    }

    @Test
    public void test01ListEmpty() {
        given().header(identityHeader, "foo")
                .param("type", dataType)
                .param("order", "asc")
                .param("size", "1")
                .param("page", "1")
                .when()
                .get(arlasAppPath)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(0))
                .body("total", equalTo(0));
    }

    @Test
    public void test02PostData() {
        id = given().header(identityHeader, "foo")
                .queryParam("type", dataType)
                .contentType("application/json")
                .body(generateData(1))
                .post(arlasAppPath)
                .then().statusCode(201)
                .body("docValue", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");
    }

    @Test
    public void test03GetData() {
        given().header(identityHeader, "foo")
                .when()
                .get(arlasAppPath + id)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("docValue", equalTo("{\"age\":1}"))
                .body("docKey", equalTo("foo"))
                .body("docType", equalTo(dataType));
    }

    @Test
    public void test04PutData() {
        given().header(identityHeader, "foo")
                .contentType("application/json")
                .body(generateData(2))
                .put(arlasAppPath + id)
                .then().statusCode(201)
                .body("docValue", equalTo("{\"age\":2}"));

        given().header(identityHeader, "foo")
                .when()
                .get(arlasAppPath + id)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("docValue", equalTo("{\"age\":2}"))
                .body("docKey", equalTo("foo"))
                .body("docType", equalTo(dataType));
    }

    @Test
    public void test05DeleteData() {
        given().header(identityHeader, "foo")
                .contentType("application/json")
                .delete(arlasAppPath + id)
                .then().statusCode(202)
                .body("id", equalTo(id));

        given().header(identityHeader, "foo")
                .when()
                .get(arlasAppPath + id)
                .then().statusCode(404);
    }

    @Test
    public void test06ListWithPagination() {
        for (int i=0; i<7; i++) {
            given().header(identityHeader, "foo")
                    .queryParam("type", dataType)
                    .contentType("application/json")
                    .body(generateData(i))
                    .post(arlasAppPath)
                    .then().statusCode(201)
                    .body("docValue", equalTo("{\"age\":"+i+"}"));
        }

        given().header(identityHeader, "foo")
                .param("type", dataType)
                .param("order", "asc")
                .param("size", "2")
                .param("page", "4")
                .when()
                .get(arlasAppPath)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(1))
                .body("total", equalTo(7));
    }


    protected Map<String, Object> generateData(Integer value) {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("age", value);
        return jsonAsMap;
    }
}