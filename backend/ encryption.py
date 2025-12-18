from cryptography.hazmat.primitives.ciphers.aead import AESGCM
import os

def encrypt(data: bytes):
    key = AESGCM.generate_key(bit_length=256)
    aes = AESGCM(key)
    nonce = os.urandom(12)
    encrypted = aes.encrypt(nonce, data, None)
    return nonce + encrypted
