Notice: for now, the UI can only read index created by Solr 5.3.1
To compile and run the UI and the content-based comparison demo, please follow these steps:
1. create an eclipse project with the name "cs572_HW2"
2. import all the files under src_ui folder into the project
3. import IndexPreProcess.java and ContentBasedCompDemo.java into the project
4. add the following packages as external JARs:
    lucene-core-5.3.1.jar
    lucene-analyzers-common-5.3.1.jar
    lucene-queryparser-5.3.1.jar
    lucene-queries-5.3.1.jar
5. edit the path in config.xml to your own index path
6. edit the path in IndexPreProcess.java and ContentBasedCompDemo.java to your own index path
7. compile and run IndexPreProcess.java ONCE AND ONLY ONCE before step 8
8. now you can compile and run the UI and the content-based comparison demo in eclipse.
