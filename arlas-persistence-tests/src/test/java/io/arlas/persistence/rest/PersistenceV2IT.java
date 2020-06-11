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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistenceV2IT {
    protected static String arlasAppPath;
    private static String id;

    static {
        String arlasHost = Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_HOST")).orElse("localhost");
        int arlasPort = Integer.valueOf(Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_PORT")).orElse("9997"));
        RestAssured.baseURI = "http://" + arlasHost;
        RestAssured.port = arlasPort;
        RestAssured.basePath = "";
        String arlasPrefix = Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_PREFIX")).orElse("/arlas_persistence_server");
        arlasAppPath = Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_APP_PATH")).orElse("/");
        if (arlasAppPath.endsWith("/"))
            arlasAppPath = arlasAppPath.substring(0, arlasAppPath.length() - 1);
        arlasAppPath = arlasAppPath + arlasPrefix;
        if (arlasAppPath.endsWith("//"))
            arlasAppPath = arlasAppPath.substring(0, arlasAppPath.length() - 1);
        if (!arlasAppPath.endsWith("/"))
            arlasAppPath = arlasAppPath + "/persistence/";
    }

    @Test
    public void test01Noresult() {
        given().pathParam("box", "foo")
                .pathParam("key", "bar")
                .param("order", "asc")
                .param("size", "1")
                .param("page", "1")
                .when()
                .get(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(404);
    }

    @Test
    public void test02PostData() {
         given().pathParam("box", "box1")
                .pathParam("key", "key1")
                .contentType("application/json")
                .body(generateData(1))
                .when()
                .post(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");

         given().pathParam("box", "box1")
                .pathParam("key", "key2")
                .contentType("application/json")
                .body(generateData(2))
                .when()
                .post(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":2}"))
                .extract().jsonPath().get("id");
    }

    @Test
    public void test03GetData() {
        given().pathParam("box", "box1")
                .pathParam("key", "key1")
                .when()
                .get(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("doc_value", equalTo("{\"age\":1}"))
                .body("doc_key", equalTo("key1"))
                .body("doc_type", equalTo("box1"));
    }

    @Test
    public void test04PutData() {
        given().pathParam("box", "box1")
                .pathParam("key", "key2")
                .contentType("application/json")
                .body(generateData(3))
                .when()
                .put(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":3}"));

        given().pathParam("box", "box1")
                .pathParam("key", "key2")
                .when()
                .get(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("doc_value", equalTo("{\"age\":3}"))
                .body("doc_key", equalTo("key2"))
                .body("doc_type", equalTo("box1"));
    }

    @Test
    public void test05DeleteData() {
        given().pathParam("box", "box1")
                .pathParam("key", "key2")
                .contentType("application/json")
                .when()
                .delete(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(202)
                .body("doc_key", equalTo("key2"));


        given().pathParam("box", "box1")
                .pathParam("key", "key1")
                .contentType("application/json")
                .when()
                .delete(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(202)
                .body("doc_key", equalTo("key1"));

        given().pathParam("box", "box1")
                .pathParam("key", "key1")
                .when()
                .get(arlasAppPath.concat("v2/{box}/{key}"))
                .then().statusCode(404);
    }

    @Test
    public void test06ListWithPagination() {
        for (int i=0; i<7; i++) {
            given().pathParam("box", "box1")
                    .pathParam("key", "key"+String.valueOf(i))
                    .contentType("application/json")
                    .body(generateData(i))
                    .post(arlasAppPath.concat("v2/{box}/{key}"))
                    .then().statusCode(201)
                    .body("doc_value", equalTo("{\"age\":"+i+"}"));
        }

        given().pathParam("box", "box1")
                .param("order", "asc")
                .param("size", "2")
                .param("page", "4")
                .when()
                .get(arlasAppPath.concat("v2/{box}"))
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