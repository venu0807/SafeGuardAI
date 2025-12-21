class FusionEngine:
    def __init__(self):
        self.audio_score = 0.0
        self.motion_score = 0.0
        self.vision_score = 0.0

    def update_audio(self, score):
        self.audio_score = score

    def compute_risk(self):
        return (
            0.4 * self.audio_score +
            0.6 * max(self.motion_score, self.vision_score)
        )
