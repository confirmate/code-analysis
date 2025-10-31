from backend.app.models import User
from backend.app.extensions import db
import jwt
from flask import current_app

def create_user(username, master_password):
    if User.query.filter_by(username=username).first():
        return None
    new_user = User(username=username)
    new_user.set_master_password(master_password)
    db.session.add(new_user)
    db.session.commit()
    return new_user

def authenticate_user(username, master_password):
    user = User.query.filter_by(username=username).first()
    if user and user.check_master_password(master_password):
        token = jwt.encode({'user_id': user.id}, current_app.config['SECRET_KEY'], algorithm="HS256")
        return token
    return None