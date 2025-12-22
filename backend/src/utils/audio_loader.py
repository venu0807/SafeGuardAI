import librosa
import os


def _resolve_path(path: str) -> str:
    if os.path.isabs(path) and os.path.exists(path):
        return path

    # If path doesn't exist as given, try resolving relative to the backend/tests folder
    base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
    candidate = os.path.abspath(os.path.join(base_dir, path))
    if os.path.exists(candidate):
        return candidate

    # Fallback to original path (will let librosa/audioread raise a FileNotFoundError)
    return path


def load_audio(path, sr=16000):
    resolved = _resolve_path(path)
    audio, _ = librosa.load(resolved, sr=sr, mono=True)
    return audio
