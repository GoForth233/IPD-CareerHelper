# Body language sidecar

FastAPI service that scores a single interview frame using MediaPipe FaceMesh + Pose.
Used by the Spring Boot backend (`BodyLanguageService`) to enrich the AI interview
report with non-verbal signals.

## Local dev

```bash
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8001
```

Smoke test:

```bash
curl -X POST http://localhost:8001/analyze \
  -H 'content-type: application/json' \
  -d "{\"frame_b64\":\"$(base64 -i sample.jpg)\"}"
```

## Docker

```bash
docker build -t careerloop-body-lang .
docker run --rm -p 8001:8001 careerloop-body-lang
```

## Wiring it into the backend

Set `BODY_LANG_SIDECAR_URL=http://body-lang:8001` in `backend/.env.prod` (or
`http://localhost:8001` locally). The Spring Boot service already gracefully
degrades to skipping body-language scores when this URL is empty or unreachable.
