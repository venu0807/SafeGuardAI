def compute_risk(audio_score, vision=0.0, motion=0.0):
    return (
        audio_score * 0.40 +
        vision * 0.35 +
        motion * 0.25
    )
