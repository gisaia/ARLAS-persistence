# ARLAS Persistence API

The ARLAS Persistence API lets you store and retrieve data from key and type.

## URL Schema
The table below lists the URL endpoints.

| PATH Template                     | Description                                                          |
| --------------------------------- | -------------------------------------------------------------------- |
| /arlas_persistence/persistence/               |Return [DataResource](./api/definitions.md#dataresource) |
| /arlas_persistence/persistence/`{id}` | Get, update or delete a  entry in ARLAS Persistence                   |


## Managing persistence

### /arlas_persistence/persistence/

| Method     | Input Data                    | Output Data                            | Description                                                                        |
| ---------- | ----------------------------- | ---------------------------------------| ---------------------------------------------------------------------------------- |
| **GET**    | `Type` as string               |[DataResource](./api/definitions.md#dataresource) | Get all the resources for the given type |
| **POST**   | `Type` as string, `value` as string          |[DataWithLinks](./api/definitions.md#datawithlinks) | Add a new entry with the given type and value |


### /arlas_persistence/persistence/{id}

The following methods let you get, update and delete an entry.

| Method     | Input Data                    | Output Data                   | Description                                             |
| ---------- | ----------------------------- | ----------------------------- | ------------------------------------------------------- |
| **GET**    | `None`                        | [DataWithLinks](./api/definitions.md#datawithlinks) | Get the entry for the given id |
| **PUT**    | `value` as string  | [DataWithLinks](./api/definitions.md#datawithlinks) |Update the entry for the given id                 |
| **DELETE** | `None`                        | `None`                        | Delete the entry for the given id                              |
