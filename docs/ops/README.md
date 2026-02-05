# Operations Documentation

Everything needed to run, deploy, configure, and monitor the P2T service.

## Contents

| Document | Description |
|----------|-------------|
| [Quick Start](./quickstart.md) | Get started in 5 minutes |
| [Configuration](./configuration.md) | All configuration options |
| [Docker Deployment](./docker.md) | Container deployment guide |
| [Monitoring](./monitoring.md) | Metrics, logging, and health checks |

## Quick Reference

### Starting the Service

```bash
# Development
./mvnw spring-boot:run

# With Docker
docker run -p 8080:8080 woped/process2text

# Production (Docker Compose)
cd infrastructure/webservices
docker-compose up -d p2t
```

### Key Ports

| Service | Port |
|---------|------|
| P2T API | 8080 |
| RAG Service | 5000 |
| model-transformer | 5001 |
| LM Studio | 1234 |

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `RAG_SERVICE_URL` | `http://localhost:5000` | RAG service endpoint |
| `TRANSFORMER_SERVICE_URL` | `http://localhost:5001` | Transformer endpoint |
| `OPENAI_API_URL` | `https://api.openai.com/v1` | OpenAI API base URL |
| `GEMINI_API_URL` | `https://generativelanguage.googleapis.com` | Gemini API URL |
| `LMSTUDIO_API_URL` | `http://localhost:1234` | LM Studio API URL |
