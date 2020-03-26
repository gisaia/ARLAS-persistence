
<a name="definitions"></a>
## Definitions

<a name="data"></a>
### Data

|Name|Schema|
|---|---|
|**creation_date**  <br>*optional*|string (date-time)|
|**doc_key**  <br>*required*|string|
|**doc_type**  <br>*required*|string|
|**doc_value**  <br>*optional*|string|
|**id**  <br>*optional*|string|


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
|**creation_date**  <br>*optional*|string (date-time)|
|**doc_key**  <br>*required*|string|
|**doc_type**  <br>*required*|string|
|**doc_value**  <br>*optional*|string|
|**id**  <br>*optional*|string|


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



