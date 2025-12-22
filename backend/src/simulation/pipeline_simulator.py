from backend.src.utils.audio_loader import load_audio
from backend.src.inference.mfcc_gate_inference import MFCCGate
from backend.src.fusion.temporal_gate import TemporalGate
from backend.src.fusion.fusion_engine import FusionEngine
from backend.src.fusion.decision_engine import DecisionEngine
from backend.config import MFCC_MODEL_PATH


audio = load_audio("tests/test_scream.wav")

mfcc_gate = MFCCGate(MFCC_MODEL_PATH)
temporal = TemporalGate()
fusion = FusionEngine()
decision = DecisionEngine()

for i in range(6):
    score = mfcc_gate.predict(audio)
    temporal.update(score)

    print(f"[{i}] MFCC score:", score)

    if temporal.is_consistent():
        fusion.update_audio(score)
        risk = fusion.compute_risk()

        print("   Temporal gate: ✅ consistent")
        print("   Risk score:", risk)

        if decision.should_trigger_sos(risk):
            print("🚨 SOS WOULD TRIGGER")
            break
    else:
        print("   Temporal gate: ⏳ accumulating")


