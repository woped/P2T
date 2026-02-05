# Quick Start Guide

Get the P2T service running in 5 minutes.

## Prerequisites

- **Java 21** (or Docker)
- **Maven 3.8+** (for local development)

## Option 1: Docker (Fastest)

```bash
# Run standalone
docker run -p 8080:8080 woped/process2text

# Verify
curl http://localhost:8080/actuator/health
```

## Option 2: Local Development

```bash
# Clone repository
git clone https://github.com/woped/P2T.git
cd P2T

# Install local dependencies
mvn install:install-file -Dfile=lib/p2t-core-1.0.0-SNAPSHOT.jar \
  -DgroupId=org.woped.p2t -DartifactId=p2t-core -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar

# Start application
./mvnw spring-boot:run
```

## Option 3: Full Stack (Docker Compose)

```bash
cd infrastructure/webservices

# Start all services
docker-compose up -d

# Check status
docker-compose ps
```

## Verify Installation

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### Swagger UI

Open in browser: http://localhost:8080/swagger-ui/

### Test Algorithmic Generation

```bash
curl -X POST http://localhost:8080/p2t/generateText \
  -H "Content-Type: text/plain" \
  -d '<?xml version="1.0"?>
<pnml>
  <net id="test" type="http://www.pnml.org/version-2009/grammar/pnmlcoremodel">
    <place id="p1"><name><text>Start</text></name></place>
    <transition id="t1"><name><text>Create Order</text></name></transition>
    <place id="p2"><name><text>End</text></name></place>
    <arc id="a1" source="p1" target="t1"/>
    <arc id="a2" source="t1" target="p2"/>
  </net>
</pnml>'
```

### Test LLM Generation (OpenAI)

```bash
curl -X POST "http://localhost:8080/p2t/generateTextLLM?apiKey=YOUR_KEY&provider=openai&gptModel=gpt-4&useRag=false" \
  -H "Content-Type: text/plain" \
  -d '<?xml version="1.0"?>
<pnml>...</pnml>'
```

### List Available Models

```bash
curl "http://localhost:8080/p2t/gptModels?apiKey=YOUR_KEY&provider=openai"
```

## Next Steps

- [Configuration Guide](./configuration.md) - Customize service settings
- [Docker Deployment](./docker.md) - Production deployment
- [Monitoring](./monitoring.md) - Set up observability

## Troubleshooting

### Port Already in Use

```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>
```

### Cannot Connect to External Services

```bash
# Check RAG service
curl http://localhost:5000/health

# Check model-transformer
curl http://localhost:5001/health

# Check LM Studio
curl http://localhost:1234/v1/models
```

### Java Version Issues

```bash
# Check version
java -version

# Should show: openjdk version "21.x.x"
```

### Maven Build Fails

```bash
# Clean and rebuild
./mvnw clean install -DskipTests

# If dependency issues, check lib/ folder
ls -la lib/
```
