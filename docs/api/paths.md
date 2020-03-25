<a name="paths"></a>
## Resources

<a name="persistence_resource"></a>
### Persistence

<a name="getall_1"></a>
#### Get all data persisted
```
GET /persistence
```

##### Description
Get all persistence data referenced in ARLAS-Pertistence storage for a a key and a type. The key is passed through a custom header defined in configuration.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**type**  <br>*mandatory*|Type of the document|string|`"pref"`|
|**Query**|**size**  <br>*optional*|Page Size|string|`10`|
|**Query**|**page**  <br>*optional*|Page ID|string|`1`|
|**Query**|**order**  <br>*optional*|Page ID|enum("desc,"asc")|`"desc"`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[DataResource](#dataresource)|
|**404**|Key not found|[Error](#error)|
|**500**|Arlas Server Error|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="get_1"></a>
#### Get an entry reference
```
GET /persistence/{id}
```


##### Description
Fetch an entry given its key and id. The key is passed through a custom header defined in configuration.

##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|The id of the data|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[DataWithLinks](#datawithlinks)|
|**404**|Key or id not found|[Error](#error)|
|**500**|Arlas Server Error|[Error](#error)|



##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="post_1"></a>
#### Add an entry
```
POST /persistence
```


##### Description
Store a new piece of data for the provided key and type (auto generate id)

##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**type**  <br>*required*|Type of the document|string||
|**Body**|**value**  <br>*required*|Value to be persisted. Valid json as string|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation.|[DataWithLinks](#datawithlinks)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`



<a name="put_1"></a>
#### Update an existing value
```
PUT /persistence/{id}
```

##### Description
Update an existing value with its key and id. The key is passed through a custom header defined in configuration.

##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|The id of the data|string||
|**Query**|**value**  <br>*required*|Value to be persisted|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**201**|Successful operation|[DataWithLinks](#datawithlinks)|
|**404**|Key or id not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="delete_1"></a>
#### Delete an entry
```
DELETE /persistence/{id}
```


##### Description
Delete a entry given its id


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|The id of the data|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**202**|Successful operation|[Success](#success)|
|**404**|Key or id not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`