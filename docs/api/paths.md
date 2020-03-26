
<a name="paths"></a>
## Resources

<a name="persistence_resource"></a>
### Persistence

<a name="create"></a>
#### Store a new piece of data for the provided key (auto generate id)
```
POST /persistence
```


##### Description
Store a new piece of data for the provided key (auto generate id)


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**type**  <br>*required*|Type of the document.|string|`"hibernate"`|
|**Body**|**value**  <br>*required*|Value to be persisted.|string||


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**201**|Successful operation|[DataWithLinks](#datawithlinks)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="list"></a>
#### Fetch a list of data related to a key.
```
GET /persistence
```


##### Description
Fetch a list of data related to a key.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Query**|**order**  <br>*optional*|Date sort order|enum (desc, asc)|`"desc"`|
|**Query**|**page**  <br>*optional*|Page ID|integer (int32)|`1`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**size**  <br>*optional*|Page Size|integer (int32)|`10`|
|**Query**|**type**  <br>*required*|Type of the document.|string|`"pref"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[DataResource](#dataresource)|
|**404**|Key not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="get"></a>
#### Fetch an entry given its key and id.
```
GET /persistence/{id}
```


##### Description
Fetch an entry given its key and id.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|The id of the data.|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[DataWithLinks](#datawithlinks)|
|**404**|Key or id not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="update"></a>
#### Update an existing value.
```
PUT /persistence/{id}
```


##### Description
Update an existing value.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|The id of the data.|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Body**|**value**  <br>*required*|Value to be persisted.|string||


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


<a name="delete"></a>
#### Delete an entry given its key and id.
```
DELETE /persistence/{id}
```


##### Description
Delete an entry given its key and id.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|The id of the data.|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**202**|Successful operation|[Data](#data)|
|**404**|Key or id not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`



