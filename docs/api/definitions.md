
<a name="definitions"></a>
## Definitions

<a name="data"></a>
### Data

|Name|Schema|
|---|---|
|**id**   <br>*mandatory*|string|
|**dock_key**   <br>*mandatory*|string|
|**creation_date**  <br>*mandatory*|string(date-time)|
|**doc_value**  <br>*mandatory*|string|
|**doc_type**  <br>*mandatory*|string|

<a name="link"></a>
### Link

|Name|Schema|
|---|---|
|**relation**  <br>*mandatory*|string|
|**href**  <br>*mandatory*|string|
|**type**  <br>*mandatory*|string|
|**method**  <br>*mandatory*|string|

<a name="datawithlinks"></a>
### DataWithLinks

|Name|Schema|
|---|---|
|**id**   <br>*mandatory*|string|
|**dock_key**   <br>*mandatory*|string|
|**creation_date**  <br>*mandatory*|string(date-time)|
|**doc_value**  <br>*mandatory*|string|
|**doc_type**  <br>*mandatory*|string|
|**_links**  <br>*mandatory*|< string, [Link](#link) > map|


<a name="dataresource"></a>
### DataResource

|Name|Schema|
|---|---|
|**count**  <br>*mandatory*|integer(int32)|
|**total**  <br>*mandatory*|integer(int64)|
|**_links**  <br>*mandatory*|< string, [Link](#link) > map|
|**data**  <br>*optional*|<[DataWithLinks](#datawithlinks)> array|

<a name="error"></a>
### Error

|Name|Schema|
|---|---|
|**status**  <br>*optional*|integer(int32)|
|**message**  <br>*optional*|string|
|**error**  <br>*optional*|string|