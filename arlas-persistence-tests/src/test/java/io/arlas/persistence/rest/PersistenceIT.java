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

import static io.restassured.RestAssured.delete;
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
    private static final UserIdentity anonymous;

    private static final String ALL = "group/user_pref/arlas_company1/all";
    private static final String TECHNICAL = "group/user_pref/arlas_company1/technical";
    private static final String SALES = "group/user_pref/arlas_company1/sales";
    private static final String ADMIN = "group/user_pref/arlas_company1/admin";
    private static final String ALL2 = "group/user_pref/arlas_company2/all";
    private static final String SALES2 = "group/user_pref/arlas_company2/sales";
    private static final String PUBLIC = "group/public";

    private static final String dataZone;
    private static String id;
    private static final List<String> idBis = new ArrayList<>();

    static {
        admin = new UserIdentity("admin", String.join(",", ALL, TECHNICAL, SALES, ADMIN, PUBLIC), "company1");
        technical = new UserIdentity("technical", String.join(",",ALL, TECHNICAL, PUBLIC), "company1");
        commercial = new UserIdentity("commercial", String.join(",",ALL, SALES, PUBLIC), "company1");
        otherCompany = new UserIdentity("other", String.join(",",ALL2, SALES2, PUBLIC), "company2");
        anonymous = new UserIdentity("anonymous", PUBLIC, "");

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
    public void test00ListEmpty() {
        listEmpty(technical);
    }

    @Test
    public void test01ListEmptyAsAnonymous() {
        listEmpty(anonymous);
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
        getData(technical, id)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("doc_value", equalTo("{\"age\":1}"))
                .body("doc_key", equalTo("myFirstDocument"))
                .body("doc_zone", equalTo(dataZone));
    }

    @Test
    public void test04UpdateData() {
        Long currentDate = getData(technical, id)
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

        getData(technical, id)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("doc_value", equalTo("{\"age\":2}"))
                .body("doc_key", equalTo("myFirstDocument"))
                .body("doc_zone", equalTo(dataZone));
    }

    @Test
    public void test05DeleteData() {
        deleteData(technical, id);
        getData(technical, id).then().statusCode(404);
    }

    @Test
    public void test06ListWithPagination() {
        for (int i = 0; i < 7; i++) {
            idBis.add(givenForUser(technical)
                    .pathParam("zone", dataZone)
                    .pathParam("key", "document".concat(String.valueOf(i)))
                    .contentType("application/json")
                    .body(generateData(i))
                    .post(arlasAppPath.concat("resource/{zone}/{key}"))
                    .then().statusCode(201)
                    .body("doc_value", equalTo("{\"age\":" + i + "}"))
                    .extract().jsonPath().get("id"));
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

        givenForUser(technical)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "10")
                .param("page", "1")
                .param("key", "document")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(7))
                .body("total", equalTo(7));

        givenForUser(technical)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "10")
                .param("page", "1")
                .param("key", "ment6")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(1))
                .body("total", equalTo(1));

        givenForUser(technical)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "10")
                .param("page", "1")
                .param("key", "MenT6")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(1))
                .body("total", equalTo(1));
    }

    @Test
    public void test07DeleteAllById() {
        for (String i : idBis) {
            deleteData(technical, i);
            getData(technical, i).then().statusCode(404);
        }
    }

    @Test
    public void test08PostWithWriteAccess() {
        id = createData(admin, "myFirstRestrictedDocument", Collections.EMPTY_LIST, List.of(SALES))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");

        listEmpty(technical);
        listEmpty(otherCompany);
        listEmpty(anonymous);
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

        getData(technical, id).then().statusCode(403);
        getData(otherCompany, id).then().statusCode(403);
        getData(commercial, id).then().statusCode(200).contentType(ContentType.JSON)
                .body("updatable", equalTo(true));


    }

    @Test
    public void test09PostWithReadWriteAccess() {
        id = createData(admin, "mySecondRestrictedDocument", List.of(TECHNICAL), List.of(SALES))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");

        listEmpty(otherCompany);
        listEmpty(anonymous);
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

        getData(technical, id).then().statusCode(200).contentType(ContentType.JSON)
                .body("updatable", equalTo(false));

        getData(otherCompany, id).then().statusCode(403);
        getData(commercial, id).then().statusCode(200).contentType(ContentType.JSON)
                .body("updatable", equalTo(true));
    }

    @Test
    public void test10UpdateWithJustReadAccess() {
        Long currentDate = getData(technical, id)
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

        Long currentDate = getData(commercial, id)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().get("last_update_date");

        givenForUser(commercial)
                .contentType("application/json")
                .body(generateData(3))
                .param("last_update", currentDate)
                .queryParam("readers", List.of(PUBLIC))
                .queryParam("writers", List.of(SALES))
                .put(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":3}"));

    }

    @Test
    public void test12UpdateConflicts() {
        Long currentDateCommercial = getData(commercial, id)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().get("last_update_date");

        Long currentDateAdmin = getData(admin, id)
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .extract().jsonPath().get("last_update_date");


        givenForUser(commercial)
                .contentType("application/json")
                .body(generateData(4))
                .param("last_update", currentDateCommercial)
                .queryParam("readers", List.of(PUBLIC))
                .queryParam("writers", List.of(SALES))
                .put(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":4}"));

        givenForUser(admin)
                .contentType("application/json")
                .body(generateData(5))
                .param("last_update", currentDateAdmin)
                .queryParam("readers", List.of(PUBLIC))
                .queryParam("writers", List.of(ADMIN))
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
        deleteData(commercial, id);
        getData(commercial, id).then().statusCode(404);
    }

    @Test
    public void test15CreateOtherOrganisation() {
        id = createData(otherCompany, "mySecondRestrictedDocument", List.of(PUBLIC), Collections.EMPTY_LIST)
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");
        deleteData(otherCompany, id);
    }

    @Test
    public void test16CreateWithoutHeader() {
        String idanon = given()
                .pathParam("key", "key")
                .pathParam("zone", "zone")
                .contentType("application/json")
                .body(generateData(1))
                .post(arlasAppPath.concat("resource/{zone}/{key}"))
                .then().statusCode(201)
                .body("doc_value", equalTo("{\"age\":1}"))
                .extract().jsonPath().get("id");

        //return anonymous data
        given()
                .pathParam("id", idanon)
                .when()
                .get(arlasAppPath.concat("resource/id/{id}")).then()
                .statusCode(200)
                .body("doc_value", equalTo("{\"age\":1}"));

        deleteData(anonymous, idanon);
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
        createData(technical, "myNewDocument", Collections.EMPTY_LIST, List.of("group/private"))
                .then().statusCode(403);
        String id2 = createData(technical, "myNewDocument2", Collections.EMPTY_LIST, Arrays.asList("group/private", TECHNICAL))
                .then().statusCode(201)
                .extract().jsonPath().get("id");
        deleteData(technical, id2);
    }

    @Test
    public void test19PostData() {
        createData(technical, "myNewDocument", List.of("group/private"), Collections.EMPTY_LIST)
                .then().statusCode(403);
    }

    @Test
    public void test20ExistsNot() {
        givenForUser(technical)
                .pathParam("id", "foo")
                .when()
                .get(arlasAppPath.concat("resource/exists/id/{id}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("exists", equalTo(false));
    }

    @Test
    public void test21ListWithPublic() {
        String id1 = createData(technical, "privateDocument", List.of(TECHNICAL), Collections.EMPTY_LIST)
                .then().statusCode(201)
                .extract().jsonPath().get("id");

        String id2 = createData(technical, "publicDocument1", List.of(PUBLIC), Collections.EMPTY_LIST)
                .then().statusCode(201)
                .extract().jsonPath().get("id");

        String id3 = createData(otherCompany, "publicDocument2", List.of(PUBLIC), Collections.EMPTY_LIST)
                .then().statusCode(201)
                .extract().jsonPath().get("id");

        givenForUser(technical)
                .pathParam("zone", dataZone)
                .param("order", "asc")
                .param("size", "10")
                .param("page", "1")
                .when()
                .get(arlasAppPath.concat("resources/{zone}"))
                .then().statusCode(200)
                .contentType(ContentType.JSON)
                .body("count", equalTo(3))
                .body("total", equalTo(3));

        givenForUser(otherCompany)
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

        givenForUser(anonymous)
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


        deleteData(technical, id1);
        deleteData(technical, id2);
        deleteData(otherCompany, id3);
    }

    @Test
    public void test22CreateWithPublicWriteAccess() {
        createData(technical, "privateDocument", List.of(TECHNICAL), List.of(PUBLIC))
                .then().statusCode(403);

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
            request = request.queryParam("readers", readers);
        if(!writers.isEmpty())
            request = request.queryParam("writers", writers);

        return request.contentType("application/json")
                .body(generateData(1))
                .post(arlasAppPath.concat("resource/{zone}/{key}"));
    }

    protected Response getData(UserIdentity userIdentity, String id) {
        return givenForUser(userIdentity)
                .pathParam("id", id)
                .when()
                .get(arlasAppPath.concat("resource/id/{id}"));
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

    protected void deleteData(UserIdentity userIdentity, String id) {
        givenForUser(userIdentity)
                .contentType("application/json")
                .delete(arlasAppPath.concat("resource/id/") + id)
                .then().statusCode(202)
                .body("id", equalTo(id));

    }
}