# dds

## Run
- Set env vars:
  * AWS_REGION
  * AWS_ACCESS_KEY_ID
  * AWS_SECRET_ACCESS_KEY


## Test
```
curl -v -X POST -F 'file=@src/test/data/252400.zip' http://localhost:8080/upload
```

```
curl -v -X POST -u user:password -F 'file=@src/test/data/252400.zip' http://localhost:8080/upload
```