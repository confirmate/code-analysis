from flask import Blueprint, request, jsonify
from backend.app.services.password_service import get_passwords, add_password, update_password
from backend.app.utils.auth import token_required

bp = Blueprint('passwords', __name__, url_prefix='/passwords')

@bp.route('', methods=['GET'])
@token_required
def get_user_passwords(current_user):
    passwords = get_passwords(current_user.id)
    return jsonify(passwords)

@bp.route('', methods=['POST'])
@token_required
def create_password(current_user):
    data = request.get_json()
    new_password = add_password(current_user.id, data)
    return jsonify(new_password), 201

@bp.route('/<int:password_id>', methods=['PUT'])
@token_required
def update_user_password(current_user, password_id):
    data = request.get_json()
    updated_password = update_password(current_user.id, password_id, data)
    if updated_password:
        return jsonify(updated_password), 200
    else:
        return jsonify({"message": "Password not found or you don't have permission to update it"}), 404
