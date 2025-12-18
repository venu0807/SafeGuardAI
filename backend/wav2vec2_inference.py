import tensorflow as tf
import numpy as np

interpreter = tf.lite.Interpreter(
    model_path="../ml/exports/audio_model.tflite"
)
interpreter.allocate_tensors()

input_idx = interpreter.get_input_details()[0]["index"]
output_idx = interpreter.get_output_details()[0]["index"]

def wav2vec2_score(audio):
    audio = audio[np.newaxis, :].astype(np.float32)
    interpreter.set_tensor(input_idx, audio)
    interpreter.invoke()
    return float(interpreter.get_tensor(output_idx)[0][0])
