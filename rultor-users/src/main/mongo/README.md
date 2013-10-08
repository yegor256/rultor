These JavaScript scripts should be executed on
production MongoDB database, one by one in the
right order:

```
$ for f in $(ls rultor-users/src/main/mongo/*.js); do mongo -u rultor -p <password> ds037688.mongolab.com:37688/pulses $f; done
```

Total execution time may be over 15 minutes, be patient.
