from flask import Flask
from flask_sqlalchemy import SQLAlchemy

# Setup Flask
app = Flask(__name__)
app.config.from_pyfile("backend.config")
db = SQLAlchemy(app)

# Load the database
from models import User, Task, users_tasks
db.create_all()
