class DecisionEngine:
    def __init__(self, threshold=0.85):
        self.threshold = threshold

    def should_trigger_sos(self, risk_score):
        return risk_score >= self.threshold
