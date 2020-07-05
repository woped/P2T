# Process to Text (P2T)
Translate a petrinet to plain text

# Requirements
Requirements for the installation of P2T
* IDE of your Choice (Installationguide is written for IntelliJ)
* Postman (For testing the API)
* Java 8

# Installationguide:
It is recommended to use the IntelliJ IDE, because the guide is written for this IDE.
1. Git clone this project in your local repository
2. Start IntelliJ and open the project
3. Wait a short time until all files are loaded
4. Add a new Library: File -> Project Structure -> Libraries -> + -> new Java Library -> mark all .jar-Dateien im lib-Ordner -> Ok -> Close Project Structure
5. Right click on pom.xml and click "Maven" -> Reimport
6. Run Application with the Start-Button or with "mvn spring-boot:run"

# Testing with Postman
1. Add a new collection in Postman
2. Add a new request in your created collection
3. Alternate "Get" to "Post" in your request
4. Enter URL: http://localhost:8080/p2t/generateText
5. Open body and choose "raw"
6. Copy the content of a .pnml-File which is a petrinet (must be sound!) in the body of the request
7. Press Send-Button
8. You get the text for the petrinet if it is sound and there is no error