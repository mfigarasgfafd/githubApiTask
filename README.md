# Github API proxy task application

Spring Boot application that acts as a proxy to Github API, listing NON-FORK repositories with their branch names, owner login and last commit SHA.


## Requirements
- Java 25+
- Gradle

Application starts on `http://localhost:8008`

## API Endpoint

```
GET /api/{username}/repos
```
## Example usage

```
curl.exe http://localhost:8008/api/mfigarasgfafd/repos
```


### Response Format

```json
[
  {
    "name": "repository-name",
    "ownerLogin": "username",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "qwe123..."
      }
    ]
  }
]
```

### Error Response (404)

```json
{
  "status": 404,
  "message": "User 'username' doesn't exist"
}
```

