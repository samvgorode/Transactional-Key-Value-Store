# Transactional Key-Value Store

An interactive command line interface to a transactional key value store.

Commands:
```
SET <key> <value> // store the value for key
GET <key> // return the current value for key
DELETE <key> // remove the entry for key
COUNT <value> // return the number of keys that have the given value
BEGIN // start a new transaction
COMMIT // complete the current transaction
ROLLBACK // revert to state prior to BEGIN call
```
