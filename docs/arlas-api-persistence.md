# ARLAS Persistence API

The ARLAS Persistence API lets you store and retrieve data from key and zone.

## URL Schema
The table below lists the URL endpoints.

| PATH Template                     | Description                                                               |
| --------------------------------- | ------------------------------------------------------------------------- |
| /persist/groups/`{zone}`          | Returns the users' groups allowed to interact with the given zone.        |
| /persist/resource/`{zone}`/`{key}`| Get, create or delete an entry in ARLAS Persistence from its zone and key.|
| /persist/resource/`{id}`          | Get, update or delete an entry in ARLAS Persistence from its id.          |
| /persist/resource/`{zone}`        | Fetch a list of  entries related to a zone in ARLAS Persistence.          |


## Managing persistence

### /persist/groups/`{zone}`

| Method     | Input Data                    | Output Data                            | Description                                                                        |
| ---------- | ----------------------------- | ---------------------------------------| ---------------------------------------------------------------- |
| **GET**    | `zone` as string              | Array of string                        |Returns the users' groups allowed to interact with the given zone.|


### /persist/resource/`{zone}`/`{key}`

The following methods let you get, create and delete an entry.

| Method     | Input Data                         | Output Data                                         | Description                                    |
| ---------- | -----------------------------      | --------------------------------------------------- | ---------------------------------------------- |
| **GET**    | `zone` as string ; `key` as string | [DataWithLinks](./api/definitions.md#datawithlinks) | Get the entry for the given key and zone.      |
| **POST**   | `zone` as string ; `key` as string | [DataWithLinks](./api/definitions.md#datawithlinks) | Update the entry for the given key and zone.   |
| **DELETE** | `zone` as string ; `key` as string | [DataWithLinks](./api/definitions.md#datawithlinks) | Delete the entry for the given key and zone.   |


### /persist/resource/`{id}`

The following methods let you get, update and delete an entry.

| Method     | Input Data                    | Output Data                                         | Description                        |
| ---------- | ----------------------------- | --------------------------------------------------- | ---------------------------------- |
| **GET**    | `id`  as string               | [DataWithLinks](./api/definitions.md#datawithlinks) | Get the entry for the given id.    |
| **PUT**    | `id`  as string               | [DataWithLinks](./api/definitions.md#datawithlinks) | Update the entry for the given id. |
| **DELETE** | `id`  as string               | [DataWithLinks](./api/definitions.md#datawithlinks) | Delete the entry for the given id. |


### /persist/resource/`{zone}`

The following methods let you get a list of entries for a zone.

| Method     | Input Data                    | Output Data                                         | Description                        |
| ---------- | ----------------------------- | --------------------------------------------------- | ---------------------------------- |
| **GET**    | `zone`  as string             | [DataResource](./api/definitions.md#dataResource)   | Get the entries for the given zone.    |
