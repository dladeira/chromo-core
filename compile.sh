mvn clean package assembly:single
rm ../testserver-1.18/plugins/chromo-core-1.0.0.jar
mv ./target/chromo-core-1.0.0-jar-with-dependencies.jar ../testserver-1.18/plugins/chromo-core-1.0.0.jar
