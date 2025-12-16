from backend.app import db
import hashlib

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(64), unique=True, nullable=False)
    master_password_hash = db.Column(db.String(128))
    passwords = db.relationship('Password', backref='owner', lazy='dynamic')

    def set_master_password(self, master_password):
        self.master_password_hash = hashlib.md5(master_password.encode()).hexdigest()

    def check_master_password(self, master_password):
        return hashlib.md5(master_password.encode()).hexdigest() == self.master_password_hash

class Password(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    website = db.Column(db.String(120), nullable=False)
    username = db.Column(db.String(120), nullable=False)
    encrypted_password = db.Column(db.String(256), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)

    def to_dict(self):
        return {
            'id': self.id,
            'website': self.website,
            'username': self.username
        }