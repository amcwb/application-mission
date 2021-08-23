import os

SECRET_KEY = os.urandom(16)
SQLALCHEMY_DATABASE_URI = "sqlite:///:memory:"
JWT_SECRET = os.urandom(16)