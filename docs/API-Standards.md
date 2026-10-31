# API Standards

## Success

```json
{
  "success": true,
  "data": {},
  "message": "Transaction created"
}
```

## Failure

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    { "field": "amount", "message": "Amount must be greater than zero" }
  ],
  "timestamp": "2026-10-31T18:30:00"
}
```

`errors` is only present for validation failures. `timestamp` is in the `Asia/Kolkata`
zone.

## Error status codes

| Status | When it is returned |
| --- | --- |
| `400 Bad Request` | Validation failure, malformed JSON, invalid path/query value, or a missing required parameter/header |
| `401 Unauthorized` | Invalid login credentials, or a missing/invalid/expired JWT |
| `403 Forbidden` | The authenticated user is not allowed to perform the action |
| `404 Not Found` | The requested resource or endpoint does not exist |
| `405 Method Not Allowed` | The endpoint does not support the HTTP method used |
| `406 Not Acceptable` | The requested response media type is not supported |
| `409 Conflict` | A duplicate resource or a database uniqueness conflict |
| `415 Unsupported Media Type` | The request body media type is not supported |
| `500 Internal Server Error` | An unexpected server error; the response intentionally omits internal details |
| `501 Not Implemented` | The requested feature is not available yet |

Unexpected exceptions are logged on the server, but clients only receive the generic
`"An unexpected error occurred"` message so that stack traces and database details are
never exposed.
