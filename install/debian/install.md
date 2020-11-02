# Installation on Alpine Linux

* Install Java
```
sudo apt install default-jre
```
* Upload seeder service file
```
scp seeder.service root@[ip address here]/tmp
```
* Move seeder service file to service directory
```
mv /tmp/seeder /etc/systemd/system/
```
* Upload jar
```
scp ra-seeder-1.0.0-with-dependencies.jar root@[ip address here]/tmp
```
* Move jar to /usr/local/seeder
```
mv /tmp/ra-seeder-1.0.0-with-dependencies.jar /usr/local/seeder/ra-seeder-1.0.0.jar
```
* Add service
```
systemctl daemon-reload
systemctl enable seeder.service
```
* Start service
```
systemctl start seeder.service
```
* Verify service is running
```
systemctl status seeder.service
```
* Check logs at: /usr/local/seeder/logs/
```
tail -f out.log.0
```
* Stop service manually if/when desired
```
systemctl stop seeder.service
```
* Restart service manually if/when desired
```
systemctl restart seeder.service
```

