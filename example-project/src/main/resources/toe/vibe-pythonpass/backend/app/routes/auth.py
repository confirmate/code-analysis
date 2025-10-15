from flask import Blueprint, request, jsonify
from backend.app.models import User
from backend.app.services.auth_service import create_user, authenticate_user

bp = Blueprint('auth', __name__, url_prefix='/auth')

@bp.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    user = create_user(data.get('username'), data.get('masterPassword'))
    if user:
        return jsonify({'message': 'User created successfully'}), 201
    return jsonify({'message': 'Username already exists'}), 400

@bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    token = authenticate_user(data.get('username'), data.get('masterPassword'))
    if token:
        return jsonify({'token': token})
    return jsonify({'message': 'Invalid credentials'}), 401