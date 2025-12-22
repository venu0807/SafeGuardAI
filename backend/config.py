# config.py

import os

# Paths (use repo-relative paths when running locally)
DATASET_ROOT = "/content/drive/MyDrive/datasets"

RAVDESS_PATH = f"{DATASET_ROOT}/ravdess"
ESC50_PATH = f"{DATASET_ROOT}/esc50/audio"

SAMPLE_RATE = 16000
# Point to the exported tflite model inside the backend folder
MFCC_MODEL_PATH = os.path.abspath(os.path.join(os.path.dirname(__file__), "models_export", "mfcc_audio_model.tflite"))
