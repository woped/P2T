# P2T Architecture

## Overview

The P2T (Process-to-Text) service converts business process models (PNML/BPMN) into natural language text descriptions. It supports two generation approaches: algorithmic NLP-based generation and LLM-based generation.

## High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[woped-frontend<br/>Angular]
        DESKTOP[WoPeD Desktop<br/>Java Swing]
    end
    
    subgraph "P2T Service"
        subgraph "Presentation Layer"
            CTRL[P2TController]
            EXC[GlobalExceptionHandler]
        end
        
        subgraph "Service Layer"
            P2TSVC[P2TService<br/>Algorithmic]
            LLMSVC[P2TLLMService<br/>LLM Facade]
            RAGSVC[RagService]
            TRANSSVC[TransformerService]
        end
        
        subgraph "LLM Provider Layer"
            FACTORY[LlmProviderFactory]
            OPENAI[OpenAiProvider]
            GEMINI[GeminiProvider]
            LMSTUDIO[LmStudioProvider]
        end
        
        subgraph "Text Generation Pipeline"
            READER[Model Readers<br/>PNML/BPMN]
            PLANNER[TextPlanner]
            REALIZER[SurfaceRealizer]
        end
    end
    
    subgraph "External Services"
        RAGEXT[RAG Service :5000]
        TRANSEXT[model-transformer :5001]
        OPENAIAPI[OpenAI API]
        GEMINIAPI[Gemini API]
        LMSTUDIOAPI[LM Studio :1234]
    end
    
    WEB --> CTRL
    DESKTOP --> CTRL
    
    CTRL --> P2TSVC
    CTRL --> LLMSVC
    CTRL --> RAGSVC
    
    P2TSVC --> READER
    READER --> PLANNER
    PLANNER --> REALIZER
    
    LLMSVC --> FACTORY
    LLMSVC --> TRANSSVC
    
    FACTORY --> OPENAI
    FACTORY --> GEMINI
    FACTORY --> LMSTUDIO
    
    RAGSVC --> RAGEXT
    TRANSSVC --> TRANSEXT
    
    OPENAI --> OPENAIAPI
    GEMINI --> GEMINIAPI
    LMSTUDIO --> LMSTUDIOAPI
```

## Design Principles

### Clean Architecture Layers

```mermaid
graph LR
    subgraph "Outer Layer"
        A[Controllers]
        B[External APIs]
    end
    
    subgraph "Middle Layer"
        C[Services]
        D[Providers]
    end
    
    subgraph "Core Layer"
        E[Domain Models]
        F[Business Logic]
    end
    
    A --> C
    B --> D
    C --> E
    D --> F
```

### Key Design Patterns

| Pattern | Usage | Location |
|---------|-------|----------|
| **Strategy** | LLM Provider abstraction | `LlmProvider` interface |
| **Factory** | Provider instantiation | `LlmProviderFactory` |
| **Facade** | LLM service orchestration | `P2TLLMService` |
| **Builder** | Request/Response construction | `LlmRequest`, `LlmResponse` |
| **Template Method** | Text generation pipeline | `TextGenerator` |

## Component Details

### Controller Layer

```mermaid
classDiagram
    class P2TController {
        +generateText(body: String): String
        +generateTextLLM(body, apiKey, prompt, model, provider, useRag): String
        +getModels(apiKey, provider): List~String~
        +getProviders(): List~String~
    }
    
    class GlobalExceptionHandler {
        +handleLlmException(ex): ResponseEntity
        +handleResponseStatusException(ex): ResponseEntity
        +handleMissingParams(ex): ResponseEntity
        +handleGenericException(ex): ResponseEntity
    }
    
    P2TController ..> GlobalExceptionHandler : exceptions handled by
```

### Service Layer

```mermaid
classDiagram
    class P2TService {
        +generateText(text: String): String
        -prepareText(pnml: String): String
    }
    
    class P2TLLMService {
        +generateText(model, provider, apiKey, model, prompt): String
        +getAvailableModels(provider, apiKey): List~String~
        +getSupportedProviders(): List~String~
        +providerRequiresApiKey(provider): boolean
        -prepareProcessModel(model): String
    }
    
    class RagService {
        +enrichPrompt(prompt, diagram): String
        +search(query, limit): String
        +isAvailable(): boolean
    }
    
    class TransformerService {
        +transform(direction, content): String
        +checkForBPMNorPNML(content): String
    }
    
    P2TLLMService --> TransformerService
    P2TLLMService --> LlmProviderFactory
```

### LLM Provider Layer

```mermaid
classDiagram
    class LlmProvider {
        <<interface>>
        +getProviderName(): String
        +generateText(request: LlmRequest): LlmResponse
        +getAvailableModels(apiKey: String): List~String~
        +requiresApiKey(): boolean
    }
    
    class OpenAiProvider {
        -serviceConfig: ServiceConfig
        +getProviderName(): String
        +generateText(request): LlmResponse
        +getAvailableModels(apiKey): List~String~
    }
    
    class GeminiProvider {
        -serviceConfig: ServiceConfig
        +getProviderName(): String
        +generateText(request): LlmResponse
        +getAvailableModels(apiKey): List~String~
    }
    
    class LmStudioProvider {
        -serviceConfig: ServiceConfig
        +getProviderName(): String
        +generateText(request): LlmResponse
        +getAvailableModels(apiKey): List~String~
        +requiresApiKey(): boolean
    }
    
    class LlmProviderFactory {
        -providers: Map~String, LlmProvider~
        +getProvider(name): LlmProvider
        +hasProvider(name): boolean
        +getAvailableProviderNames(): List~String~
    }
    
    LlmProvider <|.. OpenAiProvider
    LlmProvider <|.. GeminiProvider
    LlmProvider <|.. LmStudioProvider
    LlmProviderFactory o-- LlmProvider
```

## Configuration Architecture

```mermaid
graph TB
    subgraph "Configuration Sources"
        SYS[System Properties]
        ENV[Environment Variables]
        PROPS[application.properties]
        DEFAULT[Default Values]
    end
    
    subgraph "ServiceConfig"
        RAG_URL[ragServiceUrl]
        TRANS_URL[transformerServiceUrl]
        OPENAI_URL[openaiApiUrl]
        GEMINI_URL[geminiApiUrl]
        LMSTUDIO_URL[lmstudioApiUrl]
    end
    
    SYS --> RAG_URL
    ENV --> RAG_URL
    PROPS --> RAG_URL
    DEFAULT --> RAG_URL
    
    SYS --> TRANS_URL
    ENV --> TRANS_URL
    PROPS --> TRANS_URL
    DEFAULT --> TRANS_URL
```

**Priority Order (highest to lowest):**
1. Java System Properties (`-Drag.service.url=...`)
2. Environment Variables (`RAG_SERVICE_URL=...`)
3. `application.properties` file
4. Default values in `ServiceConfig`

## Error Handling

```mermaid
graph TB
    subgraph "Exception Types"
        LLM[LlmException]
        RSE[ResponseStatusException]
        MSP[MissingServletRequestParameterException]
        IAE[IllegalArgumentException]
        GEN[Generic Exception]
    end
    
    subgraph "Error Types"
        AUTH[AUTHENTICATION_FAILED<br/>401]
        RATE[RATE_LIMITED<br/>429]
        MISSING[MISSING_API_KEY<br/>400]
        UNAVAIL[PROVIDER_UNAVAILABLE<br/>503]
        PARSE[PARSE_ERROR<br/>500]
    end
    
    subgraph "Response"
        JSON[JSON Error Response]
    end
    
    LLM --> AUTH
    LLM --> RATE
    LLM --> MISSING
    LLM --> UNAVAIL
    LLM --> PARSE
    
    AUTH --> JSON
    RSE --> JSON
    MSP --> JSON
    IAE --> JSON
    GEN --> JSON
```

## Thread Safety

- **ServiceConfig**: Immutable after initialization
- **LlmProviderFactory**: Thread-safe provider map (immutable after construction)
- **LlmRequest/LlmResponse**: Immutable DTOs
- **Services**: Stateless Spring beans (thread-safe by default)
