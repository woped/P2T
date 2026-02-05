# P2T Code Structure

## Package Overview

```
src/main/java/de/dhbw/woped/process2text/
├── config/                    # Configuration classes
├── controller/                # REST controllers
├── exception/                 # Custom exceptions
├── model/                     # Domain models
│   ├── dsynt/                 # Deep Syntax Tree models
│   ├── intermediate/          # Intermediate representations
│   ├── process/               # Process model classes
│   └── reader/                # Model readers (PNML, BPMN)
└── service/                   # Business logic
    ├── content/               # Content determination
    ├── llm/                   # LLM integration
    │   ├── dto/               # Data Transfer Objects
    │   └── provider/          # LLM provider implementations
    ├── sentence/              # Sentence processing
    └── text/                  # Text generation
```

## Package Details

### config/

Configuration classes for Spring Boot application.

```mermaid
classDiagram
    class ServiceConfig {
        -ragServiceUrl: String
        -transformerServiceUrl: String
        -openaiApiUrl: String
        -geminiApiUrl: String
        -lmstudioApiUrl: String
        +getRagServiceUrl(): String
        +getTransformerServiceUrl(): String
        +getOpenaiApiUrl(): String
        +getGeminiApiUrl(): String
        +getLmstudioApiUrl(): String
    }
    
    class MetricsConfig {
        +httpRequestsTotal(registry): Counter
        +httpRequestDuration(registry): Timer
        +apiCallDuration(registry): Timer
    }
    
    class WebConfig {
        +corsConfigurer(): WebMvcConfigurer
    }
```

| File | Purpose |
|------|---------|
| `ServiceConfig.java` | External service URLs configuration |
| `MetricsConfig.java` | Micrometer metrics beans |
| `WebConfig.java` | CORS configuration |

### controller/

REST API endpoints.

| File | Purpose |
|------|---------|
| `P2TController.java` | Main API controller |
| `GlobalExceptionHandler.java` | Centralized error handling |

### service/

Business logic layer.

```mermaid
graph TB
    subgraph "Main Services"
        P2T[P2TService]
        LLM[P2TLLMService]
        RAG[RagService]
        TRANS[TransformerService]
    end
    
    subgraph "LLM Package"
        PROV[LlmProvider]
        FACT[LlmProviderFactory]
        EXC[LlmException]
        
        subgraph "dto"
            REQ[LlmRequest]
            RES[LlmResponse]
        end
        
        subgraph "provider"
            OAI[OpenAiProvider]
            GEM[GeminiProvider]
            LMS[LmStudioProvider]
        end
    end
    
    LLM --> FACT
    FACT --> OAI
    FACT --> GEM
    FACT --> LMS
    
    OAI --> REQ
    OAI --> RES
```

### service/text/

NLP text generation pipeline.

```mermaid
graph LR
    subgraph "Text Generation"
        GEN[TextGenerator]
        PLAN[TextPlanner]
        CONV[TextToIntermediateConverter]
        HELP[PlanningHelper]
    end
    
    GEN --> PLAN
    PLAN --> CONV
    PLAN --> HELP
```

### model/

Domain model classes.

```mermaid
graph TB
    subgraph "Process Models"
        PM[ProcessModel]
        ACT[Activity]
        EVT[Event]
        GW[Gateway]
        POOL[Pool]
        LANE[Lane]
    end
    
    subgraph "Readers"
        PNML[PNMLReader]
        BPMN[BPMNReader]
        CONV[PetriNetToProcessConverter]
    end
    
    subgraph "DSynT"
        SEN[DSynTSentence]
        MAIN[DSynTMainSentence]
        COND[DSynTConditionSentence]
    end
    
    PNML --> PM
    BPMN --> PM
    PM --> ACT
    PM --> EVT
    PM --> GW
```

## Key Classes

### P2TController

Main REST controller with endpoints:

```java
@PostMapping("/generateText")      // Algorithmic generation
@PostMapping("/generateTextLLM")   // LLM-based generation
@GetMapping("/gptModels")          // List available models
@GetMapping("/providers")          // List providers
```

### P2TLLMService

Orchestrates LLM text generation:

```java
public String generateText(processModel, provider, apiKey, model, prompt)
public List<String> getAvailableModels(provider, apiKey)
public List<String> getSupportedProviders()
public boolean providerRequiresApiKey(provider)
```

### LlmProvider (Interface)

Strategy interface for LLM providers:

```java
String getProviderName();
LlmResponse generateText(LlmRequest request);
List<String> getAvailableModels(String apiKey);
default boolean requiresApiKey() { return true; }
```

### TextGenerator

Main NLP pipeline orchestrator:

```java
public String toText(String input) {
    // 1. Detect format (PNML/BPMN)
    // 2. Parse to ProcessModel
    // 3. Annotate with linguistic info
    // 4. Convert to RPST
    // 5. Handle rigid structures
    // 6. Generate sentence plan
    // 7. Aggregate sentences
    // 8. Insert referring expressions
    // 9. Add discourse markers
    // 10. Realize surface text
}
```

## Test Structure

```
src/test/java/de/dhbw/woped/process2text/
├── controller/
│   └── P2TControllerTest.java
├── service/
│   ├── P2TLLMServiceTest.java
│   └── llm/
│       ├── LlmProviderFactoryTest.java
│       ├── dto/
│       │   └── LlmRequestTest.java
│       └── provider/
│           └── LmStudioProviderTest.java
├── TransformerServiceTests.java
└── Process2textApplicationTests.java  # Integration tests (disabled)
```

## Resources

```
src/main/resources/
├── application.properties       # Spring configuration
├── dict/                       # WordNet dictionary files
├── nlptools.wordnet.FileProperties  # WordNet config
└── ...
```
