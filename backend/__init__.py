from flask import Flask
from flask_sqlalchemy import SQLAlchemy

# Setup Flask
app = Flask(__name__)
app.config.from_pyfile("config.py")
db = SQLAlchemy(app)

# Load the database
from backend.models import User, Task, users_tasks
db.create_all()

# Load blueprints
from backend.views import tasks, user
app.register_blueprint(tasks.tasks, url_prefix='/tasks')
app.register_blueprint(user.users, url_prefix='/users')

# Run app
app.run(port=80)