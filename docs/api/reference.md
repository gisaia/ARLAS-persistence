<!-- Generator: Widdershins v4.0.1 -->

<h1 id="arlas-persistence-apis">ARLAS Persistence APIs v26.0.0-rc.1</h1>

> Scroll down for example requests and responses.

Persistence REST services.

Base URLs:

* <a href="/arlas_persistence_server">/arlas_persistence_server</a>

Email: <a href="mailto:contact@gisaia.com">Gisaia</a> Web: <a href="http://www.gisaia.com/">Gisaia</a> 
License: <a href="https://www.apache.org/licenses/LICENSE-2.0.html">Apache 2.0</a>

<h1 id="arlas-persistence-apis-persist">persist</h1>

Persistence API

## Store a new piece of data for the provided zone and key (auto generate id).

<a id="opIdcreate"></a>

`POST /persist/resource/{zone}/{key}`

Store a new piece of data for the provided zone and key (auto generate id).

> Body parameter

```json
"string"
```

<h3 id="store-a-new-piece-of-data-for-the-provided-zone-and-key-(auto-generate-id).-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|zone|path|string|true|Zone of the document.|
|key|path|string|true|The key of the data.|
|readers|query|array[string]|false|Comma separated values of groups authorized to read the data.|
|writers|query|array[string]|false|Comma separated values of groups authorized to modify the data.|
|pretty|query|boolean|false|Pretty print|
|body|body|string|true|Value to be persisted.|

> Example responses

> 201 Response

```json
{
  "id": "string",
  "doc_key": "string",
  "doc_zone": "string",
  "last_update_date": "2019-08-24T14:15:22Z",
  "doc_value": "string",
  "doc_owner": "string",
  "doc_organization": "string",
  "doc_entities": [
    "string"
  ],
  "doc_writers": [
    "string"
  ],
  "doc_readers": [
    "string"
  ],
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "updatable": true,
  "ispublic": true
}
```

<h3 id="store-a-new-piece-of-data-for-the-provided-zone-and-key-(auto-generate-id).-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Successful operation|[DataWithLinks](#schemadatawithlinks)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Fetch an entry given its id.

<a id="opIdgetById"></a>

`GET /persist/resource/id/{id}`

Fetch an entry given its id.

<h3 id="fetch-an-entry-given-its-id.-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|The id of the data.|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 200 Response

```json
{
  "id": "string",
  "doc_key": "string",
  "doc_zone": "string",
  "last_update_date": "2019-08-24T14:15:22Z",
  "doc_value": "string",
  "doc_owner": "string",
  "doc_organization": "string",
  "doc_entities": [
    "string"
  ],
  "doc_writers": [
    "string"
  ],
  "doc_readers": [
    "string"
  ],
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "updatable": true,
  "ispublic": true
}
```

<h3 id="fetch-an-entry-given-its-id.-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[DataWithLinks](#schemadatawithlinks)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Id not found.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Persistence Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Update an existing value.

<a id="opIdupdate"></a>

`PUT /persist/resource/id/{id}`

Update an existing value.

> Body parameter

```json
"string"
```

<h3 id="update-an-existing-value.-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|The id of the data.|
|key|query|string|false|The key of the data.|
|readers|query|array[string]|false|Comma separated values of groups authorized to read the data.|
|writers|query|array[string]|false|Comma separated values of groups authorized to modify the data.|
|last_update|query|integer(int64)|true|Previous date value of last modification known by client.|
|pretty|query|boolean|false|Pretty print|
|body|body|string|true|Value to be persisted.|

> Example responses

> 201 Response

```json
{
  "id": "string",
  "doc_key": "string",
  "doc_zone": "string",
  "last_update_date": "2019-08-24T14:15:22Z",
  "doc_value": "string",
  "doc_owner": "string",
  "doc_organization": "string",
  "doc_entities": [
    "string"
  ],
  "doc_writers": [
    "string"
  ],
  "doc_readers": [
    "string"
  ],
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "updatable": true,
  "ispublic": true
}
```

<h3 id="update-an-existing-value.-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Successful operation|[DataWithLinks](#schemadatawithlinks)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Key or id not found.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Delete an entry given its key and id.

<a id="opIddeleteById"></a>

`DELETE /persist/resource/id/{id}`

Delete an entry given its key and id.

<h3 id="delete-an-entry-given-its-key-and-id.-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|The id of the data.|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 202 Response

```json
{
  "id": "string",
  "doc_key": "string",
  "doc_zone": "string",
  "last_update_date": "2019-08-24T14:15:22Z",
  "doc_value": "string",
  "doc_owner": "string",
  "doc_organization": "string",
  "doc_entities": [
    "string"
  ],
  "doc_writers": [
    "string"
  ],
  "doc_readers": [
    "string"
  ],
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "updatable": true,
  "ispublic": true
}
```

<h3 id="delete-an-entry-given-its-key-and-id.-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|202|[Accepted](https://tools.ietf.org/html/rfc7231#section-6.3.3)|Successful operation|[DataWithLinks](#schemadatawithlinks)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Key or id not found.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Server Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Check the existence of an entry given its id.

<a id="opIdexistsById"></a>

`GET /persist/resource/exists/id/{id}`

Check the existence of an entry given its id.

<h3 id="check-the-existence-of-an-entry-given-its-id.-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|id|path|string|true|The id of the data.|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 200 Response

```json
{
  "exists": true
}
```

<h3 id="check-the-existence-of-an-entry-given-its-id.-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[Exists](#schemaexists)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Persistence Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Returns the users' groups allowed to interact with the given zone.

<a id="opIdgetGroupsByZone"></a>

`GET /persist/groups/{zone}`

Returns the users' groups allowed to interact with the given zone.

<h3 id="returns-the-users'-groups-allowed-to-interact-with-the-given-zone.-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|zone|path|string|true|Zone of the document.|
|pretty|query|boolean|false|Pretty print|

> Example responses

> 200 Response

```json
"string"
```

<h3 id="returns-the-users'-groups-allowed-to-interact-with-the-given-zone.-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|string|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Zone not found.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Persistence Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

## Fetch a list of data related to a zone.

<a id="opIdlist"></a>

`GET /persist/resources/{zone}`

Fetch a list of data related to a zone.

<h3 id="fetch-a-list-of-data-related-to-a-zone.-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|zone|path|string|true|Zone of the document.|
|size|query|integer(int64)|false|Page Size|
|page|query|integer(int64)|false|Page ID|
|order|query|string|false|Date sort order|
|pretty|query|boolean|false|Pretty print|
|key|query|string|false|Filter by key value|

#### Enumerated Values

|Parameter|Value|
|---|---|
|order|ASC|
|order|DESC|

> Example responses

> 200 Response

```json
{
  "count": 0,
  "total": 0,
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "data": [
    {
      "id": "string",
      "doc_key": "string",
      "doc_zone": "string",
      "last_update_date": "2019-08-24T14:15:22Z",
      "doc_value": "string",
      "doc_owner": "string",
      "doc_organization": "string",
      "doc_entities": [
        "string"
      ],
      "doc_writers": [
        "string"
      ],
      "doc_readers": [
        "string"
      ],
      "_links": {
        "property1": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        },
        "property2": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        }
      },
      "updatable": true,
      "ispublic": true
    }
  ]
}
```

<h3 id="fetch-a-list-of-data-related-to-a-zone.-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Successful operation|[DataResource](#schemadataresource)|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Zone not found.|[Error](#schemaerror)|
|500|[Internal Server Error](https://tools.ietf.org/html/rfc7231#section-6.6.1)|Arlas Persistence Error.|[Error](#schemaerror)|

<aside class="success">
This operation does not require authentication
</aside>

# Schemas

<h2 id="tocS_DataWithLinks">DataWithLinks</h2>
<!-- backwards compatibility -->
<a id="schemadatawithlinks"></a>
<a id="schema_DataWithLinks"></a>
<a id="tocSdatawithlinks"></a>
<a id="tocsdatawithlinks"></a>

```json
{
  "id": "string",
  "doc_key": "string",
  "doc_zone": "string",
  "last_update_date": "2019-08-24T14:15:22Z",
  "doc_value": "string",
  "doc_owner": "string",
  "doc_organization": "string",
  "doc_entities": [
    "string"
  ],
  "doc_writers": [
    "string"
  ],
  "doc_readers": [
    "string"
  ],
  "public": true,
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "updatable": true,
  "ispublic": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|id|string|false|none|none|
|doc_key|string|true|none|none|
|doc_zone|string|true|none|none|
|last_update_date|string(date-time)|true|none|none|
|doc_value|string|true|none|none|
|doc_owner|string|true|none|none|
|doc_organization|string|true|none|none|
|doc_entities|[string]|false|none|none|
|doc_writers|[string]|false|none|none|
|doc_readers|[string]|false|none|none|
|public|boolean|false|write-only|none|
|_links|object|false|none|none|
|» **additionalProperties**|[Link](#schemalink)|false|none|none|
|updatable|boolean|false|none|none|
|ispublic|boolean|false|none|none|

<h2 id="tocS_Link">Link</h2>
<!-- backwards compatibility -->
<a id="schemalink"></a>
<a id="schema_Link"></a>
<a id="tocSlink"></a>
<a id="tocslink"></a>

```json
{
  "relation": "string",
  "href": "string",
  "type": "string",
  "method": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|relation|string|true|none|none|
|href|string|true|none|none|
|type|string|true|none|none|
|method|string|true|none|none|

<h2 id="tocS_Error">Error</h2>
<!-- backwards compatibility -->
<a id="schemaerror"></a>
<a id="schema_Error"></a>
<a id="tocSerror"></a>
<a id="tocserror"></a>

```json
{
  "status": 0,
  "message": "string",
  "error": "string"
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|status|integer(int32)|false|none|none|
|message|string|false|none|none|
|error|string|false|none|none|

<h2 id="tocS_Exists">Exists</h2>
<!-- backwards compatibility -->
<a id="schemaexists"></a>
<a id="schema_Exists"></a>
<a id="tocSexists"></a>
<a id="tocsexists"></a>

```json
{
  "exists": true
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|exists|boolean|false|none|none|

<h2 id="tocS_DataResource">DataResource</h2>
<!-- backwards compatibility -->
<a id="schemadataresource"></a>
<a id="schema_DataResource"></a>
<a id="tocSdataresource"></a>
<a id="tocsdataresource"></a>

```json
{
  "count": 0,
  "total": 0,
  "_links": {
    "property1": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    },
    "property2": {
      "relation": "string",
      "href": "string",
      "type": "string",
      "method": "string"
    }
  },
  "data": [
    {
      "id": "string",
      "doc_key": "string",
      "doc_zone": "string",
      "last_update_date": "2019-08-24T14:15:22Z",
      "doc_value": "string",
      "doc_owner": "string",
      "doc_organization": "string",
      "doc_entities": [
        "string"
      ],
      "doc_writers": [
        "string"
      ],
      "doc_readers": [
        "string"
      ],
      "public": true,
      "_links": {
        "property1": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        },
        "property2": {
          "relation": "string",
          "href": "string",
          "type": "string",
          "method": "string"
        }
      },
      "updatable": true,
      "ispublic": true
    }
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|count|integer(int32)|false|none|none|
|total|integer(int64)|false|none|none|
|_links|object|false|none|none|
|» **additionalProperties**|[Link](#schemalink)|false|none|none|
|data|[[DataWithLinks](#schemadatawithlinks)]|false|none|none|

