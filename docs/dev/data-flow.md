# Data Flow

## Overview

This document describes how data flows through the P2T service for both algorithmic and LLM-based text generation.

## Algorithmic Text Generation

```mermaid
flowchart TB
    subgraph Input
        PNML[PNML XML]
        BPMN[BPMN XML]
    end
    
    subgraph "1. Parsing"
        DETECT[Format Detection]
        PNMLR[PNMLReader]
        BPMNR[BPMNReader]
        CONV[PetriNetToProcessConverter]
    end
    
    subgraph "2. Model"
        PM[ProcessModel]
        ACT[Activities]
        EVT[Events]
        GW[Gateways]
    end
    
    subgraph "3. Analysis"
        ANNO[Annotation<br/>EnglishLabelDeriver]
        RPST[RPST Conversion<br/>FormatConverter]
        RIGID[Rigid Structuring<br/>RigidStructurer]
    end
    
    subgraph "4. Planning"
        TPLAN[TextPlanner]
        IMCONV[IntermediateToDSynTConverter]
        SPLAN[Sentence Plan]
    end
    
    subgraph "5. Aggregation"
        AGG[SentenceAggregator]
        REF[ReferringExpressionGenerator]
        DISC[DiscourseMarker]
    end
    
    subgraph "6. Realization"
        SURF[SurfaceRealizer]
        POST[Post-processing]
    end
    
    subgraph Output
        TEXT[Natural Language Text]
    end
    
    PNML --> DETECT
    BPMN --> DETECT
    DETECT -->|pnml tag| PNMLR
    DETECT -->|bpmn:process tag| BPMNR
    PNMLR --> CONV
    CONV --> PM
    BPMNR --> PM
    
    PM --> ACT
    PM --> EVT
    PM --> GW
    
    PM --> ANNO
    ANNO --> RPST
    RPST --> RIGID
    
    RIGID --> TPLAN
    TPLAN --> IMCONV
    IMCONV --> SPLAN
    
    SPLAN --> AGG
    AGG --> REF
    REF --> DISC
    
    DISC --> SURF
    SURF --> POST
    POST --> TEXT
```

### Stage Details

#### 1. Parsing Stage

- **Format Detection**: Checks for `<pnml>` or `<bpmn:process>` tags
- **PNML Reader**: Parses Petri Net XML into `PetriNet` object
- **BPMN Reader**: Parses BPMN 2.0 XML into `ProcessModel`
- **Converter**: Transforms Petri Net to unified `ProcessModel`

#### 2. Model Stage

The `ProcessModel` contains:
- Activities (tasks, subprocesses)
- Events (start, end, intermediate)
- Gateways (XOR, AND, OR)
- Pools and Lanes (organizational info)
- Arcs (control flow)

#### 3. Analysis Stage

- **Annotation**: Extracts verbs, business objects, roles from labels
- **RPST**: Creates Refined Process Structure Tree
- **Rigid Structuring**: Handles unstructured regions

#### 4. Planning Stage

- **TextPlanner**: Traverses RPST, creates sentence plan
- **Converter**: Transforms process elements to DSynT sentences

#### 5. Aggregation Stage

- **Aggregation**: Combines sentences with same role
- **Referring Expressions**: Adds pronouns ("he", "it")
- **Discourse Markers**: Adds connectives ("then", "meanwhile")

#### 6. Realization Stage

- **Surface Realizer**: Generates final text from sentence plan
- **Post-processing**: Cleans up formatting

## LLM-based Text Generation

```mermaid
flowchart TB
    subgraph Input
        PROC[Process Model<br/>PNML/BPMN]
        PROMPT[User Prompt]
        KEY[API Key]
        MODEL[Model Selection]
    end
    
    subgraph "1. Validation"
        VPROV[Validate Provider]
        VKEY[Validate API Key]
    end
    
    subgraph "2. Transformation"
        CHECK[Check Format]
        TRANS[Transform to BPMN]
    end
    
    subgraph "3. RAG (optional)"
        ENRICH[Enrich Prompt]
        SEARCH[Semantic Search]
        CONTEXT[Add Context]
    end
    
    subgraph "4. Request Building"
        BUILD[Build LlmRequest]
        SYS[System Prompt]
        USER[User + Content]
    end
    
    subgraph "5. Provider Selection"
        FACTORY[LlmProviderFactory]
        PROV[Get Provider]
    end
    
    subgraph "6. API Call"
        CALL[Provider.generateText]
        PARSE[Parse Response]
    end
    
    subgraph Output
        TEXT[Generated Text]
    end
    
    PROC --> CHECK
    PROMPT --> VPROV
    KEY --> VKEY
    MODEL --> BUILD
    
    VPROV --> VKEY
    CHECK -->|PNML| TRANS
    CHECK -->|BPMN| BUILD
    TRANS --> BUILD
    
    VKEY -->|useRag=true| ENRICH
    ENRICH --> SEARCH
    SEARCH --> CONTEXT
    CONTEXT --> BUILD
    
    VKEY -->|useRag=false| BUILD
    
    BUILD --> SYS
    BUILD --> USER
    
    SYS --> FACTORY
    USER --> FACTORY
    FACTORY --> PROV
    PROV --> CALL
    CALL --> PARSE
    PARSE --> TEXT
```

### PNML to BPMN Transformation

LLMs perform better with BPMN format due to more descriptive element names:

```mermaid
flowchart LR
    subgraph PNML
        P1[place_1]
        T1[transition_1]
        P2[place_2]
    end
    
    subgraph BPMN
        S[Start Event]
        A["Create Order"]
        E[End Event]
    end
    
    PNML -->|model-transformer| BPMN
```

## API Request/Response Flow

### POST /generateText (Algorithmic)

```mermaid
sequenceDiagram
    participant C as Client
    participant CTRL as P2TController
    participant SVC as P2TService
    participant TG as TextGenerator
    
    C->>CTRL: POST /generateText<br/>Content-Type: text/plain<br/>[PNML/BPMN XML]
    
    CTRL->>CTRL: Increment counter
    CTRL->>SVC: generateText(body)
    SVC->>SVC: prepareText(body)
    SVC->>TG: toText(preparedText)
    
    TG->>TG: Parse XML
    TG->>TG: Build ProcessModel
    TG->>TG: NLP Pipeline
    TG-->>SVC: Generated text
    
    SVC-->>CTRL: text
    CTRL-->>C: 200 OK<br/>Content-Type: text/plain<br/>[Generated text]
```

### POST /generateTextLLM

```mermaid
sequenceDiagram
    participant C as Client
    participant CTRL as P2TController
    participant LLM as P2TLLMService
    participant RAG as RagService
    participant PROV as LlmProvider
    participant API as External API
    
    C->>CTRL: POST /generateTextLLM<br/>?apiKey=...&prompt=...&gptModel=...&provider=...&useRag=false
    
    CTRL->>CTRL: Validate provider
    CTRL->>CTRL: Validate API key
    
    alt useRag=true
        CTRL->>RAG: enrichPrompt(prompt, body)
        RAG-->>CTRL: enrichedPrompt
    end
    
    CTRL->>LLM: generateText(body, provider, apiKey, model, prompt)
    LLM->>LLM: prepareProcessModel (PNML→BPMN)
    LLM->>LLM: Build LlmRequest
    LLM->>PROV: generateText(request)
    PROV->>API: HTTP POST
    API-->>PROV: Response
    PROV-->>LLM: LlmResponse
    LLM-->>CTRL: text
    CTRL-->>C: 200 OK<br/>[Generated text]
```

## Error Flow

```mermaid
flowchart TB
    subgraph "Error Sources"
        VAL[Validation Error]
        AUTH[Auth Error]
        NET[Network Error]
        PARSE[Parse Error]
    end
    
    subgraph "Exception Handling"
        LLMEX[LlmException]
        RSEEX[ResponseStatusException]
        GENEX[Generic Exception]
    end
    
    subgraph "Global Handler"
        GEH[GlobalExceptionHandler]
    end
    
    subgraph "Response"
        JSON[JSON Error<br/>with status code]
    end
    
    VAL --> RSEEX
    AUTH --> LLMEX
    NET --> LLMEX
    PARSE --> LLMEX
    
    LLMEX --> GEH
    RSEEX --> GEH
    GENEX --> GEH
    
    GEH --> JSON
```
