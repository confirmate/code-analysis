from flask import Flask
from backend.app.extensions import db, cors
from backend.config import config_by_name
from backend.app.routes import auth, passwords

def create_app(config_name='development'):
    app = Flask(__name__)
    app.config.from_object(config_by_name[config_name])

    db.init_app(app)
    cors.init_app(app)

    app.register_blueprint(auth.bp)
    app.register_blueprint(passwords.bp)

    return app