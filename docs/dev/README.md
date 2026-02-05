# Development Documentation

Technical documentation for developers working on the P2T service.

## Contents

| Document | Description |
|----------|-------------|
| [Architecture](./architecture.md) | System architecture, design patterns, and component overview |
| [Code Structure](./code-structure.md) | Package layout and key classes |
| [LLM Integration](./llm-integration.md) | LLM provider integration details |
| [Data Flow](./data-flow.md) | Request processing and data transformation |

## Quick Reference

### Key Packages

```
de.dhbw.woped.process2text
├── config/         # ServiceConfig, MetricsConfig, WebConfig
├── controller/     # P2TController, GlobalExceptionHandler
├── service/
│   ├── llm/        # LLM provider integration
│   └── text/       # NLP text generation
└── model/          # Domain models
```

### Design Patterns

- **Strategy Pattern**: `LlmProvider` interface
- **Factory Pattern**: `LlmProviderFactory`
- **Facade Pattern**: `P2TLLMService`
- **Builder Pattern**: `LlmRequest`, `LlmResponse`

### Adding New Features

- [Adding a new LLM provider](./llm-integration.md#adding-a-new-provider)
- Architecture considerations in [architecture.md](./architecture.md)
