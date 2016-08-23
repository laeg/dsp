# IP

1. Build it:

    mvn clean package

2. Copy target/... to the plugins/ directory of your Neo4j server.

    
3. Download and copy additional jars to the plugins/ directory of your Neo4j server.

    wget http://repo1.maven.org/maven2/com/google/guava/guava/19.0/guava-19.0.jar

4. Configure Neo4j by adding a line to conf/neo4j.conf:

    dbms.unmanaged_extension_classes=com.king.dsp.neo4j=/v1

5. Start Neo4j server

6. Send a :POST to /v1/service/write/rfbjson ??

   http POST "http://localhost:7474/v1/service/write/rfbjson"