
<a name="definitions"></a>
## Definitions

<a name="dataresource"></a>
### DataResource

|Name|Schema|
|---|---|
|**_links**  <br>*optional*|< string, [Link](#link) > map|
|**count**  <br>*optional*|integer (int32)|
|**data**  <br>*optional*|< [DataWithLinks](#datawithlinks) > array|
|**total**  <br>*optional*|integer (int64)|


<a name="datawithlinks"></a>
### DataWithLinks

|Name|Schema|
|---|---|
|**_links**  <br>*optional*|< string, [Link](#link) > map|
|**doc_entities**  <br>*optional*|< string > array|
|**doc_key**  <br>*required*|string|
|**doc_organization**  <br>*required*|string|
|**doc_owner**  <br>*required*|string|
|**doc_readers**  <br>*optional*|< string > array|
|**doc_value**  <br>*required*|string|
|**doc_writers**  <br>*optional*|< string > array|
|**doc_zone**  <br>*required*|string|
|**id**  <br>*optional*|string|
|**last_update_date**  <br>*required*|string (date-time)|
|**updatable**  <br>*optional*|boolean|


<a name="error"></a>
### Error

|Name|Schema|
|---|---|
|**error**  <br>*optional*|string|
|**message**  <br>*optional*|string|
|**status**  <br>*optional*|integer (int32)|


<a name="link"></a>
### Link

|Name|Schema|
|---|---|
|**href**  <br>*required*|string|
|**method**  <br>*required*|string|
|**relation**  <br>*required*|string|
|**type**  <br>*required*|string|



