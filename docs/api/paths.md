
<a name="paths"></a>
## Resources

<a name="persist_resource"></a>
### Persist

<a name="getgroupsbyzone"></a>
#### Returns the users' groups allowed to interact with the given zone.
```
GET /persist/groups/{zone}
```


##### Description
Returns the users' groups allowed to interact with the given zone.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**zone**  <br>*required*|Zone of the document.|string|`"pref"`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[DataWithLinks](#datawithlinks)|
|**404**|Zone not found.|[Error](#error)|
|**500**|Arlas Persistence Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="getbyid"></a>
#### Fetch an entry given its id.
```
GET /persist/resource/id/{id}
```


##### Description
Fetch an entry given its id.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|The id of the data.|string||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[DataWithLinks](#datawithlinks)|
|**404**|Id not found.|[Error](#error)|
|**500**|Arlas Persistence Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="update"></a>
#### Update an existing value.
```
PUT /persist/resource/id/{id}
```


##### Description
Update an existing value.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**id**  <br>*required*|The id of the data.|string||
|**Query**|**key**  <br>*optional*|The key of the data.|string||
|**Query**|**last_update**  <br>*required*|Previous date value of last modification known by client.|integer (int64)||
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**readers**  <br>*optional*|Comma separated values of groups authorized to read the data.|< string > array(multi)||
|**Query**|**writers**  <br>*optional*|Comma separated values of groups authorized to modify the data.|< string > array(multi)||
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


<a name="deletebyid"></a>
#### Delete an entry given its key and id.
```
DELETE /persist/resource/id/{id}
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
|**202**|Successful operation|[DataWithLinks](#datawithlinks)|
|**404**|Key or id not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="create"></a>
#### Store a new piece of data for the provided zone and key (auto generate id).
```
POST /persist/resource/{zone}/{key}
```


##### Description
Store a new piece of data for the provided zone and key (auto generate id).


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**key**  <br>*required*|The key of the data.|string||
|**Path**|**zone**  <br>*required*|Zone of the document.|string|`"pref"`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**readers**  <br>*optional*|Comma separated values of groups authorized to read the data.|< string > array(multi)||
|**Query**|**writers**  <br>*optional*|Comma separated values of groups authorized to modify the data.|< string > array(multi)||
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


<a name="getbykey"></a>
#### Fetch an entry given its zone and key.
```
GET /persist/resource/{zone}/{key}
```


##### Description
Fetch an entry given its zone and key.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**key**  <br>*required*|The key of the data.|string||
|**Path**|**zone**  <br>*required*|Zone of the document.|string|`"pref"`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[DataWithLinks](#datawithlinks)|
|**404**|Key or zone not found.|[Error](#error)|
|**500**|Arlas Persistence Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="delete"></a>
#### Delete an entry given its key and id.
```
DELETE /persist/resource/{zone}/{key}
```


##### Description
Delete an entry given its key and id.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**key**  <br>*required*|The key of the data.|string||
|**Path**|**zone**  <br>*required*|Zone of the document.|string|`"pref"`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**202**|Successful operation|[DataWithLinks](#datawithlinks)|
|**404**|Zone or key not found.|[Error](#error)|
|**500**|Arlas Server Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`


<a name="list"></a>
#### Fetch a list of data related to a zone.
```
GET /persist/resources/{zone}
```


##### Description
Fetch a list of data related to a zone.


##### Parameters

|Type|Name|Description|Schema|Default|
|---|---|---|---|---|
|**Path**|**zone**  <br>*required*|Zone of the document.|string|`"pref"`|
|**Query**|**order**  <br>*optional*|Date sort order|enum (desc, asc)|`"desc"`|
|**Query**|**page**  <br>*optional*|Page ID|integer (int32)|`1`|
|**Query**|**pretty**  <br>*optional*|Pretty print|boolean|`"false"`|
|**Query**|**size**  <br>*optional*|Page Size|integer (int32)|`10`|


##### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|Successful operation|[DataResource](#dataresource)|
|**404**|Zone not found.|[Error](#error)|
|**500**|Arlas Persistence Error.|[Error](#error)|


##### Consumes

* `application/json;charset=utf-8`


##### Produces

* `application/json;charset=utf-8`



