# Process to Text (P2T)
This webservice is used to translate a petrinet into plain text.

# Requirements for development
* IDE of your choice
* Java 11

# Installation guide:
_It is recommended to use IntelliJ IDE._
1. Git clone this project onto your machine.
2. Start IntelliJ and open the project.
3. Wait until all files have been loaded.
4. Add a new Library: `File -> Project Structure -> Libraries -> + -> new Java Library -> mark all .jar-Files in the lib folder -> Ok -> Close Project Structure`
5. Right click on pom.xml and click `Maven -> Reimport`
6. Run Application with the Start-Button or with `mvn spring-boot:run`

# Testing
### Testing with Swagger UI
1. Start the application.
2. Navigate to `http://localhost:8080/p2t/swagger-ui.`
3. Paste your petrinet (the content of the xml file) in the body of the `POST /p2t/generateText` endpoint.

### Testing via the embedded GUI
1. Start the application.
2. Navigate to `http://localhost:8080/p2t/`.
3. Paste your petrinet (the content of the xml file) in the first text area and submit the form.

### Testing with Postman
1. Add a new collection in Postman.
2. Add a new request in your created collection.
3. For your request change `Get` to `Post`.
4. Enter URL `http://localhost:8080/p2t/generateText`
5. Open the body configuration and choose `raw`.
6. Copy the content of a `.pnml` file (must be a sound petrinet) in the body of the request.
7. Click send button

### Testing with the WoPeD-Client
1. Start the application.
2. Follow the installation instructions of the WoPeD-Client (`https://github.com/tfreytag/WoPeD`).
3. Start WoPeD-Client and.
4. Open the configuration and navigate to `NLP Tools`. Adapt the `Process2Text` configuration: 
   - `Server host`: `localhost`
   - `Port`: `8080`
   - `URI`: `/p2t`
5. Test your configuration.
6. Close the configuration and import or create a new petrinet.
7. Navigate to `Analyse` -> `Translate to text` and execute. The petrinet will now be transformed by your locally started P2T webservice.