mvn clean install
mvn clean package assembly:single
rm ../testserver-1.18/plugins/ladeira-core-1.0.0.jar
mv ./target/ladeiracore-1.0.0-jar-with-dependencies.jar ../testserver-1.18/plugins/ladeira-core-1.0.0.jar
