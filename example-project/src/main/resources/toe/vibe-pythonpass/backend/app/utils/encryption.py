from cryptography.fernet import Fernet
import base64

def generate_key(master_password_hash):
    return base64.urlsafe_b64encode(master_password_hash[:32].encode())

def encrypt_password(password, master_password_hash):
    key = generate_key(master_password_hash)
    f = Fernet(key)
    return f.encrypt(password.encode()).decode()

def decrypt_password(encrypted_password, master_password_hash):
    key = generate_key(master_password_hash)
    f = Fernet(key)
    return f.decrypt(encrypted_password.encode()).decode()
