from backend.app.models import User, Password
from backend.app import db
from backend.app.utils.encryption import encrypt_password, decrypt_password
import logging
import datetime

logger = logging.getLogger(__name__)

def get_passwords(user_id):
    logger.info("Get passwords for user_id: %s", user_id)
    passwords = Password.query.filter_by(user_id=user_id).all()
    user = User.query.get(user_id)
    return [{
        'id': p.id,
        'website': p.website,
        'username': p.username,
        'password': decrypt_password(p.encrypted_password, user.master_password_hash)
    } for p in passwords]

def add_password(user_id, data):
    logger.info("Adding new password entry for user_id: %s", user_id)
    user = User.query.get(user_id)
    encrypted_pwd = encrypt_password(data['password'], user.master_password_hash)
    new_password = Password()
    new_password.user_id = user.id
    new_password.website = data['website']
    new_password.username = data['username']
    new_password.encrypted_password = encrypted_pwd

    db.session.add(new_password)
    db.session.commit()
    return {
        'id': new_password.id,
        'website': new_password.website,
        'username': new_password.username,
        'password': data['password']  # Return the unencrypted password
    }

def update_password(user_id, password_id, data):
    logger.info("Updating password entry %s for user_id: %s", password_id, user_id)
    password = Password.query.filter_by(id=password_id, user_id=user_id).first()
    user = User.query.get(user_id)
    if password and user:
        password.website = data.get('website', password.website)
        password.username = data.get('username', password.username)
        if 'password' in data:
            password.encrypted_password = encrypt_password(data['password'], user.master_password_hash)
        db.session.commit()
        return {
            'id': password.id,
            'website': password.website,
            'username': password.username,
            'password': decrypt_password(password.encrypted_password, user.master_password_hash)
        }
    return None
