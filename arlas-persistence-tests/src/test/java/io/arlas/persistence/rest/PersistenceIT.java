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
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersistenceIT {
    protected static String arlasAppPath;
    private static final String userHeader;
    private static final String organizationHeader;
    private static final String groupsHeader;

    private static final UserIdentity admin;
    private static final UserIdentity technical;
    private static final UserIdentity commercial;
    private static final UserIdentity otherCompany;
    private static final UserIdentity publicProfile;

    private static final String ALL = "group/user_pref/arlas_company1/all";
    private static final String TECHNICAL = "group/user_pref/arlas_company1/technical";
    private static final String SALES = "group/user_pref/arlas_company1/sales";
    private static final String ADMIN = "group/user_pref/arlas_company1/admin";
    private static final String ALL2 = "group/user_pref/arlas_company2/all";
    private static final String SALES2 = "group/user_pref/arlas_company2/sales";
    private static final String PUBLIC = "group/public";

    private static final String dataZone;
    private static String id;

    static {
        admin = new UserIdentity("admin", String.join(",", ALL, TECHNICAL, SALES, ADMIN, PUBLIC), "company1");
        technical = new UserIdentity("technical", String.join(",",ALL, TECHNICAL, PUBLIC), "company1");
        commercial = new UserIdentity("commercial", String.join(",",ALL, SALES, PUBLIC), "company1");
        otherCompany = new UserIdentity("other", String.join(",",ALL2, SALES2, PUBLIC), "company2");

        publicProfile = new UserIdentity("public", String.join(",", PUBLIC), "company1");

        userHeader = Optional.ofNullable(System.getenv("ARLAS_USER_HEADER")).orElse("arlas-user");
        organizationHeader = Optional.ofNullable(System.getenv("ARLAS_ORGANIZATION_HEADER")).orElse("arlas-organization");
        groupsHeader = Optional.ofNullable(System.getenv("ARLAS_GROUPS_HEADER")).orElse("arlas-groups");

        dataZone = Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_DATA_TYPE")).orElse("user_pref");
        String arlasHost = Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_HOST")).orElse("localhost");
        int arlasPort = Integer.parseInt(Optional.ofNullable(System.getenv("ARLAS_PERSISTENCE_PORT")).orElse("9997"));
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
            arlasAppPath = arlasAppPath + "/persist/";
    }

    @Test
    public void test01ListEmpty() {
        listEmpty(technical);
    }

    @Test
    public void test02PostData() {
        id = createData(technical, "myFirstDocument", Collections.EMPTY_LIST, Collections.EMPTY_LIST)
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");
    }

    @Test
    public void test03GetData() {
        getData(technical, "myFirstDocument")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("doc_value", equalTo("{\"age\":1}"))
                .body("doc_key", equalTo("myFirstDocument"))
                .body("doc_zone", equalTo(dataZone));
    }

    @Test
    public void test04PutData() {
        Long currentDate = getData(technical, "myFirstDocument")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().get("last_update_date");

        givenForUser(technical)
                .contentType("application/json")
                .body(generateData(2))
                .param("last_update", currentDate)
                .put(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":2}"));

        getData(technical, "myFirstDocument")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("doc_value", equalTo("{\"age\":2}"))
                .body("doc_key", equalTo("myFirstDocument"))
                .body("doc_zone", equalTo(dataZone));
    }

    @Test
    public void test05DeleteData() {
        givenForUser(technical)
                .contentType("application/json")
                .delete(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(202)
                .body("id", equalTo(id));

        getData(technical, "myFirstDocument")
                .then().statusCode(404);
    }

    @Test
    public void test06ListWithPagination() {
        for (int i = 0; i < 7; i++) {
            givenForUser(technical)
                    .pathParam("zone", dataZone)
                    .pathParam("key", "document".concat(String.valueOf(i)))
                    .contentType("application/json")
                    .body(generateData(i))
                    .post(arlasAppPath.concat("resource/{zone}/{key}"))
                    .then().statusCode(201)
                    .body("doc_value", equalTo("{\"age\":" + i + "}"));
        }

        givenForUser(technical)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "2")
                .param("page", "4")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(1))
                .body("total", equalTo(7));


    }

    @Test
    public void test07DeleteAllByZoneKey() {
        for (int i = 0; i < 7; i++) {
            givenForUser(technical)
                    .pathParam("zone", dataZone)
                    .pathParam("key", "document".concat(String.valueOf(i)))
                    .contentType("application/json")
                    .delete(arlasAppPath.concat("resource/{zone}/{key}"))
                    .then().statusCode(202);

            getData(technical, "document".concat(String.valueOf(i)))
                    .then().statusCode(404);
        }

    }

    @Test
    public void test08PostWithWriteAccess() {
        id = createData(admin, "myFirstRestrictedDocument", Collections.EMPTY_LIST, Collections.singletonList(SALES))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");

        listEmpty(technical);
        listEmpty(otherCompany);
        givenForUser(commercial)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "1")
                .param("page", "1")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(1))
                .body("total", equalTo(1));

        getData(technical, "myFirstRestrictedDocument").then().statusCode(403);
        getData(otherCompany, "myFirstRestrictedDocument").then().statusCode(404);
        getData(commercial, "myFirstRestrictedDocument").then().statusCode(200).contentType(ContentType.JSON)
                .body("updatable", equalTo(true));


    }

    @Test
    public void test09PostWithReadWriteAccess() {
        id = createData(admin, "mySecondRestrictedDocument", Collections.singletonList(TECHNICAL), Collections.singletonList(SALES))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");

        listEmpty(otherCompany);
        givenForUser(commercial)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "10")
                .param("page", "1")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(2))
                .body("total", equalTo(2));

        givenForUser(technical)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "10")
                .param("page", "1")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(1))
                .body("total", equalTo(1));

        getData(technical, "mySecondRestrictedDocument").then().statusCode(200).contentType(ContentType.JSON)
                .body("updatable", equalTo(false));

        getData(otherCompany, "mySecondRestrictedDocument").then().statusCode(404);
        getData(commercial, "mySecondRestrictedDocument").then().statusCode(200).contentType(ContentType.JSON)
                .body("updatable", equalTo(true));


    }

    @Test
    public void test10UpdateWithJustReadAccess() {
        Long currentDate = getData(technical, "mySecondRestrictedDocument")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().get("last_update_date");

        givenForUser(technical)
                .contentType("application/json")
                .body(generateData(2))
                .param("last_update", currentDate)
                .put(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(403);

    }

    @Test
    public void test11UpdateWithJustWriteAccess() {

        Long currentDate = getData(commercial, "mySecondRestrictedDocument")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().get("last_update_date");

        givenForUser(commercial)
                .contentType("application/json")
                .body(generateData(3))
                .param("last_update", currentDate)
                .queryParam("readers", Collections.singletonList(PUBLIC))
                .queryParam("writers", Collections.singletonList(SALES))
                .put(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":3}"));

    }

    @Test
    public void test12UpdateConflicts() {
        Long currentDateCommercial = getData(commercial, "mySecondRestrictedDocument")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().get("last_update_date");

        Long currentDateAdmin = getData(admin, "mySecondRestrictedDocument")
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().get("last_update_date");


        givenForUser(commercial)
                .contentType("application/json")
                .body(generateData(4))
                .param("last_update", currentDateCommercial)
                .queryParam("readers", Collections.singletonList(PUBLIC))
                .queryParam("writers", Collections.singletonList(SALES))
                .put(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":4}"));

        givenForUser(admin)
                .contentType("application/json")
                .body(generateData(5))
                .param("last_update", currentDateAdmin)
                .queryParam("readers", Collections.singletonList(PUBLIC))
                .queryParam("writers", Collections.singletonList(ADMIN))
                .put(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(409);
    }


    @Test
    public void test13DeleteWithJustReadAccess() {
        givenForUser(publicProfile)
                .contentType("application/json")
                .delete(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(403);
    }

    @Test
    public void test14DeleteWithJustWriteAccess() {

        givenForUser(commercial)
                .contentType("application/json")
                .delete(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(202)
                .body("id", equalTo(id));

        getData(commercial, "mySecondRestrictedDocument")
                .then().statusCode(404);
    }

    @Test
    public void test15CreateOtherOrganisation() {
        id = createData(otherCompany, "mySecondRestrictedDocument", Collections.singletonList(PUBLIC), Collections.singletonList(PUBLIC))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");

        createData(otherCompany, "mySecondRestrictedDocument", Collections.singletonList(TECHNICAL), Collections.singletonList(SALES))
                .then().statusCode(500);

    }

    @Test
    public void test16CreateWithoutHeader() {
        given()
                .pathParam("key", "key")
                .pathParam("zone", "zone")
                .contentType("application/json")
                .body(generateData(1))
                .post(arlasAppPath.concat("resource/{zone}/{key}"))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"));

       //return anonymous data
        given()
                .pathParam("zone", "zone")
                .pathParam("key", "key")
                .when()
                .get(arlasAppPath.concat("resource/{zone}/{key}")).then()
                .statusCode(200)
                .body("doc_value", equalTo("{\"age\":1}"));
    }

    @Test
    public void test17GetGroups() {
       List<String> groups =  given().header(userHeader, admin.userId)
                .header(groupsHeader, admin.groups)
                .header(organizationHeader, admin.organization)
                .pathParam("zone", dataZone)
                .contentType("application/json")
                .get(arlasAppPath.concat("groups/{zone}"))
                .then().statusCode(200).extract().jsonPath().get();

        Assert.assertArrayEquals(groups.toArray(),
                Arrays.stream(admin.groups.split(","))
                        .toArray());
    }

    @Test
    public void test18PostData() {
        createData(technical, "myNewDocument", Collections.EMPTY_LIST,  Collections.singletonList("group/private"))
                .then().statusCode(403);
        createData(technical, "myNewDocument2", Collections.EMPTY_LIST,  Arrays.asList("group/private",TECHNICAL))
                .then().statusCode(403);
    }
    @Test
    public void test19PostData() {
        createData(technical, "myNewDocument", Collections.singletonList("group/private"),Collections.EMPTY_LIST)
                .then().statusCode(403);
    }
    @Test
    public void test20ExistsNot() {
        givenForUser(technical)
                .pathParam("zone", dataZone)
                .pathParam("key", "foo")
                .when()
                .get(arlasAppPath.concat("resource/exists/{zone}/{key}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("exists", equalTo(false));
    }

    @Test
    public void test21ExistsByKey() {
        createData(technical, "foo", Collections.EMPTY_LIST, Collections.EMPTY_LIST)
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"));

        givenForUser(technical)
                .pathParam("zone", dataZone)
                .pathParam("key", "foo")
                .when()
                .get(arlasAppPath.concat("resource/exists/{zone}/{key}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("exists", equalTo(true));
    }


    protected RequestSpecification givenForUser(UserIdentity userIdentity) {
        return given().header(userHeader, userIdentity.userId)
                .header(groupsHeader, userIdentity.groups)
                .header(organizationHeader, userIdentity.organization);
    }

    protected Map<String, Object> generateData(Integer value) {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put("age", value);
        return jsonAsMap;
    }

    protected Response createData(UserIdentity userIdentity, String key, List<String> readers, List<String> writers) {
        RequestSpecification request = givenForUser(userIdentity)
                .pathParam("zone", dataZone)
                .pathParam("key", key);

        if(!readers.isEmpty())
            request=request.queryParam("readers", readers);
        if(!writers.isEmpty())
            request=request.queryParam("writers", writers);
        return
                request
                .contentType("application/json")
                .body(generateData(1))
                .post(arlasAppPath.concat("resource/{zone}/{key}"));
    }

    protected Response getData(UserIdentity userIdentity, String key) {
        return givenForUser(userIdentity)
                .pathParam("zone", dataZone)
                .pathParam("key", key)
                .when()
                .get(arlasAppPath.concat("resource/{zone}/{key}"));
    }


    protected void listEmpty(UserIdentity userIdentity) {
        givenForUser(userIdentity)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "1")
                .param("page", "1")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(0))
                .body("total", equalTo(0));
    }
}