# Installation on Alpine 3.12 Linux
***** Alpine doesn't have a fully compatible OpenJDK 11 against musl (vs glibc) so this will not work
until OpenJDK 13 comes out with its full compile targeted to musl (not glibc)********

* Install Java
```
apk --no-cache add openjdk11 --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
```
* Upload seeder service file
```
scp seeder root@[ip address here]/tmp
```
* Move seeder service file to service directory
```
mv /tmp/seeder /etc/init.d/
```
* Make seeder service file executable
```
chmod +x /etc/init.d/seeder
```
* Uploade jar
```
scp ra-seeder-1.0.0.jar root@[ip address here]/tmp
```
* Move jar to /usr/local/seeder
```
mv /tmp/ra-seeder-1.0.0.jar /usr/local/seeder
```
* Add service
```
rc-update add seeder
```
* Start service
```
rc-service seeder start
```
* Verify service is running
```
rc-service seeder status
```
* Check logs at: /usr/local/seeder/logs/out.log

