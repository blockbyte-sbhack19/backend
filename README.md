


# How to deploy to AWS

* ssh to the box Open an SSH client. (find out how to connect using PuTTY)
* Locate your private key file (blockbyte.pem). 
* Your key must not be publicly viewable for SSH to work. Use this command if needed:
`chmod 400 blockbyte.pem`

then

`ssh -i "blockbyte.pem" ubuntu@ec2-34-248-121-44.eu-west-1.compute.amazonaws.com`

you should be on server

# Initial install
Install JAVA 8
`sudo apt install openjdk-8-jdk unzip`

Install Gradle
```
wget https://services.gradle.org/distributions/gradle-5.4.1-bin.zip -P /tmp
sudo unzip -d /opt/gradle /tmp/gradle-*.zip
sudo unzip -d /opt/gradle /tmp/gradle-*.zip
ls /opt/gradle/gradle-5.4.1/
sudo vi /etc/profile.d/gradle.sh
```
add
```
export GRADLE_HOME=/opt/gradle/gradle-5.0
export PATH=${GRADLE_HOME}/bin:${PATH}
```

Source that file in current shell
```
source /etc/profile.d/gradle.sh
```
Check
```
gradle -v
```

Clone the project
```
git clone https://github.com/blockbyte-sbhack19/backend.git
```

run
```
gradle build
```