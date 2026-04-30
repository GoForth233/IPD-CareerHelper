"""
CareerLoop body-language sidecar.

A small FastAPI service that scores a single interview frame using
MediaPipe FaceMesh + Pose. The Spring Boot backend calls this service from
its BodyLanguageService over HTTP — we deliberately keep the API surface
tiny (one POST /analyze endpoint) so deployment is just `uvicorn main:app`.

Returned scores are integers in [0, 100]:
- eye_contact: based on iris position relative to the eye bounding box.
- expression:  derived from the smile/lip ratio and brow lift.
- posture:     based on shoulder line + head tilt vs. vertical.

If MediaPipe can't detect a face we still return a frame with low
confidence — the caller decides whether to discard.
"""
from __future__ import annotations

import base64
import io
import logging
import os
from typing import Any, Dict, Optional

import numpy as np
from fastapi import FastAPI, HTTPException
from PIL import Image
from pydantic import BaseModel

logger = logging.getLogger("body-lang")
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

try:
    import mediapipe as mp  # type: ignore
except Exception as exc:  # pragma: no cover — MediaPipe is optional in unit tests
    logger.warning("mediapipe not available, running in stub mode: %s", exc)
    mp = None  # type: ignore


app = FastAPI(title="CareerLoop body-language sidecar", version="0.1.0")


class FrameRequest(BaseModel):
    """One frame from the interview camera. base64-encoded JPEG/PNG."""

    frame_b64: str
    interview_id: Optional[int] = None


class FrameResponse(BaseModel):
    eye_contact: int
    expression: int
    posture: int
    confidence: float
    note: Optional[str] = None


@app.get("/health")
def health() -> Dict[str, Any]:
    return {"status": "ok", "mediapipe": mp is not None}


def _decode_image(b64: str) -> np.ndarray:
    if "," in b64:
        # Strip a "data:image/jpeg;base64," prefix if the client sent one.
        b64 = b64.split(",", 1)[1]
    try:
        raw = base64.b64decode(b64, validate=False)
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"Invalid base64 payload: {exc}") from exc
    try:
        img = Image.open(io.BytesIO(raw)).convert("RGB")
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"Could not decode image: {exc}") from exc
    return np.asarray(img)


def _score_with_mediapipe(image: np.ndarray) -> FrameResponse:
    """Real scoring path. Lazy-imports the heavy modules so cold start is cheap."""

    face_mesh = mp.solutions.face_mesh.FaceMesh(  # type: ignore[attr-defined]
        static_image_mode=True,
        max_num_faces=1,
        refine_landmarks=True,
        min_detection_confidence=0.4,
    )
    pose = mp.solutions.pose.Pose(  # type: ignore[attr-defined]
        static_image_mode=True,
        model_complexity=0,
        enable_segmentation=False,
        min_detection_confidence=0.4,
    )

    eye_contact = 60
    expression = 60
    posture = 60
    confidence = 0.5
    note = None

    face_result = face_mesh.process(image)
    if face_result.multi_face_landmarks:
        landmarks = face_result.multi_face_landmarks[0].landmark
        # Eye contact heuristic — iris (468..477) center vs eye corners.
        eye_contact = _eye_contact_score(landmarks, image.shape)
        expression = _expression_score(landmarks)
        confidence = max(confidence, 0.7)
    else:
        note = "no face detected"

    pose_result = pose.process(image)
    if pose_result.pose_landmarks:
        posture = _posture_score(pose_result.pose_landmarks.landmark)
        confidence = max(confidence, 0.65)

    face_mesh.close()
    pose.close()

    return FrameResponse(
        eye_contact=int(np.clip(eye_contact, 0, 100)),
        expression=int(np.clip(expression, 0, 100)),
        posture=int(np.clip(posture, 0, 100)),
        confidence=float(round(confidence, 2)),
        note=note,
    )


def _eye_contact_score(landmarks, shape) -> int:
    # Approximate the iris center vs. the bounding box of the eye landmarks.
    # 468 is the right iris center, 473 is the left. 33/133 are the right
    # eye corners; 263/362 are the left eye corners.
    try:
        right_iris = landmarks[468]
        left_iris = landmarks[473]
        right_corners = (landmarks[33], landmarks[133])
        left_corners = (landmarks[362], landmarks[263])
        right_off = abs((right_iris.x - (right_corners[0].x + right_corners[1].x) / 2))
        left_off = abs((left_iris.x - (left_corners[0].x + left_corners[1].x) / 2))
        offset = (right_off + left_off) / 2
        # offset 0 → great eye contact, offset >0.04 → drift.
        score = 100 - min(offset / 0.04, 1.0) * 60
        return int(score)
    except IndexError:
        return 55


def _expression_score(landmarks) -> int:
    # Smile heuristic: distance between mouth corners (61, 291) divided by
    # the lip thickness (13 vs 14). A wider smile → higher score.
    try:
        left = landmarks[61]
        right = landmarks[291]
        upper = landmarks[13]
        lower = landmarks[14]
        smile_w = ((right.x - left.x) ** 2 + (right.y - left.y) ** 2) ** 0.5
        lip_h = abs(upper.y - lower.y) + 1e-6
        ratio = smile_w / lip_h
        score = min(40 + ratio * 6, 95)
        return int(score)
    except IndexError:
        return 55


def _posture_score(landmarks) -> int:
    # Compare left and right shoulder y values; closer to equal → upright.
    try:
        left_sh = landmarks[11]
        right_sh = landmarks[12]
        nose = landmarks[0]
        shoulder_skew = abs(left_sh.y - right_sh.y)
        head_tilt = abs(nose.x - (left_sh.x + right_sh.x) / 2)
        score = 100 - shoulder_skew * 240 - head_tilt * 200
        return int(score)
    except IndexError:
        return 55


def _stub_response(image: np.ndarray) -> FrameResponse:
    """Deterministic-ish placeholder when MediaPipe isn't installed.
    The numbers wobble around 70 so the report still looks alive in dev.
    """
    seed = int(image.mean() * 1000) % 30
    return FrameResponse(
        eye_contact=60 + seed,
        expression=58 + (seed * 7 % 20),
        posture=65 + (seed * 11 % 20),
        confidence=0.3,
        note="mediapipe-disabled-stub",
    )


@app.post("/analyze", response_model=FrameResponse)
def analyze(req: FrameRequest) -> FrameResponse:
    image = _decode_image(req.frame_b64)
    if mp is None:
        return _stub_response(image)
    return _score_with_mediapipe(image)


if __name__ == "__main__":
    import uvicorn

    port = int(os.environ.get("PORT", "8001"))
    uvicorn.run(app, host="0.0.0.0", port=port)
